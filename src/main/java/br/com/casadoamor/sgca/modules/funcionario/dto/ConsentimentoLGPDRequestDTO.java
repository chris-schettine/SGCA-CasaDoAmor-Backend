package br.com.casadoamor.sgca.modules.funcionario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registro de consentimento LGPD
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentimentoLGPDRequestDTO {

    @NotBlank(message = "Versão do termo é obrigatória")
    @Size(max = 50)
    private String versaoTermo;

    @NotBlank(message = "Escopo é obrigatório")
    @Size(max = 255)
    private String escopo;

    @NotNull(message = "Concordância é obrigatória")
    private Boolean concorda;

    private String ipOrigem;

    private String userAgent;

    private String metadata; // JSON string
}
