package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Prioridade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.TipoAtendimento;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoAcompanhanteResponseDTO {

    private Long id;
    private String uuid;
    private String acompanhanteId;
    private String acompanhanteNome;
    private Long tipoServicoId;
    private String tipoServicoNome;
    private Long profissionalUsuarioId;
    private String profissionalNome;
    private String pacienteVinculadoId;
    private String pacienteVinculadoNome;
    private String quartoNome;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraInicio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraFim;

    private TipoAtendimento tipoAtendimento;
    private Prioridade prioridade;
    private StatusAgendamento status;
    private String observacoes;
    private String motivoCancelamento;

    private Boolean confirmadoAcompanhante;
    private Boolean confirmadoProfissional;
    private Boolean compareceu;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime horaChegada;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime horaInicioAtendimento;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime horaFimAtendimento;

    private Long agendamentoRemarcarId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
