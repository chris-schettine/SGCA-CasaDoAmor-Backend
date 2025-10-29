package br.com.casadoamor.sgca.service.auth;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import br.com.casadoamor.sgca.dto.auth.request.RegisterRequestDTO;
import br.com.casadoamor.sgca.dto.auth.response.AuthResponseDTO;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import br.com.casadoamor.sgca.security.JwtUtil;

class AuthServiceTest {

    @Mock
    private AuthUsuarioRepository authUsuarioRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    // other dependencies mocked as no-op for register tests
    @Mock private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Mock private br.com.casadoamor.sgca.service.admin.AuditoriaService auditoriaService;
    @Mock private RecuperacaoSenhaService recuperacaoSenhaService;
    @Mock private br.com.casadoamor.sgca.service.admin.SessaoService sessaoService;
    @Mock private br.com.casadoamor.sgca.service.auth.HistoricoSenhaService historicoSenhaService;
    @Mock private TwoFactorService twoFactorService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_Success_ReturnsAuthResponse() {
        RegisterRequestDTO req = new RegisterRequestDTO();
        req.setNome("Teste");
        req.setEmail("t@example.com");
        req.setCpf("123.456.789-00");
        req.setSenha("Str0ngP@ss!");

        when(authUsuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(authUsuarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        AuthUsuario saved = AuthUsuario.builder()
                .id(42L)
                .nome(req.getNome())
                .email(req.getEmail())
                .cpf("12345678900")
                .senhaHash("hashed")
                .build();

        when(authUsuarioRepository.save(any(AuthUsuario.class))).thenReturn(saved);
        when(jwtUtil.generateToken(any())).thenReturn("jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        AuthResponseDTO resp = authService.register(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.getEmail()).isEqualTo(req.getEmail());
        assertThat(resp.getNome()).isEqualTo(req.getNome());
    }

    @Test
    void register_DuplicateEmail_ThrowsRuntimeException() {
        RegisterRequestDTO req = new RegisterRequestDTO();
        req.setNome("Teste");
        req.setEmail("t@example.com");
        req.setCpf("123.456.789-00");
        req.setSenha("pwd");

        AuthUsuario existing = new AuthUsuario(); existing.setId(1L);
        when(authUsuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(existing));

    RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
    assertThat(ex.getMessage()).isNotBlank();
    }
}
