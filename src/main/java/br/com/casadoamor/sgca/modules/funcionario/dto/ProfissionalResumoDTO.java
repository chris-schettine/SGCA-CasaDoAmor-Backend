package br.com.casadoamor.sgca.modules.funcionario.dto;

import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para listagem resumida de profissionais
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalResumoDTO {

    private String uuid;
    private String nome;
    private String telefone;
    private String email;
    private CategoriaProfissional categoria;
    private String areaAtuacao;
    private String especialidade;
    private String cargo;
    private Boolean ativo;
}
