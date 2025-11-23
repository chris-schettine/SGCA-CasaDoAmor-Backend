package br.com.casadoamor.sgca.modules.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TestAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TestAuthControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository authUsuarioRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // jwt deps (not used but present in context)
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void test_password_and_generate_hash_and_list_users() throws Exception {
        var usuario = br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.builder().id(9L).cpf("11122233344").nome("U9").senhaHash("HASH").ativo(true).build();
        org.mockito.Mockito.when(authUsuarioRepository.findByCpf(org.mockito.ArgumentMatchers.eq("11122233344"))).thenReturn(java.util.Optional.of(usuario));
        org.mockito.Mockito.when(passwordEncoder.matches(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())).thenReturn(true);

        String res = mvc.perform(get("/debug-auth/test-password").param("cpf","11122233344").param("senha","s").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(res).contains("usuarioEncontrado").contains("senhaMatches");

        // generate hash
        org.mockito.Mockito.when(passwordEncoder.encode(org.mockito.ArgumentMatchers.eq("abc"))).thenReturn("encoded");
        org.mockito.Mockito.when(passwordEncoder.matches(org.mockito.ArgumentMatchers.eq("abc"), org.mockito.ArgumentMatchers.eq("encoded"))).thenReturn(true);

        String gen = mvc.perform(get("/debug-auth/generate-hash").param("senha","abc").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(gen).contains("hash").contains("selfCheck");

        // list users
        org.mockito.Mockito.when(authUsuarioRepository.findAll()).thenReturn(List.of(usuario));

        String list = mvc.perform(get("/debug-auth/list-users").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(list).contains("U9");
    }
}
