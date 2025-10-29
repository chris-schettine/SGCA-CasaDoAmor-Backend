package br.com.casadoamor.sgca.service.auth;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.entity.auth.TokenRecuperacao;
import br.com.casadoamor.sgca.enums.TipoToken;
import br.com.casadoamor.sgca.repository.auth.TokenRecuperacaoRepository;
import br.com.casadoamor.sgca.service.common.EmailService;

class RecuperacaoSenhaServiceTest {

    @Mock
    private TokenRecuperacaoRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RecuperacaoSenhaService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void gerarTokenRecuperacao_SavesTokenAndReturnsToken() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        u.setEmail("t@example.com");

        when(tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(anyLong(), any(Boolean.class), any(LocalDateTime.class)))
            .thenReturn(List.of());

        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(i -> i.getArgument(0));

        String token = service.gerarTokenRecuperacao(u);

        assertThat(token).isNotNull();
        assertThat(token.length()).isGreaterThan(0);

        verify(tokenRepository).save(any(TokenRecuperacao.class));
    }

    @Test
    void validarToken_FoundAndValid_ReturnsOptional() throws Exception {
        String token = "abc123token";
        // compute expected hash same as service
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String tokenHash = Base64.getEncoder().encodeToString(hash);

        TokenRecuperacao tr = TokenRecuperacao.builder()
                .id(10L)
                .tokenHash(tokenHash)
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .expiracao(LocalDateTime.now().plusHours(1))
                .usado(false)
                .build();

        when(tokenRepository.findByTokenHashAndTipo(eq(tokenHash), eq(TipoToken.RECUPERACAO_SENHA)))
            .thenReturn(Optional.of(tr));

        Optional<TokenRecuperacao> out = service.validarToken(token, TipoToken.RECUPERACAO_SENHA);

        assertThat(out).isPresent();
        assertThat(out.get().getId()).isEqualTo(10L);
    }

    @Test
    void marcarTokenComoUsado_SetsUsadoAndSaves() {
        TokenRecuperacao tr = TokenRecuperacao.builder()
                .id(5L)
                .usado(false)
                .build();
        br.com.casadoamor.sgca.entity.auth.AuthUsuario u = new br.com.casadoamor.sgca.entity.auth.AuthUsuario();
        u.setEmail("x@example.com");
        tr.setUsuario(u);

        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(i -> i.getArgument(0));

        service.marcarTokenComoUsado(tr);

        assertThat(tr.getUsado()).isTrue();
        assertThat(tr.getUsadoEm()).isNotNull();
        verify(tokenRepository).save(tr);
    }

    @Test
    void enviarEmailRecuperacao_CallsEmailService() {
        AuthUsuario u = new AuthUsuario();
        u.setEmail("t@example.com");
        u.setNome("Nome");

        service.enviarEmailRecuperacao(u, "token-xyz");

        verify(emailService).enviarEmail(eq("t@example.com"), any(String.class), any(String.class));
    }
}
