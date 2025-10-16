package br.com.casadoamor.sgca.dto.twofactor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta ao configurar 2FA
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Setup2FADTO {

    private String mensagem;
    private Boolean habilitado;
    private String email;
}
