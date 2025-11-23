package br.com.casadoamor.sgca.modules.common.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.casadoamor.sgca.modules.common.dto.BuscaInteligenteResponseDTO;

@WebMvcTest(controllers = BuscaInteligenteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BuscaInteligenteControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.common.service.BuscaInteligenteService buscaInteligenteService;

    // JwtAuthenticationFilter dependencies
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void buscar_returns_expected_results() throws Exception {
        var paciente = br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO.builder().id("p1").email("p1@example.com").build();
        var acomp = br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO.builder().id("a1").pacienteNome("Paciente X").build();

        BuscaInteligenteResponseDTO dto = BuscaInteligenteResponseDTO.builder()
                .pacientes(List.of(paciente))
                .acompanhantes(List.of(acomp))
                .build();

        org.mockito.Mockito.when(buscaInteligenteService.buscar(org.mockito.ArgumentMatchers.eq("term"))).thenReturn(dto);

        String res = mvc.perform(get("/busca-inteligente").param("termo", "term").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(res).contains("p1@example.com").contains("Paciente X");
    }
}
