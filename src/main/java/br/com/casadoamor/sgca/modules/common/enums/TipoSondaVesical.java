package br.com.casadoamor.sgca.modules.common.enums;

import lombok.Getter;

/**
 * Enum para tipos de sondas vesicais
 */
@Getter
public enum TipoSondaVesical {
    NAO("Não usa sonda vesical"),
    FOLEY("Sonda de Foley"),
    CISTOSTOMIA("Cistostomia"),
    OUTRA("Outro tipo de sonda vesical");

    private final String descricao;

    TipoSondaVesical(String descricao) {
        this.descricao = descricao;
    }
}
