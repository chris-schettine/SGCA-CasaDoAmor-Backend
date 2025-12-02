package br.com.casadoamor.sgca.modules.hospedagem.service;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemRequestDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemResponseDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemSaidaDTO;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.repository.HospedagemRepository;
import br.com.casadoamor.sgca.modules.hospedagem.repository.HospedagemSpecification;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de hospedagens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HospedagemService {

    private final HospedagemRepository hospedagemRepository;
    private final PacienteRepository pacienteRepository;
    private final QuartoService quartoService;

    @Transactional
    public HospedagemResponseDTO registrarEntrada(HospedagemRequestDTO dto, AuthUsuario usuarioLogado) {
        log.info("Registrando entrada de hospedagem para paciente: {}", dto.getPacienteId());

        // Buscar paciente
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado: " + dto.getPacienteId()));

        // Verificar se paciente já tem hospedagem ativa
        if (hospedagemRepository.pacienteTemHospedagemAtiva(paciente)) {
            throw new IllegalStateException("Paciente já possui uma hospedagem ativa");
        }

        Quarto quarto = null;
        if (dto.getQuartoUuid() != null) {
            quarto = quartoService.buscarPorUuid(dto.getQuartoUuid());
            
            // Validar disponibilidade do quarto
            if (!quarto.temVagasDisponiveis()) {
                throw new IllegalStateException("Quarto não possui vagas disponíveis");
            }

            // Validar compatibilidade de gênero com a ala
            validarCompatibilidadeGeneroAla(paciente, quarto);

            // Incrementar ocupação do quarto
            quarto.incrementarOcupacao();
        }

        Hospedagem hospedagem = Hospedagem.builder()
                .paciente(paciente)
                .quarto(quarto)
                .dataEntrada(dto.getDataEntrada())
                .horaEntrada(dto.getHoraEntrada() != null ? dto.getHoraEntrada() : LocalTime.now())
                .dataSaidaPrevista(dto.getDataSaidaPrevista())
                .observacoesEntrada(dto.getObservacoesEntrada())
                .observacoesGerais(dto.getObservacoesGerais())
                .status(StatusHospedagem.ATIVA)
                .createdBy(usuarioLogado)
                .build();

        hospedagem = hospedagemRepository.save(hospedagem);
        log.info("Hospedagem registrada com sucesso: UUID={}", hospedagem.getUuid());

        return toResponseDTO(hospedagem);
    }

    @Transactional
    public HospedagemResponseDTO registrarSaida(String uuid, HospedagemSaidaDTO dto, AuthUsuario usuarioLogado) {
        log.info("Registrando saída de hospedagem: {}", uuid);

        Hospedagem hospedagem = buscarPorUuid(uuid);

        if (!hospedagem.isAtiva()) {
            throw new IllegalStateException("Hospedagem não está ativa");
        }

        // Validar data de saída
        if (dto.getDataSaida().isBefore(hospedagem.getDataEntrada())) {
            throw new IllegalArgumentException("Data de saída não pode ser anterior à data de entrada");
        }

        // Decrementar ocupação do quarto
        if (hospedagem.getQuarto() != null) {
            hospedagem.getQuarto().decrementarOcupacao();
        }

        hospedagem.encerrar(
                dto.getDataSaida(),
                dto.getHoraSaida() != null ? dto.getHoraSaida() : LocalTime.now(),
                dto.getMotivoSaida()
        );
        hospedagem.setObservacoesSaida(dto.getObservacoesSaida());
        hospedagem.setUpdatedBy(usuarioLogado);

        hospedagem = hospedagemRepository.save(hospedagem);
        log.info("Saída registrada com sucesso: UUID={}", uuid);

        return toResponseDTO(hospedagem);
    }

    @Transactional
    public HospedagemResponseDTO transferirQuarto(String hospedagemUuid, String novoQuartoUuid, 
                                                  String motivoTransferencia, AuthUsuario usuarioLogado) {
        log.info("Transferindo hospedagem {} para quarto {}", hospedagemUuid, novoQuartoUuid);

        Hospedagem hospedagem = buscarPorUuid(hospedagemUuid);

        if (!hospedagem.isAtiva()) {
            throw new IllegalStateException("Apenas hospedagens ativas podem ser transferidas");
        }

        Quarto novoQuarto = quartoService.buscarPorUuid(novoQuartoUuid);

        // Validar disponibilidade
        if (!novoQuarto.temVagasDisponiveis()) {
            throw new IllegalStateException("Novo quarto não possui vagas disponíveis");
        }

        // Validar compatibilidade de gênero
        validarCompatibilidadeGeneroAla(hospedagem.getPaciente(), novoQuarto);

        // Liberar vaga do quarto anterior
        if (hospedagem.getQuarto() != null) {
            hospedagem.getQuarto().decrementarOcupacao();
        }

        // Ocupar vaga do novo quarto
        novoQuarto.incrementarOcupacao();

        hospedagem.setQuarto(novoQuarto);
        // Manter status ATIVA após transferência (paciente continua hospedado)
        // hospedagem.setStatus(StatusHospedagem.TRANSFERENCIA); // Removido - viola constraint
        
        // Adicionar motivo da transferência às observações gerais
        if (motivoTransferencia != null && !motivoTransferencia.isBlank()) {
            String observacao = hospedagem.getObservacoesGerais() != null 
                    ? hospedagem.getObservacoesGerais() + "\nTransferência: " + motivoTransferencia
                    : "Transferência: " + motivoTransferencia;
            hospedagem.setObservacoesGerais(observacao);
        }
        
        hospedagem.setUpdatedBy(usuarioLogado);

        hospedagem = hospedagemRepository.save(hospedagem);
        log.info("Transferência realizada com sucesso");

        return toResponseDTO(hospedagem);
    }

    @Transactional(readOnly = true)
    public HospedagemResponseDTO buscarPorUuidDTO(String uuid) {
        return toResponseDTO(buscarPorUuid(uuid));
    }

    @Transactional(readOnly = true)
    public List<HospedagemResponseDTO> listarAtivas() {
        return hospedagemRepository.findHospedagensAtivas().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HospedagemResponseDTO> listarPorPaciente(String pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado"));
        
        return hospedagemRepository.findByPacienteOrderByDataEntradaDesc(paciente).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HospedagemResponseDTO> listarPorQuarto(String quartoUuid) {
        Quarto quarto = quartoService.buscarPorUuid(quartoUuid);
        
        return hospedagemRepository.findByQuartoOrderByDataEntradaDesc(quarto).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HospedagemResponseDTO> listarComPaginacao(Pageable pageable) {
        return hospedagemRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<HospedagemResponseDTO> listarComPaginacaoEFiltros(
            String nomePaciente,
            String nomeQuarto,
            AlaQuarto ala,
            StatusHospedagem status,
            LocalDate dataInicio,
            LocalDate dataFim,
            Pageable pageable
    ) {
        log.info("Listando hospedagens com filtros - nomePaciente: {}, nomeQuarto: {}, ala: {}, status: {}, dataInicio: {}, dataFim: {}",
                nomePaciente, nomeQuarto, ala, status, dataInicio, dataFim);
        
        Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                nomePaciente, nomeQuarto, ala, status, dataInicio, dataFim
        );
        
        return hospedagemRepository.findAll(spec, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public boolean pacienteTemHospedagemAtiva(String pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado"));
        
        return hospedagemRepository.pacienteTemHospedagemAtiva(paciente);
    }

    @Transactional(readOnly = true)
    public List<HospedagemResponseDTO> listarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return hospedagemRepository.findByPeriodoEntrada(dataInicio, dataFim).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HospedagemResponseDTO> listarComPrevisaoVencida() {
        return hospedagemRepository.findHospedagensComPrevisaoVencida().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletar(String uuid) {
        Hospedagem hospedagem = buscarPorUuid(uuid);
        
        // Se estiver ativa, liberar vaga do quarto
        if (hospedagem.isAtiva() && hospedagem.getQuarto() != null) {
            hospedagem.getQuarto().decrementarOcupacao();
        }

        hospedagemRepository.delete(hospedagem);
        log.info("Hospedagem deletada: UUID={}", uuid);
    }

    // Métodos auxiliares
    private void validarCompatibilidadeGeneroAla(Paciente paciente, Quarto quarto) {
        // Regra 1: Quartos de ISOLAMENTO ou MISTOS aceitam qualquer gênero
        if (quarto.getAla() == AlaQuarto.ISOLAMENTO || quarto.getAla() == AlaQuarto.MISTA) {
            log.debug("Quarto {} permite qualquer gênero (ala: {})", quarto.getNome(), quarto.getAla());
            return;
        }

        // Regra 2: Quartos com flag permiteSexoOposto = true aceitam qualquer gênero
        // (Quartos de 4 camas para pacientes debilitados ou 7 camas para acompanhantes)
        if (Boolean.TRUE.equals(quarto.getPermiteSexoOposto())) {
            log.debug("Quarto {} permite sexo oposto (capacidade: {} leitos)", 
                    quarto.getNome(), quarto.getCapacidadeTotal());
            return;
        }

        // Regra 3: Validar compatibilidade de gênero para alas específicas (FEMININA/MASCULINA)
        if (paciente.getDadoPessoal() == null || paciente.getDadoPessoal().getSexo() == null) {
            log.warn("ATENÇÃO: Paciente {} não possui gênero cadastrado. Alocação em quarto da ala {} requer validação manual",
                    paciente.getId(), quarto.getAla());
            throw new IllegalStateException(
                    "Paciente sem gênero cadastrado. Não é possível validar compatibilidade com a ala " + quarto.getAla().getDescricao());
        }

        String sexoPaciente = paciente.getDadoPessoal().getSexo().name();
        
        // Validar se o gênero é compatível com a ala
        boolean compativel = false;
        if (quarto.getAla() == AlaQuarto.FEMININA && "FEMININO".equals(sexoPaciente)) {
            compativel = true;
        } else if (quarto.getAla() == AlaQuarto.MASCULINA && "MASCULINO".equals(sexoPaciente)) {
            compativel = true;
        }

        if (!compativel) {
            String mensagem = String.format(
                    "Incompatibilidade de gênero: Paciente %s (sexo: %s) não pode ser alocado em quarto da ala %s. " +
                    "Considere usar quartos de 4 camas (debilitados), 7 camas (acompanhantes) ou ala de Isolamento.",
                    paciente.getDadoPessoal().getNome(), 
                    sexoPaciente, 
                    quarto.getAla().getDescricao());
            log.error(mensagem);
            throw new IllegalStateException(mensagem);
        }

        log.info("Validação de gênero OK: Paciente {} (sexo: {}) compatível com ala {}", 
                paciente.getDadoPessoal().getNome(), sexoPaciente, quarto.getAla().getDescricao());
    }

    public Hospedagem buscarPorUuid(String uuid) {
        return hospedagemRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Hospedagem não encontrada: " + uuid));
    }

    private HospedagemResponseDTO toResponseDTO(Hospedagem hospedagem) {
        return HospedagemResponseDTO.builder()
                .uuid(hospedagem.getUuid())
                .pacienteId(hospedagem.getPaciente().getId())
                .pacienteNome(hospedagem.getPaciente().getDadoPessoal() != null ?
                        hospedagem.getPaciente().getDadoPessoal().getNome() : "N/A")
                .pacienteCpf(hospedagem.getPaciente().getDadoPessoal() != null ?
                        hospedagem.getPaciente().getDadoPessoal().getCpf() : null)
                .quartoUuid(hospedagem.getQuarto() != null ? hospedagem.getQuarto().getUuid() : null)
                .quartoNome(hospedagem.getQuarto() != null ? hospedagem.getQuarto().getNome() : null)
                .quartoCodigo(hospedagem.getQuarto() != null ? hospedagem.getQuarto().getCodigo() : null)
                .dataEntrada(hospedagem.getDataEntrada())
                .horaEntrada(hospedagem.getHoraEntrada())
                .dataSaidaPrevista(hospedagem.getDataSaidaPrevista())
                .dataSaida(hospedagem.getDataSaida())
                .horaSaida(hospedagem.getHoraSaida())
                .status(hospedagem.getStatus())
                .motivoSaida(hospedagem.getMotivoSaida())
                .observacoesEntrada(hospedagem.getObservacoesEntrada())
                .observacoesSaida(hospedagem.getObservacoesSaida())
                .observacoesGerais(hospedagem.getObservacoesGerais())
                .createdAt(hospedagem.getCreatedAt())
                .createdByNome(hospedagem.getCreatedBy() != null ? hospedagem.getCreatedBy().getNome() : null)
                .updatedAt(hospedagem.getUpdatedAt())
                .updatedByNome(hospedagem.getUpdatedBy() != null ? hospedagem.getUpdatedBy().getNome() : null)
                .build();
    }
}
