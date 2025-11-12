package br.com.casadoamor.sgca.service.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.casadoamor.sgca.modules.admin.dtos.user.UserResponseDTO;
import br.com.casadoamor.sgca.infra.security.JwtUtil;
import br.com.casadoamor.sgca.modules.admin.service.AuditoriaService;
import br.com.casadoamor.sgca.modules.admin.service.SessaoService;
import br.com.casadoamor.sgca.modules.auth.dtos.request.ChangePasswordRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.ForgotPasswordRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.LoginRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.RegisterRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.ResetPasswordRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.VerifyEmailRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.response.AuthResponseDTO;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.TokenRecuperacao;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.auth.service.AuthService;
import br.com.casadoamor.sgca.modules.auth.service.HistoricoSenhaService;
import br.com.casadoamor.sgca.modules.auth.service.RecuperacaoSenhaService;
import br.com.casadoamor.sgca.modules.auth.service.TwoFactorService;
import br.com.casadoamor.sgca.modules.common.dto.MessageResponseDTO;
import br.com.casadoamor.sgca.modules.common.enums.TipoToken;
import jakarta.servlet.http.HttpServletRequest;

class AuthServiceTest {

    @Mock
    private AuthUsuarioRepository authUsuarioRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    // other dependencies mocked as no-op for register tests
    @Mock private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Mock private AuditoriaService auditoriaService;
    @Mock private RecuperacaoSenhaService recuperacaoSenhaService;
    @Mock private SessaoService sessaoService;
    @Mock private HistoricoSenhaService historicoSenhaService;
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

    @Test
    void login_TwoFactorDisabled_ReturnsTokenAndRegistersSession() {
    LoginRequestDTO request = new LoginRequestDTO("12345678901", "Str0ngP@ss!");
    HttpServletRequest httpRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
    when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
    when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
    when(httpRequest.getHeader("User-Agent")).thenReturn("JUnit");

    AuthUsuario usuario = AuthUsuario.builder()
        .id(10L)
        .cpf("12345678901")
        .email("user@example.com")
        .nome("User")
        .senhaHash("hashed")
        .tentativasFalhasDeLogin(2)
        .ativo(true)
        .emailVerificado(true)
        .build();

    when(auditoriaService.verificarBloqueio("12345678901")).thenReturn(false);
    when(authUsuarioRepository.findByCpf("12345678901")).thenReturn(Optional.of(usuario));
    when(authUsuarioRepository.save(any(AuthUsuario.class))).thenAnswer(i -> i.getArgument(0));

    UserDetails principal = User.builder()
        .username("12345678901")
        .password("hashed")
        .authorities("ROLE_RECEPCIONISTA")
        .build();
    Authentication authentication = new UsernamePasswordAuthenticationToken(principal, request.getSenha(), principal.getAuthorities());
    when(authenticationManager.authenticate(any())).thenReturn(authentication);

    when(twoFactorService.usuario2FAHabilitado(10L)).thenReturn(false);
    when(jwtUtil.generateToken(principal)).thenReturn("jwt-token");
    when(jwtUtil.getExpirationTime()).thenReturn(3_600_000L);
    when(sessaoService.criarSessao(eq(usuario), eq("jwt-token"), eq("127.0.0.1"), eq("JUnit"), any(LocalDateTime.class)))
        .thenReturn(null);

    AuthResponseDTO response = authService.login(request, httpRequest);

    assertThat(response.getToken()).isEqualTo("jwt-token");
    assertThat(response.getRequires2FA()).isFalse();
    assertThat(usuario.getTentativasFalhasDeLogin()).isZero();
    verify(sessaoService).criarSessao(eq(usuario), eq("jwt-token"), eq("127.0.0.1"), eq("JUnit"), any(LocalDateTime.class));
    verify(auditoriaService).registrarLoginSucesso(eq(usuario), eq("127.0.0.1"), eq("JUnit"));
    }

