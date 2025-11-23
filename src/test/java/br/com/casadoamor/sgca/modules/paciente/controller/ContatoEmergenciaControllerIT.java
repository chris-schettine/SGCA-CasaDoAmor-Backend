package br.com.casadoamor.sgca.modules.paciente.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.services.ContatoEmergenciaService;

@WebMvcTest(controllers = ContatoEmergenciaController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ContatoEmergenciaControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private ContatoEmergenciaService service;

    // security beans
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void create_list_and_delete_flow() throws Exception {
        var created = ContatoEmergenciaDTO.builder().id("c-1").nome("Contato A").email("c@example.com").telefone("+5511999999999").pacienteId("pac1").build();

        // mock create
        org.mockito.Mockito.when(service.criar(org.mockito.ArgumentMatchers.eq("pac1"), org.mockito.ArgumentMatchers.any())).thenReturn(created);

        String payload = "{" +
            "\"nome\": \"Contato A\"," +
            "\"email\": \"c@example.com\"," +
            "\"telefone\": \"+5511999999999\" }";

        String response = mvc.perform(post("/contatos-emergencia/pac1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("Contato A");

        // mock list
        org.mockito.Mockito.when(service.listarPorPaciente(org.mockito.ArgumentMatchers.eq("pac1"))).thenReturn(java.util.List.of(created));

        String listJson = mvc.perform(get("/contatos-emergencia/paciente/pac1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(listJson).contains("Contato A");

        // delete
        org.mockito.Mockito.doNothing().when(service).remover(created.getId());

        mvc.perform(delete("/contatos-emergencia/" + created.getId()))
            .andExpect(status().isNoContent());

        org.mockito.Mockito.verify(service).remover(created.getId());
    }
}
