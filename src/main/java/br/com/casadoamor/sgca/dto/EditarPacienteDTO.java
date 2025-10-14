package br.com.casadoamor.sgca.dto;

import jakarta.validation.Valid;
import lombok.Getter;

@Getter
public class EditarPacienteDTO {
  @Valid
  EditarDadoPessoalInputDTO dadoPessoal;

  @Valid
  EditarEnderecoInputDTO endereco;
}
