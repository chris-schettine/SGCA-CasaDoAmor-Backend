package br.com.casadoamor.sgca.modules.paciente.entity;

import br.com.casadoamor.sgca.modules.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contatos_emergencia")
public class ContatoEmergencia extends BaseEntity {

  @Column(nullable = false)
  private String nome;

  @Column(nullable = false)
  private String telefone;

  @Column(nullable = false)
  private String email;

  @ManyToOne
  @JoinColumn(name = "paciente_id")
  private Paciente paciente;
}
