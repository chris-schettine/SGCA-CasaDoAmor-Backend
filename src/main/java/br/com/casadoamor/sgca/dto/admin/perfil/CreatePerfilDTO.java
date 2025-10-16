package br.com.casadoamor.sgca.dto.admin.perfil;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criar/atualizar perfil
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePerfilDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome; // Ex: ROLE_ADMIN

    private String descricao;
    
    private List<Long> permissoesIds; // IDs das permissões
}
