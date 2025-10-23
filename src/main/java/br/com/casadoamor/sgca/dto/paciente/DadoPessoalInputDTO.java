package br.com.casadoamor.sgca.dto.paciente;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

public record DadoPessoalInputDTO(
  @NotBlank(message = "O nome é obrigatório")
  String nome,

  @NotNull(message = "A data de nascimento é obrigatória")
  @Past(message = "A data de nascimento deve ser no passado")
  @JsonFormat(pattern = "yyyy-MM-dd")
  Date dataNascimento,

  @NotBlank(message = "O CPF é obrigatório")
  @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 números")
  String cpf,

  @NotBlank(message = "O RG é obrigatório")
  String rg,

  @NotBlank(message = "A naturalidade é obrigatória")
  String naturalidade,

  @NotBlank(message = "O telefone é obrigatório")
  @Pattern(regexp = "\\+?\\d{10,15}", message = "Telefone inválido")
  String telefone
) {
} 
