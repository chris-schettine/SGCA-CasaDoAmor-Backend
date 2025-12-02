package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Prioridade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.TipoAtendimento;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoAcompanhanteRequestDTO {

    @NotNull(message = "ID do acompanhante é obrigatório")
    private String acompanhanteId;

    @NotNull(message = "ID do tipo de serviço é obrigatório")
    private Long tipoServicoId;

    @NotNull(message = "ID do profissional é obrigatório")
    private Long profissionalUsuarioId;

    @NotNull(message = "Data e hora de início são obrigatórias")
    private LocalDateTime dataHoraInicio;

    @NotNull(message = "Data e hora de fim são obrigatórias")
    private LocalDateTime dataHoraFim;

    private Integer duracaoMinutos;

    private String pacienteVinculadoId;

    private TipoAtendimento tipoAtendimento;

    private Prioridade prioridade;

    private StatusAgendamento status;

    private String observacoes;

    private String motivoCancelamento;

    private Boolean confirmadoAcompanhante;

    private Boolean confirmadoProfissional;
}
