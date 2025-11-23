package br.com.casadoamor.sgca.modules.agendamento.controller;

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

import br.com.casadoamor.sgca.modules.agendamento.dto.TipoServicoResponseDTO;

@WebMvcTest(controllers = TipoServicoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TipoServicoControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.agendamento.service.TipoServicoService tipoServicoService;

    // jwt deps
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void buscarPorId_and_listarAtivos() throws Exception {
        TipoServicoResponseDTO dto = TipoServicoResponseDTO.builder().id(2L).nome("Consulta").build();
        org.mockito.Mockito.when(tipoServicoService.buscarPorId(org.mockito.ArgumentMatchers.eq(2L))).thenReturn(dto);
        org.mockito.Mockito.when(tipoServicoService.listarAtivos()).thenReturn(List.of(dto));

        String found = mvc.perform(get("/api/tipos-servico/2").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(found).contains("Consulta");

        String list = mvc.perform(get("/api/tipos-servico").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(list).contains("Consulta");
    }
}
