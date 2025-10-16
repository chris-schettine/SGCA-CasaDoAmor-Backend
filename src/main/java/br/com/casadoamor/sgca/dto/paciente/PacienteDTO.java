package br.com.casadoamor.sgca.dto.paciente;

import java.util.Date;
import java.util.UUID;

import lombok.Builder;

@Builder
public record PacienteDTO(
  UUID id,
  String nome,
  Date dataNascimento,
  String naturalidade,
  String telefone,
  String logradouro,
  Integer numero,
  String complemento,
  String bairro,
  String cidade,
  String estado,
  String cep
) {
} 
