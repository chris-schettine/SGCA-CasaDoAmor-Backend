package br.com.casadoamor.sgca.modules.funcionario.dto;

import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para cadastro/atualização de profissionais (funcionários e voluntários)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    private String cpf;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{4,5}-\\d{4}", message = "Telefone deve estar no formato (00) 00000-0000")
    private String telefone;

    @Email(message = "Email deve ser válido")
    @Size(max = 255)
    private String email;

    private br.com.casadoamor.sgca.modules.funcionario.entity.enums.TipoVinculo tipoVinculo; // Padrão: FUNCIONARIO

    @NotNull(message = "Categoria é obrigatória")
    private CategoriaProfissional categoria;

    @Size(max = 255)
    private String areaAtuacao;

    @Size(max = 255)
    private String especialidade;

    @Size(max = 50)
    private String numeroRegistro;

    @Size(max = 2)
    @Pattern(regexp = "[A-Z]{2}", message = "UF deve conter 2 letras maiúsculas")
    private String ufRegistro;

    @NotNull(message = "Data de admissão é obrigatória")
    @PastOrPresent(message = "Data de admissão não pode ser futura")
    private LocalDate dataAdmissao;

    @PastOrPresent(message = "Data de desligamento não pode ser futura")
    private LocalDate dataDesligamento;

    @Size(max = 100)
    private String tipoContrato;

    @Min(value = 0, message = "Carga horária deve ser positiva")
    @Max(value = 44, message = "Carga horária não pode exceder 44 horas")
    private Integer cargaHoraria;

    @Size(max = 255)
    private String cargo;

    @Size(max = 255)
    private String departamento;

    private String dadosBancarios; // Será criptografado no service

    private String disponibilidade; // JSON string

    private String enderecoId; // UUID do endereço

    private Boolean ativo;

    private String observacoes;
}
