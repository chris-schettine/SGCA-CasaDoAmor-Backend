package br.com.casadoamor.sgca.modules.hospedagem.dto;

import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO resumido de quarto para listagens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuartoResumoDTO {

    private String uuid;
    private String nome;
    private String codigo;
    private TipoQuarto tipo;
    private AlaQuarto ala;
    private String andar;
    private Integer capacidadeTotal;
    private Integer capacidadeOcupada;
    private Integer vagasDisponiveis;
    private Boolean ativo;
    private Boolean emManutencao;
    private Boolean permiteSexoOposto;
}
