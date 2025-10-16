package br.com.casadoamor.sgca.dto.admin.user;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atribuir perfis a um usuário
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtribuirRolesDTO {

    @NotNull(message = "Lista de perfis não pode ser nula")
    @NotEmpty(message = "Pelo menos um perfil deve ser informado")
    private List<Long> perfisIds;
}
