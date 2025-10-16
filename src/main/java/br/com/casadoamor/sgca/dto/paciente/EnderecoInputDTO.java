package br.com.casadoamor.sgca.dto.paciente;

import br.com.casadoamor.sgca.enums.EstadoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record EnderecoInputDTO(
  @NotBlank(message = "O logradouro é obrigatório")
  String logradouro,

  @NotNull(message = "O número é obrigatório")
  @Positive(message = "O número deve ser positivo")
  Integer numero,

  String complemento,

  @NotBlank(message = "O bairro é obrigatório")
  String bairro,

  @NotBlank(message = "A cidade é obrigatória")
  String cidade,

  @NotNull(message = "O estado é obrigatório")
  EstadoEnum estado,

  @NotBlank(message = "O CEP é obrigatório")
  @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
  String cep
) {} 
