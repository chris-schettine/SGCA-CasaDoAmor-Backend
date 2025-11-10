package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoricoAcompanhanteDTO {
  private String id;
  private String descricao;
  private LocalDateTime dataRegistro;
  private PacienteDTO paciente; 
}
