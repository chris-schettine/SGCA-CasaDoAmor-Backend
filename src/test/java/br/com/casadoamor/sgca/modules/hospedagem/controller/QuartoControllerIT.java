package br.com.casadoamor.sgca.modules.hospedagem.controller;

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

import br.com.casadoamor.sgca.modules.hospedagem.dto.QuartoRequestDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.QuartoResponseDTO;
import br.com.casadoamor.sgca.modules.hospedagem.service.QuartoService;

@WebMvcTest(controllers = QuartoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class QuartoControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private QuartoService quartoService;

    // security beans
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void create_get_list_and_delete_flow() throws Exception {
        QuartoResponseDTO created = QuartoResponseDTO.builder().uuid("q-1").nome("Quarto 1").codigo("Q1").ativo(true).build();

        org.mockito.Mockito.when(quartoService.cadastrar(org.mockito.ArgumentMatchers.any(QuartoRequestDTO.class), org.mockito.ArgumentMatchers.any())).thenReturn(created);

        String payload = "{" +
            "\"nome\": \"Quarto 1\", \"tipo\": \"INDIVIDUAL\", \"ala\": \"MASCULINA\", \"capacidadeTotal\": 1 }";

        String response = mvc.perform(post("/api/quartos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("Quarto 1");

        org.mockito.Mockito.when(quartoService.buscarPorUuidDTO(org.mockito.ArgumentMatchers.eq("q-1"))).thenReturn(created);

        String got = mvc.perform(get("/api/quartos/q-1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(got).contains("Quarto 1");

        var resumo = br.com.casadoamor.sgca.modules.hospedagem.dto.QuartoResumoDTO.builder().uuid(created.getUuid()).nome(created.getNome()).codigo(created.getCodigo()).ativo(true).build();
        org.mockito.Mockito.when(quartoService.listarTodos()).thenReturn(java.util.List.of(resumo));

        String list = mvc.perform(get("/api/quartos").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(list).contains("Quarto 1");

        org.mockito.Mockito.doNothing().when(quartoService).deletar(org.mockito.ArgumentMatchers.eq("q-1"));

        mvc.perform(delete("/api/quartos/q-1")).andExpect(status().isNoContent());

        org.mockito.Mockito.verify(quartoService).deletar("q-1");
    }
}
