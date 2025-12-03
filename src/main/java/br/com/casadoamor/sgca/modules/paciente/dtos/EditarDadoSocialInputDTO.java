package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

@Getter
@lombok.Setter
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class EditarDadoSocialInputDTO {
  @PositiveOrZero
  private BigDecimal rendaFamiliar;

  private String composicaoFamiliar;

  private String situacaoMoradia;

  private String necessidadesEspeciais;
}
