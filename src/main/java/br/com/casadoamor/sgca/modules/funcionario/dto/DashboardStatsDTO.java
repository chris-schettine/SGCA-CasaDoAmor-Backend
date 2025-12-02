package br.com.casadoamor.sgca.modules.funcionario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para estatísticas do dashboard de profissionais
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estatísticas do dashboard de profissionais")
public class DashboardStatsDTO {
    
    @Schema(description = "Total de profissionais ativos")
    private Long totalProfissionaisAtivos;
    
    @Schema(description = "Total de profissionais inativos")
    private Long totalProfissionaisInativos;
    
    @Schema(description = "Distribuição por categoria profissional")
    private List<CategoriaCount> porCategoria;
    
    @Schema(description = "Distribuição por tipo de vínculo")
    private List<TipoVinculoCount> porTipoVinculo;
    
    @Schema(description = "Profissionais admitidos nos últimos 30 dias")
    private Long admitidosUltimos30Dias;
    
    @Schema(description = "Profissionais com endereço cadastrado")
    private Long comEnderecoCadastrado;
    
    @Schema(description = "Profissionais sem endereço cadastrado")
    private Long semEnderecoCadastrado;
    
    @Schema(description = "Distribuição por área de atuação (top 5)")
    private Map<String, Long> topAreasAtuacao;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaCount {
        @Schema(description = "Categoria profissional", example = "MEDICO")
        private String categoria;
        
        @Schema(description = "Label da categoria", example = "Médico")
        private String label;
        
        @Schema(description = "Quantidade de profissionais", example = "15")
        private Long count;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoVinculoCount {
        @Schema(description = "Código do tipo de vínculo", example = "CLT")
        private String codigo;
        
        @Schema(description = "Nome do tipo de vínculo", example = "CLT")
        private String nome;
        
        @Schema(description = "Quantidade de profissionais", example = "10")
        private Long count;
    }
}
