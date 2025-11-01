package br.com.casadoamor.sgca.modules.acompanhante.dtos;

import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import lombok.Data;

@Data
public class EditarAcompanhanteDTO {
  private Boolean podeAjudarNaCozinha;
  private DadoPessoalDTO dadoPessoal;
  private EnderecoDTO endereco;
  private Parentesco parentesco;
  private Boolean ativo;
}
