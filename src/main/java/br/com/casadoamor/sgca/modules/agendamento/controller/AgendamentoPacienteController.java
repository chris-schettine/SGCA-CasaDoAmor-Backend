package br.com.casadoamor.sgca.modules.agendamento.controller;

import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoPacienteRequestDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoPacienteResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.ConflictCheckResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.service.AgendamentoPacienteService;
import br.com.casadoamor.sgca.modules.common.dto.MessageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/agendamentos/pacientes")
@RequiredArgsConstructor
@Tag(name = "Agendamentos de Pacientes", description = "Endpoints para gerenciar agendamentos de pacientes")
public class AgendamentoPacienteController {

    private final AgendamentoPacienteService agendamentoPacienteService;

    @Operation(summary = "Listar todos os agendamentos", description = "Lista todos os agendamentos de pacientes com paginação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<AgendamentoPacienteResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Listando todos os agendamentos de pacientes - página: {}, tamanho: {}", page, size);
        List<AgendamentoPacienteResponseDTO> response = agendamentoPacienteService.listar(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Criar agendamento de paciente", description = "Cria um novo agendamento para um paciente")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    public ResponseEntity<AgendamentoPacienteResponseDTO> criar(@Valid @RequestBody AgendamentoPacienteRequestDTO requestDTO) {
        log.info("Requisição para criar agendamento de paciente");
        AgendamentoPacienteResponseDTO response = agendamentoPacienteService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar agendamento por UUID", description = "Retorna os detalhes de um agendamento específico")
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<AgendamentoPacienteResponseDTO> buscarPorUuid(@PathVariable String uuid) {
        log.info("Buscando agendamento por UUID: {}", uuid);
        AgendamentoPacienteResponseDTO response = agendamentoPacienteService.buscarPorUuid(uuid);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos por paciente", description = "Lista todos os agendamentos de um paciente específico")
    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<AgendamentoPacienteResponseDTO>> listarPorPaciente(@PathVariable String pacienteId) {
        log.info("Listando agendamentos do paciente ID: {}", pacienteId);
        List<AgendamentoPacienteResponseDTO> response = agendamentoPacienteService.listarPorPaciente(pacienteId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos por profissional", description = "Lista agendamentos de um profissional em um período específico")
    @GetMapping("/profissional/{profissionalId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<AgendamentoPacienteResponseDTO>> listarPorProfissional(
            @PathVariable Long profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        log.info("Listando agendamentos do profissional ID: {} entre {} e {}", profissionalId, inicio, fim);
        List<AgendamentoPacienteResponseDTO> response = agendamentoPacienteService.listarPorProfissional(profissionalId, inicio, fim);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verificar conflito de horário", description = "Verifica se existe conflito de agendamento para um profissional no horário especificado")
    @PostMapping("/verificar-conflito")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    public ResponseEntity<ConflictCheckResponseDTO> verificarConflito(
            @RequestParam Long profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        log.info("Verificando conflito para profissional ID: {} entre {} e {}", profissionalId, inicio, fim);
        ConflictCheckResponseDTO response = agendamentoPacienteService.verificarConflito(profissionalId, inicio, fim);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirmar agendamento", description = "Confirma um agendamento pelo paciente ou profissional")
    @PutMapping("/{uuid}/confirmar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<AgendamentoPacienteResponseDTO> confirmar(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "true") boolean confirmadoPeloPaciente) {
        log.info("Confirmando agendamento {}: confirmado pelo paciente = {}", uuid, confirmadoPeloPaciente);
        AgendamentoPacienteResponseDTO response = agendamentoPacienteService.confirmar(uuid, confirmadoPeloPaciente);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancelar agendamento", description = "Cancela um agendamento existente")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    public ResponseEntity<MessageResponseDTO> cancelar(
            @PathVariable String uuid,
            @RequestParam String motivo) {
        log.info("Cancelando agendamento {}: {}", uuid, motivo);
        agendamentoPacienteService.cancelar(uuid, motivo);
        return ResponseEntity.ok(MessageResponseDTO.success("Agendamento cancelado com sucesso"));
    }

    @Operation(summary = "Listar pacientes elegíveis", description = "Lista pacientes com hospedagem ativa que podem ter agendamentos")
    @GetMapping("/pacientes-elegiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    public ResponseEntity<?> listarPacientesElegiveis() {
        log.info("Listando pacientes elegíveis para agendamento");
        return ResponseEntity.ok(agendamentoPacienteService.listarPacientesElegiveis());
    }

    @Operation(summary = "Listar profissionais elegíveis", description = "Lista profissionais de saúde que podem atender agendamentos")
    @GetMapping("/profissionais-elegiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    public ResponseEntity<?> listarProfissionaisElegiveis() {
        log.info("Listando profissionais elegíveis para agendamento");
        return ResponseEntity.ok(agendamentoPacienteService.listarProfissionaisElegiveis());
    }
}
