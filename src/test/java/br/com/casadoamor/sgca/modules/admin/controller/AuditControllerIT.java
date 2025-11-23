package br.com.casadoamor.sgca.modules.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.com.casadoamor.sgca.modules.auth.dtos.SessaoDTO;

@WebMvcTest(controllers = AuditController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuditControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.auth.repository.TentativaLoginRepository tentativaLoginRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.auth.repository.SessaoUsuarioRepository sessaoRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.AuditoriaAdminService auditoriaAdminService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    // Jwt deps
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    void logins_report_and_sessions_list() throws Exception {
        // logins: return empty list -> should produce statistics
        org.mockito.Mockito.when(tentativaLoginRepository.findTop100ByOrderByDataTentativaDesc()).thenReturn(List.of());
        org.mockito.Mockito.when(auditoriaAdminService.listarAuditoriaPerfis()).thenReturn(List.of());

        String loginsRes = mvc.perform(get("/admin/audit/logins")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(loginsRes).contains("total");

        // sessions: return one session
        var usuario = br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.builder().id(1L).nome("U").cpf("11122233344").email("u@example.com").build();
        var s = br.com.casadoamor.sgca.modules.auth.entity.SessaoUsuario.builder().id(1L).tokenJwt("tok").ipOrigem("127.0.0.1").userAgent("ua").criadoEm(LocalDateTime.now()).expiraEm(LocalDateTime.now().plusDays(1)).ativo(true).usuario(usuario).build();
        org.mockito.Mockito.when(sessaoRepository.findByAtivoAndExpiraEmAfter(org.mockito.ArgumentMatchers.eq(true), org.mockito.ArgumentMatchers.any(LocalDateTime.class))).thenReturn(List.of(s));

        String sessions = mvc.perform(get("/admin/audit/sessions")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(sessions).contains("u@example.com");
    }

    @Test
    void auditoria_usuario_and_perfil_endpoints() throws Exception {
        // auditoria usuario
        var auditoriaUsuario = br.com.casadoamor.sgca.modules.admin.dtos.auditoria.AuditoriaUsuarioDTO.builder()
                .id(777L)
                .nome("User Name")
                .email("user@example.com")
                .cpf("11122233344")
                .tipo("PACIENTE")
                .ativo(true)
                .build();

        org.mockito.Mockito.when(auditoriaAdminService.buscarAuditoriaUsuario(org.mockito.ArgumentMatchers.eq(777L))).thenReturn(auditoriaUsuario);

        String userAudit = mvc.perform(get("/admin/audit/usuarios/777")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(userAudit).contains("user@example.com").contains("User Name");

        // auditoria perfil
        var auditoriaPerfil = br.com.casadoamor.sgca.modules.admin.dtos.auditoria.AuditoriaPerfilDTO.builder()
                .id(99L)
                .nome("ROLE_TEST")
                .descricao("desc")
                .totalUsuarios(2)
                .build();

        org.mockito.Mockito.when(auditoriaAdminService.buscarAuditoriaPerfil(org.mockito.ArgumentMatchers.eq(99L))).thenReturn(auditoriaPerfil);

        String perfilAudit = mvc.perform(get("/admin/audit/perfis/99")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(perfilAudit).contains("ROLE_TEST").contains("desc");
    }
}
