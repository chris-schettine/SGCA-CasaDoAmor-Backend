package br.com.casadoamor.sgca.dto.admin.user;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de usuário por admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    private String cpf;

    private String telefone;

    @NotBlank(message = "Tipo é obrigatório")
    private String tipo; // ADMINISTRADOR, MEDICO, etc.

    private List<Long> perfisIds; // IDs dos perfis a serem atribuídos
}
