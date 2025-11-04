package br.com.casadoamor.sgca.modules.paciente.dtos;

import br.com.casadoamor.sgca.modules.common.enums.EstadoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnderecoDTO {
  private String id;

  private String logradouro;

  private Integer numero;

    private String complemento;

  private String bairro;

  private String cidade;

  private EstadoEnum estado;

  private String cep;
} 
