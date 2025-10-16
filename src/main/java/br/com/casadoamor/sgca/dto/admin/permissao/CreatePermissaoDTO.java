package br.com.casadoamor.sgca.dto.admin.permissao;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criar/atualizar permissão
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePermissaoDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome; // Ex: PACIENTE_READ

    private String descricao;
}
