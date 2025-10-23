package br.com.casadoamor.sgca.dto.paciente;

import br.com.casadoamor.sgca.enums.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DadoClinicoInputDTO {
  
  private String diagnostico;

  private TipoTratamento tratamento;

  private String tratamentoOutroDescricao;

  private CondicaoChegada condicaoChegada;

  @NotNull(message = "Informação sobre uso de sonda é obrigatória")
  private Boolean usaSonda;

  private TipoSondaNasal tipoSondaNasal;

  private TipoSondaCirurgica tipoSondaCirurgica;

  private TipoSondaVesical tipoSondaVesical;

  private String sondaOutraDescricao;

  @NotNull(message = "Informação sobre uso de curativo é obrigatória")
  private Boolean usaCurativo;

  @NotNull(message = "Informação sobre uso de oxigenoterapia é obrigatória")
  private Boolean usaOxigenoterapia;
}
