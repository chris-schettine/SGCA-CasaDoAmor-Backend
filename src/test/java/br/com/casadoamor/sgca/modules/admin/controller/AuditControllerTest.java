package br.com.casadoamor.sgca.modules.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.casadoamor.sgca.modules.admin.dtos.auditoria.AuditoriaPerfilDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.auditoria.AuditoriaUsuarioDTO;
import br.com.casadoamor.sgca.modules.admin.service.AuditoriaAdminService;
import br.com.casadoamor.sgca.modules.admin.service.SessaoService;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.TipoUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.SessaoUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.TentativaLogin;
import br.com.casadoamor.sgca.modules.auth.repository.SessaoUsuarioRepository;
import br.com.casadoamor.sgca.modules.auth.repository.TentativaLoginRepository;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    TentativaLoginRepository tentativaLoginRepository;

    @Mock
    SessaoUsuarioRepository sessaoRepository;

    @Mock
    AuditoriaAdminService auditoriaAdminService;

    @Mock
    SessaoService sessaoService;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    AuditController controller;

    @Test
    void relatorioLogins_NoFilters_Success() {
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setId(1L);
        tentativa.setSucesso(true);
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setTipo(TipoUsuario.ADMINISTRADOR);
        tentativa.setUsuario(usuario);

        when(tentativaLoginRepository.findTop100ByOrderByDataTentativaDesc())
                .thenReturn(List.of(tentativa));

        ResponseEntity<?> response = controller.relatorioLogins(null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void relatorioLogins_WithDateFilters_Success() {
        LocalDateTime now = LocalDateTime.now();
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setId(1L);
        tentativa.setSucesso(true);
        // Set usuario to avoid NPE during DTO mapping
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setTipo(TipoUsuario.ADMINISTRADOR);
        tentativa.setUsuario(usuario);

        when(tentativaLoginRepository.findByDataTentativaBetween(any(), any()))
                .thenReturn(List.of(tentativa));

        ResponseEntity<?> response = controller.relatorioLogins(now, now, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void relatorioLogins_WithDateAndSuccessFilters_Success() {
        LocalDateTime now = LocalDateTime.now();
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setId(1L);
        tentativa.setSucesso(true);
        // Set usuario to avoid NPE during DTO mapping
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setTipo(TipoUsuario.ADMINISTRADOR);
        tentativa.setUsuario(usuario);

        when(tentativaLoginRepository.findByDataTentativaBetweenAndSucesso(any(), any(), eq(true)))
                .thenReturn(List.of(tentativa));

        ResponseEntity<?> response = controller.relatorioLogins(now, now, true, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void relatorioLogins_WithCpfFilter_Success() {
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setId(1L);
        tentativa.setSucesso(true);
        tentativa.setCpf("123");
        // Set usuario to avoid NPE during DTO mapping
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setTipo(TipoUsuario.ADMINISTRADOR);
        tentativa.setUsuario(usuario);

        when(tentativaLoginRepository.findByCpf("123"))
                .thenReturn(List.of(tentativa));

        ResponseEntity<?> response = controller.relatorioLogins(null, null, null, "123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void relatorioLogins_WithSuccessFilter_Success() {
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setId(1L);
        tentativa.setSucesso(true);
        // Set usuario to avoid NPE during DTO mapping
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setTipo(TipoUsuario.ADMINISTRADOR);
        tentativa.setUsuario(usuario);

        when(tentativaLoginRepository.findBySucesso(true))
                .thenReturn(List.of(tentativa));

        ResponseEntity<?> response = controller.relatorioLogins(null, null, true, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void sessoesAtivas_Success() {
        SessaoUsuario sessao = new SessaoUsuario();
        sessao.setId(1L);
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setTipo(TipoUsuario.ADMINISTRADOR);
        sessao.setUsuario(usuario);

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(sessaoRepository.findByAtivoAndExpiraEmAfter(eq(true), any()))
                .thenReturn(List.of(sessao));

        ResponseEntity<?> response = controller.sessoesAtivas();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void auditoriaUsuario_Success() {
        when(auditoriaAdminService.buscarAuditoriaUsuario(1L))
                .thenReturn(AuditoriaUsuarioDTO.builder().build());

        ResponseEntity<AuditoriaUsuarioDTO> response = controller.auditoriaUsuario(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void auditoriaPerfil_Success() {
        when(auditoriaAdminService.buscarAuditoriaPerfil(1L))
                .thenReturn(AuditoriaPerfilDTO.builder().build());

        ResponseEntity<AuditoriaPerfilDTO> response = controller.auditoriaPerfil(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void listarAuditoriaPerfis_Success() {
        when(auditoriaAdminService.listarAuditoriaPerfis())
                .thenReturn(List.of(AuditoriaPerfilDTO.builder().build()));
        when(tentativaLoginRepository.findTop100ByOrderByDataTentativaDesc())
                .thenReturn(List.of());

        ResponseEntity<?> response = controller.listarAuditoriaPerfis();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void revogarSessaoAdmin_Success() {
        SessaoUsuario sessao = mock(SessaoUsuario.class);
        when(sessaoRepository.findById(1L)).thenReturn(java.util.Optional.of(sessao));
        doNothing().when(sessao).revogar();
        when(sessaoRepository.save(sessao)).thenReturn(sessao);

        ResponseEntity<?> response = controller.revogarSessaoAdmin(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void revogarSessaoAdmin_NotFound() {
        when(sessaoRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        try {
            controller.revogarSessaoAdmin(1L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(br.com.casadoamor.sgca.infra.exception.ResourceNotFoundException.class);
        }
    }

    @Test
    void revogarTodasSessoesUsuario_Success() {
        doNothing().when(sessaoService).revogarTodasSessoes(1L);

        ResponseEntity<?> response = controller.revogarTodasSessoesUsuario(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
