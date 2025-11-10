package br.com.casadoamor.sgca.modules.agendamento.entity.enums;

/**
 * Nível de participação do assistido em atividade
 */
public enum NivelParticipacao {
    NAO_PARTICIPOU("Não Participou"),
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta"),
    EXCELENTE("Excelente");

    private final String descricao;

    NivelParticipacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
