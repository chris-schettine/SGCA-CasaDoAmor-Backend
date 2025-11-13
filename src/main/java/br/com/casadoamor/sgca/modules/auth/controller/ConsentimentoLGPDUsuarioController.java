package br.com.casadoamor.sgca.modules.auth.controller;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.service.ConsentimentoLGPDUsuarioService;
import br.com.casadoamor.sgca.modules.funcionario.dto.ConsentimentoLGPDRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ConsentimentoLGPDResponseDTO;
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
 * Controller para gerenciamento de consentimentos LGPD de usuários do sistema
 */
@Tag(name = "Consentimentos LGPD - Usuários", description = "Endpoints para gerenciamento de consentimentos LGPD dos usuários do sistema")
@RestController
@RequestMapping("/api/usuarios/{cpf}/consentimentos-lgpd")
@RequiredArgsConstructor
public class ConsentimentoLGPDUsuarioController {

    private final ConsentimentoLGPDUsuarioService consentimentoService;

    @PreAuthorize("hasRole('ADMINISTRADOR') or #cpf == authentication.principal.cpf")
    @Operation(summary = "Registrar consentimento LGPD", 
               description = "Registra um novo consentimento ou revogação LGPD para um usuário do sistema. " +
                           "Administradores podem registrar para qualquer usuário. " +
                           "Usuários comuns podem registrar apenas para si mesmos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consentimento registrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<ConsentimentoLGPDResponseDTO> registrarConsentimento(
            @PathVariable String cpf,
            @Valid @RequestBody ConsentimentoLGPDRequestDTO dto,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        ConsentimentoLGPDResponseDTO response = consentimentoService.registrarConsentimento(
                cpf, dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR') or #cpf == authentication.principal.cpf")
    @Operation(summary = "Listar consentimentos", 
               description = "Lista histórico completo de consentimentos de um usuário. " +
                           "Administradores e auditores podem ver de qualquer usuário. " +
                           "Usuários comuns podem ver apenas o próprio histórico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<List<ConsentimentoLGPDResponseDTO>> listarConsentimentos(
            @PathVariable String cpf) {
        
        List<ConsentimentoLGPDResponseDTO> response = consentimentoService.listarConsentimentos(cpf);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR') or #cpf == authentication.principal.cpf")
    @Operation(summary = "Verificar consentimento válido", 
               description = "Verifica se o usuário possui consentimento LGPD válido (última entrada com concorda=true)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/valido")
    public ResponseEntity<Boolean> hasConsentimentoValido(@PathVariable String cpf) {
        boolean hasConsentimento = consentimentoService.hasConsentimentoValido(cpf);
        return ResponseEntity.ok(hasConsentimento);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR') or #cpf == authentication.principal.cpf")
    @Operation(summary = "Obter consentimento atual", 
               description = "Retorna o consentimento mais recente do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consentimento retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado ou sem consentimento"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/atual")
    public ResponseEntity<ConsentimentoLGPDResponseDTO> obterConsentimentoAtual(@PathVariable String cpf) {
        ConsentimentoLGPDResponseDTO response = consentimentoService.obterConsentimentoAtual(cpf);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