    @Test
    void login_TwoFactorRequired_ReturnsChallenge() {
    LoginRequestDTO request = new LoginRequestDTO("22233344455", "Str0ngP@ss!");
    HttpServletRequest httpRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
    when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("200.200.200.200");
    when(httpRequest.getHeader("User-Agent")).thenReturn("JUnit");

    AuthUsuario usuario = AuthUsuario.builder()
        .id(25L)
        .cpf("22233344455")
        .email("twofactor@example.com")
        .nome("Two Factor")
        .senhaHash("hashed")
        .ativo(true)
        .emailVerificado(true)
        .build();

    when(auditoriaService.verificarBloqueio("22233344455")).thenReturn(false);
    when(authUsuarioRepository.findByCpf("22233344455")).thenReturn(Optional.of(usuario));

    UserDetails principal = User.builder()
        .username("22233344455")
        .password("hashed")
        .authorities("ROLE_RECEPCIONISTA")
        .build();
    Authentication authentication = new UsernamePasswordAuthenticationToken(principal, request.getSenha(), principal.getAuthorities());
    when(authenticationManager.authenticate(any())).thenReturn(authentication);

    when(twoFactorService.usuario2FAHabilitado(25L)).thenReturn(true);

    AuthResponseDTO response = authService.login(request, httpRequest);

    assertThat(response.getRequires2FA()).isTrue();
    assertThat(response.getToken()).isNull();
    assertThat(response.getUserId()).isEqualTo(25L);
    verify(twoFactorService).enviarCodigoLogin(25L);
    verify(sessaoService, never()).criarSessao(any(), anyString(), anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    void login_InvalidCredentials_IncrementsAttemptAndThrows() {
    LoginRequestDTO request = new LoginRequestDTO("33322211100", "Wrong!");
    HttpServletRequest httpRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
    when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
    when(httpRequest.getRemoteAddr()).thenReturn("10.0.0.1");
    when(httpRequest.getHeader("User-Agent")).thenReturn("JUnit");

    AuthUsuario usuario = AuthUsuario.builder()
        .id(7L)
        .cpf("33322211100")
        .email("fail@example.com")
        .senhaHash("hashed")
        .tentativasFalhasDeLogin(0)
        .ativo(true)
        .emailVerificado(true)
        .build();

    when(auditoriaService.verificarBloqueio("33322211100")).thenReturn(false);
    when(authUsuarioRepository.findByCpf("33322211100")).thenReturn(Optional.of(usuario));
    when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("bad creds"));
    when(authUsuarioRepository.save(any(AuthUsuario.class))).thenAnswer(i -> i.getArgument(0));

    RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request, httpRequest));

