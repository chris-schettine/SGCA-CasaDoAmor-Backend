package br.com.casadoamor.sgca.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import br.com.casadoamor.sgca.security.UserDetailsServiceImpl;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.entity.auth.SessaoUsuario;
import br.com.casadoamor.sgca.repository.auth.SessaoUsuarioRepository;
import br.com.casadoamor.sgca.security.JwtUtil;
import br.com.casadoamor.sgca.security.JwtAuthenticationFilter;

/*
 * Login flow: generate JWT, create session, revoke session and re-check
 * session (DB) and token (JWT) validity. Tests keep assertions concise.
 */
class SessaoLoginFlowTest {

    @Mock
    private SessaoUsuarioRepository sessaoRepository;

    @InjectMocks
    private SessaoService sessaoService;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        jwtUtil = new JwtUtil();
    String rawKey = "01234567890123456789012345678901";
        String b64 = Base64.getEncoder().encodeToString(rawKey.getBytes());

        Field secretField = JwtUtil.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, b64);

        Field expField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.setLong(jwtUtil, 1000L * 60 * 60); // 1 hour

        when(sessaoRepository.save(any(SessaoUsuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void login_then_revoke_session_checks_token_before_and_after() {
    // simulate login -> create token
        UserDetails user = new User("flow.user@example.com", "x", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtUtil.generateToken(user);

        // create corresponding AuthUsuario + SessaoUsuario
        AuthUsuario au = new AuthUsuario();
        au.setId(900L);
        au.setEmail("flow.user@example.com");

        SessaoUsuario sessao = SessaoUsuario.builder()
                .id(9000L)
                .usuario(au)
                .tokenJwt(token)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusHours(1))
                .ipOrigem("127.0.0.1")
                .userAgent("test-agent")
                .ativo(true)
                .build();

    // repository returns the session when looked up by token or id
        when(sessaoRepository.findByTokenJwt(token)).thenReturn(Optional.of(sessao));
        when(sessaoRepository.findById(sessao.getId())).thenReturn(Optional.of(sessao));

    // before revocation: DB session valid and JWT valid
        boolean sessionValidBefore = sessaoService.sessaoValida(token);
        boolean jwtValidBefore = jwtUtil.validateToken(token, user);

        assertThat(sessionValidBefore).as("session should be valid before revocation").isTrue();
        assertThat(jwtValidBefore).as("jwt should be valid before revocation").isTrue();

    // revoke
        sessaoService.revogarSessao(sessao.getId(), au.getId());

        // after revocation: DB session should be marked inactive
        assertThat(sessao.getAtivo()).as("session active flag should be false after revoke").isFalse();

        // DB session should now be invalid
        boolean sessionValidAfter = sessaoService.sessaoValida(token);
        assertThat(sessionValidAfter).as("session validity (DB) should be false after revoke").isFalse();

        // JwtUtil is stateless: token still validates by signature/expiry
        boolean jwtValidAfter = jwtUtil.validateToken(token, user);
        assertThat(jwtValidAfter).as("jwtUtil still validates token after DB revoke").isTrue();

        // sanity: sessaoValida must be false after revoke
        assertThat(sessaoService.sessaoValida(token)).as("precondition: sessaoValida false after revoke").isFalse();

        // Prepare JwtAuthenticationFilter and a mocked UserDetailsService
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        UserDetailsServiceImpl uds = Mockito.mock(UserDetailsServiceImpl.class);
        Mockito.when(uds.loadUserByUsername("flow.user@example.com")).thenReturn(user);

        // inject jwtUtil, userDetailsService and sessaoService into filter via reflection
        try {
            java.lang.reflect.Field fJwt = JwtAuthenticationFilter.class.getDeclaredField("jwtUtil");
            fJwt.setAccessible(true);
            fJwt.set(filter, jwtUtil);

            java.lang.reflect.Field fUds = JwtAuthenticationFilter.class.getDeclaredField("userDetailsService");
            fUds.setAccessible(true);
            fUds.set(filter, uds);

            java.lang.reflect.Field fSess = JwtAuthenticationFilter.class.getDeclaredField("sessaoService");
            fSess.setAccessible(true);
            fSess.set(filter, sessaoService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Build mock request with Authorization header
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        // clear SecurityContext
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        // call filter (expected: no Authentication when session revoked)
        try {
            filter.doFilter(req, res, (request, response) -> {
                // no-op
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // After the filter runs, SecurityContext should have no Authentication
        assertThat(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication())
            .isNull();

        // cleanup
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}
