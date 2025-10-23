package br.com.casadoamor.sgca.enums;

import lombok.Getter;

/**
 * Enum para tipos de sondas cirúrgicas
 */
@Getter
public enum TipoSondaCirurgica {
    G("Gastrostomia - Colocada cirurgicamente na parede abdominal, direto no estômago"),
    J("Jejunostomia - Inserida cirurgicamente na parede abdominal, direto no jejuno"),
    GJ("Gastrojejunostomia - Combina porta G (estômago) e porta J (jejuno)");

    private final String descricao;

    TipoSondaCirurgica(String descricao) {
        this.descricao = descricao;
    }
}
