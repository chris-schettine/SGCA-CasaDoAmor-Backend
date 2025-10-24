package br.com.casadoamor.sgca.dto.admin.user;

import java.util.List;

import br.com.casadoamor.sgca.dto.auth.AuthUsuarioDadosPessoaisDTO;
import br.com.casadoamor.sgca.dto.auth.AuthUsuarioEnderecoDTO;
import br.com.casadoamor.sgca.dto.auth.RegistroProfissionalDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Dados para criação de usuário pelo administrador")
public class CreateUserDTO {

    @Schema(description = "Nome completo do usuário", example = "Maria Santos", required = true)
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Schema(description = "Email do usuário", example = "maria.santos@casadoamor.com", required = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @Schema(description = "CPF do usuário (11 dígitos)", example = "98765432100", required = true)
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    private String cpf;

    @Schema(description = "Telefone do usuário", example = "(77) 98888-7777")
    private String telefone;

    @Schema(description = "Tipo de usuário", example = "ENFERMEIRO", required = true, allowableValues = {"ADMINISTRADOR", "MEDICO", "ENFERMEIRO", "PSICOLOGO", "RECEPCIONISTA"})
    @NotBlank(message = "Tipo é obrigatório")
    private String tipo; // ADMINISTRADOR, MEDICO, etc.

    @Schema(description = "IDs dos perfis (roles) a serem atribuídos", example = "[2, 3]")
    private List<Long> perfisIds; // IDs dos perfis a serem atribuídos

    @Schema(description = "Dados pessoais do usuário (sexo, gênero, etc.)")
    @Valid
    private AuthUsuarioDadosPessoaisDTO dadosPessoais;

    @Schema(description = "Endereço do usuário")
    @Valid
    private AuthUsuarioEnderecoDTO endereco;

    @Schema(description = "Registro profissional (CRM, COREN, CRO, CREFITO, CRN) - obrigatório para profissionais de saúde")
    @Valid
    private RegistroProfissionalDTO registroProfissional;
}
