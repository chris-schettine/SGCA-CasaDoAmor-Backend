package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.time.LocalDateTime;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoricoPacienteDTO {
  private String id;
  private String descricao;
  private LocalDateTime dataRegistro;
  private AcompanhanteDTO acompanhante; 
}
