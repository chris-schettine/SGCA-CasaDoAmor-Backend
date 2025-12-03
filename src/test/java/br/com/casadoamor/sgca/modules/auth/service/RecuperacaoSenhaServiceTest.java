package br.com.casadoamor.sgca.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.TokenRecuperacao;
import br.com.casadoamor.sgca.modules.auth.repository.TokenRecuperacaoRepository;
import br.com.casadoamor.sgca.modules.common.enums.TipoToken;
import br.com.casadoamor.sgca.modules.common.service.EmailService;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    @Mock
    private TokenRecuperacaoRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RecuperacaoSenhaService recuperacaoSenhaService;

    private AuthUsuario usuario;

    @BeforeEach
    void setup() {
        usuario = AuthUsuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@example.com")
                .cpf("12345678900")
                .build();
    }

    // ===================== TESTES DE GERAÇÃO DE TOKEN RECUPERAÇÃO =====================

    @Test
    void gerarTokenRecuperacao_Success_ReturnsToken() {
        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = recuperacaoSenhaService.gerarTokenRecuperacao(usuario);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        verify(tokenRepository).save(any(TokenRecuperacao.class));
    }

    @Test
    void gerarTokenRecuperacao_InvalidatesPreviousTokens() {
        TokenRecuperacao tokenAntigo = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash("old-hash")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(24))
                .build();

        List<TokenRecuperacao> tokensAntigos = new ArrayList<>();
        tokensAntigos.add(tokenAntigo);

        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(Long.class), any(Boolean.class), any(LocalDateTime.class)))
                .thenReturn(tokensAntigos);
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        recuperacaoSenhaService.gerarTokenRecuperacao(usuario);

        assertThat(tokenAntigo.getUsado()).isTrue();
        verify(tokenRepository).save(tokenAntigo);
    }

    @Test
    void gerarTokenRecuperacao_MultipleOldTokens_InvalidatesAll() {
        TokenRecuperacao token1 = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash("old-hash-1")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(24))
                .build();

        TokenRecuperacao token2 = TokenRecuperacao.builder()
                .id(2L)
                .usuario(usuario)
                .tokenHash("old-hash-2")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(12))
                .build();

        List<TokenRecuperacao> tokensAntigos = new ArrayList<>();
        tokensAntigos.add(token1);
        tokensAntigos.add(token2);

        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(Long.class), any(Boolean.class), any(LocalDateTime.class)))
                .thenReturn(tokensAntigos);
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        recuperacaoSenhaService.gerarTokenRecuperacao(usuario);

        assertThat(token1.getUsado()).isTrue();
        assertThat(token2.getUsado()).isTrue();
    }

    // ===================== TESTES DE GERAÇÃO DE TOKEN VERIFICAÇÃO =====================

    @Test
    void gerarTokenVerificacao_Success_ReturnsToken() {
        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = recuperacaoSenhaService.gerarTokenVerificacao(usuario);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        verify(tokenRepository).save(any(TokenRecuperacao.class));
    }

    @Test
    void gerarTokenVerificacao_InvalidatesPreviousVerificationTokens() {
        TokenRecuperacao tokenAntigo = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash("old-verification-hash")
                .tipo(TipoToken.VERIFICACAO_EMAIL)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(24))
                .build();

        List<TokenRecuperacao> tokensAntigos = new ArrayList<>();
        tokensAntigos.add(tokenAntigo);

        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(Long.class), any(Boolean.class), any(LocalDateTime.class)))
                .thenReturn(tokensAntigos);
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        recuperacaoSenhaService.gerarTokenVerificacao(usuario);

        assertThat(tokenAntigo.getUsado()).isTrue();
    }

    // ===================== TESTES DE VALIDAÇÃO DE TOKEN =====================

    @Test
    void validarToken_ValidToken_ReturnsToken() {
        String tokenOriginal = "valid-token-123";
        TokenRecuperacao tokenRecuperacao = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash("hashed-token")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.findByTokenHashAndTipo(anyString(), any(TipoToken.class)))
                .thenReturn(Optional.of(tokenRecuperacao));

        Optional<TokenRecuperacao> result = recuperacaoSenhaService.validarToken(tokenOriginal,
                TipoToken.RECUPERACAO_SENHA);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(tokenRecuperacao);
    }

    @Test
    void validarToken_TokenNotFound_ReturnsEmpty() {
        when(tokenRepository.findByTokenHashAndTipo(anyString(), any(TipoToken.class)))
                .thenReturn(Optional.empty());

        Optional<TokenRecuperacao> result = recuperacaoSenhaService.validarToken("invalid-token",
                TipoToken.RECUPERACAO_SENHA);

        assertThat(result).isEmpty();
    }

    @Test
    void validarToken_ExpiredToken_ReturnsEmpty() {
        TokenRecuperacao tokenExpirado = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash("hashed-token")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(false)
                .expiracao(LocalDateTime.now().minusHours(1))
                .build();

        when(tokenRepository.findByTokenHashAndTipo(anyString(), any(TipoToken.class)))
                .thenReturn(Optional.of(tokenExpirado));

        Optional<TokenRecuperacao> result = recuperacaoSenhaService.validarToken("expired-token",
                TipoToken.RECUPERACAO_SENHA);

        assertThat(result).isEmpty();
    }

    @Test
    void validarToken_UsedToken_ReturnsEmpty() {
        TokenRecuperacao tokenUsado = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash("hashed-token")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(true)
                .usadoEm(LocalDateTime.now().minusMinutes(10))
                .expiracao(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.findByTokenHashAndTipo(anyString(), any(TipoToken.class)))
                .thenReturn(Optional.of(tokenUsado));

        Optional<TokenRecuperacao> result = recuperacaoSenhaService.validarToken("used-token",
                TipoToken.RECUPERACAO_SENHA);

        assertThat(result).isEmpty();
    }

    @Test
    void validarToken_ValidVerificationToken_ReturnsToken() {
        String tokenOriginal = "verify-token-123";
        TokenRecuperacao tokenVerificacao = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash("hashed-verification-token")
                .tipo(TipoToken.VERIFICACAO_EMAIL)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.findByTokenHashAndTipo(anyString(), any(TipoToken.class)))
                .thenReturn(Optional.of(tokenVerificacao));

        Optional<TokenRecuperacao> result = recuperacaoSenhaService.validarToken(tokenOriginal,
                TipoToken.VERIFICACAO_EMAIL);

        assertThat(result).isPresent();
        assertThat(result.get().getTipo()).isEqualTo(TipoToken.VERIFICACAO_EMAIL);
    }

    // ===================== TESTES DE MARCAÇÃO DE USO =====================

    @Test
    void marcarTokenComoUsado_Success_MarksAsUsed() {
        TokenRecuperacao token = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash("hashed-token")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.save(any(TokenRecuperacao.class))).thenReturn(token);

        recuperacaoSenhaService.marcarTokenComoUsado(token);

        assertThat(token.getUsado()).isTrue();
        assertThat(token.getUsadoEm()).isNotNull();
        verify(tokenRepository).save(token);
    }

    @Test
    void marcarTokenComoUsado_SetsUsadoEmTimestamp() {
        TokenRecuperacao token = TokenRecuperacao.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash("hashed-token")
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .usado(false)
                .expiracao(LocalDateTime.now().plusHours(1))
                .build();

        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);
        
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenReturn(token);

        recuperacaoSenhaService.marcarTokenComoUsado(token);

        LocalDateTime depois = LocalDateTime.now().plusSeconds(1);
        
        assertThat(token.getUsadoEm()).isAfter(antes);
        assertThat(token.getUsadoEm()).isBefore(depois);
    }

    // ===================== TESTES DE ENVIO DE EMAIL =====================

    @Test
    void enviarEmailRecuperacao_Success_SendsEmail() {
        String token = "recovery-token-123";

        recuperacaoSenhaService.enviarEmailRecuperacao(usuario, token);

        verify(emailService).enviarEmail(
                anyString(), // email
                anyString(), // assunto
                anyString() // mensagem
        );
    }

    @Test
    void enviarEmailRecuperacao_ContainsCorrectData() {
        String token = "recovery-token-123";

        recuperacaoSenhaService.enviarEmailRecuperacao(usuario, token);

        verify(emailService).enviarEmail(
                org.mockito.ArgumentMatchers.eq("joao@example.com"),
                org.mockito.ArgumentMatchers.contains("Recuperação de Senha"),
                org.mockito.ArgumentMatchers.argThat(msg -> 
                    msg.contains("João Silva") && 
                    msg.contains(token) &&
                    msg.contains("reset-password"))
        );
    }

    @Test
    void enviarEmailVerificacao_Success_SendsEmail() {
        String token = "verification-token-123";

        recuperacaoSenhaService.enviarEmailVerificacao(usuario, token);

        verify(emailService).enviarEmail(
                anyString(), // email
                anyString(), // assunto
                anyString() // mensagem
        );
    }

    @Test
    void enviarEmailVerificacao_ContainsCorrectData() {
        String token = "verification-token-123";

        recuperacaoSenhaService.enviarEmailVerificacao(usuario, token);

        verify(emailService).enviarEmail(
                org.mockito.ArgumentMatchers.eq("joao@example.com"),
                org.mockito.ArgumentMatchers.contains("Verificação de Email"),
                org.mockito.ArgumentMatchers.argThat(msg -> 
                    msg.contains("João Silva") && 
                    msg.contains(token) &&
                    msg.contains("verify-email"))
        );
    }

    // ===================== TESTES DE LIMPEZA =====================

    @Test
    void limparTokensExpirados_Success_DeletesExpiredTokens() {
        recuperacaoSenhaService.limparTokensExpirados();

        verify(tokenRepository).deleteByExpiracaoBefore(any(LocalDateTime.class));
    }

    // ===================== TESTES DE GERAÇÃO DE TOKEN SEGURO =====================

    @Test
    void gerarTokenRecuperacao_GeneratesUniqueTokens() {
        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token1 = recuperacaoSenhaService.gerarTokenRecuperacao(usuario);
        String token2 = recuperacaoSenhaService.gerarTokenRecuperacao(usuario);

        // Tokens should be different (cryptographically random)
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void gerarTokenRecuperacao_TokenHasCorrectLength() {
        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = recuperacaoSenhaService.gerarTokenRecuperacao(usuario);

        // Base64 URL encoded 32 bytes = approximately 43 characters
        assertThat(token.length()).isGreaterThanOrEqualTo(40);
    }

    // ===================== TESTES DE HASH DE TOKEN =====================

    @Test
    void validarToken_HashesTokenBeforeSearch() {
        String tokenOriginal = "test-token-123";
        
        when(tokenRepository.findByTokenHashAndTipo(anyString(), any(TipoToken.class)))
                .thenReturn(Optional.empty());

        recuperacaoSenhaService.validarToken(tokenOriginal, TipoToken.RECUPERACAO_SENHA);

        // Verify that the search was done with a hashed version (not the original)
        verify(tokenRepository).findByTokenHashAndTipo(
                org.mockito.ArgumentMatchers.argThat(hash -> !hash.equals(tokenOriginal)),
                any(TipoToken.class)
        );
    }

    @Test
    void gerarTokenRecuperacao_StoresHashedToken() {
        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> {
            TokenRecuperacao saved = invocation.getArgument(0);
            // The stored hash should be different from any plain token
            assertThat(saved.getTokenHash()).isNotNull();
            assertThat(saved.getTokenHash().length()).isGreaterThan(20);
            return saved;
        });

        String token = recuperacaoSenhaService.gerarTokenRecuperacao(usuario);

        // The returned token should be the original (not hashed)
        assertThat(token).isNotNull();
        verify(tokenRepository).save(any(TokenRecuperacao.class));
    }

    // ===================== TESTES DE EXPIRAÇÃO =====================

    @Test
    void gerarTokenRecuperacao_SetsCorrectExpiration() {
        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        
        LocalDateTime antes = LocalDateTime.now().plusHours(24).minusMinutes(1);
        LocalDateTime depois = LocalDateTime.now().plusHours(24).plusMinutes(1);
        
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> {
            TokenRecuperacao saved = invocation.getArgument(0);
            assertThat(saved.getExpiracao()).isAfter(antes);
            assertThat(saved.getExpiracao()).isBefore(depois);
            assertThat(saved.getTipo()).isEqualTo(TipoToken.RECUPERACAO_SENHA);
            return saved;
        });

        recuperacaoSenhaService.gerarTokenRecuperacao(usuario);

        verify(tokenRepository).save(any(TokenRecuperacao.class));
    }

    @Test
    void gerarTokenVerificacao_SetsCorrectExpiration() {
        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        
        LocalDateTime antes = LocalDateTime.now().plusHours(24).minusMinutes(1);
        LocalDateTime depois = LocalDateTime.now().plusHours(24).plusMinutes(1);
        
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(invocation -> {
            TokenRecuperacao saved = invocation.getArgument(0);
            assertThat(saved.getExpiracao()).isAfter(antes);
            assertThat(saved.getExpiracao()).isBefore(depois);
            assertThat(saved.getTipo()).isEqualTo(TipoToken.VERIFICACAO_EMAIL);
            return saved;
        });

        recuperacaoSenhaService.gerarTokenVerificacao(usuario);

        verify(tokenRepository).save(any(TokenRecuperacao.class));
    }
}
