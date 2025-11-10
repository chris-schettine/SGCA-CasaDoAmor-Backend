package br.com.casadoamor.sgca.modules.agendamento.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para inscrição em atividade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoAtividadeRequestDTO {

    @NotBlank(message = "UUID do paciente é obrigatório")
    private String pacienteUuid;
}
