package br.com.casadoamor.sgca.modules.common.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dados_sociais")
public class DadoSocial extends BaseEntity {

  @Column(name = "renda_familiar", precision = 10, scale = 2)
  private BigDecimal rendaFamiliar;

  @Column(name = "composicao_familiar", length = 255)
  private String composicaoFamiliar;

  @Column(name = "situacao_moradia", length = 100)
  private String situacaoMoradia;

  @Column(name = "necessidades_especiais")
  private String necessidadesEspeciais;
}
