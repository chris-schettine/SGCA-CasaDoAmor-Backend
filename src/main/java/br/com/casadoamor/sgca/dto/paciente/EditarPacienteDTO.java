package br.com.casadoamor.sgca.dto.paciente;

import jakarta.validation.Valid;
import lombok.Getter;

@Getter
public class EditarPacienteDTO {
	@Valid
	EditarDadoPessoalInputDTO dadoPessoal;

	@Valid
	EditarEnderecoInputDTO endereco;
}
