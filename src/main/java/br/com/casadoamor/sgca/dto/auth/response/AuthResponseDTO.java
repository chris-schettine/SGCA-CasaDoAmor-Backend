package br.com.casadoamor.sgca.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de autenticação (login/registro bem-sucedido)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    
    private String token;
    private String tipo; // "Bearer"
    private String email;
    private String nome;
    private String tipoUsuario;
    private Long expiresIn; // Tempo de expiração em milissegundos
    
    // Campos para 2FA
    private Boolean requires2FA; // Indica se 2FA é necessário
    private Long userId; // ID do usuário para verificação 2FA
    private String message; // Mensagem adicional (ex: código enviado)
}
