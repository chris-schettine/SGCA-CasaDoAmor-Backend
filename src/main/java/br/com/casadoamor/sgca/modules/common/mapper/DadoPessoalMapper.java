package br.com.casadoamor.sgca.modules.common.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.infra.util.CpfUtil;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalInputDTO;

@Component
public class DadoPessoalMapper {
  public DadoPessoal toEntity (DadoPessoalInputDTO dadoPessoalInputDTO) {
    String cpfLimpo = CpfUtil.limparCpf(dadoPessoalInputDTO.getCpf());

    return DadoPessoal.builder()
      .nome(dadoPessoalInputDTO.getNome())
      .nomeMae(dadoPessoalInputDTO.getNomeMae())
      .dataNascimento(dadoPessoalInputDTO.getDataNascimento())
      .cpf(cpfLimpo)
      .rg(dadoPessoalInputDTO.getRg())
      .naturalidade(dadoPessoalInputDTO.getNaturalidade())
      .profissao(dadoPessoalInputDTO.getProfissao())
      .telefone(dadoPessoalInputDTO.getTelefone())
      .build();
  }

  public DadoPessoalDTO mapToDTO(DadoPessoal dadoPessoal) {
    return DadoPessoalDTO.builder()
      .id(dadoPessoal.getId())
      .nome(dadoPessoal.getNome())
      .nomeMae(dadoPessoal.getNomeMae())
      .dataNascimento(dadoPessoal.getDataNascimento())
      .cpf(dadoPessoal.getCpf())
      .rg(dadoPessoal.getRg())
      .naturalidade(dadoPessoal.getNaturalidade())
      .profissao(dadoPessoal.getProfissao())
      .telefone(dadoPessoal.getTelefone())
      .build();
  }
}
