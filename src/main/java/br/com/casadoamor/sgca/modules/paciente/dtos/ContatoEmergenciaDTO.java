package br.com.casadoamor.sgca.modules.paciente.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContatoEmergenciaDTO {
  private String id;

  private String nome;

  private String email;

  private String telefone;
} 
