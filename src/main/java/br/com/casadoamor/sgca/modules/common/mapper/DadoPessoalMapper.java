package br.com.casadoamor.sgca.modules.common.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.infra.util.CpfUtil;
import br.com.casadoamor.sgca.infra.util.RgUtil;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO;

@Component
public class DadoPessoalMapper {
  public DadoPessoal toEntity (DadoPessoalInputDTO dadoPessoalInputDTO) {
    String cpfLimpo = CpfUtil.limparCpf(dadoPessoalInputDTO.getCpf());
    String rgLimpo = RgUtil.limparRg(dadoPessoalInputDTO.getRg());
    String telefoneLimpo = CpfUtil.limparTelefone(dadoPessoalInputDTO.getTelefone());

    return DadoPessoal.builder()
      .nome(dadoPessoalInputDTO.getNome())
      .nomeMae(dadoPessoalInputDTO.getNomeMae())
      .dataNascimento(dadoPessoalInputDTO.getDataNascimento())
      .cpf(cpfLimpo)
      .rg(rgLimpo)
      .naturalidade(dadoPessoalInputDTO.getNaturalidade())
      .profissao(dadoPessoalInputDTO.getProfissao())
      .telefone(telefoneLimpo)
      .estadoCivil(dadoPessoalInputDTO.getEstadoCivil())
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
      .estadoCivil(dadoPessoal.getEstadoCivil())
      .build();
  }

  public DadoPessoal updateEntity (DadoPessoal dadoPessoal, EditarDadoPessoalInputDTO dto) {
    String cpfLimpo = CpfUtil.limparCpf(dto.getCpf());
    String rgLimpo = RgUtil.limparRg(dto.getRg());
    String telefoneLimpo = CpfUtil.limparTelefone(dto.getTelefone());

    if (dto.getCpf() != null) {
      dadoPessoal.setCpf(cpfLimpo);
    }
    if (dto.getRg() != null) {
      dadoPessoal.setRg(rgLimpo);
    }
    if (dto.getTelefone() != null) {
      dadoPessoal.setTelefone(telefoneLimpo);
    }
    if (dto.getNome() != null) {
      dadoPessoal.setNome(dto.getNome());
    }
    if (dto.getNomeMae() != null) {
      dadoPessoal.setNomeMae(dto.getNomeMae());
    }
    if (dto.getDataNascimento() != null) {
      dadoPessoal.setDataNascimento(dto.getDataNascimento());
    }
    if (dto.getNaturalidade() != null) {
      dadoPessoal.setNaturalidade(dto.getNaturalidade());
    }
    if (dto.getProfissao() != null) {
      dadoPessoal.setProfissao(dto.getProfissao());
    }
    if (dto.getEstadoCivil() != null) {
      dadoPessoal.setEstadoCivil(dto.getEstadoCivil());
    }
    
    return dadoPessoal;
  }
}
