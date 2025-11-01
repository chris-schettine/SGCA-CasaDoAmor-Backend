package br.com.casadoamor.sgca.modules.acompanhante.entity;

import br.com.casadoamor.sgca.modules.common.entity.BaseEntity;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
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
@Table(name = "acompanhantes")
public class Acompanhante extends BaseEntity {
  @Column(nullable = false)
  private Boolean podeAjudarNaCozinha;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "dado_pessoal_id")
  private DadoPessoal dadoPessoal;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "endereco_id")
  private Endereco endereco;
  
  @Column(nullable = false)
  @Builder.Default
  private boolean ativo = true;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Parentesco parentesco;

  @ManyToOne
  @JoinColumn(name = "paciente_id", nullable = false)
  private Paciente paciente;
}
