package br.com.casadoamor.sgca.modules.funcionario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de consentimento LGPD
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentimentoLGPDResponseDTO {

    private Long id;
    private String versaoTermo;
    private String escopo;
    private Boolean concorda;
    private LocalDateTime dataConsentimento;
    private String ipOrigem;
    private String registradoPorNome;
    private String metadata;
}
