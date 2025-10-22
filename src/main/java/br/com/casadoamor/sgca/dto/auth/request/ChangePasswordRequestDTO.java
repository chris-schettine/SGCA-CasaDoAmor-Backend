package br.com.casadoamor.sgca.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para alteração de senha (usuário autenticado)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para alteração de senha do usuário autenticado")
public class ChangePasswordRequestDTO {

    @Schema(description = "Senha atual do usuário", example = "SenhaAntiga@123", required = true)
    @NotBlank(message = "Senha atual é obrigatória")
    private String senhaAtual;

    @Schema(description = "Nova senha do usuário (mínimo 6 caracteres)", example = "NovaSenha@456", required = true)
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "Nova senha deve ter no mínimo 6 caracteres")
    private String novaSenha;
}
