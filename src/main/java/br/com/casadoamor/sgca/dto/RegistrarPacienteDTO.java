package br.com.casadoamor.sgca.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record RegistrarPacienteDTO(
  @NotNull(message = "Dado pessoal é obrigatório")
  @Valid
  DadoPessoalInputDTO dadoPessoal,

  @NotNull(message = "Endereço é obrigatório")
  @Valid
  EnderecoInputDTO endereco
) {}
