package br.com.casadoamor.sgca.modules.hospedagem.controller;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.dto.*;
import br.com.casadoamor.sgca.modules.hospedagem.service.HospedagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller para gerenciamento de hospedagens (histórico de estadias)
 */
@Tag(name = "Hospedagens", description = "Endpoints para gerenciamento de hospedagens e histórico de estadias")
@RestController
@RequestMapping("/api/hospedagens")
@RequiredArgsConstructor
public class HospedagemController {

    private final HospedagemService hospedagemService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Registrar entrada de paciente", description = "Registra a entrada de um paciente em um quarto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Entrada registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Paciente já possui hospedagem ativa ou quarto sem vagas"),
            @ApiResponse(responseCode = "404", description = "Paciente ou quarto não encontrado")
    })
    @PostMapping
    public ResponseEntity<HospedagemResponseDTO> registrarEntrada(
            @Valid @RequestBody HospedagemRequestDTO dto,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        HospedagemResponseDTO response = hospedagemService.registrarEntrada(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Registrar saída de paciente", description = "Registra a saída de um paciente e libera o leito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saída registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Hospedagem não encontrada"),
            @ApiResponse(responseCode = "400", description = "Data de saída anterior à data de entrada")
    })
    @PutMapping("/{uuid}/saida")
    public ResponseEntity<HospedagemResponseDTO> registrarSaida(
            @PathVariable String uuid,
            @Valid @RequestBody HospedagemSaidaDTO dto,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        HospedagemResponseDTO response = hospedagemService.registrarSaida(uuid, dto, usuarioLogado);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Transferir paciente de quarto", description = "Move o paciente para outro quarto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Hospedagem ou quarto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Quarto destino sem vagas ou hospedagem não está ativa")
    })
    @PutMapping("/{uuid}/transferir")
    public ResponseEntity<HospedagemResponseDTO> transferirQuarto(
            @PathVariable String uuid,
            @RequestParam String novoQuartoUuid,
            @RequestParam(required = false) String motivoTransferencia,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        HospedagemResponseDTO response = hospedagemService.transferirQuarto(
                uuid, novoQuartoUuid, motivoTransferencia, usuarioLogado);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Buscar hospedagem por UUID", description = "Retorna os dados completos de uma hospedagem")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hospedagem encontrada"),
            @ApiResponse(responseCode = "404", description = "Hospedagem não encontrada")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<HospedagemResponseDTO> buscarPorUuid(@PathVariable String uuid) {
        HospedagemResponseDTO response = hospedagemService.buscarPorUuidDTO(uuid);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar hospedagens ativas", description = "Retorna todas as hospedagens em andamento")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/ativas")
    public ResponseEntity<List<HospedagemResponseDTO>> listarAtivas() {
        List<HospedagemResponseDTO> response = hospedagemService.listarAtivas();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar hospedagens por paciente", description = "Retorna o histórico completo de hospedagens de um paciente")
    @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso")
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<HospedagemResponseDTO>> listarPorPaciente(@PathVariable String pacienteId) {
        List<HospedagemResponseDTO> response = hospedagemService.listarPorPaciente(pacienteId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar hospedagens por quarto", description = "Retorna o histórico de hospedagens de um quarto")
    @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso")
    @GetMapping("/quarto/{quartoUuid}")
    public ResponseEntity<List<HospedagemResponseDTO>> listarPorQuarto(@PathVariable String quartoUuid) {
        List<HospedagemResponseDTO> response = hospedagemService.listarPorQuarto(quartoUuid);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar hospedagens por período", description = "Retorna hospedagens filtradas por data de entrada")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/periodo")
    public ResponseEntity<List<HospedagemResponseDTO>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        List<HospedagemResponseDTO> response = hospedagemService.listarPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar hospedagens com previsão de saída vencida", 
               description = "Retorna hospedagens ativas cuja data prevista de saída já passou")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/previsao-vencida")
    public ResponseEntity<List<HospedagemResponseDTO>> listarComPrevisaoVencida() {
        List<HospedagemResponseDTO> response = hospedagemService.listarComPrevisaoVencida();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar com paginação", description = "Retorna lista paginada de hospedagens")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping("/paginated")
    public ResponseEntity<Page<HospedagemResponseDTO>> listarComPaginacao(
            @PageableDefault(size = 20, sort = "dataEntrada", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<HospedagemResponseDTO> response = hospedagemService.listarComPaginacao(pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Verificar se paciente tem hospedagem ativa", 
               description = "Retorna true se o paciente possui hospedagem em andamento")
    @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso")
    @GetMapping("/paciente/{pacienteId}/ativa")
    public ResponseEntity<Boolean> verificarHospedagemAtiva(@PathVariable String pacienteId) {
        boolean temHospedagemAtiva = hospedagemService.pacienteTemHospedagemAtiva(pacienteId);
        return ResponseEntity.ok(temHospedagemAtiva);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar hospedagem", description = "Remove logicamente uma hospedagem (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Hospedagem deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Hospedagem não encontrada")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deletar(@PathVariable String uuid) {
        hospedagemService.deletar(uuid);
        return ResponseEntity.noContent().build();
    }
}
