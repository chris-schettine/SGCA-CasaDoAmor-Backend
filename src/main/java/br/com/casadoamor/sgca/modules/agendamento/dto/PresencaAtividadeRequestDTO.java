package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.NivelParticipacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para registro de presença em atividade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresencaAtividadeRequestDTO {

    @NotBlank(message = "UUID do paciente é obrigatório")
    private String pacienteUuid;

    @NotNull(message = "Status de presença é obrigatório")
    private Boolean presente;

    private LocalDateTime horarioChegada;

    private LocalDateTime horarioSaida;

    private NivelParticipacao nivelParticipacao;

    private String observacoes;
}
