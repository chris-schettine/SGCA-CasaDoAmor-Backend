package br.com.casadoamor.sgca.mapper.paciente;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.dto.paciente.DadoPessoalInputDTO;
import br.com.casadoamor.sgca.entity.paciente.DadoPessoal;

@Component
public class DadoPessoalMapper {
  public DadoPessoal toEntity (DadoPessoalInputDTO dadoPessoalInputDTO) {
    return DadoPessoal.builder()
      .nome(dadoPessoalInputDTO.nome())
      .dataNascimento(dadoPessoalInputDTO.dataNascimento())
      .cpf(dadoPessoalInputDTO.cpf())
      .rg(dadoPessoalInputDTO.rg())
      .naturalidade(dadoPessoalInputDTO.naturalidade())
      .telefone(dadoPessoalInputDTO.telefone())
      .build();
  }
}
