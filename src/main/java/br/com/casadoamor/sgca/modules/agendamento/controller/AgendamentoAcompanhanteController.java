package br.com.casadoamor.sgca.modules.agendamento.controller;

import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoAcompanhanteRequestDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoAcompanhanteResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.ConflictCheckResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.service.AgendamentoAcompanhanteService;
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
@RequestMapping("/api/agendamentos/acompanhantes")
@RequiredArgsConstructor
@Tag(name = "Agendamentos de Acompanhantes", description = "Endpoints para gerenciar agendamentos de acompanhantes")
public class AgendamentoAcompanhanteController {

    private final AgendamentoAcompanhanteService agendamentoAcompanhanteService;

    @Operation(summary = "Listar todos os agendamentos", description = "Lista todos os agendamentos de acompanhantes com paginação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<AgendamentoAcompanhanteResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Listando todos os agendamentos de acompanhantes - página: {}, tamanho: {}", page, size);
        List<AgendamentoAcompanhanteResponseDTO> response = agendamentoAcompanhanteService.listar(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Criar agendamento de acompanhante", description = "Cria um novo agendamento para um acompanhante")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    public ResponseEntity<AgendamentoAcompanhanteResponseDTO> criar(@Valid @RequestBody AgendamentoAcompanhanteRequestDTO requestDTO) {
        log.info("Requisição para criar agendamento de acompanhante");
        AgendamentoAcompanhanteResponseDTO response = agendamentoAcompanhanteService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar agendamento por UUID", description = "Retorna os detalhes de um agendamento específico")
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<AgendamentoAcompanhanteResponseDTO> buscarPorUuid(@PathVariable String uuid) {
        log.info("Buscando agendamento por UUID: {}", uuid);
        AgendamentoAcompanhanteResponseDTO response = agendamentoAcompanhanteService.buscarPorUuid(uuid);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos por acompanhante", description = "Lista todos os agendamentos de um acompanhante específico")
    @GetMapping("/acompanhante/{acompanhanteId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<AgendamentoAcompanhanteResponseDTO>> listarPorAcompanhante(@PathVariable String acompanhanteId) {
        log.info("Listando agendamentos do acompanhante ID: {}", acompanhanteId);
        List<AgendamentoAcompanhanteResponseDTO> response = agendamentoAcompanhanteService.listarPorAcompanhante(acompanhanteId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos por profissional", description = "Lista agendamentos de um profissional em um período específico")
    @GetMapping("/profissional/{profissionalId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<AgendamentoAcompanhanteResponseDTO>> listarPorProfissional(
            @PathVariable Long profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        log.info("Listando agendamentos do profissional ID: {} entre {} e {}", profissionalId, inicio, fim);
        List<AgendamentoAcompanhanteResponseDTO> response = agendamentoAcompanhanteService.listarPorProfissional(profissionalId, inicio, fim);
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
        ConflictCheckResponseDTO response = agendamentoAcompanhanteService.verificarConflito(profissionalId, inicio, fim);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirmar agendamento", description = "Confirma um agendamento pelo acompanhante ou profissional")
    @PutMapping("/{uuid}/confirmar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA', 'MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<AgendamentoAcompanhanteResponseDTO> confirmar(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "true") boolean confirmadoPeloAcompanhante) {
        log.info("Confirmando agendamento {}: confirmado pelo acompanhante = {}", uuid, confirmadoPeloAcompanhante);
        AgendamentoAcompanhanteResponseDTO response = agendamentoAcompanhanteService.confirmar(uuid, confirmadoPeloAcompanhante);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancelar agendamento", description = "Cancela um agendamento existente")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    public ResponseEntity<MessageResponseDTO> cancelar(
            @PathVariable String uuid,
            @RequestParam String motivo) {
        log.info("Cancelando agendamento {}: {}", uuid, motivo);
        agendamentoAcompanhanteService.cancelar(uuid, motivo);
        return ResponseEntity.ok(MessageResponseDTO.success("Agendamento cancelado com sucesso"));
    }

    @Operation(summary = "Listar acompanhantes elegíveis", description = "Lista acompanhantes de pacientes com hospedagem ativa")
    @GetMapping("/acompanhantes-elegiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    public ResponseEntity<?> listarAcompanhantesElegiveis() {
        log.info("Listando acompanhantes elegíveis para agendamento");
        return ResponseEntity.ok(agendamentoAcompanhanteService.listarAcompanhantesElegiveis());
    }

    @Operation(summary = "Listar profissionais elegíveis", description = "Lista profissionais de saúde que podem atender agendamentos")
    @GetMapping("/profissionais-elegiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    public ResponseEntity<?> listarProfissionaisElegiveis() {
        log.info("Listando profissionais elegíveis para agendamento");
        return ResponseEntity.ok(agendamentoAcompanhanteService.listarProfissionaisElegiveis());
    }
}
