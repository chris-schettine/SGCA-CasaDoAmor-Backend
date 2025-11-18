package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RegistrarObitoDTO {
  private LocalDateTime dataHoraObito;

  private String localObito;

  private String causaObito;

  private String numeroDeclaracaoObito;

  private String observacoes;

  private String responsavelComunicacao;
}
