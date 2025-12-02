package br.com.casadoamor.sgca.modules.funcionario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para categorias profissionais (dropdown)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Categoria/área de atuação do profissional")
public class CategoriaProfissionalDTO {
    
    @Schema(description = "Valor do enum (chave)", example = "MEDICO")
    private String valor;
    
    @Schema(description = "Descrição amigável (label)", example = "Médico")
    private String descricao;
}
