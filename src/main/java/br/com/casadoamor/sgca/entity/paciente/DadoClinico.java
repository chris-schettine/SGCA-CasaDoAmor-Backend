package br.com.casadoamor.sgca.entity.paciente;

import br.com.casadoamor.sgca.entity.common.BaseEntity;
import br.com.casadoamor.sgca.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dados_clinicos")
public class DadoClinico extends BaseEntity {
  
  @Column(columnDefinition = "TEXT")
  private String diagnostico;

  @Enumerated(EnumType.STRING)
  @Column(name = "tratamento")
  private TipoTratamento tratamento;

  @Column(name = "tratamento_outro_descricao", columnDefinition = "TEXT")
  private String tratamentoOutroDescricao;

  @Enumerated(EnumType.STRING)
  @Column(name = "condicao_chegada", length = 50)
  private CondicaoChegada condicaoChegada;

  // Sondas nasais/orais para alimentação
  @Column(nullable = false)
  private Boolean usaSonda;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_sonda_nasal", length = 50)
  private TipoSondaNasal tipoSondaNasal;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_sonda_cirurgica", length = 50)
  private TipoSondaCirurgica tipoSondaCirurgica;

  // Sondas vesicais
  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_sonda_vesical", length = 50)
  private TipoSondaVesical tipoSondaVesical;

  @Column(name = "sonda_outra_descricao", columnDefinition = "TEXT")
  private String sondaOutraDescricao;

  // Curativo
  @Column(nullable = false)
  private Boolean usaCurativo;

  // Oxigenoterapia
  @Column(name = "usa_oxigenoterapia", nullable = false)
  private Boolean usaOxigenoterapia = false;

  @ManyToOne
  @JoinColumn(name = "paciente_id", nullable = false)
  private Paciente paciente;
}
