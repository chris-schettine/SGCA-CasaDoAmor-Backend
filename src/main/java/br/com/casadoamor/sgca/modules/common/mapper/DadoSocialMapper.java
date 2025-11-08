package br.com.casadoamor.sgca.modules.common.mapper;

import org.springframework.stereotype.Component;

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
}
