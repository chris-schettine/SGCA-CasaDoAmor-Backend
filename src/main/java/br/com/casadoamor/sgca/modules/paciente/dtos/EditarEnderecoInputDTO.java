package br.com.casadoamor.sgca.modules.paciente.dtos;

import br.com.casadoamor.sgca.modules.common.enums.EstadoEnum;
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
