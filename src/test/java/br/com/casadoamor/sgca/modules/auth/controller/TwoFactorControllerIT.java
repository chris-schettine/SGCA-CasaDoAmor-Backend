package br.com.casadoamor.sgca.modules.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.casadoamor.sgca.modules.auth.dtos.response.AuthResponseDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.twofactor.Enable2FADTO;
import br.com.casadoamor.sgca.modules.auth.dtos.twofactor.Setup2FADTO;
import br.com.casadoamor.sgca.modules.auth.dtos.twofactor.Verify2FADTO;

@WebMvcTest(controllers = TwoFactorController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TwoFactorControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.auth.service.TwoFactorService twoFactorService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.auth.service.AuthService authService;

    // JwtAuthenticationFilter dependencies
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void setup_enable_verify_and_resend_flow() throws Exception {
        // setup
        org.mockito.Mockito.when(authService.buscarIdPorCpf(org.mockito.ArgumentMatchers.anyString())).thenReturn(1L);
        Setup2FADTO setupDto = new Setup2FADTO("ok", true, "u@example.com");
        org.mockito.Mockito.when(twoFactorService.configurar2FA(org.mockito.ArgumentMatchers.eq(1L))).thenReturn(setupDto);

        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken("11122233344", null);

        String setupJson = mvc.perform(post("/auth/2fa/setup").principal(auth))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(setupJson).contains("u@example.com");

        // enable
        Enable2FADTO enableDto = new Enable2FADTO("123456", true);
        org.mockito.Mockito.doNothing().when(twoFactorService).alterarStatus2FA(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Enable2FADTO.class));

        String enablePayload = "{\"codigo\":\"123456\", \"habilitar\": true}";
        String enableRes = mvc.perform(post("/auth/2fa/enable").principal(auth).contentType(MediaType.APPLICATION_JSON).content(enablePayload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(enableRes).contains("2FA habilitado");

        // verify (valid code)
        Verify2FADTO verifyDto = new Verify2FADTO("11122233344", "123456");
        org.mockito.Mockito.when(twoFactorService.validarCodigoLogin(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("123456"))).thenReturn(true);
        org.mockito.Mockito.when(authService.gerarTokenPorCpf(org.mockito.ArgumentMatchers.eq("11122233344"))).thenReturn(AuthResponseDTO.builder().token("tok").tipo("Bearer").email("u@example.com").nome("Usuario").build());

        String verifyPayload = "{\"cpf\":\"11122233344\",\"codigo\":\"123456\"}";
        String verifyRes = mvc.perform(post("/auth/2fa/verify").contentType(MediaType.APPLICATION_JSON).content(verifyPayload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(verifyRes).contains("tok");

        // verify (invalid code)
        org.mockito.Mockito.when(twoFactorService.validarCodigoLogin(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("000000"))).thenReturn(false);

        String badVerifyPayload = "{\"cpf\":\"11122233344\",\"codigo\":\"000000\"}";
        mvc.perform(post("/auth/2fa/verify").contentType(MediaType.APPLICATION_JSON).content(badVerifyPayload))
            .andExpect(status().isUnauthorized());

        // resend
        org.mockito.Mockito.doNothing().when(twoFactorService).enviarCodigoLogin(org.mockito.ArgumentMatchers.eq(1L));

        String resendRes = mvc.perform(post("/auth/2fa/resend").principal(auth))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(resendRes).contains("Novo").contains("enviado");
    }
}
