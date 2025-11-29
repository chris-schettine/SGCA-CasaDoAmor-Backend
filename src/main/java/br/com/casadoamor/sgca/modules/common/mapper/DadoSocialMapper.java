package br.com.casadoamor.sgca.modules.common.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.common.dto.DadoSocialDTO;
import br.com.casadoamor.sgca.modules.common.dto.DadoSocialInputDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoSocial;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoSocialInputDTO;

@Component
public class DadoSocialMapper {
  public DadoSocial toEntity (DadoSocialInputDTO dadoSocialInputDTO) {
    if (dadoSocialInputDTO == null) {
      return null;
    }

    return DadoSocial.builder()
      .rendaFamiliar(dadoSocialInputDTO.getRendaFamiliar())
      .composicaoFamiliar(dadoSocialInputDTO.getComposicaoFamiliar())
      .situacaoMoradia(dadoSocialInputDTO.getSituacaoMoradia())
      .necessidadesEspeciais(dadoSocialInputDTO.getNecessidadesEspeciais())
      .build();
  }

  public DadoSocialDTO toDTO (DadoSocial dadoSocial) {
    if (dadoSocial == null) {
      return null;
    }
    
    return DadoSocialDTO.builder()
      .rendaFamiliar(dadoSocial.getRendaFamiliar())
      .composicaoFamiliar(dadoSocial.getComposicaoFamiliar())
      .situacaoMoradia(dadoSocial.getSituacaoMoradia())
      .necessidadesEspeciais(dadoSocial.getNecessidadesEspeciais())
      .build();
  }

  public DadoSocial toEntityFromEditarDadoPessoalInputDTO (DadoSocial dadoSocial, EditarDadoSocialInputDTO dto) {
    if (dadoSocial == null) {
      dadoSocial = new DadoSocial();
    }

    if (dto.getRendaFamiliar() != null) {
      dadoSocial.setRendaFamiliar(dto.getRendaFamiliar());
    }
    if (dto.getComposicaoFamiliar() != null) {
      dadoSocial.setComposicaoFamiliar(dto.getComposicaoFamiliar());
    }
    if (dto.getSituacaoMoradia() != null) {
      dadoSocial.setSituacaoMoradia(dto.getSituacaoMoradia());
    }
    if (dto.getNecessidadesEspeciais() != null) {
      dadoSocial.setNecessidadesEspeciais(dto.getNecessidadesEspeciais());
    }
    return dadoSocial;
  }
}
