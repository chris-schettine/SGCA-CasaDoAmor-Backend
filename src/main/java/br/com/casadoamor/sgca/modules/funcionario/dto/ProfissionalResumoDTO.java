package br.com.casadoamor.sgca.modules.funcionario.dto;

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
    private String cpf;
    private String telefone;
    private String email;
    private String numeroRegistro;
    private String ufRegistro;
    private CategoriaProfissionalDTO categoria;
    private String areaAtuacao;
    private String especialidade;
    private String cargo;
    private Boolean ativo;
    private String fotoUrl; // URL da foto de perfil
}
