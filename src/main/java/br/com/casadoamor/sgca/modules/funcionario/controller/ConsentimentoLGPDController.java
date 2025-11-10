package br.com.casadoamor.sgca.modules.funcionario.controller;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.funcionario.dto.ConsentimentoLGPDRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ConsentimentoLGPDResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.service.ConsentimentoLGPDService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento de consentimentos LGPD de profissionais
 */
@Tag(name = "Consentimentos LGPD", description = "Endpoints para gerenciamento de consentimentos LGPD")
@RestController
@RequestMapping("/api/profissionais/{profissionalUuid}/consentimentos")
@RequiredArgsConstructor
public class ConsentimentoLGPDController {

    private final ConsentimentoLGPDService consentimentoService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Registrar consentimento LGPD", description = "Registra um novo consentimento ou revogação LGPD para um profissional")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consentimento registrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ConsentimentoLGPDResponseDTO> registrarConsentimento(
            @PathVariable String profissionalUuid,
            @Valid @RequestBody ConsentimentoLGPDRequestDTO dto,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        ConsentimentoLGPDResponseDTO response = consentimentoService.registrarConsentimento(
                profissionalUuid, dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar consentimentos", description = "Lista histórico de consentimentos de um profissional")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    @GetMapping
    public ResponseEntity<List<ConsentimentoLGPDResponseDTO>> listarConsentimentos(
            @PathVariable String profissionalUuid) {
        
        List<ConsentimentoLGPDResponseDTO> response = consentimentoService.listarConsentimentos(profissionalUuid);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Verificar consentimento válido", description = "Verifica se profissional possui consentimento válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    @GetMapping("/valido")
    public ResponseEntity<Boolean> hasConsentimentoValido(@PathVariable String profissionalUuid) {
        boolean hasConsentimento = consentimentoService.hasConsentimentoValido(profissionalUuid);
        return ResponseEntity.ok(hasConsentimento);
    }
}
