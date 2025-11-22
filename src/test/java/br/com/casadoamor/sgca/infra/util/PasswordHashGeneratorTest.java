package br.com.casadoamor.sgca.infra.util;

import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.Test;

class PasswordHashGeneratorTest {

    @Test
    void main_runsWithoutException() {
        // just ensure the main method runs and doesn't throw
        assertThatCode(() -> PasswordHashGenerator.main(new String[0]))
                .doesNotThrowAnyException();
    }
}
