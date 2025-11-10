package br.com.casadoamor.sgca.modules.hospedagem.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para registro de hospedagem (entrada)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospedagemRequestDTO {

    @NotBlank(message = "ID do paciente é obrigatório")
    private String pacienteId;

    private String quartoUuid;

    @NotNull(message = "Data de entrada é obrigatória")
    @PastOrPresent(message = "Data de entrada não pode ser futura")
    private LocalDate dataEntrada;

    private LocalTime horaEntrada;

    @Future(message = "Data de saída prevista deve ser futura")
    private LocalDate dataSaidaPrevista;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoesEntrada;

    @Size(max = 1000, message = "Observações gerais devem ter no máximo 1000 caracteres")
    private String observacoesGerais;
}
