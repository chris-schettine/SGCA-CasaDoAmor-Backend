package br.com.casadoamor.sgca.modules.common.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

@Getter
public class DadoSocialInputDTO {
  @PositiveOrZero
  private BigDecimal rendaFamiliar;

  private String composicaoFamiliar;

  private String situacaoMoradia;

  private String necessidadesEspeciais;
}
