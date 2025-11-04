package br.com.casadoamor.sgca.modules.acompanhante.dtos;

import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoInputDTO;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class EditarAcompanhanteDTO {
  private Boolean podeAjudarNaCozinha;

  @Valid
  private DadoPessoalInputDTO dadoPessoal;

  @Valid
  private EnderecoInputDTO endereco;

  private Parentesco parentesco;
  
  private Boolean ativo;
}
