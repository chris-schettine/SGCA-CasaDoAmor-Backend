package br.com.casadoamor.sgca.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de registro de novo usuário
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para registro de novo usuário")
public class RegisterRequestDTO {
    
    @Schema(description = "Nome completo do usuário", example = "João Silva", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String nome;
    
    @Schema(description = "Email do usuário", example = "joao.silva@example.com", required = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
    
    @Schema(description = "Senha do usuário (mínimo 6 caracteres)", example = "SenhaForte@123", required = true)
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;
    
    @Schema(description = "CPF do usuário (11 dígitos)", example = "12345678901", required = true)
    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos")
    private String cpf;
    
    @Schema(description = "Telefone do usuário", example = "(77) 99999-8888")
    private String telefone;
    
    @Schema(description = "Tipo de usuário", example = "MEDICO", allowableValues = {"ADMINISTRADOR", "MEDICO", "ENFERMEIRO", "PSICOLOGO", "RECEPCIONISTA"})
    // Tipo de usuário (opcional, padrão será RECEPCIONISTA)
    private String tipo;
}
