package br.com.casadoamor.sgca.dto.admin.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de usuário por admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserDTO {

    private String nome;
    private String telefone;
    private String email;
    private Boolean ativo;
    private String tipo; // ADMINISTRADOR, MEDICO, etc.
}
