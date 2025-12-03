package br.com.casadoamor.sgca.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.casadoamor.sgca.infra.security.JwtUtil;
import br.com.casadoamor.sgca.modules.admin.dtos.user.UserResponseDTO;
import br.com.casadoamor.sgca.modules.admin.entity.Perfil;
import br.com.casadoamor.sgca.modules.admin.entity.Permissao;
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
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioDadosPessoais;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioEndereco;
import br.com.casadoamor.sgca.modules.auth.entity.TokenRecuperacao;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.TipoUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.common.dto.MessageResponseDTO;
import br.com.casadoamor.sgca.modules.common.enums.TipoToken;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUsuarioRepository authUsuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private RecuperacaoSenhaService recuperacaoSenhaService;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private HistoricoSenhaService historicoSenhaService;

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private AuthUsuario usuarioMock;

    @BeforeEach
    void setup() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setNome("João Silva");
        registerRequest.setEmail("joao@example.com");
        registerRequest.setCpf("123.456.789-00");
        registerRequest.setSenha("Senh@Forte123");
        registerRequest.setTelefone("+5511999999999");
        registerRequest.setTipo("RECEPCIONISTA");

        loginRequest = new LoginRequestDTO();
        loginRequest.setCpf("123.456.789-00");
        loginRequest.setSenha("Senh@Forte123");

        usuarioMock = AuthUsuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@example.com")
                .cpf("12345678900")
                .senhaHash("$2a$10$hashedpassword")
                .telefone("+5511999999999")
                .ativo(true)
                .emailVerificado(true)
                .senhaTemporaria(false)
                .tentativasFalhasDeLogin(0)
                .tipo(TipoUsuario.RECEPCIONISTA)
                .perfis(new HashSet<>())
                .build();

        // Use lenient() for stubs that may not be used in all tests
        lenient().when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(httpRequest.getHeader("User-Agent")).thenReturn("Test Browser");
        lenient().when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
    }

    // ===================== TESTES DE REGISTRO =====================

    @Test
    void register_Success_ReturnsAuthResponse() {
        when(authUsuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(authUsuarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(authUsuarioRepository.save(any(AuthUsuario.class))).thenReturn(usuarioMock);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        AuthResponseDTO response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getEmail()).isEqualTo("joao@example.com");
        assertThat(response.getNome()).isEqualTo("João Silva");
        assertThat(response.getTipoUsuario()).isEqualTo("RECEPCIONISTA");
        verify(authUsuarioRepository).save(any(AuthUsuario.class));
        verify(historicoSenhaService).salvarHistorico(any(), anyString());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        AuthUsuario existingUser = new AuthUsuario();
        existingUser.setEmail("joao@example.com");
        when(authUsuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(authUsuarioRepository, never()).save(any(AuthUsuario.class));
    }

    @Test
    void register_CpfAlreadyExists_ThrowsException() {
        when(authUsuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        AuthUsuario existingUser = new AuthUsuario();
        existingUser.setCpf("12345678900");
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(authUsuarioRepository, never()).save(any(AuthUsuario.class));
    }

    @Test
    void register_InvalidPassword_ThrowsException() {
        registerRequest.setSenha("weak");

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(authUsuarioRepository, never()).save(any(AuthUsuario.class));
    }

    @Test
    void register_DefaultsTipoRecepcionistaWhenInvalidTipo() {
        registerRequest.setTipo("INVALID_TIPO");
        when(authUsuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(authUsuarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(authUsuarioRepository.save(any(AuthUsuario.class))).thenAnswer(invocation -> {
            AuthUsuario user = invocation.getArgument(0);
            assertThat(user.getTipo()).isEqualTo(TipoUsuario.RECEPCIONISTA);
            return usuarioMock;
        });
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        authService.register(registerRequest);

        verify(authUsuarioRepository).save(any(AuthUsuario.class));
    }

    @Test
    void register_NullTipo_DefaultsToRecepcionista() {
        registerRequest.setTipo(null);
        when(authUsuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(authUsuarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(authUsuarioRepository.save(any(AuthUsuario.class))).thenAnswer(invocation -> {
            AuthUsuario user = invocation.getArgument(0);
            assertThat(user.getTipo()).isEqualTo(TipoUsuario.RECEPCIONISTA);
            return usuarioMock;
        });
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        authService.register(registerRequest);

        verify(authUsuarioRepository).save(any(AuthUsuario.class));
    }

    @Test
    void register_EmptyTipo_DefaultsToRecepcionista() {
        registerRequest.setTipo("");
        when(authUsuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(authUsuarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(authUsuarioRepository.save(any(AuthUsuario.class))).thenAnswer(invocation -> {
            AuthUsuario user = invocation.getArgument(0);
            assertThat(user.getTipo()).isEqualTo(TipoUsuario.RECEPCIONISTA);
            return usuarioMock;
        });
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        authService.register(registerRequest);

        verify(authUsuarioRepository).save(any(AuthUsuario.class));
    }

    @Test
    void register_ValidTipoMedico_SetsMedico() {
        registerRequest.setTipo("MEDICO");
        when(authUsuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(authUsuarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        
        AuthUsuario medicoMock = AuthUsuario.builder()
                .id(2L)
                .nome("João Silva")
                .email("joao@example.com")
                .cpf("12345678900")
                .senhaHash("$2a$10$hashedpassword")
                .tipo(TipoUsuario.MEDICO)
                .perfis(new HashSet<>())
                .build();
        
        when(authUsuarioRepository.save(any(AuthUsuario.class))).thenReturn(medicoMock);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        AuthResponseDTO response = authService.register(registerRequest);

        assertThat(response.getTipoUsuario()).isEqualTo("MEDICO");
        verify(authUsuarioRepository).save(any(AuthUsuario.class));
    }

    // ===================== TESTES DE LOGIN =====================

    @Test
    void login_Success_ReturnsAuthResponse() {
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(twoFactorService.usuario2FAHabilitado(any())).thenReturn(false);

        UserDetails userDetails = User.builder()
                .username("12345678900")
                .password("$2a$10$hashedpassword")
                .authorities("ROLE_RECEPCIONISTA")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        AuthResponseDTO response = authService.login(loginRequest, httpRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRequires2FA()).isFalse();
        verify(auditoriaService).registrarLoginSucesso(any(), anyString(), anyString());
        verify(sessaoService).criarSessao(any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void login_SuccessResetsFailedAttempts() {
        usuarioMock.setTentativasFalhasDeLogin(3);
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(twoFactorService.usuario2FAHabilitado(any())).thenReturn(false);

        UserDetails userDetails = User.builder()
                .username("12345678900")
                .password("$2a$10$hashedpassword")
                .authorities("ROLE_RECEPCIONISTA")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        authService.login(loginRequest, httpRequest);

        // Should reset failed attempts
        assertThat(usuarioMock.getTentativasFalhasDeLogin()).isEqualTo(0);
        // save is called twice: once for resetting failed attempts and once for updating last login
        verify(authUsuarioRepository, org.mockito.Mockito.atLeastOnce()).save(usuarioMock);
    }

    @Test
    void login_AccountBlocked_ThrowsException() {
        when(auditoriaService.verificarBloqueio("12345678900")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("bloqueada temporariamente");

        verify(auditoriaService).registrarLoginFalha(anyString(), anyString(), anyString(), anyString());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_InvalidCpf_ThrowsException() {
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Credenciais inválidas");

        verify(auditoriaService).registrarLoginFalha(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_InactiveUser_ThrowsException() {
        usuarioMock.setAtivo(false);
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));

        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta inativa");

        verify(auditoriaService).registrarLoginFalha(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_LockedAccount_ThrowsException() {
        usuarioMock.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));

        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta bloqueada até");

        verify(auditoriaService).registrarLoginFalha(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_ExpiredLock_AllowsLogin() {
        usuarioMock.setLockedUntil(LocalDateTime.now().minusMinutes(1)); // Lock expired
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(twoFactorService.usuario2FAHabilitado(any())).thenReturn(false);

        UserDetails userDetails = User.builder()
                .username("12345678900")
                .password("$2a$10$hashedpassword")
                .authorities("ROLE_RECEPCIONISTA")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        AuthResponseDTO response = authService.login(loginRequest, httpRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_TemporaryPasswordNotActivated_ThrowsException() {
        usuarioMock.setSenhaTemporaria(true);
        usuarioMock.setEmailVerificado(false);
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));

        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ativar sua conta primeiro");

        verify(auditoriaService).registrarLoginFalha(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_With2FAEnabled_ReturnsResponseRequiring2FA() {
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(twoFactorService.usuario2FAHabilitado(1L)).thenReturn(true);

        UserDetails userDetails = User.builder()
                .username("12345678900")
                .password("$2a$10$hashedpassword")
                .authorities("ROLE_RECEPCIONISTA")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        AuthResponseDTO response = authService.login(loginRequest, httpRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNull();
        assertThat(response.getRequires2FA()).isTrue();
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(twoFactorService).enviarCodigoLogin(1L);
    }

    @Test
    void login_2FACodeSendFailure_ThrowsException() {
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(twoFactorService.usuario2FAHabilitado(1L)).thenReturn(true);

        UserDetails userDetails = User.builder()
                .username("12345678900")
                .password("$2a$10$hashedpassword")
                .authorities("ROLE_RECEPCIONISTA")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(twoFactorService.usuario2FAHabilitado(1L)).thenReturn(true);
        
        // Simulate 2FA code send failure
        // Note: The inner try/catch rethrows with "Erro ao enviar código de verificação" 
        // but the outer catch block catches it and throws "Credenciais inválidas"
        org.mockito.Mockito.doThrow(new RuntimeException("Email server error"))
                .when(twoFactorService).enviarCodigoLogin(1L);

        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    void login_WrongPassword_IncrementsFailedAttempts() {
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Credenciais inválidas");

        verify(authUsuarioRepository).save(usuarioMock);
        assertThat(usuarioMock.getTentativasFalhasDeLogin()).isEqualTo(1);
        verify(auditoriaService).registrarLoginFalha(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_FifthFailedAttempt_LocksAccount() {
        usuarioMock.setTentativasFalhasDeLogin(4);
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest))
                .isInstanceOf(RuntimeException.class);

        assertThat(usuarioMock.getTentativasFalhasDeLogin()).isEqualTo(5);
        assertThat(usuarioMock.getLockedUntil()).isNotNull();
        verify(authUsuarioRepository).save(usuarioMock);
    }

    @Test
    void login_WithXForwardedForHeader_UsesForwardedIp() {
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(twoFactorService.usuario2FAHabilitado(any())).thenReturn(false);

        UserDetails userDetails = User.builder()
                .username("12345678900")
                .password("$2a$10$hashedpassword")
                .authorities("ROLE_RECEPCIONISTA")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        authService.login(loginRequest, httpRequest);

        verify(auditoriaService).registrarLoginSucesso(any(), anyString(), anyString());
    }

    @Test
    void login_WithNullUserAgent_UsesUnknown() {
        when(httpRequest.getHeader("User-Agent")).thenReturn(null);
        when(auditoriaService.verificarBloqueio(anyString())).thenReturn(false);
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(twoFactorService.usuario2FAHabilitado(any())).thenReturn(false);

        UserDetails userDetails = User.builder()
                .username("12345678900")
                .password("$2a$10$hashedpassword")
                .authorities("ROLE_RECEPCIONISTA")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        authService.login(loginRequest, httpRequest);

        verify(auditoriaService).registrarLoginSucesso(any(), anyString(), anyString());
    }

    // ===================== TESTES DE FORGOT PASSWORD =====================

    @Test
    void forgotPassword_ExistingEmail_SendsRecoveryEmail() {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("joao@example.com");

        when(authUsuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuarioMock));
        when(recuperacaoSenhaService.gerarTokenRecuperacao(usuarioMock)).thenReturn("recovery-token");

        MessageResponseDTO response = authService.forgotPassword(request);

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        verify(recuperacaoSenhaService).gerarTokenRecuperacao(usuarioMock);
        verify(recuperacaoSenhaService).enviarEmailRecuperacao(usuarioMock, "recovery-token");
    }

    @Test
    void forgotPassword_NonExistingEmail_ReturnsSuccessForSecurity() {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("naoexiste@example.com");

        when(authUsuarioRepository.findByEmail("naoexiste@example.com")).thenReturn(Optional.empty());

        MessageResponseDTO response = authService.forgotPassword(request);

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        verify(recuperacaoSenhaService, never()).gerarTokenRecuperacao(any());
    }

    // ===================== TESTES DE RESET PASSWORD =====================

    @Test
    void resetPassword_ValidToken_ResetsPassword() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("valid-token");
        request.setNovaSenha("N3wP@ss123!");

        TokenRecuperacao tokenRecuperacao = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuarioMock)
                .tokenHash("hashed-token")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(1))
                .build();

        when(recuperacaoSenhaService.validarToken("valid-token", TipoToken.RECUPERACAO_SENHA))
                .thenReturn(Optional.of(tokenRecuperacao));
        when(historicoSenhaService.senhaJaUsada(any(), anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newhashedpassword");

        MessageResponseDTO response = authService.resetPassword(request);

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        verify(authUsuarioRepository).save(usuarioMock);
        verify(historicoSenhaService).salvarHistorico(any(), anyString());
        verify(recuperacaoSenhaService).marcarTokenComoUsado(tokenRecuperacao);
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("invalid-token");
        request.setNovaSenha("N3wP@ss123!");

        when(recuperacaoSenhaService.validarToken("invalid-token", TipoToken.RECUPERACAO_SENHA))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token inválido ou expirado");
    }

    @Test
    void resetPassword_WeakPassword_ThrowsException() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("valid-token");
        request.setNovaSenha("weak");

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resetPassword_PasswordAlreadyUsed_ThrowsException() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("valid-token");
        request.setNovaSenha("N3wP@ss123!");

        TokenRecuperacao tokenRecuperacao = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuarioMock)
                .tokenHash("hashed-token")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(1))
                .build();

        when(recuperacaoSenhaService.validarToken("valid-token", TipoToken.RECUPERACAO_SENHA))
                .thenReturn(Optional.of(tokenRecuperacao));
        when(historicoSenhaService.senhaJaUsada(1L, "N3wP@ss123!")).thenReturn(true);

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já foi utilizada recentemente");
    }

    // ===================== TESTES DE VERIFY EMAIL =====================

    @Test
    void verifyEmail_ValidToken_VerifiesEmail() {
        VerifyEmailRequestDTO request = new VerifyEmailRequestDTO();
        request.setToken("email-verify-token");

        TokenRecuperacao tokenRecuperacao = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuarioMock)
                .tokenHash("hashed-token")
                .tipo(TipoToken.VERIFICACAO_EMAIL)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(1))
                .build();

        when(recuperacaoSenhaService.validarToken("email-verify-token", TipoToken.VERIFICACAO_EMAIL))
                .thenReturn(Optional.of(tokenRecuperacao));

        MessageResponseDTO response = authService.verifyEmail(request);

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(usuarioMock.getEmailVerificado()).isTrue();
        verify(authUsuarioRepository).save(usuarioMock);
        verify(recuperacaoSenhaService).marcarTokenComoUsado(tokenRecuperacao);
    }

    @Test
    void verifyEmail_InvalidToken_ThrowsException() {
        VerifyEmailRequestDTO request = new VerifyEmailRequestDTO();
        request.setToken("invalid-token");

        when(recuperacaoSenhaService.validarToken("invalid-token", TipoToken.VERIFICACAO_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token inválido ou expirado");
    }

    // ===================== TESTES DE ALTERAÇÃO DE SENHA =====================

    @Test
    void changePassword_Success_ReturnsSuccessMessage() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setSenhaAtual("OldP@ss123");
        request.setNovaSenha("N3wP@ss123!");

        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("OldP@ss123", "$2a$10$hashedpassword")).thenReturn(true);
        when(historicoSenhaService.senhaJaUsada(any(), anyString())).thenReturn(false);
        when(passwordEncoder.encode("N3wP@ss123!")).thenReturn("$2a$10$newhashedpassword");

        MessageResponseDTO response = authService.changePassword(request, "12345678900");

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).contains("sucesso");
        verify(authUsuarioRepository).save(usuarioMock);
        verify(historicoSenhaService).salvarHistorico(any(), anyString());
    }

    @Test
    void changePassword_WrongOldPassword_ThrowsException() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setSenhaAtual("WrongP@ss123");
        request.setNovaSenha("N3wP@ss123!");

        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("WrongP@ss123", "$2a$10$hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(request, "12345678900"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Senha atual incorreta");

        verify(authUsuarioRepository, never()).save(any());
    }

    @Test
    void changePassword_PasswordAlreadyUsed_ThrowsException() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setSenhaAtual("OldP@ss123");
        request.setNovaSenha("N3wP@ss123!");

        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("OldP@ss123", "$2a$10$hashedpassword")).thenReturn(true);
        when(historicoSenhaService.senhaJaUsada(1L, "N3wP@ss123!")).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(request, "12345678900"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já foi utilizada recentemente");

        verify(authUsuarioRepository, never()).save(any());
    }

    @Test
    void changePassword_UserNotFound_ThrowsException() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setSenhaAtual("OldP@ss123");
        request.setNovaSenha("N3wP@ss123!");

        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword(request, "12345678900"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    void changePassword_WeakNewPassword_ThrowsException() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setSenhaAtual("OldP@ss123");
        request.setNovaSenha("weak");

        assertThatThrownBy(() -> authService.changePassword(request, "12345678900"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===================== TESTES DE PERFIL =====================

    @Test
    void getUserProfile_Success_ReturnsUserProfile() {
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));

        UserResponseDTO response = authService.getUserProfile("12345678900");

        assertThat(response).isNotNull();
        assertThat(response.getCpf()).isEqualTo("12345678900");
        assertThat(response.getEmail()).isEqualTo("joao@example.com");
        assertThat(response.getNome()).isEqualTo("João Silva");
    }

    @Test
    void getUserProfile_UserNotFound_ThrowsException() {
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserProfile("12345678900"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    void getUserProfile_WithPerfisAndPermissoes_ReturnsFullProfile() {
        Permissao permissao = Permissao.builder()
                .id(1L)
                .nome("VIEW_USERS")
                .descricao("Visualizar usuários")
                .build();
        
        Set<Permissao> permissoes = new HashSet<>();
        permissoes.add(permissao);
        
        Perfil perfil = Perfil.builder()
                .id(1L)
                .nome("ADMIN")
                .descricao("Administrador")
                .deletadoEm(null) // Not deleted
                .permissoes(permissoes)
                .build();
        
        Set<Perfil> perfis = new HashSet<>();
        perfis.add(perfil);
        usuarioMock.setPerfis(perfis);

        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));

        UserResponseDTO response = authService.getUserProfile("12345678900");

        assertThat(response).isNotNull();
        assertThat(response.getPerfis()).isNotEmpty();
        assertThat(response.getPerfis().get(0).getNome()).isEqualTo("ADMIN");
        assertThat(response.getPerfis().get(0).getPermissoes()).isNotEmpty();
    }

    @Test
    void getUserProfile_WithDadosPessoaisAndEndereco_ReturnsFullProfile() {
        AuthUsuarioDadosPessoais dadosPessoais = new AuthUsuarioDadosPessoais();
        dadosPessoais.setId(1L);
        dadosPessoais.setNomeMae("Maria Silva");
        dadosPessoais.setProfissao("Engenheiro");
        
        AuthUsuarioEndereco endereco = new AuthUsuarioEndereco();
        endereco.setLogradouro("Rua Teste");
        endereco.setNumero("123");
        endereco.setCidade("São Paulo");
        endereco.setUf("SP");
        endereco.setCep("01234-567");
        
        usuarioMock.setDadosPessoais(dadosPessoais);
        usuarioMock.setEndereco(endereco);

        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));

        UserResponseDTO response = authService.getUserProfile("12345678900");

        assertThat(response).isNotNull();
        assertThat(response.getDadosPessoais()).isNotNull();
        assertThat(response.getDadosPessoais().getNomeMae()).isEqualTo("Maria Silva");
        assertThat(response.getEndereco()).isNotNull();
        assertThat(response.getEndereco().getCidade()).isEqualTo("São Paulo");
    }

    @Test
    void getUserProfile_WithDeletedPerfis_FiltersOut() {
        Perfil perfilAtivo = Perfil.builder()
                .id(1L)
                .nome("ADMIN")
                .descricao("Administrador")
                .deletadoEm(null) // Not deleted
                .permissoes(new HashSet<>())
                .build();
        
        Perfil perfilDeletado = Perfil.builder()
                .id(2L)
                .nome("OLD_ROLE")
                .descricao("Perfil Antigo")
                .deletadoEm(LocalDateTime.now().minusDays(1)) // Deleted
                .permissoes(new HashSet<>())
                .build();
        
        Set<Perfil> perfis = new HashSet<>();
        perfis.add(perfilAtivo);
        perfis.add(perfilDeletado);
        usuarioMock.setPerfis(perfis);

        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));

        UserResponseDTO response = authService.getUserProfile("12345678900");

        assertThat(response).isNotNull();
        assertThat(response.getPerfis()).hasSize(1);
        assertThat(response.getPerfis().get(0).getNome()).isEqualTo("ADMIN");
    }

    @Test
    void findUserIdByCpf_Success_ReturnsUserId() {
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));

        Optional<Long> result = authService.findUserIdByCpf("123.456.789-00");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(1L);
    }

    @Test
    void findUserIdByCpf_NotFound_ReturnsEmpty() {
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        Optional<Long> result = authService.findUserIdByCpf("123.456.789-00");

        assertThat(result).isEmpty();
    }

    @Test
    void buscarIdPorCpf_Success_ReturnsUserId() {
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));

        Long result = authService.buscarIdPorCpf("12345678900");

        assertThat(result).isEqualTo(1L);
    }

    @Test
    void buscarIdPorCpf_NotFound_ThrowsException() {
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.buscarIdPorCpf("12345678900"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    void gerarTokenPorCpf_Success_ReturnsAuthResponse() {
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioMock));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("generated-token");
        when(jwtUtil.getExpirationTime()).thenReturn(3600000L);

        AuthResponseDTO response = authService.gerarTokenPorCpf("12345678900");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("generated-token");
        assertThat(response.getEmail()).isEqualTo("joao@example.com");
        assertThat(response.getNome()).isEqualTo("João Silva");
    }

    @Test
    void gerarTokenPorCpf_UserNotFound_ThrowsException() {
        when(authUsuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.gerarTokenPorCpf("12345678900"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }
}
