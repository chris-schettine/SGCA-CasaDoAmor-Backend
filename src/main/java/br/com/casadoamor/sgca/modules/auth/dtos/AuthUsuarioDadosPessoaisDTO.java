package br.com.casadoamor.sgca.modules.auth.dtos;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados pessoais de usuário do sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados pessoais do usuário")
public class AuthUsuarioDadosPessoaisDTO {

    @Schema(description = "ID dos dados pessoais")
    private Long id;

    @Schema(description = "Data de nascimento", example = "1990-05-15")
    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @Schema(description = "Sexo biológico", example = "MASCULINO", allowableValues = {"MASCULINO", "FEMININO"}, required = true)
    @NotNull(message = "Sexo é obrigatório")
    private String sexo;

    @Schema(description = "Identidade de gênero", example = "CISGENERO", allowableValues = {"BINARIO", "NAO_BINARIO", "TRANSGENERO", "CISGENERO", "PREFIRO_NAO_INFORMAR"}, required = true)
    @NotNull(message = "Gênero é obrigatório")
    private String genero;

    @Schema(description = "Cidade/estado de nascimento", example = "Salvador/BA")
    @Size(max = 100, message = "Naturalidade deve ter no máximo 100 caracteres")
    private String naturalidade;

    @Schema(description = "Estado civil", example = "CASADO", allowableValues = {"SOLTEIRO", "CASADO", "DIVORCIADO", "VIUVO", "SEPARADO", "UNIAO_ESTAVEL"})
    private String estadoCivil;

    @Schema(description = "Nome completo da mãe", example = "Maria Silva")
    @Size(max = 255, message = "Nome da mãe deve ter no máximo 255 caracteres")
    private String nomeMae;

    @Schema(description = "Nome completo do pai", example = "João Silva")
    @Size(max = 255, message = "Nome do pai deve ter no máximo 255 caracteres")
    private String nomePai;

    @Schema(description = "Profissão/ocupação", example = "Comerciante")
    @Size(max = 100, message = "Profissão deve ter no máximo 100 caracteres")
    private String profissao;
}
