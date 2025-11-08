package br.com.casadoamor.sgca.modules.paciente.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.paciente.entity.PoliticaPrivacidade;

@Component
public class PoliticaPrivacidadeMapper {
  public PoliticaPrivacidade aceitouPolitica () {
    return PoliticaPrivacidade.builder()
      .aceitouPoliticaPrivacidade(true)
      .build();
  }
}
