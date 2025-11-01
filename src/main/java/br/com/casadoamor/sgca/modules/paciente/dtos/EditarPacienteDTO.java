package br.com.casadoamor.sgca.modules.paciente.dtos;

import jakarta.validation.Valid;
import lombok.Getter;

@Getter
public class EditarPacienteDTO {
	@Valid
	EditarDadoPessoalInputDTO dadoPessoal;

	@Valid
	EditarEnderecoInputDTO endereco;
}
