package br.com.casadoamor.sgca.modules.paciente.entity;

import java.time.LocalDate;

import br.com.casadoamor.sgca.modules.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "informacoes_hospitalares")
public class InformacaoHospitalar extends BaseEntity {

  @Column(name = "nome_hospital_referencia", length = 255)
  private String nomeHospitalReferencia;

  @Column(name = "medico_responsavel", length = 150)
  private String medicoResponsavel;

  @Column(name = "setor_ala", length = 100)
  private String setorAla;

  @Column(name = "data_internacao")
  private LocalDate dataInternacao;
}
