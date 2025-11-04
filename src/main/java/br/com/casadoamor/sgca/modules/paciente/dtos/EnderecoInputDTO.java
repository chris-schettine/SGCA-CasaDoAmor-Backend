package br.com.casadoamor.sgca.modules.paciente.dtos;

import br.com.casadoamor.sgca.modules.common.enums.EstadoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnderecoInputDTO {
  @NotBlank(message = "O logradouro é obrigatório")
  private String logradouro;

  @NotNull(message = "O número é obrigatório")
  @Positive(message = "O número deve ser positivo")
  private Integer numero;

  private String complemento;

  @NotBlank(message = "O bairro é obrigatório")
  private String bairro;

  @NotBlank(message = "A cidade é obrigatória")
  private String cidade;

  @NotNull(message = "O estado é obrigatório")
  private EstadoEnum estado;

  @NotBlank(message = "O CEP é obrigatório")
  @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
  private String cep;
} 
