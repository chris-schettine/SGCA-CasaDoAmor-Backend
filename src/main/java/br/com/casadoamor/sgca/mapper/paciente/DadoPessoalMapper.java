package br.com.casadoamor.sgca.mapper.paciente;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.dto.paciente.DadoPessoalInputDTO;
import br.com.casadoamor.sgca.entity.paciente.DadoPessoal;

@Component
public class DadoPessoalMapper {
  public DadoPessoal toEntity (DadoPessoalInputDTO dadoPessoalInputDTO) {
    return DadoPessoal.builder()
      .nome(dadoPessoalInputDTO.getNome())
      .dataNascimento(dadoPessoalInputDTO.getDataNascimento())
      .cpf(dadoPessoalInputDTO.getCpf())
      .rg(dadoPessoalInputDTO.getRg())
      .naturalidade(dadoPessoalInputDTO.getNaturalidade())
      .telefone(dadoPessoalInputDTO.getTelefone())
      .build();
  }
}
