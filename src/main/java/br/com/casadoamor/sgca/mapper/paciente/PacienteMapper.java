package br.com.casadoamor.sgca.mapper.paciente;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.dto.paciente.PacienteDTO;
import br.com.casadoamor.sgca.entity.paciente.DadoPessoal;
import br.com.casadoamor.sgca.entity.paciente.Endereco;
import br.com.casadoamor.sgca.entity.paciente.Paciente;

@Component
public class PacienteMapper {
  public Paciente toEntityFromEntities (DadoPessoal dadoPessoal, Endereco endereco) {
    return Paciente.builder()
      .dadoPessoal(dadoPessoal)
      .endereco(endereco)
      .build();
  }

  public PacienteDTO toDTO (Paciente paciente) {
    DadoPessoal dadoPessoal = paciente.getDadoPessoal();
    Endereco endereco = paciente.getEndereco();

    return PacienteDTO.builder()
      .id(paciente.getId())
      .nome(dadoPessoal.getNome())
      .dataNascimento(dadoPessoal.getDataNascimento())
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
