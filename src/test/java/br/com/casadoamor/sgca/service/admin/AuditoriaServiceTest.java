package br.com.casadoamor.sgca.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.casadoamor.sgca.modules.admin.service.AuditoriaService;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.TentativaLogin;
import br.com.casadoamor.sgca.modules.auth.repository.TentativaLoginRepository;

class AuditoriaServiceTest {

    @Mock
    private TentativaLoginRepository tentativaLoginRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registrarLoginSucesso_PersistTentativaComSucesso() {
        AuthUsuario usuario = AuthUsuario.builder()
                .id(1L)
                .cpf("12345678901")
                .email("user@example.com")
                .build();

        auditoriaService.registrarLoginSucesso(usuario, "1.1.1.1", "JUnit");

        ArgumentCaptor<TentativaLogin> captor = ArgumentCaptor.forClass(TentativaLogin.class);
        verify(tentativaLoginRepository).save(captor.capture());
        TentativaLogin tentativa = captor.getValue();
        assertThat(tentativa.getSucesso()).isTrue();
        assertThat(tentativa.getCpf()).isEqualTo("12345678901");
        assertThat(tentativa.getIpOrigem()).isEqualTo("1.1.1.1");
        assertThat(tentativa.getUserAgent()).isEqualTo("JUnit");
        assertThat(tentativa.getBloqueado()).isFalse();
    }

    @Test
    void registrarLoginFalha_ComBloqueioRegistraFlag() {
        when(tentativaLoginRepository.countByCpfAndSucessoAndDataTentativaAfter(eq("00000000000"), eq(false), any(LocalDateTime.class)))
                .thenReturn(5L);

        auditoriaService.registrarLoginFalha("00000000000", "2.2.2.2", "JUnit", "SENHA_INVALIDA");

        ArgumentCaptor<TentativaLogin> captor = ArgumentCaptor.forClass(TentativaLogin.class);
        verify(tentativaLoginRepository).save(captor.capture());
        TentativaLogin tentativa = captor.getValue();
        assertThat(tentativa.getSucesso()).isFalse();
        assertThat(tentativa.getBloqueado()).isTrue();
        assertThat(tentativa.getMotivoFalha()).isEqualTo("SENHA_INVALIDA");
    }

    @Test
    void verificarBloqueio_QuandoTentativasAcimaLimite_RetornaVerdadeiro() {
        when(tentativaLoginRepository.countByCpfAndSucessoAndDataTentativaAfter(eq("11111111111"), eq(false), any(LocalDateTime.class)))
                .thenReturn(10L);

        boolean bloqueado = auditoriaService.verificarBloqueio("11111111111");

        assertThat(bloqueado).isTrue();
    }

    @Test
    void verificarBloqueio_AbaixoDoLimite_RetornaFalso() {
        when(tentativaLoginRepository.countByCpfAndSucessoAndDataTentativaAfter(eq("22222222222"), eq(false), any(LocalDateTime.class)))
                .thenReturn(2L);

        boolean bloqueado = auditoriaService.verificarBloqueio("22222222222");

        assertThat(bloqueado).isFalse();
    }

    @Test
    void obterHistoricoUsuario_PuxaDoRepositorio() {
        TentativaLogin tentativa = TentativaLogin.builder().id(1L).cpf("33333333333").build();
        when(tentativaLoginRepository.findByUsuarioIdOrderByDataTentativaDesc(1L))
                .thenReturn(List.of(tentativa));

        List<TentativaLogin> historico = auditoriaService.obterHistoricoUsuario(1L);

        assertThat(historico).containsExactly(tentativa);
    }

    @Test
    void obterTentativasSuspeitas_RetornaUltimasCem() {
        TentativaLogin tentativa = TentativaLogin.builder().id(10L).cpf("44444444444").build();
        when(tentativaLoginRepository.findTop100ByOrderByDataTentativaDesc())
                .thenReturn(List.of(tentativa));

        List<TentativaLogin> suspeitas = auditoriaService.obterTentativasSuspeitas();

        assertThat(suspeitas).containsExactly(tentativa);
    }
}
