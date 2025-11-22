package br.com.casadoamor.sgca.modules.auth.service;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import br.com.casadoamor.sgca.modules.admin.entity.Perfil;
import br.com.casadoamor.sgca.modules.admin.entity.Permissao;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private AuthUsuarioRepository repository;

    @InjectMocks
    private PermissionService service;

    private AuthUsuario buildUsuarioWithPermissions(String cpf, String tipo, String... perms) {
        AuthUsuario u = AuthUsuario.builder().cpf(cpf).build();
        Perfil perfil = Perfil.builder().nome("ROLE_TEST").build();
        for (String p : perms) {
            Permissao perm = Permissao.builder().nome(p).build();
            perfil.getPermissoes().add(perm);
        }
        u.getPerfis().add(perfil);
        if (tipo != null) {
            try { // set enum by name if exists
                u.setTipo(AuthUsuario.TipoUsuario.valueOf(tipo));
            } catch (Exception ignored) {
            }
        }
        return u;
    }

    @Test
    void hasPermission_userHasPermission_returnsTrue() {
        AuthUsuario user = buildUsuarioWithPermissions("111", null, "P_READ", "P_WRITE");
        when(repository.findByCpf("111")).thenReturn(Optional.of(user));

        boolean ok = service.hasPermission("111", "P_READ");
        assertThat(ok).isTrue();
    }

    @Test
    void hasPermission_userNotFound_returnsFalse() {
        when(repository.findByCpf("missing")).thenReturn(Optional.empty());

        assertThat(service.hasPermission("missing", "ANY")).isFalse();
    }

    @Test
    void hasAllPermissions_returnsExpectedForMultiple() {
        AuthUsuario user = buildUsuarioWithPermissions("22", null, "A", "B");
        when(repository.findByCpf("22")).thenReturn(Optional.of(user));

        assertThat(service.hasAllPermissions("22", "A", "B")).isTrue();
        assertThat(service.hasAllPermissions("22", "A", "X")).isFalse();
    }

    @Test
    void hasAnyPermission_returnsExpected() {
        AuthUsuario u = buildUsuarioWithPermissions("33", null, "X");
        when(repository.findByCpf("33")).thenReturn(Optional.of(u));

        assertThat(service.hasAnyPermission("33", "Y", "X")).isTrue();
        assertThat(service.hasAnyPermission("33", "Y", "Z")).isFalse();
    }

    @Test
    void getUserPermissions_returnsSetOrEmptyOnError() {
        AuthUsuario u = buildUsuarioWithPermissions("44", null, "PERM1", "PERM2");
        when(repository.findByCpf("44")).thenReturn(Optional.of(u));

        Set<String> perms = service.getUserPermissions("44");
        assertThat(perms).containsExactlyInAnyOrder("PERM1", "PERM2");

        when(repository.findByCpf("err")).thenThrow(new RuntimeException("boom"));
        assertThat(service.getUserPermissions("err")).isEmpty();
    }

    @Test
    void isAdmin_trueAndFalseCases() {
        AuthUsuario admin = buildUsuarioWithPermissions("a", "ADMINISTRADOR", "P");
        when(repository.findByCpf("a")).thenReturn(Optional.of(admin));
        assertThat(service.isAdmin("a")).isTrue();

        AuthUsuario other = buildUsuarioWithPermissions("b", "RECEPCIONISTA", "P");
        when(repository.findByCpf("b")).thenReturn(Optional.of(other));
        assertThat(service.isAdmin("b")).isFalse();

        when(repository.findByCpf("missing")).thenReturn(Optional.empty());
        assertThat(service.isAdmin("missing")).isFalse();
    }

    @Test
    void hasPermission_withAuthentication() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("99");
        when(auth.isAuthenticated()).thenReturn(true);

        AuthUsuario u = buildUsuarioWithPermissions("99", null, "X1");
        when(repository.findByCpf("99")).thenReturn(Optional.of(u));

        assertThat(service.hasPermission(auth, "X1")).isTrue();

        // not authenticated or null
        when(auth.isAuthenticated()).thenReturn(false);
        assertThat(service.hasPermission(auth, "X1")).isFalse();

        assertThat(service.hasPermission((Authentication) null, "X1")).isFalse();
    }

    @Test
    void getAuthorities_returnsPermissionsAndRole_orEmptyOnError() {
        AuthUsuario u = buildUsuarioWithPermissions("777", "ADMINISTRADOR", "AA", "BB");
        when(repository.findByCpf("777")).thenReturn(Optional.of(u));

        var authorities = service.getAuthorities("777");
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).anyMatch(a -> a.getAuthority().equals("AA"));
        assertThat(authorities).anyMatch(a -> a.getAuthority().equals("BB"));
        assertThat(authorities).anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        when(repository.findByCpf("err")).thenThrow(new RuntimeException("boom"));
        assertThat(service.getAuthorities("err")).isEmpty();
    }
}
