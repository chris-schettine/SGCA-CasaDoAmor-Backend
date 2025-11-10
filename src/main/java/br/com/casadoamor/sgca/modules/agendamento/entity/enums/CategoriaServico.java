package br.com.casadoamor.sgca.modules.agendamento.entity.enums;

/**
 * Categoria do serviço oferecido
 */
public enum CategoriaServico {
    MEDICO("Médico"),
    ODONTOLOGICO("Odontológico"),
    ENFERMAGEM("Enfermagem"),
    NUTRICAO("Nutrição"),
    FISIOTERAPIA("Fisioterapia"),
    PSICOLOGIA("Psicologia"),
    ASSISTENCIA_SOCIAL("Assistência Social"),
    PEDAGOGIA("Pedagogia"),
    OUTROS("Outros");

    private final String descricao;

    CategoriaServico(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
