package br.com.casadoamor.sgca.modules.hospedagem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para retornar valores do enum TipoQuarto para dropdown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para valores do enum TipoQuarto")
public class TipoQuartoDTO {
    
    @Schema(description = "Valor do enum (código)", example = "INDIVIDUAL")
    private String valor;
    
    @Schema(description = "Descrição amigável", example = "Individual")
    private String descricao;
}
