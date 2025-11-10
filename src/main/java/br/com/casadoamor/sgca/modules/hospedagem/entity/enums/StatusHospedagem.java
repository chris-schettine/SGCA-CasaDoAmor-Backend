package br.com.casadoamor.sgca.modules.hospedagem.entity.enums;

/**
 * Status da hospedagem do paciente
 */
public enum StatusHospedagem {
    ATIVA("Ativa"),
    ENCERRADA("Encerrada"),
    TRANSFERENCIA("Transferência");

    private final String descricao;

    StatusHospedagem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
