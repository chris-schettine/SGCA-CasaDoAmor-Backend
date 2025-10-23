package br.com.casadoamor.sgca.dto.auth;

import java.time.LocalDate;

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
public class AuthUsuarioDadosPessoaisDTO {

    private Long id;

    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @NotNull(message = "Sexo é obrigatório")
    private String sexo; // MASCULINO, FEMININO

    @NotNull(message = "Gênero é obrigatório")
    private String genero; // BINARIO, NAO_BINARIO, TRANSGENERO, CISGENERO, PREFIRO_NAO_INFORMAR

    @Size(max = 20, message = "RG deve ter no máximo 20 caracteres")
    private String rg;

    @Size(max = 10, message = "Órgão emissor deve ter no máximo 10 caracteres")
    private String orgaoEmissor;

    @Size(max = 100, message = "Naturalidade deve ter no máximo 100 caracteres")
    private String naturalidade;

    private String estadoCivil; // SOLTEIRO, CASADO, DIVORCIADO, VIUVO, UNIAO_ESTAVEL

    @Size(max = 255, message = "Nome da mãe deve ter no máximo 255 caracteres")
    private String nomeMae;

    @Size(max = 255, message = "Nome do pai deve ter no máximo 255 caracteres")
    private String nomePai;

    @Size(max = 100, message = "Profissão deve ter no máximo 100 caracteres")
    private String profissao;
}
