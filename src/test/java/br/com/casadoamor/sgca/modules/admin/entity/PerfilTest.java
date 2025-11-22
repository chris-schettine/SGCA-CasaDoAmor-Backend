package br.com.casadoamor.sgca.modules.admin.entity;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class PerfilTest {

    @Test
    void isDeletado_falseThenTrue() {
        Perfil p = Perfil.builder().nome("P").build();
        assertThat(p.isDeletado()).isFalse();

        p.setDeletadoEm(java.time.LocalDateTime.now());
        assertThat(p.isDeletado()).isTrue();
    }

    @Test
    void adicionarRemoverPermissao_updatesBothSides() {
        Perfil perfil = Perfil.builder().nome("ROLE_X").build();
        Permissao perm = Permissao.builder().nome("P_X").build();

        perfil.adicionarPermissao(perm);

        assertThat(perfil.getPermissoes()).contains(perm);
        assertThat(perm.getPerfis()).contains(perfil);

        perfil.removerPermissao(perm);

        assertThat(perfil.getPermissoes()).doesNotContain(perm);
        assertThat(perm.getPerfis()).doesNotContain(perfil);
    }
}
