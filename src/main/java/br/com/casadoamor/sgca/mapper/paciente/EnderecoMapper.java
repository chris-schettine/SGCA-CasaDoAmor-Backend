package br.com.casadoamor.sgca.mapper.paciente;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.dto.paciente.EnderecoInputDTO;
import br.com.casadoamor.sgca.entity.paciente.Endereco;

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
