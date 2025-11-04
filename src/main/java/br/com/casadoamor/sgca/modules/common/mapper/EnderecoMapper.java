package br.com.casadoamor.sgca.modules.common.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoInputDTO;

@Component
public class EnderecoMapper {
  public Endereco toEntity (EnderecoInputDTO enderecoInput) {
    return Endereco.builder()
      .cep(enderecoInput.getCep())
      .logradouro(enderecoInput.getLogradouro())
      .numero(enderecoInput.getNumero())
      .complemento(enderecoInput.getComplemento())
      .bairro(enderecoInput.getBairro())
      .cidade(enderecoInput.getCidade())
      .estado(enderecoInput.getEstado())
      .build();
  }

  public EnderecoDTO mapToDTO(Endereco endereco) {
    return EnderecoDTO.builder()
      .id(endereco.getId())
      .cep(endereco.getCep())
      .logradouro(endereco.getLogradouro())
      .numero(endereco.getNumero())
      .complemento(endereco.getComplemento())
      .bairro(endereco.getBairro())
      .cidade(endereco.getCidade())
      .estado(endereco.getEstado())
      .build();
  }
}
