package br.com.casadoamor.sgca.modules.admin.dtos.perfil;

import java.util.List;

import br.com.casadoamor.sgca.modules.admin.dtos.permissao.PermissaoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Perfil (Role)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilDTO {

    private Long id;
    private String nome;
    private String descricao;
    private List<PermissaoDTO> permissoes;
    private Integer totalPermissoes;
}
