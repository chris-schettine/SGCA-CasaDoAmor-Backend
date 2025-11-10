package br.com.casadoamor.sgca.modules.agendamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para remarcar agendamento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemarcarAgendamentoDTO {

    @NotNull(message = "Nova data de início é obrigatória")
    private LocalDateTime novaDataInicio;

    @NotNull(message = "Nova data de fim é obrigatória")
    private LocalDateTime novaDataFim;

    @NotBlank(message = "Motivo da remarcação é obrigatório")
    @Size(max = 500)
    private String motivo;
}
