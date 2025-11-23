package br.com.casadoamor.sgca.modules.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import br.com.casadoamor.sgca.modules.auth.dtos.response.AuthResponseDTO;
import br.com.casadoamor.sgca.modules.auth.service.AuthService;
import br.com.casadoamor.sgca.modules.admin.service.SessaoService;
import br.com.casadoamor.sgca.modules.common.dto.MessageResponseDTO;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mvc;


    @org.springframework.boot.test.mock.mockito.MockBean
    private AuthService authService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private SessaoService sessaoService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.auth.service.AccountActivationService accountActivationService;

    // security beans used by JwtAuthenticationFilter
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    void login_and_forgot_password_and_list_sessions() throws Exception {
        AuthResponseDTO r = AuthResponseDTO.builder().token("tok").tipo("Bearer").email("u@example.com").nome("Usuario").requires2FA(false).build();

        org.mockito.Mockito.when(authService.login(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(r);

        String loginPayload = "{\"cpf\":\"11122233344\", \"senha\":\"secret\"}";

        String res = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginPayload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(res).contains("tok");

        MessageResponseDTO forgot = MessageResponseDTO.success("If exists, email sent");
        org.mockito.Mockito.when(authService.forgotPassword(org.mockito.ArgumentMatchers.any())).thenReturn(forgot);

        String forgotPayload = "{\"email\":\"u@example.com\"}";
        String fRes = mvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(forgotPayload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(fRes).contains("email sent");

        // list sessions - controller expects Authentication; use TestingAuthenticationToken
        var s = br.com.casadoamor.sgca.modules.auth.dtos.SessaoDTO.builder()
            .id(1L)
            .ipOrigem("127.0.0.1")
            .userAgent("ua")
            .ativo(true)
            .atual(true)
            .usuario(br.com.casadoamor.sgca.modules.auth.dtos.SessaoDTO.UsuarioSessaoDTO.builder().id(1L).nome("Usuario").email("u@example.com").cpf("11122233344").tipo("ADMIN").build())
            .build();
        org.mockito.Mockito.when(sessaoService.listarSessoesAtivas(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString())).thenReturn(java.util.List.of(s));

        // controller calls authService.findUserIdByCpf(...) so we must mock it
        org.mockito.Mockito.when(authService.findUserIdByCpf(org.mockito.ArgumentMatchers.anyString())).thenReturn(java.util.Optional.of(1L));

        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken("11122233344", null);
        String listJson = mvc.perform(get("/auth/sessions").principal(auth).header("Authorization", "Bearer tok"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(listJson).contains("u@example.com");
    }
}
