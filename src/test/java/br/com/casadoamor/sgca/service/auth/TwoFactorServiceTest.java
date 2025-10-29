package br.com.casadoamor.sgca.service.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import br.com.casadoamor.sgca.dto.twofactor.Setup2FADTO;
import br.com.casadoamor.sgca.entity.auth.Autenticacao2FA;
import br.com.casadoamor.sgca.entity.auth.Autenticacao2FARateLimit;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.auth.Autenticacao2FARateLimitRepository;
import br.com.casadoamor.sgca.repository.auth.Autenticacao2FARepository;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import br.com.casadoamor.sgca.service.common.EmailService;

class TwoFactorServiceTest {

    @Mock
    private Autenticacao2FARepository autenticacao2FARepository;

    @Mock
    private Autenticacao2FARateLimitRepository rateLimitRepository;

    @Mock
    private AuthUsuarioRepository authUsuarioRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TwoFactorService twoFactorService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void configurar2FA_NewConfig_SendsEmailAndReturnsSetupDTO() {
        Long userId = 1L;
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(userId);
        usuario.setEmail("usuario@example.com");

        when(authUsuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(autenticacao2FARepository.findByUsuarioId(userId)).thenReturn(Optional.empty());
        when(rateLimitRepository.findByUsuarioId(userId)).thenReturn(Optional.empty());

        Setup2FADTO dto = twoFactorService.configurar2FA(userId);

        // Should return a message containing masked email
        assertThat(dto.getMensagem()).contains("***@example.com");
        verify(autenticacao2FARepository).save(org.mockito.Mockito.any(Autenticacao2FA.class));
    verify(emailService).send2FACode(org.mockito.Mockito.eq("usuario@example.com"), org.mockito.Mockito.anyString());
    }

    @Test
    void validarCodigoLogin_ValidCode_ReturnsTrue() {
        Long userId = 2L;
        Autenticacao2FA config = new Autenticacao2FA();
        config.setUsuarioId(userId);
        config.setHabilitado(true);
        config.setCodigoAtual("123456");
        config.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(5));

        when(autenticacao2FARepository.findByUsuarioId(userId)).thenReturn(Optional.of(config));

        boolean result = twoFactorService.validarCodigoLogin(userId, "123456");

        assertThat(result).isTrue();
        verify(autenticacao2FARepository).save(org.mockito.Mockito.any(Autenticacao2FA.class));
    }

    @Test
    void validarCodigoLogin_InvalidCode_ReturnsFalseAndIncrementsAttempts() {
        Long userId = 3L;
        Autenticacao2FA config = new Autenticacao2FA();
        config.setUsuarioId(userId);
        config.setHabilitado(true);
        config.setCodigoAtual("654321");
        config.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(5));
        config.setTentativasFalhas(0);

        when(autenticacao2FARepository.findByUsuarioId(userId)).thenReturn(Optional.of(config));

        boolean result = twoFactorService.validarCodigoLogin(userId, "000000");

        assertThat(result).isFalse();
        // After invalid attempt, save should be called to persist incremented attempts
        verify(autenticacao2FARepository).save(org.mockito.Mockito.any(Autenticacao2FA.class));
        assertThat(config.getTentativasFalhas()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void validarCodigoLogin_Expired_ThrowsException() {
        Long userId = 4L;
        Autenticacao2FA config = new Autenticacao2FA();
        config.setUsuarioId(userId);
        config.setHabilitado(true);
        config.setCodigoAtual("111111");
        config.setExpiracaoCodigo(LocalDateTime.now().minusMinutes(1));

        when(autenticacao2FARepository.findByUsuarioId(userId)).thenReturn(Optional.of(config));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> twoFactorService.validarCodigoLogin(userId, "111111"));
        assertThat(ex.getMessage()).isNotNull();
    }

    @Test
    void enviarCodigoLogin_NotConfigured_ThrowsException() {
        Long userId = 5L;
        when(autenticacao2FARepository.findByUsuarioId(userId)).thenReturn(Optional.empty());
        when(authUsuarioRepository.findById(userId)).thenReturn(Optional.of(new AuthUsuario()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> twoFactorService.enviarCodigoLogin(userId));
        assertThat(ex.getMessage()).isNotNull();
    }

    @Test
    void configurar2FA_RateLimitExceeded_ThrowsException() {
        Long userId = 6L;
        // rate limit present and blocked
        Autenticacao2FARateLimit rate = new Autenticacao2FARateLimit();
        rate.setUsuarioId(userId);
        rate.setBloqueadoAte(java.time.LocalDateTime.now().plusMinutes(10));

        when(rateLimitRepository.findByUsuarioId(userId)).thenReturn(Optional.of(rate));

        // auth user exists
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(userId);
        usuario.setEmail("u@example.com");
        when(authUsuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        // expect rate limit exception
        RuntimeException ex = assertThrows(RuntimeException.class, () -> twoFactorService.configurar2FA(userId));
        assertThat(ex.getMessage()).isNotNull();
    }

    @Test
    void ativar2FAAutomaticamente_EmailSendFailure_ThrowsException() {
        Long userId = 7L;
        String email = "fail@example.com";

        // rate limit empty -> ok
        when(rateLimitRepository.findByUsuarioId(userId)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.findByUsuarioId(userId)).thenReturn(Optional.empty());

        // simulate email sending failure
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down")).when(emailService).send2FACode(org.mockito.Mockito.eq(email), org.mockito.Mockito.anyString());

    RuntimeException ex = assertThrows(RuntimeException.class, () -> twoFactorService.ativar2FAAutomaticamente(userId, email));
    assertThat(ex.getMessage()).isNotBlank();
    }

}
