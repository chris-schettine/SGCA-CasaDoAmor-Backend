package br.com.casadoamor.sgca.modules.funcionario.entity.enums;

/**
 * Tipo de vínculo do profissional com a instituição
 */
public enum TipoVinculo {
    FUNCIONARIO("Funcionário"),
    VOLUNTARIO("Voluntário");

    private final String descricao;

    TipoVinculo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
