package br.com.casadoamor.sgca.controller.auth;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.casadoamor.sgca.dto.SessaoDTO;
import br.com.casadoamor.sgca.dto.auth.request.ActivateAccountRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.ChangePasswordRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.FirstLoginPasswordChangeDTO;
import br.com.casadoamor.sgca.dto.auth.request.ForgotPasswordRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.LoginRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.RegisterRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.ResetPasswordRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.VerifyEmailRequestDTO;
import br.com.casadoamor.sgca.dto.auth.response.AuthResponseDTO;
import br.com.casadoamor.sgca.dto.common.MessageResponseDTO;
import br.com.casadoamor.sgca.service.admin.SessaoService;
import br.com.casadoamor.sgca.service.auth.AccountActivationService;
import br.com.casadoamor.sgca.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller de autenticação - endpoints para registro, login, e gerenciamento de conta
 */
@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints completos de autenticação e gerenciamento de conta")
public class AuthController {

    private final AuthService authService;
    private final SessaoService sessaoService;
    private final AccountActivationService accountActivationService;

    /**
     * Endpoint para registrar um novo usuário
     * POST /auth/register
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria um novo usuário no sistema e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ou CPF já cadastrado")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            AuthResponseDTO response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erro ao registrar usuário: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para fazer login
     * POST /auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "403", description = "Conta bloqueada ou inativa")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        try {
            AuthResponseDTO response = authService.login(request, httpRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erro ao fazer login: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para solicitar recuperação de senha
     * POST /auth/forgot-password
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Esqueci minha senha", description = "Solicita recuperação de senha via email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email de recuperação enviado (se existir)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<MessageResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        MessageResponseDTO response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para redefinir senha com token
     * POST /auth/reset-password
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Redefine a senha usando token de recuperação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        try {
            MessageResponseDTO response = authService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint para verificar email
     * POST /auth/verify-email
     */
    @PostMapping("/verify-email")
    @Operation(summary = "Verificar email", description = "Verifica o email do usuário usando token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verificado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequestDTO request) {
        try {
            MessageResponseDTO response = authService.verifyEmail(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint para alterar senha (usuário autenticado)
     * POST /auth/change-password
     */
    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Alterar senha", description = "Altera a senha do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request,
                                           Authentication authentication) {
        try {
            String cpf = authentication.getName();
            MessageResponseDTO response = authService.changePassword(request, cpf);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint para listar sessões ativas
     * GET /auth/sessions
     */
    @GetMapping("/sessions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar sessões ativas", description = "Lista todas as sessões ativas do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de sessões retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<SessaoDTO>> listSessions(Authentication authentication,
                                                        @RequestHeader("Authorization") String token) {
        String cpf = authentication.getName();
        Long usuarioId = obterUsuarioId(cpf);
        String tokenJwt = token.replace("Bearer ", "");
        
        List<SessaoDTO> sessoes = sessaoService.listarSessoesAtivas(usuarioId, tokenJwt);
        return ResponseEntity.ok(sessoes);
    }

    /**
     * Endpoint para revogar sessão específica
     * DELETE /auth/sessions/{id}
     */
    @DeleteMapping("/sessions/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Revogar sessão", description = "Revoga uma sessão específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessão revogada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<?> revokeSession(@PathVariable Long id, Authentication authentication) {
        try {
            String cpf = authentication.getName();
            Long usuarioId = obterUsuarioId(cpf);
            
            sessaoService.revogarSessao(id, usuarioId);
            return ResponseEntity.ok(MessageResponseDTO.success("Sessão revogada com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Método auxiliar para obter ID do usuário
     */
    private Long obterUsuarioId(String cpf) {
        // Buscar o usuário pelo cpf e retornar o ID real
        return authService.findUserIdByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Classe interna para respostas de erro
     */
    /**
     * Endpoint para ativar conta e definir senha definitiva
     * POST /auth/activate-account
     */
    @PostMapping("/activate-account")
    @Operation(summary = "Ativar conta", description = "Ativa conta criada por admin e define senha definitiva")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "409", description = "Conta já ativada")
    })
    public ResponseEntity<?> activateAccount(@Valid @RequestBody ActivateAccountRequestDTO request) {
        try {
            MessageResponseDTO response = accountActivationService.ativarConta(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint para reenviar email de ativação
     * POST /auth/resend-activation
     */
    @PostMapping("/resend-activation")
    @Operation(summary = "Reenviar email de ativação", description = "Reenvia email com novo token e senha temporária")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email reenviado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Conta já ativada ou não encontrada")
    })
    public ResponseEntity<?> resendActivation(@RequestBody ResendActivationDTO request) {
        try {
            MessageResponseDTO response = accountActivationService.reenviarEmailAtivacao(request.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint para troca de senha temporária (primeiro acesso)
     * POST /auth/first-login-password-change
     */
    @PostMapping("/first-login-password-change")
    @Operation(summary = "Trocar senha temporária", description = "Permite usuário trocar senha temporária no primeiro acesso")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha temporária inválida ou senhas não conferem"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<?> firstLoginPasswordChange(
            @Valid @RequestBody FirstLoginPasswordChangeDTO request,
            Authentication authentication) {
        try {
            // Implementar lógica de troca de senha temporária
            String cpf = authentication.getName();
            accountActivationService.trocarSenhaTemporaria(cpf, request);
            return ResponseEntity.ok(MessageResponseDTO.success("Senha alterada com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Classes internas para DTOs simples
    @SuppressWarnings("unused")
    private static class ResendActivationDTO {
        private String email;
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }

    @SuppressWarnings("unused")
    private static class ErrorResponse {
        private final String message;
        private final long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
