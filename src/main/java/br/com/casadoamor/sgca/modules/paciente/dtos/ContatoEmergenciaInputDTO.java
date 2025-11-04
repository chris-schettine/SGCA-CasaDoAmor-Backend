package br.com.casadoamor.sgca.modules.paciente.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContatoEmergenciaInputDTO {
  @NotBlank(message = "O nome é obrigatório")
  private String nome;

  @NotBlank(message = "O email é obrigatório")
  private String email;

  @NotBlank(message = "O telefone é obrigatório")
  @Pattern(regexp = "\\+?\\d{10,15}", message = "Telefone inválido")
  private String telefone;
}
