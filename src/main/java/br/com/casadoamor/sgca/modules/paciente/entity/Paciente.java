package br.com.casadoamor.sgca.modules.paciente.entity;

import java.util.List;

import br.com.casadoamor.sgca.modules.common.entity.BaseEntity;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
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

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "dado_pessoal_id")
  private DadoPessoal dadoPessoal;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "endereco_id")
  private Endereco endereco;

  @Column(unique = true, nullable = false)
  private String email;

  private String caminhoDaImagemNoBucket; // nunca deve ser armazenado o arquivo em si, apenas o caminho no bucket

  @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ContatoEmergencia> contatosEmergencia;
}
