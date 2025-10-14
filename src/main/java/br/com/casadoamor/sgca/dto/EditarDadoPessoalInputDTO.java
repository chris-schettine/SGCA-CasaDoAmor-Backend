package br.com.casadoamor.sgca.dto;

import java.util.Date;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class EditarDadoPessoalInputDTO {
  String nome;

  @Past(message = "A data de nascimento deve ser no passado")
  Date dataNascimento;

  @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 números")
  String cpf;

  String rg;

  String naturalidade;

  @Pattern(regexp = "\\+?\\d{10,15}", message = "Telefone inválido")
  String telefone;
}
