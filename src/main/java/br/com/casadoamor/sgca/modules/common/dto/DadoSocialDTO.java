package br.com.casadoamor.sgca.modules.common.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DadoSocialDTO {
  @NotNull
  @PositiveOrZero
  private BigDecimal rendaFamiliar;

  @NotBlank()
  private String composicaoFamiliar;

  @NotBlank()
  private String situacaoMoradia;

  @NotBlank()
  private String necessidadesEspeciais;
}
