package br.com.casadoamor.sgca.modules.hospedagem.entity.enums;

/**
 * Tipo de quarto/acomodação
 */
public enum TipoQuarto {
    INDIVIDUAL("Individual"),
    COMPARTILHADO("Compartilhado"),
    // Suporte para quartos de isolamento
    ISOLAMENTO("Isolamento");

    private final String descricao;

    TipoQuarto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
