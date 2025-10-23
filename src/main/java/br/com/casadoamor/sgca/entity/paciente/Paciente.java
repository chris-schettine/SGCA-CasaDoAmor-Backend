package br.com.casadoamor.sgca.entity.paciente;

import br.com.casadoamor.sgca.entity.common.BaseEntity;
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
@Table(name = "pacientes")
public class Paciente extends BaseEntity {

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "dado_pessoal_id")
  private DadoPessoal dadoPessoal;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "endereco_id")
  private Endereco endereco;
}
