package br.com.casadoamor.sgca.dto.admin.user;

import br.com.casadoamor.sgca.dto.auth.AuthUsuarioDadosPessoaisDTO;
import br.com.casadoamor.sgca.dto.auth.AuthUsuarioEnderecoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de usuário por admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para atualização de usuário pelo administrador")
public class UpdateUserDTO {

    @Schema(description = "Nome completo do usuário", example = "Maria Santos Silva")
    private String nome;
    
    @Schema(description = "Telefone do usuário", example = "(77) 98888-7777")
    private String telefone;
    
    @Schema(description = "Email do usuário", example = "maria.silva@casadoamor.com")
    private String email;
    
    @Schema(description = "Status de ativação da conta", example = "true")
    private Boolean ativo;
    
    @Schema(description = "Tipo de usuário", example = "MEDICO", allowableValues = {"ADMINISTRADOR", "MEDICO", "ENFERMEIRO", "PSICOLOGO", "RECEPCIONISTA"})
    private String tipo; // ADMINISTRADOR, MEDICO, etc.

    @Schema(description = "Dados pessoais do usuário (sexo, gênero, etc.)")
    @Valid
    private AuthUsuarioDadosPessoaisDTO dadosPessoais;

    @Schema(description = "Endereço do usuário")
    @Valid
    private AuthUsuarioEnderecoDTO endereco;
}
