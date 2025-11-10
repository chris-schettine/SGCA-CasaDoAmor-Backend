package br.com.casadoamor.sgca.modules.common.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.common.dto.DadoSocialDTO;
import br.com.casadoamor.sgca.modules.common.dto.DadoSocialInputDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoSocial;

@Component
public class DadoSocialMapper {
  public DadoSocial toEntity (DadoSocialInputDTO dadoSocialInputDTO) {
    return DadoSocial.builder()
      .rendaFamiliar(dadoSocialInputDTO.getRendaFamiliar())
      .composicaoFamiliar(dadoSocialInputDTO.getComposicaoFamiliar())
      .situacaoMoradia(dadoSocialInputDTO.getSituacaoMoradia())
      .necessidadesEspeciais(dadoSocialInputDTO.getNecessidadesEspeciais())
      .build();
  }

  public DadoSocialDTO toDTO (DadoSocial dadoSocial) {
    return DadoSocialDTO.builder()
      .rendaFamiliar(dadoSocial.getRendaFamiliar())
      .composicaoFamiliar(dadoSocial.getComposicaoFamiliar())
      .situacaoMoradia(dadoSocial.getSituacaoMoradia())
      .necessidadesEspeciais(dadoSocial.getNecessidadesEspeciais())
      .build();
  }
}
