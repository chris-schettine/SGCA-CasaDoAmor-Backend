package br.com.casadoamor.sgca.modules.auth.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TestAuthControllerTest {

    @Mock
    private AuthUsuarioRepository repo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @org.junit.jupiter.api.BeforeEach
    void setup() throws Exception {
        java.lang.reflect.Field f = controller.getClass().getDeclaredField("passwordEncoder");
        f.setAccessible(true);
        f.set(controller, encoder);
    }

    @InjectMocks
    private TestAuthController controller;

    @Test
    void testPassword_userFound_matchesAndMap() {
        AuthUsuario u = AuthUsuario.builder().cpf("000").nome("X").senhaHash(encoder.encode("pw")).ativo(true).build();
        when(repo.findByCpf("000")).thenReturn(Optional.of(u));

        ResponseEntity<Map<String, Object>> resp = controller.testPassword("000", "pw");
        Map<String, Object> body = resp.getBody();

        assertThat(body.get("usuarioEncontrado")).isEqualTo(Boolean.TRUE);
        assertThat(body.get("senhaMatches")).isEqualTo(Boolean.TRUE);
        assertThat(body.get("usuarioNome")).isEqualTo("X");
    }

    @Test
    void testPassword_userMissing_returnsErrorMap() {
        when(repo.findByCpf("missing")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> resp = controller.testPassword("missing", "x");
        Map<String, Object> body = resp.getBody();

        assertThat(body.get("usuarioEncontrado")).isEqualTo(Boolean.FALSE);
        assertThat(body.get("erro")).isNotNull();
    }

    @Test
    void listUsers_returnsListOfMaps() {
        AuthUsuario u = AuthUsuario.builder().id(10L).cpf("c").nome("N").email("e@e").tipo(AuthUsuario.TipoUsuario.RECEPCIONISTA).ativo(true).build();
        when(repo.findAll()).thenReturn(List.of(u));

        ResponseEntity<List<Map<String, Object>>> resp = controller.listUsers();
        assertThat(resp.getBody()).hasSize(1);
        assertThat(resp.getBody().get(0).get("cpf")).isEqualTo("c");
    }

    @Test
    void generateHash_returnsHashAndSelfCheck() {
        ResponseEntity<Map<String, Object>> resp = controller.generateHash("abc123");
        Map<String, Object> body = resp.getBody();
        assertThat(body.get("senha")).isEqualTo("abc123");
        assertThat(body.get("hash")).isNotNull();
        assertThat(body.get("selfCheck")).isEqualTo(Boolean.TRUE);
    }
}
