package br.com.casadoamor.sgca.modules.acompanhante.dtos;

import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarAcompanhanteDTO {
  @NotNull(message = "Dado pessoal é obrigatório")
  @Valid
  private DadoPessoalDTO dadoPessoal;

  @NotNull(message = "Endereço é obrigatório")
  @Valid
  private EnderecoDTO endereco;

  @NotNull(message = "Parentesco é obrigatório")
  private Parentesco parentesco;

  @NotNull(message = "ID do paciente é obrigatório")
  private String pacienteId;

  @NotNull(message = "Indicação de ajuda na cozinha é obrigatória")
  private Boolean podeAjudarNaCozinha;
}
