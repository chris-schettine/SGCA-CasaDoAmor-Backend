package br.com.casadoamor.sgca.enums;

import lombok.Getter;

/**
 * Enum para tipos de sondas nasais/orais
 */
@Getter
public enum TipoSondaNasal {
    SNG("Sonda Nasogástrica - Inserida pelo nariz até o estômago"),
    SNE("Sonda Nasoenteral - Inserida pelo nariz até o intestino delgado"),
    OROGASTRICA("Sonda Orogástrica - Inserida pela boca até o estômago");

    private final String descricao;

    TipoSondaNasal(String descricao) {
        this.descricao = descricao;
    }
}
