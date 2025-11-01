package br.com.casadoamor.sgca.modules.admin.dtos.perfil;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atribuir/remover permissões de um perfil
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtribuirPermissoesDTO {

    @NotNull(message = "Lista de permissões não pode ser nula")
    @NotEmpty(message = "Pelo menos uma permissão deve ser informada")
    private List<Long> permissoesIds;
}
