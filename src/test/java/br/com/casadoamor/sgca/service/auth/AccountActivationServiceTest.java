package br.com.casadoamor.sgca.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import br.com.casadoamor.sgca.modules.auth.dtos.request.ActivateAccountRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.FirstLoginPasswordChangeDTO;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.TokenRecuperacao;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.auth.repository.TokenRecuperacaoRepository;
import br.com.casadoamor.sgca.modules.auth.service.AccountActivationService;
import br.com.casadoamor.sgca.modules.auth.service.HistoricoSenhaService;
import br.com.casadoamor.sgca.modules.auth.service.TwoFactorService;
import br.com.casadoamor.sgca.modules.common.dto.MessageResponseDTO;
import br.com.casadoamor.sgca.modules.common.enums.TipoToken;
import br.com.casadoamor.sgca.modules.common.service.EmailService;

class AccountActivationServiceTest {

    @Mock
    private AuthUsuarioRepository usuarioRepository;

    @Mock
    private TokenRecuperacaoRepository tokenRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private HistoricoSenhaService historicoSenhaService;

    @Mock
    private TwoFactorService twoFactorService;

    @InjectMocks
    private AccountActivationService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void ativarConta_ComDadosValidos_AtualizaUsuarioEAtiva2FA() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(1L)
                .email("user@example.com")
                .nome("Usuário")
                .senhaHash("hash-temporario")
                .senhaTemporaria(true)
                .emailVerificado(false)
                .ativo(false)
                .build();

        TokenRecuperacao token = TokenRecuperacao.builder()
                .id(10L)
                .usuario(usuario)
                .tokenHash("token-value")
                .tipo(TipoToken.VERIFICACAO_EMAIL)
                .expiracao(LocalDateTime.now().plusHours(2))
                .usado(false)
                .build();

