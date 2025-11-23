package br.com.casadoamor.sgca.modules.lgpd.controller;

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

import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDRequestDTO;
import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDResponseDTO;

@WebMvcTest(controllers = ConsentimentoLGPDController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ConsentimentoLGPDControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.lgpd.service.ConsentimentoLGPDService consentimentoService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository authUsuarioRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.funcionario.repository.ProfissionalRepository profissionalRepository;

    // Jwt filter deps
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void registrar_and_list_and_check_user_consent() throws Exception {
        // create user and mock repository lookups
        var usuario = br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.builder().id(1L).cpf("11122233344").nome("User A").build();
        org.mockito.Mockito.when(authUsuarioRepository.findByCpf(org.mockito.ArgumentMatchers.eq("11122233344"))).thenReturn(java.util.Optional.of(usuario));

        // service returns a response
        ConsentimentoLGPDResponseDTO resp = ConsentimentoLGPDResponseDTO.builder().id(10L).versaoTermo("v1").concorda(true).build();
        org.mockito.Mockito.when(consentimentoService.registrarConsentimento(org.mockito.ArgumentMatchers.eq(br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD.TipoEntidadeLGPD.USUARIO), org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(ConsentimentoLGPDRequestDTO.class), org.mockito.ArgumentMatchers.any()))
                .thenReturn(resp);

        String payload = "{\"versaoTermo\":\"v1\",\"concorda\":true}";
        // use a different authenticated user (registrado) to avoid colliding with the target user's CPF
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken("22233344455", null);

        // authenticated user must be found by repository (registrado)
        var registrado = br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.builder().id(2L).cpf("22233344455").nome("Registrar").build();
        org.mockito.Mockito.when(authUsuarioRepository.findByCpf(org.mockito.ArgumentMatchers.eq("22233344455"))).thenReturn(java.util.Optional.of(registrado));

        String created = mvc.perform(post("/api/usuarios/111.222.333-44/consentimentos-lgpd").principal(auth).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(created).contains("v1");

        // listar
        org.mockito.Mockito.when(consentimentoService.listarConsentimentos(org.mockito.ArgumentMatchers.eq(br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD.TipoEntidadeLGPD.USUARIO), org.mockito.ArgumentMatchers.eq(1L)))
                .thenReturn(List.of(resp));

        String listJson = mvc.perform(get("/api/usuarios/11122233344/consentimentos-lgpd").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(listJson).contains("v1");

        // verificar valido
        org.mockito.Mockito.when(consentimentoService.hasConsentimentoValido(org.mockito.ArgumentMatchers.eq(br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD.TipoEntidadeLGPD.USUARIO), org.mockito.ArgumentMatchers.eq(1L))).thenReturn(true);

        String validJson = mvc.perform(get("/api/usuarios/11122233344/consentimentos-lgpd/valido").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(validJson).contains("valido");
    }

    @Test
    void registrar_and_list_professional_consent() throws Exception {
        // professional exists
        var prof = br.com.casadoamor.sgca.modules.funcionario.entity.Profissional.builder().id(7L).uuid("uuid-1").build();
        org.mockito.Mockito.when(profissionalRepository.findByUuid(org.mockito.ArgumentMatchers.eq("uuid-1"))).thenReturn(java.util.Optional.of(prof));

        ConsentimentoLGPDResponseDTO resp = ConsentimentoLGPDResponseDTO.builder().id(11L).versaoTermo("v2").concorda(false).build();
        org.mockito.Mockito.when(consentimentoService.registrarConsentimento(org.mockito.ArgumentMatchers.eq(br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD.TipoEntidadeLGPD.PROFISSIONAL), org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any(ConsentimentoLGPDRequestDTO.class), org.mockito.ArgumentMatchers.any()))
            .thenReturn(resp);

        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken("11122233344", null);

        // authenticated user must be found by repository for professional registration
        var registradoProf = br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.builder().id(3L).cpf("11122233344").nome("RegistrarProf").build();
        org.mockito.Mockito.when(authUsuarioRepository.findByCpf(org.mockito.ArgumentMatchers.eq("11122233344"))).thenReturn(java.util.Optional.of(registradoProf));

        String payload = "{\"versaoTermo\":\"v2\",\"concorda\":false}";
        String created = mvc.perform(post("/api/profissionais/uuid-1/consentimentos").principal(auth).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(created).contains("v2");
    }
}
