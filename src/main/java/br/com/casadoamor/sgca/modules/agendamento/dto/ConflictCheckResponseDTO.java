package br.com.casadoamor.sgca.modules.agendamento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictCheckResponseDTO {

    private Boolean temConflito;
    private String mensagem;
    private ConflictDetails details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictDetails {
        private String tipo; // AGENDAMENTO, BLOQUEIO, FORA_HORARIO
        private String descricao;
        private LocalDateTime conflitanteInicio;
        private LocalDateTime conflitanteFim;
        private String profissionalNome;
    }
}
