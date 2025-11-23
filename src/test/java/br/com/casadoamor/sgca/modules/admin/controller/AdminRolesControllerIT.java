package br.com.casadoamor.sgca.modules.admin.controller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.casadoamor.sgca.modules.admin.dtos.perfil.CreatePerfilDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.perfil.PerfilDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.permissao.CreatePermissaoDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.permissao.PermissaoDTO;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminRolesControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.UserManagementService userManagementService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.PerfilService perfilService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.PermissaoService permissaoService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.auth.service.AuthService authService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.common.service.UserPhotoService userPhotoService;

    // Jwt deps
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void create_and_list_perfil_and_permission() throws Exception {
        CreatePerfilDTO create = CreatePerfilDTO.builder().nome("ROLE_TEST").descricao("desc").build();
        PerfilDTO perfil = PerfilDTO.builder().id(12L).nome("ROLE_TEST").build();

        // mock admin id lookup
        org.mockito.Mockito.when(authService.findUserIdByCpf(org.mockito.ArgumentMatchers.eq("11122233344"))).thenReturn(java.util.Optional.of(1L));

        org.mockito.Mockito.when(perfilService.criarPerfil(org.mockito.ArgumentMatchers.any(CreatePerfilDTO.class), org.mockito.ArgumentMatchers.eq(1L))).thenReturn(perfil);
        org.mockito.Mockito.when(perfilService.listarPerfis()).thenReturn(List.of(perfil));

        // mock admin id lookup
        org.mockito.Mockito.when(userManagementService.atribuirPerfis(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(br.com.casadoamor.sgca.modules.admin.dtos.user.UserResponseDTO.builder().id(1L).nome("x").build());

        org.mockito.Mockito.when(permissaoService.criarPermissao(org.mockito.ArgumentMatchers.any(CreatePermissaoDTO.class), org.mockito.ArgumentMatchers.eq(1L))).thenReturn(PermissaoDTO.builder().id(20L).nome("PERM_TEST").build());

        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken("11122233344", null);
        org.mockito.Mockito.when(perfilService.criarPerfil(org.mockito.ArgumentMatchers.any(CreatePerfilDTO.class), org.mockito.ArgumentMatchers.eq(1L))).thenReturn(perfil);

        String created = mvc.perform(post("/admin/roles").principal(auth).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"ROLE_TEST\",\"descricao\":\"desc\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(created).contains("ROLE_TEST");

        String list = mvc.perform(get("/admin/roles").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(list).contains("ROLE_TEST");
    }

    @Test
    void create_and_list_permissions() throws Exception {
        // mock admin id lookup
        org.mockito.Mockito.when(authService.findUserIdByCpf(org.mockito.ArgumentMatchers.eq("11122233344"))).thenReturn(java.util.Optional.of(1L));

        PermissaoDTO dto = PermissaoDTO.builder().id(20L).nome("PERM_TEST").descricao("desc").build();
        org.mockito.Mockito.when(permissaoService.criarPermissao(org.mockito.ArgumentMatchers.any(CreatePermissaoDTO.class), org.mockito.ArgumentMatchers.eq(1L))).thenReturn(dto);
        org.mockito.Mockito.when(permissaoService.listarPermissoes()).thenReturn(List.of(dto));

        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken("11122233344", null);

        String created = mvc.perform(post("/admin/permissions").principal(auth).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"PERM_TEST\",\"descricao\":\"desc\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(created).contains("PERM_TEST");

        String list = mvc.perform(get("/admin/permissions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(list).contains("PERM_TEST");
    }
}
