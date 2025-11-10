package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.CategoriaServico;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de tipo de serviço
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoServicoResponseDTO {

    private Long id;
    private String codigo;
    private String nome;
    private String descricao;
    private CategoriaServico categoria;
    private Integer duracaoMinutos;
    private Boolean requerProfissional;
    private Boolean permiteAcompanhante;
    private Boolean ativo;
    private String observacoes;
}
