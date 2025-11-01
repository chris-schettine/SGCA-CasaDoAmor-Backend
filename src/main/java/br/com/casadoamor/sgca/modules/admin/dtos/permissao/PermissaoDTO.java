package br.com.casadoamor.sgca.modules.admin.dtos.permissao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Permissão
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissaoDTO {

    private Long id;
    private String nome;
    private String descricao;
}
