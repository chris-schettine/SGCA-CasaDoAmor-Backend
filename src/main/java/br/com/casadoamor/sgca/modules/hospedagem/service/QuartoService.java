package br.com.casadoamor.sgca.modules.hospedagem.service;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.dto.*;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto;
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

        // Gerar código automaticamente se não fornecido
        String codigo = dto.getCodigo();
        if (codigo == null || codigo.trim().isEmpty()) {
            codigo = gerarCodigoQuarto(dto.getAla(), dto.getAndar());
        } else {
            // Validar código único se fornecido
            if (quartoRepository.existsByCodigo(codigo)) {
                throw new IllegalArgumentException("Já existe um quarto com o código: " + codigo);
            }
        }

        Quarto quarto = Quarto.builder()
                .nome(dto.getNome())
                .codigo(codigo)
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
        log.info("Quarto cadastrado com sucesso: UUID={}, Código={}", quarto.getUuid(), quarto.getCodigo());

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
    public Page<QuartoResumoDTO> listarComPaginacao(
            String nome,
            AlaQuarto ala,
            TipoQuarto tipo,
            Boolean ativo,
            Pageable pageable) {
        return quartoRepository.searchQuartos(nome, ala, tipo, ativo, pageable)
                .map(this::toResumoDTO);
    }

    @Transactional(readOnly = true)
    public EstatisticasOcupacaoDTO obterEstatisticas() {
        // Estatísticas de ocupação
        Integer capTotal = quartoRepository.contarCapacidadeTotal();
        Integer ocupTotal = quartoRepository.contarOcupacaoTotal();
        Integer vagasDisp = quartoRepository.contarVagasDisponiveis();

        // Estatísticas de quartos
        Long totalQuartos = quartoRepository.contarTotalQuartos();
        Long quartosAtivos = quartoRepository.contarQuartosAtivos();
        Long quartosInativos = quartoRepository.contarQuartosInativos();
        Long quartosEmManutencao = quartoRepository.contarQuartosEmManutencao();
        Long quartosDisponiveisAdmissao = quartoRepository.contarQuartosDisponiveisAdmissao();

        // Distribuição por tipo
        Long quartosIndividuais = quartoRepository.contarQuartosPorTipo(TipoQuarto.INDIVIDUAL);
        Long quartosCompartilhados = quartoRepository.contarQuartosPorTipo(TipoQuarto.COMPARTILHADO);
        Long quartosIsolamento = quartoRepository.contarQuartosPorTipo(TipoQuarto.ISOLAMENTO);

        // Métricas operacionais
        Long quartosLotados = quartoRepository.contarQuartosLotados();
        Long quartosVazios = quartoRepository.contarQuartosVazios();
        Long quartosParcialmenteOcupados = quartoRepository.contarQuartosParcialmenteOcupados();
        Long quartosPermitemSexoOposto = quartoRepository.contarQuartosPermitemSexoOposto();

        return EstatisticasOcupacaoDTO.builder()
                // Ocupação
                .capacidadeTotal(capTotal != null ? capTotal : 0)
                .ocupacaoTotal(ocupTotal != null ? ocupTotal : 0)
                .vagasDisponiveis(vagasDisp != null ? vagasDisp : 0)
                .percentualOcupacao(calcularPercentual(ocupTotal, capTotal))
                // Quartos
                .totalQuartos(totalQuartos != null ? totalQuartos.intValue() : 0)
                .quartosAtivos(quartosAtivos != null ? quartosAtivos.intValue() : 0)
                .quartosInativos(quartosInativos != null ? quartosInativos.intValue() : 0)
                .quartosEmManutencao(quartosEmManutencao != null ? quartosEmManutencao.intValue() : 0)
                .quartosDisponiveisAdmissao(quartosDisponiveisAdmissao != null ? quartosDisponiveisAdmissao.intValue() : 0)
                // Tipos
                .quartosIndividuais(quartosIndividuais != null ? quartosIndividuais.intValue() : 0)
                .quartosCompartilhados(quartosCompartilhados != null ? quartosCompartilhados.intValue() : 0)
                .quartosIsolamento(quartosIsolamento != null ? quartosIsolamento.intValue() : 0)
                // Operacionais
                .quartosLotados(quartosLotados != null ? quartosLotados.intValue() : 0)
                .quartosVazios(quartosVazios != null ? quartosVazios.intValue() : 0)
                .quartosParcialmenteOcupados(quartosParcialmenteOcupados != null ? quartosParcialmenteOcupados.intValue() : 0)
                .quartosPermitemSexoOposto(quartosPermitemSexoOposto != null ? quartosPermitemSexoOposto.intValue() : 0)
                // Por ala
                .alaFeminina(obterEstatisticasAla(AlaQuarto.FEMININA))
                .alaMasculina(obterEstatisticasAla(AlaQuarto.MASCULINA))
                .alaMista(obterEstatisticasAla(AlaQuarto.MISTA))
                .build();
    }

    private EstatisticasOcupacaoDTO.EstatisticasAlaDTO obterEstatisticasAla(AlaQuarto ala) {
        // Ocupação
        Integer capTotal = quartoRepository.contarCapacidadeTotalPorAla(ala);
        Integer ocupTotal = quartoRepository.contarOcupacaoTotalPorAla(ala);
        Integer vagasDisp = quartoRepository.contarVagasDisponiveisPorAla(ala);

        // Status dos quartos
        Long totalQuartos = quartoRepository.contarTotalQuartosPorAla(ala);
        Long quartosAtivos = quartoRepository.contarQuartosAtivosPorAla(ala);
        Long quartosInativos = quartoRepository.contarQuartosInativosPorAla(ala);
        Long quartosEmManutencao = quartoRepository.contarQuartosEmManutencaoPorAla(ala);
        Long quartosDisponiveisAdmissao = quartoRepository.contarQuartosDisponiveisAdmissaoPorAla(ala);

        // Operacional
        Long quartosLotados = quartoRepository.contarQuartosLotadosPorAla(ala);
        Long quartosVazios = quartoRepository.contarQuartosVaziosPorAla(ala);
        Long quartosParcialmenteOcupados = quartoRepository.contarQuartosParcialmenteOcupadosPorAla(ala);

        return EstatisticasOcupacaoDTO.EstatisticasAlaDTO.builder()
                // Ocupação
                .capacidadeTotal(capTotal != null ? capTotal : 0)
                .ocupacaoTotal(ocupTotal != null ? ocupTotal : 0)
                .vagasDisponiveis(vagasDisp != null ? vagasDisp : 0)
                .percentualOcupacao(calcularPercentual(ocupTotal, capTotal))
                // Status
                .totalQuartos(totalQuartos != null ? totalQuartos.intValue() : 0)
                .quartosAtivos(quartosAtivos != null ? quartosAtivos.intValue() : 0)
                .quartosInativos(quartosInativos != null ? quartosInativos.intValue() : 0)
                .quartosEmManutencao(quartosEmManutencao != null ? quartosEmManutencao.intValue() : 0)
                .quartosDisponiveisAdmissao(quartosDisponiveisAdmissao != null ? quartosDisponiveisAdmissao.intValue() : 0)
                // Operacional
                .quartosLotados(quartosLotados != null ? quartosLotados.intValue() : 0)
                .quartosVazios(quartosVazios != null ? quartosVazios.intValue() : 0)
                .quartosParcialmenteOcupados(quartosParcialmenteOcupados != null ? quartosParcialmenteOcupados.intValue() : 0)
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
    public void ativar(String uuid, AuthUsuario usuarioLogado) {
        Quarto quarto = buscarPorUuid(uuid);
        
        quarto.setAtivo(true);
        quarto.setUpdatedBy(usuarioLogado);
        quartoRepository.save(quarto);
        log.info("Quarto ativado: UUID={}", uuid);
    }

    @Transactional
    public void ativarManutencao(String uuid, AuthUsuario usuarioLogado) {
        Quarto quarto = buscarPorUuid(uuid);
        
        if (quarto.getCapacidadeOcupada() > 0) {
            throw new IllegalStateException("Não é possível colocar em manutenção um quarto com leitos ocupados");
        }

        quarto.setEmManutencao(true);
        quarto.setUpdatedBy(usuarioLogado);
        quartoRepository.save(quarto);
        log.info("Quarto colocado em manutenção: UUID={}", uuid);
    }

    @Transactional
    public void desativarManutencao(String uuid, AuthUsuario usuarioLogado) {
        Quarto quarto = buscarPorUuid(uuid);
        
        quarto.setEmManutencao(false);
        quarto.setUpdatedBy(usuarioLogado);
        quartoRepository.save(quarto);
        log.info("Quarto removido de manutenção: UUID={}", uuid);
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
                .tipo(new TipoQuartoDTO(quarto.getTipo().name(), quarto.getTipo().getDescricao()))
                .ala(new AlaQuartoDTO(quarto.getAla().name(), quarto.getAla().getDescricao()))
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
                .tipo(new TipoQuartoDTO(quarto.getTipo().name(), quarto.getTipo().getDescricao()))
                .ala(new AlaQuartoDTO(quarto.getAla().name(), quarto.getAla().getDescricao()))
                .andar(quarto.getAndar())
                .capacidadeTotal(quarto.getCapacidadeTotal())
                .capacidadeOcupada(quarto.getCapacidadeOcupada())
                .vagasDisponiveis(quarto.getVagasDisponiveis())
                .ativo(quarto.getAtivo())
                .emManutencao(quarto.getEmManutencao())
                .permiteSexoOposto(quarto.getPermiteSexoOposto())
                .build();
    }

    /**
     * Gera código único para o quarto baseado na ala e andar
     * Formato: [ALA]-[ANDAR]-[CONTADOR] (ex: FEM-1-001, MASC-2-015)
     */
    private String gerarCodigoQuarto(AlaQuarto ala, String andar) {
        String prefixoAla = switch (ala) {
            case FEMININA -> "FEM";
            case MASCULINA -> "MASC";
            case MISTA -> "MIST";
            case ISOLAMENTO -> "ISO";
        };
        
        String andarFormatado = (andar != null && !andar.trim().isEmpty()) ? andar.trim() : "0";
        String prefixo = prefixoAla + "-" + andarFormatado + "-";
        
        // Buscar último código com esse prefixo para incrementar
        int contador = 1;
        String codigoGerado;
        do {
            codigoGerado = prefixo + String.format("%03d", contador);
            contador++;
        } while (quartoRepository.existsByCodigo(codigoGerado));
        
        return codigoGerado;
    }
}
