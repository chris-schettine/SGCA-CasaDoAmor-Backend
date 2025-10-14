package br.com.casadoamor.sgca.entity;

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
}
