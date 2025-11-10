package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusInscricao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de inscrição em atividade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoAtividadeResponseDTO {

    private Long id;
    private String atividadeUuid;
    private String atividadeTitulo;
    private String pacienteNome;
    private StatusInscricao status;
    private LocalDateTime inscritoEm;
    private LocalDateTime confirmadoEm;
    private LocalDateTime canceladoEm;
    private String inscritoPorNome;
}
