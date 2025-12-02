package br.com.casadoamor.sgca.modules.agendamento.entity.enums;

/**
 * Especialidade do profissional de saúde
 */
public enum EspecialidadeProfissional {
    MEDICO("Médico"),
    ENFERMAGEM("Enfermagem"),
    NUTRICAO("Nutrição"),
    ODONTOLOGICO("Odontológico"),
    FISIOTERAPIA("Fisioterapia"),
    PSICOLOGIA("Psicologia"),
    ASSISTENCIA_SOCIAL("Assistência Social");

    private final String descricao;

    EspecialidadeProfissional(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
