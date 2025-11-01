package br.com.casadoamor.sgca.modules.paciente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.common.entity.BaseEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historico_paciente")
public class HistoricoPaciente extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "paciente_id", nullable = false)
  private Paciente paciente;

  @ManyToOne
  @JoinColumn(name = "acompanhante_id")
  private Acompanhante acompanhante;

  @Column(nullable = false)
  private String descricao;

  @Column(nullable = false)
  private LocalDateTime dataRegistro;
}
