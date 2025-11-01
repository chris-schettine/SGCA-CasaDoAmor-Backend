package br.com.casadoamor.sgca.modules.auth.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para troca de senha temporária (primeiro acesso)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirstLoginPasswordChangeDTO {

    @NotBlank(message = "Senha temporária é obrigatória")
    private String senhaTemporaria;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, message = "Nova senha deve ter no mínimo 8 caracteres")
    private String novaSenha;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmarSenha;
}
