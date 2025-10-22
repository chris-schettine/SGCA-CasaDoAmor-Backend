package br.com.casadoamor.sgca.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para autenticação do usuário")
public class LoginRequestDTO {
    
    @Schema(description = "CPF do usuário (11 dígitos)", example = "00000000000", required = true)
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    private String cpf;
    
    @Schema(description = "Senha do usuário", example = "Admin@123", required = true)
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}
