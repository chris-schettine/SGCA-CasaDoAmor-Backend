package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformacaoHospitalarDTO {

  @NotBlank()
  String nomeHospitalReferencia;

  @NotBlank()
  String medicoResponsavel;

  @NotBlank()
  String setorAla;

  @NotNull()
  LocalDate dataInternacao;
}
