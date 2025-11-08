package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.time.LocalDate;

import br.com.casadoamor.sgca.modules.common.enums.EstadoCivilEnum;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class EditarDadoPessoalInputDTO {
    String nome;

    String nomeMae;

    @Past(message = "A data de nascimento deve ser no passado")
    LocalDate dataNascimento;

    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 números")
    String cpf;

    String rg;

    String naturalidade;

    String profissao;

    @Pattern(regexp = "\\+?\\d{10,15}", message = "Telefone inválido")
    String telefone;

    EstadoCivilEnum estadoCivil;
}