    assertThat(ex.getMessage()).isEqualTo("Credenciais inválidas");
    assertThat(usuario.getTentativasFalhasDeLogin()).isEqualTo(1);
    verify(authUsuarioRepository).save(usuario);
    verify(auditoriaService).registrarLoginFalha(eq("33322211100"), eq("10.0.0.1"), eq("JUnit"), eq("SENHA_INVALIDA"));
    }

    @Test
    void forgotPassword_WhenUserExists_SendsEmail() {
    ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
    request.setEmail("exists@example.com");

    AuthUsuario usuario = AuthUsuario.builder()
        .id(5L)
        .email("exists@example.com")
        .nome("Name")
        .build();

    when(authUsuarioRepository.findByEmail("exists@example.com")).thenReturn(Optional.of(usuario));
    when(recuperacaoSenhaService.gerarTokenRecuperacao(usuario)).thenReturn("token");
    doNothing().when(recuperacaoSenhaService).enviarEmailRecuperacao(usuario, "token");

    MessageResponseDTO response = authService.forgotPassword(request);

    assertThat(response.getSuccess()).isTrue();
    verify(recuperacaoSenhaService).gerarTokenRecuperacao(usuario);
    verify(recuperacaoSenhaService).enviarEmailRecuperacao(usuario, "token");
    }

    @Test
    void forgotPassword_WhenUserMissing_ReturnsGenericSuccess() {
    ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
    request.setEmail("missing@example.com");

    when(authUsuarioRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    MessageResponseDTO response = authService.forgotPassword(request);

    assertThat(response.getSuccess()).isTrue();
    verify(recuperacaoSenhaService, never()).gerarTokenRecuperacao(any());
    }

    @Test
    void changePassword_WhenValid_UpdatesCredentialsAndHistory() {
    ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
    request.setSenhaAtual("OldP@ss1!");
    request.setNovaSenha("N3wP@ss1!");

    AuthUsuario usuario = AuthUsuario.builder()
        .id(12L)
        .cpf("12345678901")
        .senhaHash("old-hash")
        .build();

    when(authUsuarioRepository.findByCpf("12345678901")).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches("OldP@ss1!", "old-hash")).thenReturn(true);
    when(historicoSenhaService.senhaJaUsada(12L, "N3wP@ss1!")).thenReturn(false);
    when(passwordEncoder.encode("N3wP@ss1!")).thenReturn("new-hash");

    MessageResponseDTO response = authService.changePassword(request, "12345678901");

    assertThat(response.getSuccess()).isTrue();
    assertThat(usuario.getSenhaHash()).isEqualTo("new-hash");
    verify(authUsuarioRepository).save(usuario);
    verify(historicoSenhaService).salvarHistorico(12L, "new-hash");
    }

    @Test
    void changePassword_WhenCurrentPasswordIncorrect_Throws() {
    ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
    request.setSenhaAtual("wrong");
    request.setNovaSenha("N3wP@ss1!");

    AuthUsuario usuario = AuthUsuario.builder()
        .id(12L)
        .cpf("12345678901")
        .senhaHash("hash")
        .build();

    when(authUsuarioRepository.findByCpf("12345678901")).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.changePassword(request, "12345678901"));

    assertThat(ex.getMessage()).isEqualTo("Senha atual incorreta");
    verify(authUsuarioRepository, never()).save(any());
    }

    @Test
    void resetPassword_WithValidToken_UpdatesPassword() {
    ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
    request.setToken("token-value");
    request.setNovaSenha("N3wP@ss1!");

    AuthUsuario usuario = AuthUsuario.builder()
        .id(15L)
        .senhaHash("old")
        .tentativasFalhasDeLogin(3)
        .build();

    TokenRecuperacao token = TokenRecuperacao.builder()
        .id(20L)
        .usado(false)
        .usuario(usuario)
        .tipo(TipoToken.RECUPERACAO_SENHA)
        .expiracao(LocalDateTime.now().plusHours(2))
        .build();

    when(recuperacaoSenhaService.validarToken("token-value", TipoToken.RECUPERACAO_SENHA))
        .thenReturn(Optional.of(token));
    when(historicoSenhaService.senhaJaUsada(15L, "N3wP@ss1!")).thenReturn(false);
    when(passwordEncoder.encode("N3wP@ss1!")).thenReturn("new-hash");
    when(authUsuarioRepository.save(usuario)).thenReturn(usuario);

    MessageResponseDTO response = authService.resetPassword(request);

    assertThat(response.getSuccess()).isTrue();
    assertThat(usuario.getSenhaHash()).isEqualTo("new-hash");
    assertThat(usuario.getTentativasFalhasDeLogin()).isZero();
    verify(historicoSenhaService).salvarHistorico(15L, "new-hash");
    verify(recuperacaoSenhaService).marcarTokenComoUsado(token);
    }

    @Test
    void resetPassword_WhenReusingPassword_ThrowsIllegalArgumentException() {
    ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
    request.setToken("token-value");
    request.setNovaSenha("N3wP@ss1!");

    AuthUsuario usuario = AuthUsuario.builder()
        .id(30L)
        .senhaHash("old")
        .build();

    TokenRecuperacao token = TokenRecuperacao.builder()
        .usuario(usuario)
        .tipo(TipoToken.RECUPERACAO_SENHA)
        .expiracao(LocalDateTime.now().plusHours(1))
        .build();

    when(recuperacaoSenhaService.validarToken("token-value", TipoToken.RECUPERACAO_SENHA))
        .thenReturn(Optional.of(token));
    when(historicoSenhaService.senhaJaUsada(30L, "N3wP@ss1!")).thenReturn(true);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));

    assertThat(ex.getMessage()).contains("já foi utilizada");
    verify(authUsuarioRepository, never()).save(any());
    }

    @Test
    void verifyEmail_WithValidToken_MarksUserVerified() {
    VerifyEmailRequestDTO request = new VerifyEmailRequestDTO();
    request.setToken("verify-token");

    AuthUsuario usuario = AuthUsuario.builder()
        .id(40L)
        .emailVerificado(false)
        .build();

    TokenRecuperacao token = TokenRecuperacao.builder()
        .usuario(usuario)
        .usado(false)
        .tipo(TipoToken.VERIFICACAO_EMAIL)
        .expiracao(LocalDateTime.now().plusHours(1))
        .build();

    when(recuperacaoSenhaService.validarToken("verify-token", TipoToken.VERIFICACAO_EMAIL))
        .thenReturn(Optional.of(token));
    when(authUsuarioRepository.save(usuario)).thenReturn(usuario);

    MessageResponseDTO response = authService.verifyEmail(request);

    assertThat(response.getSuccess()).isTrue();
    assertThat(usuario.getEmailVerificado()).isTrue();
    verify(recuperacaoSenhaService).marcarTokenComoUsado(token);
    }

    @Test
    void getUserProfile_WhenUserExists_ReturnsDto() {
    AuthUsuario usuario = AuthUsuario.builder()
        .id(50L)
        .nome("Profile")
        .email("profile@example.com")
        .cpf("999")
        .telefone("123")
        .tipo(AuthUsuario.TipoUsuario.ADMINISTRADOR)
        .ativo(true)
        .emailVerificado(true)
        .build();

    when(authUsuarioRepository.findByCpf("999"))
        .thenReturn(Optional.of(usuario));

    UserResponseDTO dto = authService.getUserProfile("999");

    assertThat(dto.getId()).isEqualTo(50L);
    assertThat(dto.getEmail()).isEqualTo("profile@example.com");
    }
}
