package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.NivelParticipacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de presença em atividade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresencaAtividadeResponseDTO {

    private Long id;
    private String atividadeTitulo;
    private String pacienteNome;
    private Boolean presente;
    private LocalDateTime horarioChegada;
    private LocalDateTime horarioSaida;
    private NivelParticipacao nivelParticipacao;
    private String observacoes;
}
