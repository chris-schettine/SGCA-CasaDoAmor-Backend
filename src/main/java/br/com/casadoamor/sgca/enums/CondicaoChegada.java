package br.com.casadoamor.sgca.enums;

import lombok.Getter;

/**
 * Enum para condição de chegada do paciente
 */
@Getter
public enum CondicaoChegada {
    AMBULANCIA("Chegou de ambulância"),
    MACA("Chegou em maca"),
    CADEIRA_RODAS("Chegou em cadeira de rodas"),
    NENHUMA("Chegou sem auxílio - caminhando");

    private final String descricao;

    CondicaoChegada(String descricao) {
        this.descricao = descricao;
    }
}
