package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.util.List;

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
  private EnderecoDTO endereco;

  private List<ContatoEmergenciaDTO> contatosDeEmergencia;

  @NotNull(message = "Email é obrigatório")
  private String email;
}
