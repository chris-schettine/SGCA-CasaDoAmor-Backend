package br.com.casadoamor.sgca.modules.hospedagem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para retornar valores do enum AlaQuarto para dropdown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para valores do enum AlaQuarto")
public class AlaQuartoDTO {
    
    @Schema(description = "Valor do enum (código)", example = "FEMININA")
    private String valor;
    
    @Schema(description = "Descrição amigável", example = "Feminina")
    private String descricao;
}
