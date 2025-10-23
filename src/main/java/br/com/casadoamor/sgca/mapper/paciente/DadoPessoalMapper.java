package br.com.casadoamor.sgca.mapper.paciente;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.dto.paciente.DadoPessoalInputDTO;
import br.com.casadoamor.sgca.entity.paciente.DadoPessoal;

@Component
public class DadoPessoalMapper {
  public DadoPessoal toEntity (DadoPessoalInputDTO dadoPessoalInputDTO) {
    return DadoPessoal.builder()
      .nome(dadoPessoalInputDTO.getNome())
      .nomeMae(dadoPessoalInputDTO.getNomeMae())
      .dataNascimento(dadoPessoalInputDTO.getDataNascimento())
      .cpf(dadoPessoalInputDTO.getCpf())
      .rg(dadoPessoalInputDTO.getRg())
      .naturalidade(dadoPessoalInputDTO.getNaturalidade())
      .profissao(dadoPessoalInputDTO.getProfissao())
      .telefone(dadoPessoalInputDTO.getTelefone())
      .build();
  }
}
