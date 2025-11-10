package br.com.casadoamor.sgca.modules.agendamento.entity.enums;

/**
 * Status da atividade em grupo
 */
public enum StatusAtividade {
    PLANEJADA("Planejada"),
    CONFIRMADA("Confirmada"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada"),
    ADIADA("Adiada");

    private final String descricao;

    StatusAtividade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
