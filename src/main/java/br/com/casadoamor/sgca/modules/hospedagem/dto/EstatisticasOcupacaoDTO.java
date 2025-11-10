package br.com.casadoamor.sgca.modules.hospedagem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO com estatísticas de ocupação
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasOcupacaoDTO {

    private Integer capacidadeTotal;
    private Integer ocupacaoTotal;
    private Integer vagasDisponiveis;
    private Double percentualOcupacao;
    
    // Estatísticas por ala
    private EstatisticasAlaDTO alaFeminina;
    private EstatisticasAlaDTO alaMasculina;
    private EstatisticasAlaDTO alaMista;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstatisticasAlaDTO {
        private Integer capacidadeTotal;
        private Integer ocupacaoTotal;
        private Integer vagasDisponiveis;
        private Double percentualOcupacao;
    }
}
