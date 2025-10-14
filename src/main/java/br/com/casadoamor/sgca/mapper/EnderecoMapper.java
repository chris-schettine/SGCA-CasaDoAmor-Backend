package br.com.casadoamor.sgca.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.dto.EnderecoInputDTO;
import br.com.casadoamor.sgca.entity.Endereco;

@Component
public class EnderecoMapper {
  public Endereco toEntity (EnderecoInputDTO enderecoInput) {
    return Endereco.builder()
      .cep(enderecoInput.cep())
      .logradouro(enderecoInput.logradouro())
      .numero(enderecoInput.numero())
      .complemento(enderecoInput.complemento())
      .bairro(enderecoInput.bairro())
      .cidade(enderecoInput.cidade())
      .estado(enderecoInput.estado())
      .build();
  }
}
