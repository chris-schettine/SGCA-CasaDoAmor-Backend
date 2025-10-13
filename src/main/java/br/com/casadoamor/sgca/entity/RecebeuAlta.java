package br.com.casadoamor.sgca.entity;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
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
@Table(name = "recebeu_alta")
public class RecebeuAlta extends BaseEntity {

  @Column(nullable = false)
  @Builder.Default
  private boolean recebeuAlta = true;

  @JsonIgnore
  private LocalDateTime data;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "paciente_id")
  private Paciente Paciente;
}
