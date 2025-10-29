package br.com.casadoamor.sgca.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Base64;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.casadoamor.sgca.service.admin.SessaoService;

class JwtAuthenticationFilterTest {

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private JwtUtil jwtUtil;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);

        jwtUtil = new JwtUtil();
        String rawKey = "01234567890123456789012345678901"; // 32 chars
        String b64 = Base64.getEncoder().encodeToString(rawKey.getBytes());

        Field secretField = JwtUtil.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, b64);

        Field expField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.setLong(jwtUtil, 1000L * 60 * 60); // 1 hour

    // inject jwtUtil into the filter via reflection
        Field jwtField = JwtAuthenticationFilter.class.getDeclaredField("jwtUtil");
        jwtField.setAccessible(true);
        jwtField.set(filter, jwtUtil);

    // inject mocked userDetailsService
        Field udsField = JwtAuthenticationFilter.class.getDeclaredField("userDetailsService");
        udsField.setAccessible(true);
        udsField.set(filter, userDetailsService);

    // inject sessaoService mock into filter
    Field sessField = JwtAuthenticationFilter.class.getDeclaredField("sessaoService");
    sessField.setAccessible(true);
    sessField.set(filter, sessaoService);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void whenSessionRevoked_authIsNotSet() throws Exception {
        // Correct behavior: if the session is revoked (sessaoValida == false),
        // the filter should NOT set authentication.
        UserDetails user = new User("u@example.com", "x", java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtUtil.generateToken(user);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userDetailsService.loadUserByUsername("u@example.com")).thenReturn(user);
        // sessaoService reports false -> session revoked
        when(sessaoService.sessaoValida(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        // Now we expect NO authentication to be present because session is revoked
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void whenSessionActive_authIsSet() throws Exception {
        UserDetails user = new User("u2@example.com", "x", java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtUtil.generateToken(user);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userDetailsService.loadUserByUsername("u2@example.com")).thenReturn(user);
        when(sessaoService.sessaoValida(token)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("u2@example.com");
    }
}
