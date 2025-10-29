package br.com.casadoamor.sgca.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.casadoamor.sgca.dto.SessaoDTO;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.entity.auth.SessaoUsuario;
import br.com.casadoamor.sgca.exception.ResourceNotFoundException;
import br.com.casadoamor.sgca.repository.auth.SessaoUsuarioRepository;
import br.com.casadoamor.sgca.security.JwtUtil;

class SessaoServiceTest {

    @Mock
    private SessaoUsuarioRepository sessaoRepository;

    @InjectMocks
    private SessaoService sessaoService;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Prepare JwtUtil with a deterministic secret and expiration for token-related tests
        jwtUtil = new JwtUtil();
        String rawKey = "01234567890123456789012345678901"; // 32 chars
        String b64 = Base64.getEncoder().encodeToString(rawKey.getBytes());

        Field secretField = JwtUtil.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, b64);

        Field expField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.setLong(jwtUtil, 1000L * 60 * 60); // 1 hour
    }

    @Test
    void listarSessoesAtivas_ReturnsDtosWithAtualFlag() {
        Long userId = 1L;
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(userId);
        usuario.setNome("Usuário");
        usuario.setEmail("u@example.com");
        usuario.setCpf("00000000000");

        LocalDateTime now = LocalDateTime.now();

        SessaoUsuario s1 = SessaoUsuario.builder()
                .id(10L)
                .usuario(usuario)
                .tokenJwt("token1")
                .ipOrigem("1.1.1.1")
                .userAgent("ua1")
                .criadoEm(now)
                .expiraEm(now.plusHours(1))
                .ativo(true)
                .build();

        SessaoUsuario s2 = SessaoUsuario.builder()
                .id(11L)
                .usuario(usuario)
                .tokenJwt("token2")
                .ipOrigem("2.2.2.2")
                .userAgent("ua2")
                .criadoEm(now)
                .expiraEm(now.plusHours(1))
                .ativo(true)
                .build();

        when(sessaoRepository.findByUsuarioIdAndAtivoAndExpiraEmAfter(anyLong(), any(Boolean.class), any(LocalDateTime.class)))
            .thenReturn(List.of(s1, s2));

        List<SessaoDTO> dtos = sessaoService.listarSessoesAtivas(userId, "token2");

        assertThat(dtos).hasSize(2);
        SessaoDTO atual = dtos.stream().filter(d -> d.getId().equals(11L)).findFirst().orElseThrow();
        assertThat(atual.getAtual()).isTrue();
        assertThat(atual.getIpOrigem()).isEqualTo("2.2.2.2");
    }

    @Test
    void revogarSessao_Success_RevokesAndSaves() {
        Long sessaoId = 123L;
        Long userId = 1L;

        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(userId);

        SessaoUsuario s = SessaoUsuario.builder()
                .id(sessaoId)
                .usuario(usuario)
                .tokenJwt("t")
                .ativo(true)
                .expiraEm(LocalDateTime.now().plusHours(1))
                .build();

        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.of(s));

        sessaoService.revogarSessao(sessaoId, userId);

        ArgumentCaptor<SessaoUsuario> captor = ArgumentCaptor.forClass(SessaoUsuario.class);
        verify(sessaoRepository).save(captor.capture());
        SessaoUsuario saved = captor.getValue();
        assertThat(saved.getAtivo()).isFalse();
    }

    @Test
    void revogarSessao_NotFound_Throws() {
        when(sessaoRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> sessaoService.revogarSessao(999L, 1L));
        // ensure exception has a message
        assertThat(ex.getMessage()).isNotBlank();
    }

    @Test
    void revogarSessao_WrongUser_Throws() {
        Long sessaoId = 200L;
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(2L);

        SessaoUsuario s = SessaoUsuario.builder().id(sessaoId).usuario(usuario).ativo(true).expiraEm(LocalDateTime.now().plusHours(1)).build();
        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.of(s));

        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> sessaoService.revogarSessao(sessaoId, 1L));
        assertThat(iae.getMessage()).isNotBlank();
    }

    @Test
    void revogarTodasSessoes_ExcludingCurrent_RevokesOthers() {
        Long userId = 3L;
        String tokenAtual = "keep";

        AuthUsuario usuario = new AuthUsuario(); usuario.setId(userId);

        SessaoUsuario a = SessaoUsuario.builder().id(1L).usuario(usuario).tokenJwt("t1").ativo(true).expiraEm(LocalDateTime.now().plusHours(1)).build();
        SessaoUsuario b = SessaoUsuario.builder().id(2L).usuario(usuario).tokenJwt(tokenAtual).ativo(true).expiraEm(LocalDateTime.now().plusHours(1)).build();
        SessaoUsuario c = SessaoUsuario.builder().id(3L).usuario(usuario).tokenJwt("t3").ativo(true).expiraEm(LocalDateTime.now().plusHours(1)).build();

        when(sessaoRepository.findByUsuarioIdAndAtivo(userId, true)).thenReturn(List.of(a, b, c));

        sessaoService.revogarTodasSessoes(userId, tokenAtual);

        // a and c should be revoked
        assertThat(a.getAtivo()).isFalse();
        assertThat(c.getAtivo()).isFalse();
        // b should remain active
        assertThat(b.getAtivo()).isTrue();

        verify(sessaoRepository, times(2)).save(any(SessaoUsuario.class));
    }

    @Test
    void contarSessoesAtivas_ReturnsCount() {
        when(sessaoRepository.countByUsuarioIdAndAtivoAndExpiraEmAfter(anyLong(), any(Boolean.class), any(LocalDateTime.class)))
            .thenReturn(5L);

        Long count = sessaoService.contarSessoesAtivas(1L);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void limparSessoesExpiradas_MarksInactiveAndSaves() {
        AuthUsuario usuario = new AuthUsuario(); usuario.setId(4L);
        SessaoUsuario s = SessaoUsuario.builder().id(50L).usuario(usuario).ativo(true).expiraEm(LocalDateTime.now().plusMinutes(10)).build();

        when(sessaoRepository.findByAtivoAndExpiraEmAfter(any(Boolean.class), any(LocalDateTime.class))).thenReturn(List.of(s));

        sessaoService.limparSessoesExpiradas();

        assertThat(s.getAtivo()).isFalse();
        verify(sessaoRepository).save(s);
    }

    // token validity tests
    @Test
    void sessaoValida_WhenTokenExistsAndNotExpiredAndActive_ReturnsTrue() {
        String token = "valid-token";
        AuthUsuario u = new AuthUsuario(); u.setId(1L); u.setEmail("t@example.com");

        SessaoUsuario s = SessaoUsuario.builder()
                .id(10L)
                .usuario(u)
                .tokenJwt(token)
                .ativo(true)
                .expiraEm(LocalDateTime.now().plusMinutes(10))
                .build();

        when(sessaoRepository.findByTokenJwt(token)).thenReturn(Optional.of(s));

        boolean ok = sessaoService.sessaoValida(token);

        assertThat(ok).isTrue();
    }

    @Test
    void sessaoValida_WhenTokenExpired_ReturnsFalse() {
        String token = "expired-token";
        AuthUsuario u = new AuthUsuario(); u.setId(2L);

        SessaoUsuario s = SessaoUsuario.builder()
                .id(11L)
                .usuario(u)
                .tokenJwt(token)
                .ativo(true)
                .expiraEm(LocalDateTime.now().minusMinutes(5))
                .build();

        when(sessaoRepository.findByTokenJwt(token)).thenReturn(Optional.of(s));

        boolean ok = sessaoService.sessaoValida(token);

        assertThat(ok).isFalse();
    }

    @Test
    void sessaoValida_WhenSessionInactive_ReturnsFalse() {
        String token = "inactive-token";
        AuthUsuario u = new AuthUsuario(); u.setId(3L);

        SessaoUsuario s = SessaoUsuario.builder()
                .id(12L)
                .usuario(u)
                .tokenJwt(token)
                .ativo(false)
                .expiraEm(LocalDateTime.now().plusMinutes(10))
                .build();

        when(sessaoRepository.findByTokenJwt(token)).thenReturn(Optional.of(s));

        boolean ok = sessaoService.sessaoValida(token);

        assertThat(ok).isFalse();
    }

    @Test
    void sessaoValida_WhenTokenNotFound_ReturnsFalse() {
        String token = "missing-token";

        when(sessaoRepository.findByTokenJwt(token)).thenReturn(Optional.empty());

        boolean ok = sessaoService.sessaoValida(token);

        assertThat(ok).isFalse();
    }

    @Test
    void revokingSession_doesNotInvalidate_jwtToken() {
        // create a UserDetails and token
        UserDetails user = new User("user@example.com", "x", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtUtil.generateToken(user);

        // prepare SessaoUsuario linked to the same "user"
        AuthUsuario au = new AuthUsuario(); au.setId(1L); au.setEmail("user@example.com");

        SessaoUsuario s = SessaoUsuario.builder()
                .id(1L)
                .usuario(au)
                .tokenJwt(token)
                .ativo(true)
                .build();

        when(sessaoRepository.findById(1L)).thenReturn(Optional.of(s));

        // revoke the session
        sessaoService.revogarSessao(1L, 1L);

        // session should be inactive now
        assertThat(s.getAtivo()).isFalse();

        // but the JWT validation still relies only on signature & expiry (no DB lookup)
        boolean valid = jwtUtil.validateToken(token, user);
        assertThat(valid).isTrue();
    }

}
