package br.com.casadoamor.sgca.modules.agendamento.entity.enums;

/**
 * Prioridade do agendamento
 */
public enum Prioridade {
    BAIXA("Baixa"),
    NORMAL("Normal"),
    ALTA("Alta"),
    URGENTE("Urgente");

    private final String descricao;

    Prioridade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
