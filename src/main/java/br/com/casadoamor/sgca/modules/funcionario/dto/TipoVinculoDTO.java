package br.com.casadoamor.sgca.modules.funcionario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para tipos de vínculo (dropdown)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tipo de vínculo do profissional")
public class TipoVinculoDTO {
    
    @Schema(description = "ID do tipo de vínculo (PK)", example = "2")
    private Long id;

    @Schema(description = "Código do tipo de vínculo", example = "CLT")
    private String codigo;
    
    @Schema(description = "Nome/descrição do tipo de vínculo", example = "CLT")
    private String nome;
    
    @Schema(description = "Indica se o tipo de vínculo está ativo", example = "true")
    private Boolean ativo;
}
