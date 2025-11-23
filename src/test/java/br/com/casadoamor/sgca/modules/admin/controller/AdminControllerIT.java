package br.com.casadoamor.sgca.modules.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.casadoamor.sgca.modules.admin.dtos.user.CreateUserDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.user.UserResponseDTO;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminControllerIT {

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

    // Jwt filter deps
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void create_get_and_delete_user() throws Exception {
        CreateUserDTO req = CreateUserDTO.builder().nome("Test User").cpf("11122233344").email("t@example.com").build();

        UserResponseDTO resp = UserResponseDTO.builder().id(5L).nome("Test User").email("t@example.com").build();
        org.mockito.Mockito.when(userManagementService.criarUsuario(org.mockito.ArgumentMatchers.any(CreateUserDTO.class), org.mockito.ArgumentMatchers.eq(1L))).thenReturn(resp);

        // mock admin lookup
        org.mockito.Mockito.when(authService.findUserIdByCpf(org.mockito.ArgumentMatchers.eq("11122233344"))).thenReturn(java.util.Optional.of(1L));

        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken("11122233344", null);

        String created = mvc.perform(post("/admin/users").principal(auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Test User\",\"cpf\":\"11122233344\",\"email\":\"t@example.com\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        assertThat(created).contains("Test User");

        // fetch by id
        org.mockito.Mockito.when(userManagementService.buscarPorId(org.mockito.ArgumentMatchers.eq(5L))).thenReturn(resp);

        String got = mvc.perform(get("/admin/users/5").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(got).contains("t@example.com");

        // delete
        org.mockito.Mockito.doNothing().when(userManagementService).deletarUsuario(org.mockito.ArgumentMatchers.eq(5L), org.mockito.ArgumentMatchers.eq(1L));

        String deleted = mvc.perform(delete("/admin/users/5").principal(auth))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(deleted).contains("deletado");
    }

    @Test
    void toggle_user_status() throws Exception {
        // prepare a sample response
        br.com.casadoamor.sgca.modules.admin.dtos.user.UserResponseDTO toggled = br.com.casadoamor.sgca.modules.admin.dtos.user.UserResponseDTO.builder().id(5L).nome("Test User").ativo(false).build();

        // mock admin lookup and service behavior
        org.mockito.Mockito.when(authService.findUserIdByCpf(org.mockito.ArgumentMatchers.eq("11122233344"))).thenReturn(java.util.Optional.of(1L));
        org.mockito.Mockito.when(userManagementService.toggleUserStatus(org.mockito.ArgumentMatchers.eq(5L), org.mockito.ArgumentMatchers.eq(1L))).thenReturn(toggled);

        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken("11122233344", null);

        String res = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/admin/users/5/toggle-status").principal(auth))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(res).contains("ativo");
    }
}
