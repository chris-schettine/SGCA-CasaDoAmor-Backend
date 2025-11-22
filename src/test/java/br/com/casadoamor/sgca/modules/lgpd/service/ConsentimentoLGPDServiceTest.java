package br.com.casadoamor.sgca.modules.lgpd.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDRequestDTO;
import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDResponseDTO;
import br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD;
import br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD.TipoEntidadeLGPD;
import br.com.casadoamor.sgca.modules.lgpd.repository.ConsentimentoLGPDRepository;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class ConsentimentoLGPDServiceTest {

    @Mock
    private ConsentimentoLGPDRepository repository;

    @InjectMocks
    private ConsentimentoLGPDService service;

    @BeforeEach
    void setUp() {
        // make sure we start with no request attributes
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void registrarConsentimento_withHeaders_shouldSaveWithIpAndUserAgent() {
        // Arrange
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 5.6.7.8");
        when(req.getHeader("User-Agent")).thenReturn("TestAgent/1.0");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        ConsentimentoLGPDRequestDTO request = ConsentimentoLGPDRequestDTO.builder()
                .versaoTermo("v1")
                .escopo("escopo")
                .concorda(true)
                .metadata(null)
                .build();

        AuthUsuario registradoPor = new AuthUsuario();
        registradoPor.setId(500L);
        registradoPor.setNome("QuemRegistra");

        ConsentimentoLGPD saved = ConsentimentoLGPD.builder()
                .id(123L)
                .uuid("uuid-1")
                .tipoEntidade(TipoEntidadeLGPD.USUARIO)
                .entidadeId(1L)
                .versaoTermo("v1")
                .escopo("escopo")
                .concorda(true)
                .dataConsentimento(LocalDateTime.now())
                .ipOrigem("1.2.3.4")
                .userAgent("TestAgent/1.0")
                .registradoPor(registradoPor)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.save(any(ConsentimentoLGPD.class))).thenReturn(saved);

        // Act
        ConsentimentoLGPDResponseDTO dto = service.registrarConsentimento(
                TipoEntidadeLGPD.USUARIO, 1L, request, registradoPor);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(123L);
        assertThat(dto.getUuid()).isEqualTo("uuid-1");
        assertThat(dto.getIpOrigem()).isEqualTo("1.2.3.4");
        assertThat(dto.getUserAgent()).isEqualTo("TestAgent/1.0");

        ArgumentCaptor<ConsentimentoLGPD> captor = ArgumentCaptor.forClass(ConsentimentoLGPD.class);
        verify(repository).save(captor.capture());
        ConsentimentoLGPD toSave = captor.getValue();
        assertThat(toSave.getIpOrigem()).isEqualTo("1.2.3.4");
        assertThat(toSave.getUserAgent()).isEqualTo("TestAgent/1.0");
        assertThat(toSave.getVersaoTermo()).isEqualTo("v1");
    }

    @Test
    void registrarConsentimento_withoutRequestAttributes_shouldSaveWithNullIpAndUserAgent() {
        // Arrange: explicitly no request attributes
        RequestContextHolder.resetRequestAttributes();

        ConsentimentoLGPDRequestDTO request = ConsentimentoLGPDRequestDTO.builder()
                .versaoTermo("v2")
                .concorda(false)
                .build();

        AuthUsuario registradoPor = new AuthUsuario();
        registradoPor.setId(1L);

        ConsentimentoLGPD saved = ConsentimentoLGPD.builder()
                .id(2L)
                .uuid("uuid-2")
                .tipoEntidade(TipoEntidadeLGPD.PACIENTE)
                .versaoTermo("v2")
                .concorda(false)
                .build();

        when(repository.save(any(ConsentimentoLGPD.class))).thenReturn(saved);

        // Act
        ConsentimentoLGPDResponseDTO dto = service.registrarConsentimento(
                TipoEntidadeLGPD.PACIENTE, 99L, request, registradoPor);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(2L);
        assertNull(dto.getIpOrigem());
        assertNull(dto.getUserAgent());
    }

    @Test
    void listarConsentimentos_shouldMapListToDto() {
        // Arrange
        ConsentimentoLGPD one = ConsentimentoLGPD.builder()
                .id(1L)
                .uuid("u1")
                .entidadeId(10L)
                .tipoEntidade(TipoEntidadeLGPD.USUARIO)
                .versaoTermo("v1")
                .dataConsentimento(LocalDateTime.now())
                .build();

        ConsentimentoLGPD two = ConsentimentoLGPD.builder()
                .id(2L)
                .uuid("u2")
                .entidadeId(10L)
                .tipoEntidade(TipoEntidadeLGPD.USUARIO)
                .versaoTermo("v1")
                .dataConsentimento(LocalDateTime.now().minusDays(1))
                .build();

        when(repository.findByTipoEntidadeAndEntidadeIdOrderByDataConsentimentoDesc(TipoEntidadeLGPD.USUARIO, 10L))
                .thenReturn(List.of(one, two));

        // Act
        List<ConsentimentoLGPDResponseDTO> result = service.listarConsentimentos(TipoEntidadeLGPD.USUARIO, 10L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUuid()).isEqualTo("u1");
        assertThat(result.get(1).getUuid()).isEqualTo("u2");
    }

    @Test
    void obterConsentimentoAtual_whenPresent_returnsDto_elseNull() {
        // Arrange
        ConsentimentoLGPD consent = ConsentimentoLGPD.builder()
                .id(10L)
                .uuid("xx")
                .tipoEntidade(TipoEntidadeLGPD.PROFISSIONAL)
                .entidadeId(77L)
                .versaoTermo("vX")
                .build();

        when(repository.findFirstByTipoEntidadeAndEntidadeIdOrderByDataConsentimentoDesc(
                TipoEntidadeLGPD.PROFISSIONAL, 77L)).thenReturn(Optional.of(consent));

        ConsentimentoLGPDResponseDTO dto = service.obterConsentimentoAtual(TipoEntidadeLGPD.PROFISSIONAL, 77L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);

        when(repository.findFirstByTipoEntidadeAndEntidadeIdOrderByDataConsentimentoDesc(
                TipoEntidadeLGPD.PROFISSIONAL, 999L)).thenReturn(Optional.empty());

        ConsentimentoLGPDResponseDTO dto2 = service.obterConsentimentoAtual(TipoEntidadeLGPD.PROFISSIONAL, 999L);
        assertThat(dto2).isNull();
    }

    @Test
    void hasConsentimentoValido_and_contarConsentimentos_and_listByVersao_tipo_periodo_and_desatualizados() {
        // Arrange / Act / Assert in small combos to cover simple pass-throughs
        when(repository.hasConsentimentoValido(TipoEntidadeLGPD.USUARIO, 1L)).thenReturn(true);
        assertThat(service.hasConsentimentoValido(TipoEntidadeLGPD.USUARIO, 1L)).isTrue();

        when(repository.countByTipoEntidadeAndConcorda(TipoEntidadeLGPD.USUARIO, true)).thenReturn(5L);
        assertThat(service.contarConsentimentos(TipoEntidadeLGPD.USUARIO, true)).isEqualTo(5L);

        ConsentimentoLGPD c = ConsentimentoLGPD.builder().id(3L).versaoTermo("v9").tipoEntidade(TipoEntidadeLGPD.USUARIO).build();
        when(repository.findByVersaoTermoOrderByDataConsentimentoDesc("v9")).thenReturn(List.of(c));
        assertThat(service.listarPorVersao("v9")).hasSize(1);

        when(repository.findByTipoEntidadeOrderByDataConsentimentoDesc(TipoEntidadeLGPD.PROFISSIONAL))
                .thenReturn(List.of(c));
        assertThat(service.listarPorTipo(TipoEntidadeLGPD.PROFISSIONAL)).hasSize(1);

        LocalDateTime a = LocalDateTime.now().minusDays(5);
        LocalDateTime b = LocalDateTime.now();
        when(repository.findByDataConsentimentoBetween(a, b)).thenReturn(List.of(c));
        assertThat(service.listarPorPeriodo(a, b)).hasSize(1);

        when(repository.findConsentimentosDesatualizados(TipoEntidadeLGPD.USUARIO, "vOld"))
                .thenReturn(List.of(c));
        assertThat(service.listarConsentimentosDesatualizados(TipoEntidadeLGPD.USUARIO, "vOld")).hasSize(1);
    }
}
