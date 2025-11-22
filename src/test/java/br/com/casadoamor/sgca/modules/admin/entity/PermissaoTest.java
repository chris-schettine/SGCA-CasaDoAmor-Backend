package br.com.casadoamor.sgca.modules.admin.entity;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class PermissaoTest {

    @Test
    void isDeletado_false_then_true() {
        Permissao p = Permissao.builder().nome("P").build();
        assertThat(p.isDeletado()).isFalse();

        p.setDeletadoEm(java.time.LocalDateTime.now());
        assertThat(p.isDeletado()).isTrue();
    }
}
