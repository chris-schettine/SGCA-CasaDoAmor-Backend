package br.com.casadoamor.sgca.entity.auth;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class Autenticacao2FARateLimitTest {

    @Test
    void podeEnviarNovoCodigo_WhenNeverSent_ReturnsTrue() {
        Autenticacao2FARateLimit r = new Autenticacao2FARateLimit();
        // By default, ultimoEnvio is null -> available now
        assertThat(r.podeEnviarNovoCodigo()).isTrue();
    }

    @Test
    void podeEnviarNovoCodigo_WhenTooManyIn15Min_Blocks() {
        Autenticacao2FARateLimit r = new Autenticacao2FARateLimit();
        r.setTentativasUltimos15Min(3);
        r.setUltimoEnvio(LocalDateTime.now().minusMinutes(1));

        boolean can = r.podeEnviarNovoCodigo();

        assertThat(can).isFalse();
        assertThat(r.isBloqueado()).isTrue();
    }

    @Test
    void incrementarContadores_IncrementsAndSetsUltimoEnvio() {
        Autenticacao2FARateLimit r = new Autenticacao2FARateLimit();
        r.setTentativasUltimos15Min(0);
        r.setTentativasUltimaHora(0);
        r.setTentativasHoje(0);

        r.incrementarContadores();

        assertThat(r.getTentativasUltimos15Min()).isEqualTo(1);
        assertThat(r.getTentativasUltimaHora()).isEqualTo(1);
        assertThat(r.getTentativasHoje()).isEqualTo(1);
        assertThat(r.getUltimoEnvio()).isNotNull();
    }

    @Test
    void getTempoEsperaFormatado_ReturnsNonEmptyStringWhenBlocked() {
        Autenticacao2FARateLimit r = new Autenticacao2FARateLimit();
        r.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));

        String s = r.getTempoEsperaFormatado();

        assertThat(s).isNotBlank();
    }

}
