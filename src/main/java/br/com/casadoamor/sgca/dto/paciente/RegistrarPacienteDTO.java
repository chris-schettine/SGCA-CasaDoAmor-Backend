package br.com.casadoamor.sgca.dto.paciente;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RegistrarPacienteDTO(
  @NotNull(message = "Dado pessoal é obrigatório")
  @Valid
  DadoPessoalInputDTO dadoPessoal,

  @NotNull(message = "Endereço é obrigatório")
  @Valid
  EnderecoInputDTO endereco
) {}
