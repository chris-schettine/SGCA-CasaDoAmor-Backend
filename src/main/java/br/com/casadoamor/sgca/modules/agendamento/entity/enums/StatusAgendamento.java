package br.com.casadoamor.sgca.modules.agendamento.entity.enums;

/**
 * Status do agendamento
 */
public enum StatusAgendamento {
    AGENDADO("Agendado"),
    CONFIRMADO("Confirmado"),
    EM_ATENDIMENTO("Em Atendimento"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado"),
    REMARCADO("Remarcado"),
    FALTOSO("Faltoso"),
    PACIENTE_NAO_COMPARECEU("Paciente Não Compareceu");

    private final String descricao;

    StatusAgendamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
