package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Modalidade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAtividade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de atividade em grupo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeGrupoResponseDTO {

    private String uuid;
    private String titulo;
    private String descricao;
    private String tipo;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String local;
    private Modalidade modalidade;
    private Integer capacidade;
    private Integer vagasOcupadas;
    private Integer vagasDisponiveis;
    private String publicoAlvo;
    private Integer idadeMinima;
    private Integer idadeMaxima;
    private Long responsavelPrincipalId;
    private String responsavelPrincipalNome;
    private Long responsavelApoioId;
    private String responsavelApoioNome;
    private String materiais;
    private StatusAtividade status;
    private String observacoes;
    private String observacoesPosAtividade;
    private String motivoCancelamento;
    private Boolean requerInscricao;
    private Boolean inscricoesAbertas;
    private LocalDateTime createdAt;
    private String createdByNome;
}
