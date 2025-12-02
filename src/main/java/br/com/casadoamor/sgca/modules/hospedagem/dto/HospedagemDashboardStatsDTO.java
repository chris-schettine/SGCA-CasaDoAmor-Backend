package br.com.casadoamor.sgca.modules.hospedagem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO com estatísticas do dashboard de hospedagens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estatísticas do dashboard de hospedagens")
public class HospedagemDashboardStatsDTO {

    // === HOSPEDAGENS ===
    @Schema(description = "Total de hospedagens ativas no momento")
    private Long totalHospedagensAtivas;

    @Schema(description = "Total de hospedagens no mês atual")
    private Long totalHospedagensMesAtual;

    @Schema(description = "Total de hospedagens no mês anterior")
    private Long totalHospedagensMesAnterior;

    @Schema(description = "Total de hospedagens encerradas nos últimos 30 dias")
    private Long totalHospedagensEncerradasUltimos30Dias;

    @Schema(description = "Total de hospedagens com previsão de saída vencida")
    private Long totalHospedagensPreviaoVencida;

    @Schema(description = "Média de dias de permanência (hospedagens encerradas)")
    private Double mediaDiasPermanencia;

    @Schema(description = "Tempo médio de permanência em formato legível (ex: '15 dias')")
    private String tempoMedioPermanencia;

    // === QUARTOS ===
    @Schema(description = "Total de quartos cadastrados")
    private Long totalQuartos;

    @Schema(description = "Total de quartos ativos")
    private Long totalQuartosAtivos;

    @Schema(description = "Total de quartos em manutenção")
    private Long totalQuartosEmManutencao;

    @Schema(description = "Total de leitos disponíveis (soma de capacidades)")
    private Long totalLeitosDisponiveis;

    @Schema(description = "Total de leitos ocupados")
    private Long totalLeitosOcupados;

    @Schema(description = "Total de leitos vagos")
    private Long totalLeitosVagos;

    @Schema(description = "Taxa de ocupação global (%)")
    private BigDecimal taxaOcupacaoGlobal;

    @Schema(description = "Taxa de ocupação por ala (FEMININA, MASCULINA, MISTA)")
    private Map<String, OcupacaoPorAlaDTO> ocupacaoPorAla;

    // === PACIENTES ===
    @Schema(description = "Total de pacientes cadastrados")
    private Long totalPacientes;

    @Schema(description = "Total de pacientes ativos (não deletados)")
    private Long totalPacientesAtivos;

    @Schema(description = "Total de pacientes atualmente hospedados")
    private Long totalPacientesHospedados;

    @Schema(description = "Total de novos pacientes no mês atual")
    private Long totalNovosPacientesMesAtual;

    // === AGENDAMENTOS (se houver) ===
    @Schema(description = "Total de agendamentos do dia")
    private Long totalAgendamentosHoje;

    @Schema(description = "Total de agendamentos da semana")
    private Long totalAgendamentosSemana;

    @Schema(description = "Total de agendamentos pendentes")
    private Long totalAgendamentosPendentes;

    // === PREVISÕES ===
    @Schema(description = "Previsão de saídas para hoje")
    private Long previsaoSaidasHoje;

    @Schema(description = "Previsão de saídas para os próximos 7 dias")
    private Long previsaoSaidasProximos7Dias;

    @Schema(description = "Previsão de saídas para os próximos 30 dias")
    private Long previsaoSaidasProximos30Dias;

    // === HISTÓRICO RECENTE ===
    @Schema(description = "Últimas 5 entradas")
    private List<HospedagemResumoDTO> ultimasEntradas;

    @Schema(description = "Últimas 5 saídas")
    private List<HospedagemResumoDTO> ultimasSaidas;

    @Schema(description = "Quartos com maior ocupação")
    private List<QuartoOcupacaoDTO> quartosComMaiorOcupacao;

    // === TENDÊNCIAS ===
    @Schema(description = "Comparativo com mês anterior (%)")
    private BigDecimal crescimentoMesAtual;

    @Schema(description = "Hospedagens por mês (últimos 12 meses)")
    private List<HospedagensPorMesDTO> hospedagensPorMes;

    // === DTOs INTERNOS ===

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Estatísticas de ocupação por ala")
    public static class OcupacaoPorAlaDTO {
        @Schema(description = "Nome da ala")
        private String ala;

        @Schema(description = "Total de leitos")
        private Long totalLeitos;

        @Schema(description = "Leitos ocupados")
        private Long leitosOcupados;

        @Schema(description = "Leitos vagos")
        private Long leitosVagos;

        @Schema(description = "Taxa de ocupação (%)")
        private BigDecimal taxaOcupacao;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumo de hospedagem para dashboard")
    public static class HospedagemResumoDTO {
        @Schema(description = "UUID da hospedagem")
        private String uuid;

        @Schema(description = "Nome do paciente")
        private String nomePaciente;

        @Schema(description = "Nome do quarto")
        private String nomeQuarto;

        @Schema(description = "Data de entrada")
        private LocalDate dataEntrada;

        @Schema(description = "Data de saída")
        private LocalDate dataSaida;

        @Schema(description = "Status")
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Ocupação de quarto")
    public static class QuartoOcupacaoDTO {
        @Schema(description = "UUID do quarto")
        private String uuid;

        @Schema(description = "Nome do quarto")
        private String nome;

        @Schema(description = "Ala")
        private String ala;

        @Schema(description = "Capacidade total")
        private Integer capacidadeTotal;

        @Schema(description = "Capacidade ocupada")
        private Integer capacidadeOcupada;

        @Schema(description = "Taxa de ocupação (%)")
        private BigDecimal taxaOcupacao;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Hospedagens por mês")
    public static class HospedagensPorMesDTO {
        @Schema(description = "Mês (1-12)")
        private Integer mes;

        @Schema(description = "Ano")
        private Integer ano;

        @Schema(description = "Nome do mês")
        private String nomeMes;

        @Schema(description = "Total de hospedagens")
        private Long total;

        @Schema(description = "Total de entradas")
        private Long totalEntradas;

        @Schema(description = "Total de saídas")
        private Long totalSaidas;
    }
}
