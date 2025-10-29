package br.com.casadoamor.sgca.dto.paciente;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarPacienteDTO {
    @NotNull(message = "Dado pessoal é obrigatório")
    @Valid
    private DadoPessoalInputDTO dadoPessoal;

    @NotNull(message = "Endereço é obrigatório")
    @Valid
    private EnderecoInputDTO endereco;
}
