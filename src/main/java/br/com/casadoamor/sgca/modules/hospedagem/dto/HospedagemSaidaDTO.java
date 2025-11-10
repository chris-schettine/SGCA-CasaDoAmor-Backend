package br.com.casadoamor.sgca.modules.hospedagem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para encerramento de hospedagem (saída)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospedagemSaidaDTO {

    @NotNull(message = "Data de saída é obrigatória")
    @PastOrPresent(message = "Data de saída não pode ser futura")
    private LocalDate dataSaida;

    private LocalTime horaSaida;

    @NotBlank(message = "Motivo da saída é obrigatório")
    @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    private String motivoSaida;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoesSaida;
}
