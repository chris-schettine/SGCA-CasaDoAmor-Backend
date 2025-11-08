package br.com.casadoamor.sgca.modules.paciente.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.paciente.dtos.InformacaoHospitalarDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.InformacaoHospitalarInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.InformacaoHospitalar;

@Component
public class InformacaoHospitalarMapper {
  public InformacaoHospitalarDTO toDTO (InformacaoHospitalar informacaoHospitalar) {
    return InformacaoHospitalarDTO.builder()
      .nomeHospitalReferencia(informacaoHospitalar.getNomeHospitalReferencia())
      .medicoResponsavel(informacaoHospitalar.getMedicoResponsavel())
      .setorAla(informacaoHospitalar.getSetorAla())
      .dataInternacao(informacaoHospitalar.getDataInternacao())
      .build();
  }

  public InformacaoHospitalar toEntity (InformacaoHospitalarInputDTO dto) {
    return InformacaoHospitalar.builder()
      .nomeHospitalReferencia(dto.getNomeHospitalReferencia())
      .medicoResponsavel(dto.getMedicoResponsavel())
      .setorAla(dto.getSetorAla())
      .dataInternacao(dto.getDataInternacao())
      .build();
  }
}
