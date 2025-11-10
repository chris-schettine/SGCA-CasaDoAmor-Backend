package br.com.casadoamor.sgca.modules.agendamento.entity.enums;

/**
 * Modalidade do atendimento
 */
public enum Modalidade {
    PRESENCIAL("Presencial"),
    ONLINE("Online"),
    DOMICILIAR("Domiciliar"),
    HIBRIDA("Híbrida");

    private final String descricao;

    Modalidade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
