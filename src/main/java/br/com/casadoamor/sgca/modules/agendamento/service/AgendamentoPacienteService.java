package br.com.casadoamor.sgca.modules.agendamento.service;

import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoPacienteRequestDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoPacienteResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.ConflictCheckResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.entity.AgendamentoPaciente;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.repository.AgendamentoPacienteRepository;
import br.com.casadoamor.sgca.modules.agendamento.repository.BloqueioAgendaRepository;
import br.com.casadoamor.sgca.modules.agendamento.repository.HorarioProfissionalRepository;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.hospedagem.repository.HospedagemRepository;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import br.com.casadoamor.sgca.modules.agendamento.entity.TipoServico;
import br.com.casadoamor.sgca.modules.agendamento.repository.TipoServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgendamentoPacienteService {

    private final AgendamentoPacienteRepository agendamentoPacienteRepository;
    private final PacienteRepository pacienteRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final AuthUsuarioRepository authUsuarioRepository;
    private final HospedagemRepository hospedagemRepository;
    private final BloqueioAgendaRepository bloqueioAgendaRepository;
    private final HorarioProfissionalRepository horarioProfissionalRepository;

    @Transactional(readOnly = true)
    public List<AgendamentoPacienteResponseDTO> listar(int page, int size) {
        return agendamentoPacienteRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size))
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }

    public AgendamentoPacienteResponseDTO criar(AgendamentoPacienteRequestDTO requestDTO) {
        log.info("Criando agendamento para paciente ID: {}", requestDTO.getPacienteId());

        // Verificar conflito
        ConflictCheckResponseDTO conflictCheck = verificarConflito(
            requestDTO.getProfissionalUsuarioId(),
            requestDTO.getDataHoraInicio(),
            requestDTO.getDataHoraFim()
        );

        if (conflictCheck.getTemConflito()) {
            throw new RuntimeException("Conflito de horário: " + conflictCheck.getMensagem());
        }

        // Buscar entidades
        Paciente paciente = pacienteRepository.findById(requestDTO.getPacienteId())
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        TipoServico tipoServico = tipoServicoRepository.findById(requestDTO.getTipoServicoId())
            .orElseThrow(() -> new RuntimeException("Tipo de serviço não encontrado"));

        AuthUsuario profissional = authUsuarioRepository.findById(requestDTO.getProfissionalUsuarioId())
            .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        // Usar duração do DTO ou calcular
        int duracaoMinutos = requestDTO.getDuracaoMinutos() != null 
            ? requestDTO.getDuracaoMinutos()
            : (int) java.time.Duration.between(
                requestDTO.getDataHoraInicio(), 
                requestDTO.getDataHoraFim()
            ).toMinutes();

        // Criar agendamento
        AgendamentoPaciente agendamento = AgendamentoPaciente.builder()
            .paciente(paciente)
            .tipoServico(tipoServico)
            .profissionalUsuario(profissional)
            .dataHoraInicio(requestDTO.getDataHoraInicio())
            .dataHoraFim(requestDTO.getDataHoraFim())
            .duracaoMinutos(duracaoMinutos)
            .tipoAtendimento(requestDTO.getTipoAtendimento())
            .prioridade(requestDTO.getPrioridade())
            .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : StatusAgendamento.AGENDADO)
            .observacoes(requestDTO.getObservacoes())
            .confirmadoPaciente(requestDTO.getConfirmadoPaciente() != null ? requestDTO.getConfirmadoPaciente() : false)
            .confirmadoProfissional(requestDTO.getConfirmadoProfissional() != null ? requestDTO.getConfirmadoProfissional() : false)
            .geradoAutomaticamente(false)
            .build();

        if (requestDTO.getHospedagemId() != null) {
            // Implementar lookup de Hospedagem se necessário
            log.info("Hospedagem ID fornecido: {}", requestDTO.getHospedagemId());
        }

        agendamento = agendamentoPacienteRepository.save(agendamento);
        log.info("Agendamento criado com UUID: {}", agendamento.getUuid());

        return toResponseDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public ConflictCheckResponseDTO verificarConflito(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        AuthUsuario profissional = authUsuarioRepository.findById(profissionalId)
            .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        // Verificar bloqueios
        boolean temBloqueio = bloqueioAgendaRepository.existsBloqueioNoHorario(profissional, inicio, fim);
        if (temBloqueio) {
            return ConflictCheckResponseDTO.builder()
                .temConflito(true)
                .mensagem("Profissional tem bloqueio de agenda neste horário")
                .build();
        }

        // Verificar horário de trabalho
        Integer diaSemana = inicio.getDayOfWeek().getValue();
        LocalTime horaInicio = inicio.toLocalTime();
        boolean dentroHorario = horarioProfissionalRepository
            .findHorarioDisponivel(profissional, diaSemana, horaInicio)
            .isPresent();

        if (!dentroHorario) {
            return ConflictCheckResponseDTO.builder()
                .temConflito(true)
                .mensagem("Fora do horário de trabalho do profissional")
                .build();
        }

        // Verificar agendamentos existentes
        List<AgendamentoPaciente> agendamentosExistentes = agendamentoPacienteRepository
            .findByProfissionalAndPeriodo(profissional, inicio, fim);

        if (!agendamentosExistentes.isEmpty()) {
            return ConflictCheckResponseDTO.builder()
                .temConflito(true)
                .mensagem("Profissional já possui agendamento neste horário")
                .build();
        }

        return ConflictCheckResponseDTO.builder()
            .temConflito(false)
            .mensagem("Horário disponível")
            .build();
    }

    @Transactional(readOnly = true)
    public AgendamentoPacienteResponseDTO buscarPorUuid(String uuid) {
        AgendamentoPaciente agendamento = agendamentoPacienteRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        return toResponseDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoPacienteResponseDTO> listarPorPaciente(String pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        
        return agendamentoPacienteRepository.findByPaciente(paciente).stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoPacienteResponseDTO> listarPorProfissional(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        AuthUsuario profissional = authUsuarioRepository.findById(profissionalId)
            .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
        
        return agendamentoPacienteRepository.findByProfissionalAndPeriodo(profissional, inicio, fim).stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public AgendamentoPacienteResponseDTO confirmar(String uuid, boolean confirmadoPeloPaciente) {
        AgendamentoPaciente agendamento = agendamentoPacienteRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (confirmadoPeloPaciente) {
            agendamento.setConfirmadoPaciente(true);
            agendamento.setConfirmadoPacienteEm(LocalDateTime.now());
        } else {
            agendamento.setConfirmadoProfissional(true);
            agendamento.setConfirmadoProfissionalEm(LocalDateTime.now());
        }

        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        agendamento = agendamentoPacienteRepository.save(agendamento);

        return toResponseDTO(agendamento);
    }

    @Transactional
    public void cancelar(String uuid, String motivo) {
        AgendamentoPaciente agendamento = agendamentoPacienteRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamento.setMotivoCancelamento(motivo);
        agendamento.setCanceledAt(LocalDateTime.now());

        agendamentoPacienteRepository.save(agendamento);
        log.info("Agendamento {} cancelado: {}", uuid, motivo);
    }

    private AgendamentoPacienteResponseDTO toResponseDTO(AgendamentoPaciente agendamento) {
        // Get active hospedagem for the patient to retrieve room name
        String quartoNome = null;
        String hospedagemId = null;
        if (agendamento.getHospedagem() != null) {
            hospedagemId = agendamento.getHospedagem().getUuid();
            if (agendamento.getHospedagem().getQuarto() != null) {
                quartoNome = agendamento.getHospedagem().getQuarto().getNome();
            }
        } else {
            // If no hospedagem in agendamento, try to find active hospedagem for patient
            hospedagemRepository.findHospedagemAtivaDoPaciente(agendamento.getPaciente())
                .ifPresent(h -> {
                    if (h.getQuarto() != null) {
                        agendamento.setHospedagem(h);
                    }
                });
            if (agendamento.getHospedagem() != null) {
                hospedagemId = agendamento.getHospedagem().getUuid();
                if (agendamento.getHospedagem().getQuarto() != null) {
                    quartoNome = agendamento.getHospedagem().getQuarto().getNome();
                }
            }
        }

        return AgendamentoPacienteResponseDTO.builder()
            .id(agendamento.getId())
            .uuid(agendamento.getUuid())
            .pacienteId(agendamento.getPaciente().getId())
            .pacienteNome(agendamento.getPaciente().getDadoPessoal() != null ? 
                agendamento.getPaciente().getDadoPessoal().getNome() : "N/A")
            .tipoServicoId(agendamento.getTipoServico().getId())
            .tipoServicoNome(agendamento.getTipoServico().getNome())
            .profissionalUsuarioId(agendamento.getProfissionalUsuario() != null ? 
                agendamento.getProfissionalUsuario().getId() : null)
            .profissionalNome(agendamento.getProfissionalUsuario() != null ? 
                agendamento.getProfissionalUsuario().getNome() : "Não atribuído")
            .hospedagemId(hospedagemId)
            .quartoNome(quartoNome)
            .dataHoraInicio(agendamento.getDataHoraInicio())
            .dataHoraFim(agendamento.getDataHoraFim())
            .tipoAtendimento(agendamento.getTipoAtendimento())
            .prioridade(agendamento.getPrioridade())
            .status(agendamento.getStatus())
            .observacoes(agendamento.getObservacoes())
            .motivoCancelamento(agendamento.getMotivoCancelamento())
            .confirmadoPaciente(agendamento.getConfirmadoPaciente())
            .confirmadoProfissional(agendamento.getConfirmadoProfissional())
            .compareceu(agendamento.getCompareceu())
            .horaChegada(agendamento.getHoraChegada())
            .horaInicioAtendimento(agendamento.getHoraInicioAtendimento())
            .horaFimAtendimento(agendamento.getHoraFimAtendimento())
            .geradoAutomaticamente(agendamento.getGeradoAutomaticamente())
            .motivoGeracaoAutomatica(agendamento.getMotivoGeracaoAutomatica())
            .createdAt(agendamento.getCreatedAt())
            .updatedAt(agendamento.getUpdatedAt())
            .build();
    }

    @Transactional(readOnly = true)
    public List<?> listarPacientesElegiveis() {
        log.info("Buscando pacientes com hospedagem ativa");
        List<Paciente> pacientes = hospedagemRepository.findHospedagensAtivas().stream()
            .map(h -> h.getPaciente())
            .distinct()
            .collect(Collectors.toList());
        
        return pacientes.stream()
            .map(p -> {
                var hospedagem = hospedagemRepository.findHospedagemAtivaDoPaciente(p).orElse(null);
                return java.util.Map.of(
                    "id", p.getId(),
                    "nome", p.getDadoPessoal().getNome(),
                    "cpf", p.getDadoPessoal().getCpf(),
                    "quartoNome", hospedagem != null && hospedagem.getQuarto() != null ? hospedagem.getQuarto().getNome() : "Sem quarto",
                    "hospedagemId", hospedagem != null ? hospedagem.getId() : null
                );
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<?> listarProfissionaisElegiveis() {
        log.info("Buscando profissionais de saúde elegíveis (excluindo administrativos)");
        return authUsuarioRepository.findAll().stream()
            .filter(u -> u.getAtivo() != null && u.getAtivo())
            .filter(u -> {
                AuthUsuario.TipoUsuario tipo = u.getTipo();
                return tipo != AuthUsuario.TipoUsuario.ADMINISTRADOR && 
                       tipo != AuthUsuario.TipoUsuario.RECEPCIONISTA && 
                       tipo != AuthUsuario.TipoUsuario.AUDITOR;
            })
            .map(u -> java.util.Map.of(
                "id", u.getId(),
                "nome", u.getNome(),
                "tipo", u.getTipo().name(),
                "email", u.getEmail()
            ))
            .collect(Collectors.toList());
    }
}
