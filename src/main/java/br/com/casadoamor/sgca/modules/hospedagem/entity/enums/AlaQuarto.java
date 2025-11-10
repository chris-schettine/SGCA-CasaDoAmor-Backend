package br.com.casadoamor.sgca.modules.hospedagem.entity.enums;

/**
 * Ala/setor do quarto (separação por gênero)
 */
public enum AlaQuarto {
    FEMININA("Feminina"),
    MASCULINA("Masculina"),
    MISTA("Mista");

    private final String descricao;

    AlaQuarto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
