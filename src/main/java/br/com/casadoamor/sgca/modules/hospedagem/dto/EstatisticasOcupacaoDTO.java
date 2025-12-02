package br.com.casadoamor.sgca.modules.hospedagem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO com estatísticas completas de ocupação e gerenciamento de quartos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasOcupacaoDTO {

    // === ESTATÍSTICAS DE OCUPAÇÃO ===
    private Integer capacidadeTotal;
    private Integer ocupacaoTotal;
    private Integer vagasDisponiveis;
    private Double percentualOcupacao;
    
    // === ESTATÍSTICAS DE QUARTOS ===
    private Integer totalQuartos;              // Total de quartos (ativos + inativos)
    private Integer quartosAtivos;             // Quartos ativos
    private Integer quartosInativos;           // Quartos inativos
    private Integer quartosEmManutencao;       // Quartos em manutenção
    private Integer quartosDisponiveisAdmissao; // Ativos, sem manutenção, com vagas
    
    // === DISTRIBUIÇÃO POR TIPO ===
    private Integer quartosIndividuais;        // Tipo INDIVIDUAL
    private Integer quartosCompartilhados;     // Tipo COMPARTILHADO
    private Integer quartosIsolamento;         // Tipo ISOLAMENTO
    
    // === MÉTRICAS OPERACIONAIS ===
    private Integer quartosLotados;            // Capacidade = ocupação
    private Integer quartosVazios;             // Ocupação = 0
    private Integer quartosParcialmenteOcupados; // 0 < ocupação < capacidade
    private Integer quartosPermitemSexoOposto;  // Flag permiteSexoOposto = true
    
    // === ESTATÍSTICAS POR ALA ===
    private EstatisticasAlaDTO alaFeminina;
    private EstatisticasAlaDTO alaMasculina;
    private EstatisticasAlaDTO alaMista;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstatisticasAlaDTO {
        // Ocupação
        private Integer capacidadeTotal;
        private Integer ocupacaoTotal;
        private Integer vagasDisponiveis;
        private Double percentualOcupacao;
        
        // Status dos quartos
        private Integer totalQuartos;
        private Integer quartosAtivos;
        private Integer quartosInativos;
        private Integer quartosEmManutencao;
        private Integer quartosDisponiveisAdmissao;
        
        // Operacional
        private Integer quartosLotados;
        private Integer quartosVazios;
        private Integer quartosParcialmenteOcupados;
    }
}
