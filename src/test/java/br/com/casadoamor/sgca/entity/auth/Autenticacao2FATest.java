package br.com.casadoamor.sgca.entity.auth;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class Autenticacao2FATest {

    @Test
    void isCodigoExpirado_WhenExpired_ReturnsTrue() {
        Autenticacao2FA a = new Autenticacao2FA();
        a.setExpiracaoCodigo(LocalDateTime.now().minusMinutes(1));

        assertThat(a.isCodigoExpirado()).isTrue();
    }

    @Test
    void isCodigoExpirado_WhenNotExpired_ReturnsFalse() {
        Autenticacao2FA a = new Autenticacao2FA();
        a.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(5));

        assertThat(a.isCodigoExpirado()).isFalse();
    }

    @Test
    void isBloqueado_WhenBlocked_ReturnsTrue() {
        Autenticacao2FA a = new Autenticacao2FA();
        a.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));

        assertThat(a.isBloqueado()).isTrue();
    }

    @Test
    void isBloqueado_WhenNotBlocked_ReturnsFalse() {
        Autenticacao2FA a = new Autenticacao2FA();
        a.setBloqueadoAte(LocalDateTime.now().minusMinutes(10));

        assertThat(a.isBloqueado()).isFalse();
    }

    @Test
    void incrementarTentativasFalhas_BlocksAfterFiveAttempts() {
        Autenticacao2FA a = new Autenticacao2FA();
        a.setTentativasFalhas(4);

        a.incrementarTentativasFalhas();

        assertThat(a.getTentativasFalhas()).isGreaterThanOrEqualTo(5);
        assertThat(a.getBloqueadoAte()).isNotNull();
        assertThat(a.isBloqueado()).isTrue();
    }

    @Test
    void resetarTentativasFalhas_ClearsAttemptsAndBlock() {
        Autenticacao2FA a = new Autenticacao2FA();
        a.setTentativasFalhas(3);
        a.setBloqueadoAte(LocalDateTime.now().plusMinutes(5));

        a.resetarTentativasFalhas();

        assertThat(a.getTentativasFalhas()).isEqualTo(0);
        assertThat(a.getBloqueadoAte()).isNull();
        assertThat(a.isBloqueado()).isFalse();
    }

}
