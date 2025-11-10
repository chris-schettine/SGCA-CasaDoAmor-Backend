package br.com.casadoamor.sgca.modules.agendamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cancelar agendamento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelarAgendamentoDTO {

    @NotBlank(message = "Motivo do cancelamento é obrigatório")
    @Size(min = 10, max = 500, message = "Motivo deve ter entre 10 e 500 caracteres")
    private String motivo;
}
