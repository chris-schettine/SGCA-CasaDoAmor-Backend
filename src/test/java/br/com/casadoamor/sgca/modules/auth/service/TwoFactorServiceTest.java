package br.com.casadoamor.sgca.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.auth.dtos.twofactor.Enable2FADTO;
import br.com.casadoamor.sgca.modules.auth.dtos.twofactor.Setup2FADTO;
import br.com.casadoamor.sgca.modules.auth.entity.Autenticacao2FA;
import br.com.casadoamor.sgca.modules.auth.entity.Autenticacao2FARateLimit;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.Autenticacao2FARateLimitRepository;
import br.com.casadoamor.sgca.modules.auth.repository.Autenticacao2FARepository;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.common.service.EmailService;

@ExtendWith(MockitoExtension.class)
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

    private Autenticacao2FA autenticacao2FA;
    private Autenticacao2FARateLimit rateLimit;
    private AuthUsuario usuarioMock;

    @BeforeEach
    void setup() {
        autenticacao2FA = new Autenticacao2FA();
        autenticacao2FA.setId(1L);
        autenticacao2FA.setUsuarioId(10L);
        autenticacao2FA.setHabilitado(false);
        autenticacao2FA.setTentativasFalhas(0);

        rateLimit = new Autenticacao2FARateLimit();
        rateLimit.setId(1L);
        rateLimit.setUsuarioId(10L);

        usuarioMock = AuthUsuario.builder()
                .id(10L)
                .email("user@example.com")
                .nome("Test User")
                .build();
    }

    // ===================== TESTES DE CONFIGURAÇÃO 2FA =====================

    @Test
    void configurar2FA_NovoUsuario_CriaConfiguracao() {
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        when(rateLimitRepository.save(any(Autenticacao2FARateLimit.class))).thenAnswer(i -> i.getArgument(0));

        Setup2FADTO resultado = twoFactorService.configurar2FA(10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getMensagem()).isNotBlank();
        assertThat(resultado.getEmail()).contains("***");
        verify(autenticacao2FARepository).save(any(Autenticacao2FA.class));
        verify(emailService).send2FACode(anyString(), anyString());
    }

    @Test
    void configurar2FA_UsuarioExistente_EnviaNovoCodigo() {
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        when(rateLimitRepository.save(any(Autenticacao2FARateLimit.class))).thenAnswer(i -> i.getArgument(0));

        Setup2FADTO resultado = twoFactorService.configurar2FA(10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getMensagem()).isNotBlank();
        verify(autenticacao2FARepository).save(autenticacao2FA);
        verify(emailService).send2FACode(anyString(), anyString());
    }

    @Test
    void configurar2FA_UsuarioNaoEncontrado_ThrowsException() {
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorService.configurar2FA(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    void configurar2FA_RateLimitExcedido_ThrowsException() {
        rateLimit.setTentativasUltimos15Min(5);
        rateLimit.setUltimoEnvio(LocalDateTime.now().minusSeconds(10));
        
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.of(rateLimit));

        assertThatThrownBy(() -> twoFactorService.configurar2FA(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Limite de envios excedido");
    }

    @Test
    void configurar2FA_ErroEnvioEmail_ThrowsException() {
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("Email error")).when(emailService).send2FACode(anyString(), anyString());

        assertThatThrownBy(() -> twoFactorService.configurar2FA(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao enviar código 2FA");
    }

    // ===================== TESTES DE HABILITAR/DESABILITAR 2FA =====================

    @Test
    void alterarStatus2FA_HabilitarComCodigoValido_RetornaSucesso() {
        autenticacao2FA.setCodigoAtual("123456");
        autenticacao2FA.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));

        Enable2FADTO dto = new Enable2FADTO();
        dto.setHabilitar(true);
        dto.setCodigo("123456");

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));

        twoFactorService.alterarStatus2FA(10L, dto);

        assertThat(autenticacao2FA.getHabilitado()).isTrue();
        assertThat(autenticacao2FA.getDataHabilitacao()).isNotNull();
        assertThat(autenticacao2FA.getCodigoAtual()).isNull();
        verify(autenticacao2FARepository).save(autenticacao2FA);
    }

    @Test
    void alterarStatus2FA_SemConfiguracaoAnterior_ThrowsException() {
        Enable2FADTO dto = new Enable2FADTO();
        dto.setHabilitar(true);
        dto.setCodigo("123456");

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorService.alterarStatus2FA(10L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Configure o 2FA primeiro");
    }

    @Test
    void alterarStatus2FA_UsuarioBloqueado_ThrowsException() {
        autenticacao2FA.setBloqueadoAte(LocalDateTime.now().plusMinutes(15));

        Enable2FADTO dto = new Enable2FADTO();
        dto.setHabilitar(true);
        dto.setCodigo("123456");

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));

        assertThatThrownBy(() -> twoFactorService.alterarStatus2FA(10L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Muitas tentativas falhas");
    }

    @Test
    void alterarStatus2FA_CodigoInvalido_LancaExcecao() {
        autenticacao2FA.setCodigoAtual("123456");
        autenticacao2FA.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));

        Enable2FADTO dto = new Enable2FADTO();
        dto.setHabilitar(true);
        dto.setCodigo("999999");

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> twoFactorService.alterarStatus2FA(10L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Código inválido");

        assertThat(autenticacao2FA.getTentativasFalhas()).isEqualTo(1);
        verify(autenticacao2FARepository).save(autenticacao2FA);
    }

    @Test
    void alterarStatus2FA_CodigoExpirado_LancaExcecao() {
        autenticacao2FA.setCodigoAtual("123456");
        autenticacao2FA.setExpiracaoCodigo(LocalDateTime.now().minusMinutes(1));

        Enable2FADTO dto = new Enable2FADTO();
        dto.setHabilitar(true);
        dto.setCodigo("123456");

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));

        assertThatThrownBy(() -> twoFactorService.alterarStatus2FA(10L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void alterarStatus2FA_Desabilitar_RetornaSucesso() {
        autenticacao2FA.setHabilitado(true);
        autenticacao2FA.setCodigoAtual("123456");
        autenticacao2FA.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));

        Enable2FADTO dto = new Enable2FADTO();
        dto.setHabilitar(false);
        dto.setCodigo("123456");

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));

        twoFactorService.alterarStatus2FA(10L, dto);

        assertThat(autenticacao2FA.getHabilitado()).isFalse();
        assertThat(autenticacao2FA.getDataDesabilitacao()).isNotNull();
        verify(autenticacao2FARepository).save(autenticacao2FA);
    }

    // ===================== TESTES DE VERIFICAÇÃO 2FA =====================

    @Test
    void usuario2FAHabilitado_Habilitado_RetornaTrue() {
        autenticacao2FA.setHabilitado(true);
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));

        boolean resultado = twoFactorService.usuario2FAHabilitado(10L);

        assertThat(resultado).isTrue();
    }

    @Test
    void usuario2FAHabilitado_Desabilitado_RetornaFalse() {
        autenticacao2FA.setHabilitado(false);
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));

        boolean resultado = twoFactorService.usuario2FAHabilitado(10L);

        assertThat(resultado).isFalse();
    }

    @Test
    void usuario2FAHabilitado_SemConfiguracao_RetornaFalse() {
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());

        boolean resultado = twoFactorService.usuario2FAHabilitado(10L);

        assertThat(resultado).isFalse();
    }

    // ===================== TESTES DE LOGIN COM 2FA =====================

    @Test
    void enviarCodigoLogin_Success_EnviaCodigo() {
        autenticacao2FA.setHabilitado(true);
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        when(rateLimitRepository.save(any(Autenticacao2FARateLimit.class))).thenAnswer(i -> i.getArgument(0));

        twoFactorService.enviarCodigoLogin(10L);

        verify(autenticacao2FARepository).save(autenticacao2FA);
        verify(emailService).send2FACode(anyString(), anyString());
        assertThat(autenticacao2FA.getCodigoAtual()).isNotNull();
        assertThat(autenticacao2FA.getExpiracaoCodigo()).isNotNull();
    }

    @Test
    void enviarCodigoLogin_UsuarioNaoEncontrado_ThrowsException() {
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorService.enviarCodigoLogin(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    void enviarCodigoLogin_2FANaoConfigurado_ThrowsException() {
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorService.enviarCodigoLogin(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("2FA não configurado");
    }

    @Test
    void enviarCodigoLogin_2FANaoHabilitado_ThrowsException() {
        autenticacao2FA.setHabilitado(false);
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));

        assertThatThrownBy(() -> twoFactorService.enviarCodigoLogin(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("2FA não está habilitado");
    }

    @Test
    void enviarCodigoLogin_UsuarioBloqueado_ThrowsException() {
        autenticacao2FA.setHabilitado(true);
        autenticacao2FA.setBloqueadoAte(LocalDateTime.now().plusMinutes(15));
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));

        assertThatThrownBy(() -> twoFactorService.enviarCodigoLogin(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Muitas tentativas falhas");
    }

    @Test
    void enviarCodigoLogin_ErroEnvioEmail_ThrowsException() {
        autenticacao2FA.setHabilitado(true);
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("Email error")).when(emailService).send2FACode(anyString(), anyString());

        assertThatThrownBy(() -> twoFactorService.enviarCodigoLogin(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao enviar código 2FA");
    }

    // ===================== TESTES DE VALIDAÇÃO DE CÓDIGO LOGIN =====================

    @Test
    void validarCodigoLogin_CodigoValido_RetornaTrue() {
        autenticacao2FA.setCodigoAtual("123456");
        autenticacao2FA.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));
        autenticacao2FA.setTentativasFalhas(0);

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));

        boolean resultado = twoFactorService.validarCodigoLogin(10L, "123456");

        assertThat(resultado).isTrue();
        assertThat(autenticacao2FA.getTentativasFalhas()).isEqualTo(0);
        assertThat(autenticacao2FA.getCodigoAtual()).isNull();
        assertThat(autenticacao2FA.getExpiracaoCodigo()).isNull();
        verify(autenticacao2FARepository).save(autenticacao2FA);
    }

    @Test
    void validarCodigoLogin_CodigoInvalido_RetornaFalse() {
        autenticacao2FA.setCodigoAtual("123456");
        autenticacao2FA.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));
        autenticacao2FA.setTentativasFalhas(0);

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));

        boolean resultado = twoFactorService.validarCodigoLogin(10L, "999999");

        assertThat(resultado).isFalse();
        assertThat(autenticacao2FA.getTentativasFalhas()).isEqualTo(1);
        verify(autenticacao2FARepository).save(autenticacao2FA);
    }

    @Test
    void validarCodigoLogin_2FANaoConfigurado_ThrowsException() {
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorService.validarCodigoLogin(10L, "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("2FA não configurado");
    }

    @Test
    void validarCodigoLogin_CodigoExpirado_LancaExcecao() {
        autenticacao2FA.setCodigoAtual("123456");
        autenticacao2FA.setExpiracaoCodigo(LocalDateTime.now().minusMinutes(1));

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));

        assertThatThrownBy(() -> twoFactorService.validarCodigoLogin(10L, "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void validarCodigoLogin_UsuarioBloqueado_LancaExcecao() {
        autenticacao2FA.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));

        assertThatThrownBy(() -> twoFactorService.validarCodigoLogin(10L, "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tentativas");
    }

    @Test
    void validarCodigoLogin_QuintaTentativaFalha_BloqueiaUsuario() {
        autenticacao2FA.setCodigoAtual("123456");
        autenticacao2FA.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));
        autenticacao2FA.setTentativasFalhas(4); // 4 tentativas anteriores

        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));

        boolean resultado = twoFactorService.validarCodigoLogin(10L, "999999");

        assertThat(resultado).isFalse();
        assertThat(autenticacao2FA.getTentativasFalhas()).isEqualTo(5);
        assertThat(autenticacao2FA.getBloqueadoAte()).isNotNull();
    }

    // ===================== TESTES DE ATIVAÇÃO AUTOMÁTICA =====================

    @Test
    void ativar2FAAutomaticamente_NovaConfiguracao_HabilitaEEnviaCodigo() {
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        when(rateLimitRepository.save(any(Autenticacao2FARateLimit.class))).thenAnswer(i -> i.getArgument(0));

        twoFactorService.ativar2FAAutomaticamente(10L, "user@example.com");

        verify(autenticacao2FARepository).save(any(Autenticacao2FA.class));
        verify(emailService).send2FACode(anyString(), anyString());
    }

    @Test
    void ativar2FAAutomaticamente_ConfiguracaoExistente_AtualizaEEnviaCodigo() {
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.of(autenticacao2FA));
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        when(rateLimitRepository.save(any(Autenticacao2FARateLimit.class))).thenAnswer(i -> i.getArgument(0));

        twoFactorService.ativar2FAAutomaticamente(10L, "user@example.com");

        assertThat(autenticacao2FA.getHabilitado()).isTrue();
        assertThat(autenticacao2FA.getDataHabilitacao()).isNotNull();
        assertThat(autenticacao2FA.getCodigoAtual()).isNotNull();
        verify(autenticacao2FARepository).save(autenticacao2FA);
        verify(emailService).send2FACode(anyString(), anyString());
    }

    @Test
    void ativar2FAAutomaticamente_RateLimitExcedido_ThrowsException() {
        rateLimit.setTentativasUltimos15Min(5);
        rateLimit.setUltimoEnvio(LocalDateTime.now().minusSeconds(10));
        
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.of(rateLimit));

        assertThatThrownBy(() -> twoFactorService.ativar2FAAutomaticamente(10L, "user@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Limite de envios excedido");
    }

    @Test
    void ativar2FAAutomaticamente_ErroEnvioEmail_ThrowsException() {
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("Email error")).when(emailService).send2FACode(anyString(), anyString());

        assertThatThrownBy(() -> twoFactorService.ativar2FAAutomaticamente(10L, "user@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao enviar código 2FA");
    }

    // ===================== TESTES DE MASCARAMENTO DE EMAIL =====================

    @Test
    void configurar2FA_EmailCurto_MascaraCorretamente() {
        usuarioMock.setEmail("ab@test.com");
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        when(rateLimitRepository.save(any(Autenticacao2FARateLimit.class))).thenAnswer(i -> i.getArgument(0));

        Setup2FADTO resultado = twoFactorService.configurar2FA(10L);

        assertThat(resultado.getEmail()).contains("***");
    }

    @Test
    void configurar2FA_EmailNormal_MascaraCorretamente() {
        usuarioMock.setEmail("joaosilva@test.com");
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(autenticacao2FARepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(rateLimitRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(autenticacao2FARepository.save(any(Autenticacao2FA.class))).thenAnswer(i -> i.getArgument(0));
        when(rateLimitRepository.save(any(Autenticacao2FARateLimit.class))).thenAnswer(i -> i.getArgument(0));

        Setup2FADTO resultado = twoFactorService.configurar2FA(10L);

        assertThat(resultado.getEmail()).isEqualTo("joa***@test.com");
    }
}
