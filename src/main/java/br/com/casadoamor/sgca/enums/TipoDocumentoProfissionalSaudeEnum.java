package br.com.casadoamor.sgca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoDocumentoProfissionalSaudeEnum {
    CRM(0),
    COREN(1),
    CRP(2),
    CRN(3),
    OUTRO(4);

    private final int codigo;

    public static TipoDocumentoProfissionalSaudeEnum fromCodigo(int codigo) {
        for (TipoDocumentoProfissionalSaudeEnum tipo : values()) {
            if (tipo.getCodigo() == codigo) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
}
