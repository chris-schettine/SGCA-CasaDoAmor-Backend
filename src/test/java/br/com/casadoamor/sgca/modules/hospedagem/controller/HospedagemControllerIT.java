package br.com.casadoamor.sgca.modules.hospedagem.controller;

import java.time.LocalDate;
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

import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemRequestDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemResponseDTO;

@WebMvcTest(controllers = HospedagemController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class HospedagemControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.hospedagem.service.HospedagemService hospedagemService;

    // JwtAuthenticationFilter dependencies
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void registrarEntrada_and_buscar_and_listarAtivas() throws Exception {
        // registrar entrada
        HospedagemRequestDTO req = HospedagemRequestDTO.builder()
                .pacienteId("p1")
                .dataEntrada(LocalDate.now())
                .build();

        HospedagemResponseDTO resp = HospedagemResponseDTO.builder().uuid("h1").pacienteId("p1").build();

        org.mockito.Mockito.when(hospedagemService.registrarEntrada(org.mockito.ArgumentMatchers.any(HospedagemRequestDTO.class), org.mockito.ArgumentMatchers.any()))
                .thenReturn(resp);

        // principal as AuthUsuario
        var user = br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.builder().id(1L).cpf("11122233344").build();
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken(user, null);

        String created = mvc.perform(post("/api/hospedagens").principal(auth).contentType(MediaType.APPLICATION_JSON).content("{\"pacienteId\":\"p1\",\"dataEntrada\":\"2025-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(created).contains("h1");

        // buscar por uuid
        org.mockito.Mockito.when(hospedagemService.buscarPorUuidDTO(org.mockito.ArgumentMatchers.eq("h1"))).thenReturn(resp);

        String found = mvc.perform(get("/api/hospedagens/h1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(found).contains("h1");

        // listar ativas
        org.mockito.Mockito.when(hospedagemService.listarAtivas()).thenReturn(List.of(resp));

        String listJson = mvc.perform(get("/api/hospedagens/ativas").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(listJson).contains("h1");
    }

        @Test
        void transferir_quarto() throws Exception {
                HospedagemResponseDTO resp = HospedagemResponseDTO.builder().uuid("h1").pacienteId("p1").quartoUuid("q2").quartoNome("Quarto B").build();

                org.mockito.Mockito.when(hospedagemService.transferirQuarto(org.mockito.ArgumentMatchers.eq("h1"), org.mockito.ArgumentMatchers.eq("q2"), org.mockito.ArgumentMatchers.eq("Troca"), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(resp);

                // principal as AuthUsuario
                var user = br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.builder().id(1L).cpf("11122233344").build();
                org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken(user, null);

                String result = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/hospedagens/h1/transferir")
                                                .principal(auth)
                                                .param("novoQuartoUuid", "q2")
                                                .param("motivoTransferencia", "Troca")
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

                assertThat(result).contains("q2").contains("Quarto B");
        }
}
