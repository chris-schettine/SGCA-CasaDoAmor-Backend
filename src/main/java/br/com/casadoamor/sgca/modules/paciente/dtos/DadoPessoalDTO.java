package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.time.LocalDate;

import br.com.casadoamor.sgca.modules.common.enums.EstadoCivilEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DadoPessoalDTO {
  private String id;
  
  private String nome;

	private String nomeMae;

  private LocalDate dataNascimento;

  private String cpf;

  private String rg;

  private String naturalidade;

	private String profissao;

  private String telefone;

  private EstadoCivilEnum estadoCivil;
} 
