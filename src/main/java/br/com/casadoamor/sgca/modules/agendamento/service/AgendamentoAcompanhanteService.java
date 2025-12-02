package br.com.casadoamor.sgca.modules.agendamento.service;

import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoAcompanhanteRequestDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoAcompanhanteResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.ConflictCheckResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.entity.AgendamentoAcompanhante;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.repository.AgendamentoAcompanhanteRepository;
import br.com.casadoamor.sgca.modules.agendamento.repository.BloqueioAgendaRepository;
import br.com.casadoamor.sgca.modules.agendamento.repository.HorarioProfissionalRepository;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.acompanhante.repository.AcompanhanteRepository;
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
public class AgendamentoAcompanhanteService {

    private final AgendamentoAcompanhanteRepository agendamentoAcompanhanteRepository;
    private final AcompanhanteRepository acompanhanteRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final AuthUsuarioRepository authUsuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final HospedagemRepository hospedagemRepository;
    private final BloqueioAgendaRepository bloqueioAgendaRepository;
    private final HorarioProfissionalRepository horarioProfissionalRepository;

    @Transactional(readOnly = true)
    public List<AgendamentoAcompanhanteResponseDTO> listar(int page, int size) {
        return agendamentoAcompanhanteRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size))
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }

    public AgendamentoAcompanhanteResponseDTO criar(AgendamentoAcompanhanteRequestDTO requestDTO) {
        log.info("Criando agendamento para acompanhante ID: {}", requestDTO.getAcompanhanteId());

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
        Acompanhante acompanhante = acompanhanteRepository.findById(String.valueOf(requestDTO.getAcompanhanteId()))
            .orElseThrow(() -> new RuntimeException("Acompanhante não encontrado"));

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
        AgendamentoAcompanhante agendamento = AgendamentoAcompanhante.builder()
            .acompanhante(acompanhante)
            .tipoServico(tipoServico)
            .profissionalUsuario(profissional)
            .dataHoraInicio(requestDTO.getDataHoraInicio())
            .dataHoraFim(requestDTO.getDataHoraFim())
            .duracaoMinutos(duracaoMinutos)
            .tipoAtendimento(requestDTO.getTipoAtendimento())
            .prioridade(requestDTO.getPrioridade())
            .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : StatusAgendamento.AGENDADO)
            .observacoes(requestDTO.getObservacoes())
            .confirmadoAcompanhante(requestDTO.getConfirmadoAcompanhante() != null ? requestDTO.getConfirmadoAcompanhante() : false)
            .confirmadoProfissional(requestDTO.getConfirmadoProfissional() != null ? requestDTO.getConfirmadoProfissional() : false)
            .build();

        if (requestDTO.getPacienteVinculadoId() != null) {
            Paciente pacienteVinculado = pacienteRepository.findById(String.valueOf(requestDTO.getPacienteVinculadoId()))
                .orElseThrow(() -> new RuntimeException("Paciente vinculado não encontrado"));
            agendamento.setPacienteVinculado(pacienteVinculado);
        }

        agendamento = agendamentoAcompanhanteRepository.save(agendamento);
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
        List<AgendamentoAcompanhante> agendamentosExistentes = agendamentoAcompanhanteRepository
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
    public AgendamentoAcompanhanteResponseDTO buscarPorUuid(String uuid) {
        AgendamentoAcompanhante agendamento = agendamentoAcompanhanteRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        return toResponseDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoAcompanhanteResponseDTO> listarPorAcompanhante(String acompanhanteId) {
        Acompanhante acompanhante = acompanhanteRepository.findById(acompanhanteId)
            .orElseThrow(() -> new RuntimeException("Acompanhante não encontrado"));
        
        return agendamentoAcompanhanteRepository.findByAcompanhante(acompanhante).stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoAcompanhanteResponseDTO> listarPorProfissional(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        AuthUsuario profissional = authUsuarioRepository.findById(profissionalId)
            .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
        
        return agendamentoAcompanhanteRepository.findByProfissionalAndPeriodo(profissional, inicio, fim).stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public AgendamentoAcompanhanteResponseDTO confirmar(String uuid, boolean confirmadoPeloAcompanhante) {
        AgendamentoAcompanhante agendamento = agendamentoAcompanhanteRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (confirmadoPeloAcompanhante) {
            agendamento.setConfirmadoAcompanhante(true);
            agendamento.setConfirmadoAcompanhanteEm(LocalDateTime.now());
        } else {
            agendamento.setConfirmadoProfissional(true);
            agendamento.setConfirmadoProfissionalEm(LocalDateTime.now());
        }

        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        agendamento = agendamentoAcompanhanteRepository.save(agendamento);

        return toResponseDTO(agendamento);
    }

    @Transactional
    public void cancelar(String uuid, String motivo) {
        AgendamentoAcompanhante agendamento = agendamentoAcompanhanteRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamento.setMotivoCancelamento(motivo);
        agendamento.setCanceledAt(LocalDateTime.now());

        agendamentoAcompanhanteRepository.save(agendamento);
        log.info("Agendamento {} cancelado: {}", uuid, motivo);
    }

    private AgendamentoAcompanhanteResponseDTO toResponseDTO(AgendamentoAcompanhante agendamento) {
        // Get room name from the linked patient's active hospedagem
        String quartoNome = null;
        if (agendamento.getPacienteVinculado() != null) {
            hospedagemRepository.findHospedagemAtivaDoPaciente(agendamento.getPacienteVinculado())
                .ifPresent(h -> {
                    if (h.getQuarto() != null) {
                        // Store in a final variable to use in lambda
                    }
                });
            // Alternative approach: directly query and set
            var hospedagemOpt = hospedagemRepository.findHospedagemAtivaDoPaciente(agendamento.getPacienteVinculado());
            if (hospedagemOpt.isPresent() && hospedagemOpt.get().getQuarto() != null) {
                quartoNome = hospedagemOpt.get().getQuarto().getNome();
            }
        }

        return AgendamentoAcompanhanteResponseDTO.builder()
            .id(agendamento.getId())
            .uuid(agendamento.getUuid())
            .acompanhanteId(agendamento.getAcompanhante().getId())
            .acompanhanteNome(agendamento.getAcompanhante().getDadoPessoal().getNome())
            .tipoServicoId(agendamento.getTipoServico().getId())
            .tipoServicoNome(agendamento.getTipoServico().getNome())
            .profissionalUsuarioId(agendamento.getProfissionalUsuario().getId())
            .profissionalNome(agendamento.getProfissionalUsuario().getNome())
            .pacienteVinculadoId(agendamento.getPacienteVinculado() != null ? agendamento.getPacienteVinculado().getId() : null)
            .pacienteVinculadoNome(agendamento.getPacienteVinculado() != null ? agendamento.getPacienteVinculado().getDadoPessoal().getNome() : null)
            .quartoNome(quartoNome)
            .dataHoraInicio(agendamento.getDataHoraInicio())
            .dataHoraFim(agendamento.getDataHoraFim())
            .tipoAtendimento(agendamento.getTipoAtendimento())
            .prioridade(agendamento.getPrioridade())
            .status(agendamento.getStatus())
            .observacoes(agendamento.getObservacoes())
            .motivoCancelamento(agendamento.getMotivoCancelamento())
            .confirmadoAcompanhante(agendamento.getConfirmadoAcompanhante())
            .confirmadoProfissional(agendamento.getConfirmadoProfissional())
            .compareceu(agendamento.getCompareceu())
            .horaChegada(agendamento.getHoraChegada())
            .horaInicioAtendimento(agendamento.getHoraInicioAtendimento())
            .horaFimAtendimento(agendamento.getHoraFimAtendimento())
            .createdAt(agendamento.getCreatedAt())
            .updatedAt(agendamento.getUpdatedAt())
            .build();
    }

    @Transactional(readOnly = true)
    public List<?> listarAcompanhantesElegiveis() {
        log.info("Buscando acompanhantes de pacientes com hospedagem ativa");
        List<Paciente> pacientesComHospedagem = hospedagemRepository.findHospedagensAtivas().stream()
            .map(h -> h.getPaciente())
            .distinct()
            .collect(Collectors.toList());
        
        return pacientesComHospedagem.stream()
            .flatMap(p -> acompanhanteRepository.findByPacienteAndDeletedAtIsNull(p).stream())
            .filter(a -> a.isAtivo())
            .map(a -> {
                var hospedagem = hospedagemRepository.findHospedagemAtivaDoPaciente(a.getPaciente()).orElse(null);
                return java.util.Map.of(
                    "id", a.getId(),
                    "nome", a.getDadoPessoal().getNome(),
                    "cpf", a.getDadoPessoal().getCpf(),
                    "pacienteNome", a.getPaciente().getDadoPessoal().getNome(),
                    "pacienteId", a.getPaciente().getId(),
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
                return tipo != AuthUsuario.TipoUsuario.ADMINISTRADOR
                    && tipo != AuthUsuario.TipoUsuario.RECEPCIONISTA
                    && tipo != AuthUsuario.TipoUsuario.AUDITOR;
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
