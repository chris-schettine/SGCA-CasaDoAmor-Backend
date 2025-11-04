package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.util.List;

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
  private DadoPessoalInputDTO dadoPessoal;

  @NotNull(message = "Dado clínico é obrigatório")
  private DadoClinicoInputDTO dadoClinico;

  @NotNull(message = "Endereço é obrigatório")
  private EnderecoInputDTO endereco;

  @Valid
  private List<ContatoEmergenciaInputDTO> contatosDeEmergencia;

  @NotNull(message = "Email é obrigatório")
  private String email;
}