        ActivateAccountRequestDTO request = new ActivateAccountRequestDTO(
                "token-value",
                "user@example.com",
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.findByTokenHashAndTipo("token-value", TipoToken.VERIFICACAO_EMAIL)).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("Temp@123", "hash-temporario")).thenReturn(true);
        when(historicoSenhaService.senhaJaUsada(1L, "N3wP@ss1!")).thenReturn(false);
        when(passwordEncoder.encode("N3wP@ss1!")).thenReturn("hash-novo");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        MessageResponseDTO response = service.ativarConta(request);

        assertThat(response.getSuccess()).isTrue();
        assertThat(usuario.getEmailVerificado()).isTrue();
        assertThat(usuario.getSenhaTemporaria()).isFalse();
        assertThat(usuario.getSenhaHash()).isEqualTo("hash-novo");
        assertThat(token.getUsado()).isTrue();
        verify(historicoSenhaService).salvarHistorico(1L, "hash-novo");
        verify(twoFactorService).ativar2FAAutomaticamente(1L, "user@example.com");
    }

    @Test
    void ativarConta_ContaJaAtivada_LancaExcecao() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(2L)
                .email("ativo@example.com")
                .emailVerificado(true)
                .build();

        ActivateAccountRequestDTO request = new ActivateAccountRequestDTO(
                "token",
                "ativo@example.com",
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");

        when(usuarioRepository.findByEmail("ativo@example.com")).thenReturn(Optional.of(usuario));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.ativarConta(request));

        assertThat(ex.getMessage()).contains("já foi ativada");
        verify(tokenRepository, never()).findByTokenHashAndTipo(anyString(), any());
    }

    @Test
    void ativarConta_TokenNaoPertenceAoUsuario_LancaExcecao() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(3L)
                .email("user@example.com")
                .emailVerificado(false)
                .build();

        AuthUsuario outroUsuario = AuthUsuario.builder()
                .id(999L)
                .email("outro@example.com")
                .build();

        TokenRecuperacao token = TokenRecuperacao.builder()
                .usuario(outroUsuario)
                .tokenHash("token")
                .tipo(TipoToken.VERIFICACAO_EMAIL)
                .expiracao(LocalDateTime.now().plusHours(1))
                .usado(false)
                .build();

        ActivateAccountRequestDTO request = new ActivateAccountRequestDTO(
                "token",
                "user@example.com",
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.findByTokenHashAndTipo("token", TipoToken.VERIFICACAO_EMAIL)).thenReturn(Optional.of(token));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.ativarConta(request));

        assertThat(ex.getMessage()).contains("não pertence");
        verify(usuarioRepository, never()).save(any(AuthUsuario.class));
    }

    @Test
    void ativarConta_ReutilizaSenhaHistorico_LancaIllegalArgumentException() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(4L)
                .email("user@example.com")
                .emailVerificado(false)
                .senhaHash("hash-temporario")
                .build();

        TokenRecuperacao token = TokenRecuperacao.builder()
                .usuario(usuario)
                .tokenHash("token")
                .tipo(TipoToken.VERIFICACAO_EMAIL)
                .expiracao(LocalDateTime.now().plusHours(1))
                .usado(false)
                .build();

        ActivateAccountRequestDTO request = new ActivateAccountRequestDTO(
                "token",
                "user@example.com",
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.findByTokenHashAndTipo("token", TipoToken.VERIFICACAO_EMAIL)).thenReturn(Optional.of(token));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(historicoSenhaService.senhaJaUsada(4L, "N3wP@ss1!")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.ativarConta(request));

        assertThat(ex.getMessage()).contains("já foi utilizada");
        verify(usuarioRepository, never()).save(any(AuthUsuario.class));
    }

    @Test
    void ativarConta_AtivaMesmoSe2FAFalhar() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(5L)
                .email("user@example.com")
                .senhaHash("hash-temporario")
                .senhaTemporaria(true)
                .emailVerificado(false)
                .build();

        TokenRecuperacao token = TokenRecuperacao.builder()
                .usuario(usuario)
                .tokenHash("token")
                .tipo(TipoToken.VERIFICACAO_EMAIL)
                .expiracao(LocalDateTime.now().plusHours(1))
                .usado(false)
                .build();

        ActivateAccountRequestDTO request = new ActivateAccountRequestDTO(
                "token",
                "user@example.com",
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.findByTokenHashAndTipo("token", TipoToken.VERIFICACAO_EMAIL)).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("Temp@123", "hash-temporario")).thenReturn(true);
        when(historicoSenhaService.senhaJaUsada(5L, "N3wP@ss1!")).thenReturn(false);
        when(passwordEncoder.encode("N3wP@ss1!")).thenReturn("hash-novo");
        doNothing().when(historicoSenhaService).salvarHistorico(5L, "hash-novo");
        doAnswer(inv -> { throw new RuntimeException("mail"); }).when(twoFactorService).ativar2FAAutomaticamente(5L, "user@example.com");

        MessageResponseDTO response = service.ativarConta(request);

        assertThat(response.getSuccess()).isTrue();
        assertThat(usuario.getEmailVerificado()).isTrue();
        verify(twoFactorService).ativar2FAAutomaticamente(5L, "user@example.com");
    }

    @Test
    void reenviarEmailAtivacao_DadosValidos_EnviaNovoEmail() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(6L)
                .email("user@example.com")
                .nome("Usuário")
                .senhaTemporaria(true)
                .emailVerificado(false)
                .build();

        TokenRecuperacao valido = TokenRecuperacao.builder()
                .id(11L)
                .usuario(usuario)
                .usado(false)
                .tipo(TipoToken.VERIFICACAO_EMAIL)
                .expiracao(LocalDateTime.now().plusHours(1))
                .build();

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.findByUsuarioAndTipo(usuario, TipoToken.VERIFICACAO_EMAIL))
                .thenReturn(List.of(valido));
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "enc-" + inv.getArgument(0));
        when(tokenRepository.save(any(TokenRecuperacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        ArgumentCaptor<String> senhaTemporariaCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(emailService).enviarEmailAtivacaoConta(
                anyString(),
                anyString(),
                anyString(),
                senhaTemporariaCaptor.capture());

        MessageResponseDTO response = service.reenviarEmailAtivacao("user@example.com");

        assertThat(response.getSuccess()).isTrue();
        assertThat(senhaTemporariaCaptor.getValue()).isNotBlank();
        assertThat(usuario.getSenhaHash()).isEqualTo("enc-" + senhaTemporariaCaptor.getValue());
        assertThat(valido.getUsado()).isTrue();
        verify(tokenRepository, times(2)).save(any(TokenRecuperacao.class));
        verify(emailService).enviarEmailAtivacaoConta(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void reenviarEmailAtivacao_ContaJaAtivada_LancaExcecao() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(7L)
                .email("ativo@example.com")
                .senhaTemporaria(true)
                .emailVerificado(true)
                .build();

        when(usuarioRepository.findByEmail("ativo@example.com")).thenReturn(Optional.of(usuario));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.reenviarEmailAtivacao("ativo@example.com"));

        assertThat(ex.getMessage()).contains("Conta já está ativada");
    }

    @Test
    void trocarSenhaTemporaria_ComFlagAtiva_AtualizaSenha() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(8L)
                .cpf("12345678901")
                .senhaTemporaria(true)
                .senhaHash("temp-hash")
                .build();

        FirstLoginPasswordChangeDTO request = new FirstLoginPasswordChangeDTO(
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");

        when(usuarioRepository.findByCpf("12345678901")).thenReturn(Optional.of(usuario));
        when(historicoSenhaService.senhaJaUsada(8L, "N3wP@ss1!")).thenReturn(false);
        when(passwordEncoder.encode("N3wP@ss1!")).thenReturn("hash-novo");

        service.trocarSenhaTemporaria("12345678901", request);

        assertThat(usuario.getSenhaTemporaria()).isFalse();
        assertThat(usuario.getSenhaHash()).isEqualTo("hash-novo");
        verify(historicoSenhaService).salvarHistorico(8L, "hash-novo");
    }

    @Test
    void trocarSenhaTemporaria_SemFlag_LancaExcecao() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(9L)
                .cpf("12345678901")
                .senhaTemporaria(false)
                .build();

        FirstLoginPasswordChangeDTO request = new FirstLoginPasswordChangeDTO(
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");

        when(usuarioRepository.findByCpf("12345678901")).thenReturn(Optional.of(usuario));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.trocarSenhaTemporaria("12345678901", request));

        assertThat(ex.getMessage()).contains("não está marcada");
        verify(usuarioRepository, never()).save(any(AuthUsuario.class));
    }
}
