package br.com.casadoamor.sgca.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dados_clinicos")
public class DadoClinico extends BaseEntity {
  @Column()
  private String diagnostico;

  @Column()
  private String tratamento;

  @Column(nullable = false)
  private Boolean usaSonda;

  @Column
  private String tipoSonda;

  @Column(nullable = false)
  private Boolean usaCurativo;

  @ManyToOne
  @JoinColumn(name = "paciente_id", nullable = false)
  private Paciente paciente;
}
