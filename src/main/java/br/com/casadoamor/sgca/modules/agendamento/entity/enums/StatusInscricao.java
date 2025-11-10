package br.com.casadoamor.sgca.modules.agendamento.entity.enums;

/**
 * Status da inscrição em atividade
 */
public enum StatusInscricao {
    PENDENTE("Pendente"),
    CONFIRMADA("Confirmada"),
    LISTA_ESPERA("Lista de Espera"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusInscricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
