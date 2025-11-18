package br.com.casadoamor.sgca.modules.paciente.entity;

import java.time.LocalDateTime;

import br.com.casadoamor.sgca.modules.common.entity.BaseEntity;
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
@Table(name = "obituarios")
public class Obituario extends BaseEntity {

  @Column(name = "data_hora_obito")
  private LocalDateTime dataHoraObito;

  @Column(name = "local_obito")
  private String localObito;

  @Column(name = "causa_obito")
  private String causaObito;

  @Column(name = "numero_declaracao_obito")
  private String numeroDeclaracaoObito; 

  @Column(name = "observacoes_obito", columnDefinition = "TEXT")
  private String observacoesObito;

  @Column(name = "responsavel_comunicacao_obito")
  private String responsavelComunicacaoObito;
}
