package br.com.casadoamor.sgca.dto.paciente;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DadoPessoalInputDTO {
  @NotBlank(message = "O nome é obrigatório")
  private String nome;

  @NotNull(message = "A data de nascimento é obrigatória")
  @Past(message = "A data de nascimento deve ser no passado")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date dataNascimento;

  @NotBlank(message = "O CPF é obrigatório")
  @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 números")
  private String cpf;

  @NotBlank(message = "O RG é obrigatório")
  private String rg;

  @NotBlank(message = "A naturalidade é obrigatória")
  private String naturalidade;

  @NotBlank(message = "O telefone é obrigatório")
  @Pattern(regexp = "\\+?\\d{10,15}", message = "Telefone inválido")
  private String telefone;
} 
