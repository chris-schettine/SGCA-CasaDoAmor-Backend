package br.com.casadoamor.sgca.modules.common.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarEnderecoInputDTO;
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

  public Endereco updateEntity (Endereco endereco, EditarEnderecoInputDTO dto) {
    if (dto.getCep() != null) {
      endereco.setCep(dto.getCep());
    }
    if (dto.getLogradouro() != null) {
      endereco.setLogradouro(dto.getLogradouro());
    }
    if (dto.getNumero() != null) {
      endereco.setNumero(dto.getNumero());
    }
    if (dto.getComplemento() != null) {
      endereco.setComplemento(dto.getComplemento());
    }
    if (dto.getBairro() != null) {
      endereco.setBairro(dto.getBairro());
    }
    if (dto.getCidade() != null) {
      endereco.setCidade(dto.getCidade());
    }
    if (dto.getEstado() != null) {
      endereco.setEstado(dto.getEstado());
    }

    return endereco;
  }
}
