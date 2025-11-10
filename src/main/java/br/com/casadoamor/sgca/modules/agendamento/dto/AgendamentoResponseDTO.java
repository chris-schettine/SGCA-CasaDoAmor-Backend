package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Modalidade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de agendamento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoResponseDTO {

    private String uuid;
    private Long tipoServicoId;
    private String tipoServicoNome;
    private String tipoServicoCategoria;
    private String pacienteUuid;
    private String pacienteNome;
    private Long profissionalId;
    private String profissionalNome;
    private String acompanhanteNome;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private StatusAgendamento status;
    private String local;
    private Modalidade modalidade;
    private String observacoes;
    private String motivoCancelamento;
    private String observacoesAtendimento;
    private Boolean lembreteEnviado;
    private LocalDateTime lembreteEnviadoEm;
    private Boolean confirmacaoPaciente;
    private LocalDateTime confirmacaoPacienteEm;
    private LocalDateTime createdAt;
    private String createdByNome;
}
