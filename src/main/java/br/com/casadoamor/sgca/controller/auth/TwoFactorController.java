package br.com.casadoamor.sgca.controller.auth;

import br.com.casadoamor.sgca.dto.auth.response.AuthResponseDTO;
import br.com.casadoamor.sgca.dto.common.MessageResponseDTO;
import br.com.casadoamor.sgca.dto.twofactor.Enable2FADTO;
import br.com.casadoamor.sgca.dto.twofactor.Setup2FADTO;
import br.com.casadoamor.sgca.dto.twofactor.Verify2FADTO;
import br.com.casadoamor.sgca.service.auth.AuthService;
import br.com.casadoamor.sgca.service.auth.TwoFactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para autenticação de dois fatores (2FA)
 */
@RestController
@RequestMapping("/auth/2fa")
@RequiredArgsConstructor
@Slf4j
public class TwoFactorController {

    private final TwoFactorService twoFactorService;
    private final AuthService authService;

    /**
     * Endpoint 25: Configurar 2FA
     * POST /auth/2fa/setup
     * 
     * Inicia configuração de 2FA enviando código por email
     */
    @PostMapping("/setup")
    public ResponseEntity<Setup2FADTO> setup(Authentication authentication) {
        log.info("Solicitação de configuração 2FA para usuário: {}", authentication.getName());

        Long usuarioId = authService.buscarIdPorCpf(authentication.getName());
        Setup2FADTO response = twoFactorService.configurar2FA(usuarioId);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 26: Habilitar/Desabilitar 2FA
     * POST /auth/2fa/enable
     * 
     * Ativa ou desativa 2FA após validar código
     */
    @PostMapping("/enable")
    public ResponseEntity<MessageResponseDTO> enable(
            @Valid @RequestBody Enable2FADTO dto,
            Authentication authentication) {
        
        log.info("Alterando status 2FA para usuário: {}", authentication.getName());

        Long usuarioId = authService.buscarIdPorCpf(authentication.getName());
        twoFactorService.alterarStatus2FA(usuarioId, dto);

        String mensagem = dto.getHabilitar() 
                ? "2FA habilitado com sucesso" 
                : "2FA desabilitado com sucesso";

        return ResponseEntity.ok(MessageResponseDTO.success(mensagem));
    }

    /**
     * Endpoint 27: Verificar código 2FA no login
     * POST /auth/2fa/verify
     * 
     * Valida código 2FA e retorna JWT
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody Verify2FADTO dto) {
        log.info("Verificando código 2FA para CPF: {}", dto.getCpf());

        try {
            Long usuarioId = authService.buscarIdPorCpf(dto.getCpf());
            
            boolean codigoValido = twoFactorService.validarCodigoLogin(usuarioId, dto.getCodigo());
            
            if (!codigoValido) {
                return ResponseEntity.status(401)
                        .body(MessageResponseDTO.error("Código 2FA inválido"));
            }

            // Gera JWT após validação 2FA
            AuthResponseDTO token = authService.gerarTokenPorCpf(dto.getCpf());

            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("Erro ao verificar 2FA: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(MessageResponseDTO.error(e.getMessage()));
        }
    }

    /**
     * Endpoint auxiliar: Reenviar código 2FA
     * POST /auth/2fa/resend
     * 
     * Reenvia código caso tenha expirado
     */
    @PostMapping("/resend")
    public ResponseEntity<MessageResponseDTO> resend(Authentication authentication) {
        log.info("Reenviando código 2FA para usuário: {}", authentication.getName());

        Long usuarioId = authService.buscarIdPorCpf(authentication.getName());
        twoFactorService.enviarCodigoLogin(usuarioId);

        return ResponseEntity.ok(MessageResponseDTO.success("Novo código enviado"));
    }
}
