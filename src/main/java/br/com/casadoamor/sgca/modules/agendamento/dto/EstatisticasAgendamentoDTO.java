package br.com.casadoamor.sgca.modules.agendamento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasAgendamentoDTO {

    // ===== ESTATÍSTICAS GERAIS =====
    private Long totalAgendamentosAtivos;
    private Long totalAgendamentosHoje;
    private Long totalAgendamentosSemana;
    private Long totalAgendamentosMes;
    private Long totalAgendamentosAno;
    
    // ===== ESTATÍSTICAS POR STATUS =====
    private Long agendamentosAgendados;
    private Long agendamentosConfirmados;
    private Long agendamentosEmAtendimento;
    private Long agendamentosConcluidos;
    private Long agendamentosCancelados;
    private Long agendamentosNaoCompareceram;
    
    // ===== ESTATÍSTICAS POR TIPO =====
    private Long agendamentosPacientes;
    private Long agendamentosAcompanhantes;
    private Long agendamentosAutomaticos;
    
    // ===== ESTATÍSTICAS POR PRIORIDADE =====
    private Long agendamentosUrgentes;
    private Long agendamentosAltaPrioridade;
    private Long agendamentosNormalPrioridade;
    private Long agendamentosBaixaPrioridade;
    
    // ===== ESTATÍSTICAS POR TIPO DE ATENDIMENTO =====
    private Long agendamentosPrimeiraVez;
    private Long agendamentosRetorno;
    private Long agendamentosEmergenciais;
    private Long agendamentosRotina;
    private Long agendamentosTriagem;
    
    // ===== CONFIRMAÇÕES =====
    private Long agendamentosPendentesConfirmacao;
    private Long agendamentosConfirmadosPaciente;
    private Long agendamentosConfirmadosProfissional;
    private Long agendamentosConfirmadosAmbos;
    
    // ===== TOP PROFISSIONAIS =====
    private List<ProfissionalEstatisticaDTO> topProfissionaisPorAgendamentos;
    private List<ProfissionalEstatisticaDTO> topProfissionaisPorConcluidos;
    
    // ===== TOP SERVIÇOS =====
    private List<ServicoEstatisticaDTO> topServicosMaisSolicitados;
    
    // ===== DISTRIBUIÇÃO POR DIA DA SEMANA =====
    private Map<String, Long> agendamentosPorDiaSemana;
    
    // ===== DISTRIBUIÇÃO POR HORA DO DIA =====
    private Map<Integer, Long> agendamentosPorHora;
    
    // ===== TAXA DE COMPARECIMENTO =====
    private Double taxaComparecimento;
    private Double taxaNaoComparecimento;
    private Double taxaCancelamento;
    
    // ===== TEMPO MÉDIO =====
    private Double duracaoMediaMinutos;
    private Double tempoMedioEsperaMinutos;
    
    // ===== METADADOS =====
    private LocalDateTime dataHoraConsulta;
    private String periodoAnalisado;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfissionalEstatisticaDTO {
        private Long profissionalId;
        private String profissionalNome;
        private String especialidade;
        private Long totalAgendamentos;
        private Long agendamentosConcluidos;
        private Double taxaConclusao;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicoEstatisticaDTO {
        private Long servicoId;
        private String servicoNome;
        private String categoria;
        private Long totalAgendamentos;
        private Double percentualTotal;
    }
}
