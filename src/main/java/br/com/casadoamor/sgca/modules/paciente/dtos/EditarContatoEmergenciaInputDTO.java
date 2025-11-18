package br.com.casadoamor.sgca.modules.paciente.dtos;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarContatoEmergenciaInputDTO {
  private String nome;

  private String email;

  @Pattern(regexp = "\\+?\\d{10,15}", message = "Telefone inválido")
  private String telefone;
}
