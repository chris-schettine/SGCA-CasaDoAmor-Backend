package br.com.casadoamor.sgca.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para ativar conta e definir senha definitiva
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivateAccountRequestDTO {

    @NotBlank(message = "Token de ativação é obrigatório")
    private String token;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Senha temporária é obrigatória")
    private String senhaTemporaria;

    @NotBlank(message = "Nova senha é obrigatória")
    private String novaSenha;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmarSenha;
}
