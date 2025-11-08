package br.com.casadoamor.sgca.modules.paciente.entity;

import br.com.casadoamor.sgca.modules.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "politica_privacidade")
public class PoliticaPrivacidade extends BaseEntity {

  @Column(name = "aceitou_politica_privacidade", nullable = false)
  @Builder.Default
  private Boolean aceitouPoliticaPrivacidade = true;

  @Column(name = "caminho_documento_bucket", length = 255)
  private String caminhoDoDocumentoNoBucket;
}
