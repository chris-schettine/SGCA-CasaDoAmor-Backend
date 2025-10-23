package br.com.casadoamor.sgca.mapper.paciente;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.dto.paciente.EnderecoInputDTO;
import br.com.casadoamor.sgca.entity.paciente.Endereco;

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
}
