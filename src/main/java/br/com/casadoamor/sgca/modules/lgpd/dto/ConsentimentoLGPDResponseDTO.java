package br.com.casadoamor.sgca.modules.lgpd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de consentimento LGPD unificado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentimentoLGPDResponseDTO {

    private Long id;
    private String uuid;
    private String tipoEntidade;  // USUARIO, PROFISSIONAL, PACIENTE, etc.
    private Long entidadeId;
    private String versaoTermo;
    private String escopo;
    private Boolean concorda;
    private LocalDateTime dataConsentimento;
    private String ipOrigem;
    private String userAgent;
    private String registradoPorNome;
    private String metadata;
    private LocalDateTime createdAt;
}
