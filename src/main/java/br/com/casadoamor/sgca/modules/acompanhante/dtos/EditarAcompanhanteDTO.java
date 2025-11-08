package br.com.casadoamor.sgca.modules.acompanhante.dtos;

import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarEnderecoInputDTO;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class EditarAcompanhanteDTO {
  private Boolean podeAjudarNaCozinha;

  @Valid
  private EditarDadoPessoalInputDTO dadoPessoal;

  @Valid
  private EditarEnderecoInputDTO endereco;

  private Parentesco parentesco;
  
  private Boolean ativo;
}
