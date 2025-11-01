package br.com.casadoamor.sgca.modules.acompanhante.dtos;


import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcompanhanteDTO {
  private String id;
  private Boolean podeAjudarNaCozinha;
  private DadoPessoalDTO dadoPessoal;
  private EnderecoDTO endereco;
  private Parentesco parentesco;
  private Boolean ativo;
  private String pacienteNome;
}
