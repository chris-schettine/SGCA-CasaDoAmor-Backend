package br.com.casadoamor.sgca.modules.funcionario.entity.enums;

/**
 * Categoria/área de atuação do profissional na instituição
 */
public enum CategoriaProfissional {
    MEDICO("Médico"),
    ENFERMAGEM("Enfermagem"),
    ODONTOLOGIA("Odontologia"),
    PSICOLOGIA("Psicologia"),
    NUTRICAO("Nutrição"),
    FISIOTERAPIA("Fisioterapia"),
    ASSISTENCIA_SOCIAL("Assistência Social"),
    PEDAGOGIA("Pedagogia"),
    ADMINISTRATIVO("Administrativo"),
    RECEPCAO("Recepção"),
    OUTROS("Outros");

    private final String descricao;

    CategoriaProfissional(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
