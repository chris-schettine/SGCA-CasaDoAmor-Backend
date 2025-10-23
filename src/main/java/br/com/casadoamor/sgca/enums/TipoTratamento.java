package br.com.casadoamor.sgca.enums;

import lombok.Getter;

/**
 * Enum para tipos de tratamento
 */
@Getter
public enum TipoTratamento {
    RADIOTERAPIA("Radioterapia"),
    QUIMIOTERAPIA("Quimioterapia"),
    AMBOS("Radioterapia e Quimioterapia"),
    OUTRO("Outro tipo de tratamento");

    private final String descricao;

    TipoTratamento(String descricao) {
        this.descricao = descricao;
    }
}
