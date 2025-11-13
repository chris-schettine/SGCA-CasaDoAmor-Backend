package br.com.casadoamor.sgca.modules.hospedagem.service;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.dto.*;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.repository.QuartoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de quartos/leitos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuartoService {

    private final QuartoRepository quartoRepository;

    @Transactional
    public QuartoResponseDTO cadastrar(QuartoRequestDTO dto, AuthUsuario usuarioLogado) {
        log.info("Cadastrando novo quarto: {}", dto.getNome());

        // Validar código único
        if (dto.getCodigo() != null && quartoRepository.existsByCodigo(dto.getCodigo())) {
            throw new IllegalArgumentException("Já existe um quarto com o código: " + dto.getCodigo());
        }

        Quarto quarto = Quarto.builder()
                .nome(dto.getNome())
                .codigo(dto.getCodigo())
                .tipo(dto.getTipo())
                .ala(dto.getAla())
                .andar(dto.getAndar())
                .capacidadeTotal(dto.getCapacidadeTotal())
                .capacidadeOcupada(0) // Sempre inicia vazio
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .emManutencao(dto.getEmManutencao() != null ? dto.getEmManutencao() : false)
                .permiteSexoOposto(dto.getPermiteSexoOposto() != null ? dto.getPermiteSexoOposto() : false)
                .observacoes(dto.getObservacoes())
                .createdBy(usuarioLogado)
                .build();

        quarto = quartoRepository.save(quarto);
        log.info("Quarto cadastrado com sucesso: UUID={}", quarto.getUuid());

        return toResponseDTO(quarto);
    }

    @Transactional
    public QuartoResponseDTO atualizar(String uuid, QuartoRequestDTO dto, AuthUsuario usuarioLogado) {
        log.info("Atualizando quarto: {}", uuid);

        Quarto quarto = buscarPorUuid(uuid);

        // Validar código único (se alterado)
        if (dto.getCodigo() != null && !dto.getCodigo().equals(quarto.getCodigo())) {
            if (quartoRepository.existsByCodigoAndIdNot(dto.getCodigo(), quarto.getId())) {
                throw new IllegalArgumentException("Já existe outro quarto com o código: " + dto.getCodigo());
            }
        }

        // Validar redução de capacidade
        if (dto.getCapacidadeTotal() < quarto.getCapacidadeOcupada()) {
            throw new IllegalArgumentException(
                    String.format("Não é possível reduzir a capacidade para %d. Há %d leitos ocupados.",
                            dto.getCapacidadeTotal(), quarto.getCapacidadeOcupada()));
        }

        quarto.setNome(dto.getNome());
        quarto.setCodigo(dto.getCodigo());
        quarto.setTipo(dto.getTipo());
        quarto.setAla(dto.getAla());
        quarto.setAndar(dto.getAndar());
        quarto.setCapacidadeTotal(dto.getCapacidadeTotal());
        quarto.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : quarto.getAtivo());
        quarto.setEmManutencao(dto.getEmManutencao() != null ? dto.getEmManutencao() : quarto.getEmManutencao());
        quarto.setPermiteSexoOposto(dto.getPermiteSexoOposto() != null ? dto.getPermiteSexoOposto() : quarto.getPermiteSexoOposto());
        quarto.setObservacoes(dto.getObservacoes());
        quarto.setUpdatedBy(usuarioLogado);

        quarto = quartoRepository.save(quarto);
        log.info("Quarto atualizado com sucesso: UUID={}", quarto.getUuid());

        return toResponseDTO(quarto);
    }

    @Transactional(readOnly = true)
    public QuartoResponseDTO buscarPorUuidDTO(String uuid) {
        return toResponseDTO(buscarPorUuid(uuid));
    }

    @Transactional(readOnly = true)
    public List<QuartoResumoDTO> listarTodos() {
        return quartoRepository.findAll().stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuartoResumoDTO> listarAtivos() {
        return quartoRepository.findByAtivoTrue().stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuartoResumoDTO> listarPorAla(AlaQuarto ala) {
        return quartoRepository.findByAla(ala).stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuartoResumoDTO> listarComVagas() {
        return quartoRepository.findQuartosComVagas().stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuartoResumoDTO> listarComVagasPorAla(AlaQuarto ala) {
        return quartoRepository.findQuartosComVagasPorAla(ala).stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<QuartoResumoDTO> listarComPaginacao(Pageable pageable) {
        return quartoRepository.findAll(pageable)
                .map(this::toResumoDTO);
    }

    @Transactional(readOnly = true)
    public EstatisticasOcupacaoDTO obterEstatisticas() {
        Integer capTotal = quartoRepository.contarCapacidadeTotal();
        Integer ocupTotal = quartoRepository.contarOcupacaoTotal();
        Integer vagasDisp = quartoRepository.contarVagasDisponiveis();

        return EstatisticasOcupacaoDTO.builder()
                .capacidadeTotal(capTotal != null ? capTotal : 0)
                .ocupacaoTotal(ocupTotal != null ? ocupTotal : 0)
                .vagasDisponiveis(vagasDisp != null ? vagasDisp : 0)
                .percentualOcupacao(calcularPercentual(ocupTotal, capTotal))
                .alaFeminina(obterEstatisticasAla(AlaQuarto.FEMININA))
                .alaMasculina(obterEstatisticasAla(AlaQuarto.MASCULINA))
                .alaMista(obterEstatisticasAla(AlaQuarto.MISTA))
                .build();
    }

    private EstatisticasOcupacaoDTO.EstatisticasAlaDTO obterEstatisticasAla(AlaQuarto ala) {
        Integer capTotal = quartoRepository.contarCapacidadeTotalPorAla(ala);
        Integer ocupTotal = quartoRepository.contarOcupacaoTotalPorAla(ala);
        Integer vagasDisp = quartoRepository.contarVagasDisponiveisPorAla(ala);

        return EstatisticasOcupacaoDTO.EstatisticasAlaDTO.builder()
                .capacidadeTotal(capTotal != null ? capTotal : 0)
                .ocupacaoTotal(ocupTotal != null ? ocupTotal : 0)
                .vagasDisponiveis(vagasDisp != null ? vagasDisp : 0)
                .percentualOcupacao(calcularPercentual(ocupTotal, capTotal))
                .build();
    }

    private Double calcularPercentual(Integer ocupacao, Integer capacidade) {
        if (capacidade == null || capacidade == 0) return 0.0;
        if (ocupacao == null) return 0.0;
        return (ocupacao * 100.0) / capacidade;
    }

    @Transactional
    public void inativar(String uuid, AuthUsuario usuarioLogado) {
        Quarto quarto = buscarPorUuid(uuid);
        
        if (quarto.getCapacidadeOcupada() > 0) {
            throw new IllegalStateException("Não é possível inativar um quarto com leitos ocupados");
        }

        quarto.setAtivo(false);
        quarto.setUpdatedBy(usuarioLogado);
        quartoRepository.save(quarto);
        log.info("Quarto inativado: UUID={}", uuid);
    }

    @Transactional
    public void deletar(String uuid) {
        Quarto quarto = buscarPorUuid(uuid);
        
        if (quarto.getCapacidadeOcupada() > 0) {
            throw new IllegalStateException("Não é possível deletar um quarto com leitos ocupados");
        }

        quartoRepository.delete(quarto);
        log.info("Quarto deletado: UUID={}", uuid);
    }

    // Métodos auxiliares
    public Quarto buscarPorUuid(String uuid) {
        return quartoRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Quarto não encontrado: " + uuid));
    }

    private QuartoResponseDTO toResponseDTO(Quarto quarto) {
        return QuartoResponseDTO.builder()
                .uuid(quarto.getUuid())
                .nome(quarto.getNome())
                .codigo(quarto.getCodigo())
                .tipo(quarto.getTipo())
                .ala(quarto.getAla())
                .andar(quarto.getAndar())
                .capacidadeTotal(quarto.getCapacidadeTotal())
                .capacidadeOcupada(quarto.getCapacidadeOcupada())
                .vagasDisponiveis(quarto.getVagasDisponiveis())
                .ativo(quarto.getAtivo())
                .emManutencao(quarto.getEmManutencao())
                .permiteSexoOposto(quarto.getPermiteSexoOposto())
                .observacoes(quarto.getObservacoes())
                .createdAt(quarto.getCreatedAt())
                .createdByNome(quarto.getCreatedBy() != null ? quarto.getCreatedBy().getNome() : null)
                .updatedAt(quarto.getUpdatedAt())
                .updatedByNome(quarto.getUpdatedBy() != null ? quarto.getUpdatedBy().getNome() : null)
                .build();
    }

    private QuartoResumoDTO toResumoDTO(Quarto quarto) {
        return QuartoResumoDTO.builder()
                .uuid(quarto.getUuid())
                .nome(quarto.getNome())
                .codigo(quarto.getCodigo())
                .tipo(quarto.getTipo())
                .ala(quarto.getAla())
                .andar(quarto.getAndar())
                .capacidadeTotal(quarto.getCapacidadeTotal())
                .capacidadeOcupada(quarto.getCapacidadeOcupada())
                .vagasDisponiveis(quarto.getVagasDisponiveis())
                .ativo(quarto.getAtivo())
                .emManutencao(quarto.getEmManutencao())
                .permiteSexoOposto(quarto.getPermiteSexoOposto())
                .build();
    }
}
