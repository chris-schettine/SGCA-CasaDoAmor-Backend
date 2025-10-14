package br.com.casadoamor.sgca.dto;

import br.com.casadoamor.sgca.enums.EstadoEnum;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class EditarEnderecoInputDTO {
  String logradouro;

  @Positive(message = "O número deve ser positivo")
  Integer numero;

  String complemento;

  String bairro;

  String cidade;

  EstadoEnum estado;

  @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
  String cep;
}
