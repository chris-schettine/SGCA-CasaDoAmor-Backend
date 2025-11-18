package br.com.casadoamor.sgca.modules.paciente.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.casadoamor.sgca.modules.common.entity.BaseEntity;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.DadoSocial;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.enums.PacienteStatus;
import br.com.casadoamor.sgca.modules.dadoClinico.entity.DadoClinico;
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

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "dado_social_id")
  private DadoSocial dadoSocial;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "politica_privacidade_id")
  private PoliticaPrivacidade politicaPrivacidade;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(name = "caminho_da_imagem_no_bucket")
  private String caminhoDaImagemNoBucket;

  @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ContatoEmergencia> contatosEmergencia = new ArrayList<>();

  @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<DadoClinico> dadosClinicos = new ArrayList<>();

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "informacao_hospitalar_id")
  private InformacaoHospitalar informacaoHospitalar;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "obituario_id")
  private Obituario obituario;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private String deletedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private PacienteStatus status = PacienteStatus.ATIVO;

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void markAsDeleted(String deletedBy) {
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
  }
}
