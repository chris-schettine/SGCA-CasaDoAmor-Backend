package br.com.casadoamor.sgca.modules.hospedagem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de resposta com dados completos do quarto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuartoResponseDTO {

    private String uuid;
    private String nome;
    private String codigo;
    private TipoQuartoDTO tipo;
    private AlaQuartoDTO ala;
    private String andar;
    private Integer capacidadeTotal;
    private Integer capacidadeOcupada;
    private Integer vagasDisponiveis;
    private Boolean ativo;
    private Boolean emManutencao;
    private Boolean permiteSexoOposto;
    private String observacoes;
    
    // Auditoria
    private LocalDateTime createdAt;
    private String createdByNome;
    private LocalDateTime updatedAt;
    private String updatedByNome;
}
