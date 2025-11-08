package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class EditarInformacaoHospitalarInputDTO {
  
  private String nomeHospitalReferencia;

  private String medicoResponsavel;

  private String setorAla;

  private LocalDate dataInternacao;
}
