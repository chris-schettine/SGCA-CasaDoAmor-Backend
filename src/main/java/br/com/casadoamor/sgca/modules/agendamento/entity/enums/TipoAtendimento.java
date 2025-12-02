package br.com.casadoamor.sgca.modules.agendamento.entity.enums;

/**
 * Tipo de atendimento do agendamento
 */
public enum TipoAtendimento {
    PRIMEIRA_VEZ("Primeira Vez"),
    RETORNO("Retorno"),
    EMERGENCIAL("Emergencial"),
    ROTINA("Rotina"),
    TRIAGEM("Triagem");

    private final String descricao;

    TipoAtendimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
