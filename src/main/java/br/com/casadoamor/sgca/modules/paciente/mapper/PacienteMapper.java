package br.com.casadoamor.sgca.modules.paciente.mapper;


import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@Component
public class PacienteMapper {
  public Paciente toEntityFromEntities (DadoPessoal dadoPessoal, Endereco endereco, String email) {
    return Paciente.builder()
      .dadoPessoal(dadoPessoal)
      .endereco(endereco)
      .email(email)
      .build();
  }

  public PacienteDTO toDTO (Paciente paciente) {
    DadoPessoal dadoPessoal = paciente.getDadoPessoal();
    Endereco endereco = paciente.getEndereco();

    return PacienteDTO.builder()
      .id(paciente.getId())
      .nome(dadoPessoal.getNome())
      .nomeMae(dadoPessoal.getNomeMae())
      .cpf(dadoPessoal.getCpf())
      .rg(dadoPessoal.getRg())
      .dataNascimento(dadoPessoal.getDataNascimento())
      .profissao(dadoPessoal.getProfissao())
      .naturalidade(dadoPessoal.getNaturalidade())
      .telefone(dadoPessoal.getTelefone())
      .logradouro(endereco.getLogradouro())
      .numero(endereco.getNumero())
      .complemento(endereco.getComplemento())
      .bairro(endereco.getBairro())
      .cidade(endereco.getCidade())
      .estado(endereco.getEstado().name())
      .cep(endereco.getCep())
      .build();
  }
}
