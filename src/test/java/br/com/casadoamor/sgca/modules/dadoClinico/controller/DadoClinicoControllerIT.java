package br.com.casadoamor.sgca.modules.dadoClinico.controller;

import java.time.LocalDateTime;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.casadoamor.sgca.modules.dadoClinico.service.DadoClinicoService;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoDTO;

@WebMvcTest(controllers = DadoClinicoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DadoClinicoControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private DadoClinicoService dadoClinicoService;

    // security beans
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void create_and_list_and_get_flow() throws Exception {
        DadoClinicoDTO dto = DadoClinicoDTO.builder().id("dc-1").diagnostico("Hipertensão").createdAt(LocalDateTime.now()).build();

        org.mockito.Mockito.when(dadoClinicoService.criarDadoClinico(org.mockito.ArgumentMatchers.eq("pac-1"), org.mockito.ArgumentMatchers.any())).thenReturn(dto);

        String payload = "{" +
            "\"diagnostico\": \"Hipertensão\", \"usaSonda\": true, \"usaCurativo\": false, \"usaOxigenoterapia\": false, \"tipoSanguineo\": \"O_POSITIVO\" }";

        String response = mvc.perform(post("/dados-clinicos/pacientes/pac-1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("Hipertens");

        // list by paciente
        org.mockito.Mockito.when(dadoClinicoService.buscarDadosClinicosPorPaciente(org.mockito.ArgumentMatchers.eq("pac-1"))).thenReturn(java.util.List.of(dto));

        String listJson = mvc.perform(get("/dados-clinicos/pacientes/pac-1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(listJson).contains("Hipertens");

        // get by id
        org.mockito.Mockito.when(dadoClinicoService.buscarPorId(org.mockito.ArgumentMatchers.eq("dc-1"))).thenReturn(dto);

        String got = mvc.perform(get("/dados-clinicos/dc-1/pacientes/pac-1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(got).contains("Hipertens");
    }
}
