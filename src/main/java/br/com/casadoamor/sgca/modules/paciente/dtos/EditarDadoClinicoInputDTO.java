package br.com.casadoamor.sgca.modules.paciente.dtos;

import br.com.casadoamor.sgca.modules.common.enums.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarDadoClinicoInputDTO {
  
  private String diagnostico;

  private TipoTratamento tratamento;

  private String tratamentoOutroDescricao;

  private CondicaoChegada condicaoChegada;

  private Boolean usaSonda;

  private TipoSondaNasal tipoSondaNasal;

  private TipoSondaCirurgica tipoSondaCirurgica;

  private TipoSondaVesical tipoSondaVesical;

  private String sondaOutraDescricao;

  private Boolean usaCurativo;

  private Boolean usaOxigenoterapia;

  private TipoSanguineoEnum tipoSanguineo;
}
