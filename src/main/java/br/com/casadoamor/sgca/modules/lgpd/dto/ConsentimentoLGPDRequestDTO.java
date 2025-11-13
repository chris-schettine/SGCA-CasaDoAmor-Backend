package br.com.casadoamor.sgca.modules.lgpd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registro de consentimento LGPD unificado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentimentoLGPDRequestDTO {

    @NotBlank(message = "Versão do termo é obrigatória")
    @Size(max = 50, message = "Versão do termo deve ter no máximo 50 caracteres")
    private String versaoTermo;

    @Size(max = 5000, message = "Escopo deve ter no máximo 5000 caracteres")
    private String escopo;

    @NotNull(message = "Concordância é obrigatória")
    private Boolean concorda;

    private String metadata; // JSON string
}
