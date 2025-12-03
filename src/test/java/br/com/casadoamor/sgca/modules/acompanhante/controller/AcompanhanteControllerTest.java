package br.com.casadoamor.sgca.modules.acompanhante.controller;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.RegistrarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.service.AcompanhanteService;
import br.com.casadoamor.sgca.modules.common.dto.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoInputDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AcompanhanteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AcompanhanteControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AcompanhanteService acompanhanteService;

        // JwtAuthenticationFilter dependencies
        @MockBean
        private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

        @MockBean
        private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

        @MockBean
        private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

        @Test
        @DisplayName("Deve registrar acompanhante com sucesso")
        void deveRegistrarAcompanhante() throws Exception {
                DadoPessoalInputDTO dadoPessoal = new DadoPessoalInputDTO();
                dadoPessoal.setNome("Nome Teste");
                dadoPessoal.setCpf("123.456.789-00");

                EnderecoInputDTO endereco = new EnderecoInputDTO();
                endereco.setLogradouro("Rua Teste");
                endereco.setNumero(123);

                RegistrarAcompanhanteDTO request = new RegistrarAcompanhanteDTO(
                                dadoPessoal,
                                endereco,
                                Parentesco.OUTRO,
                                "uuid-paciente",
                                true);

                AcompanhanteDTO response = AcompanhanteDTO.builder()
                                .id(UUID.randomUUID().toString())
                                .dadoPessoal(DadoPessoalDTO.builder().nome("Nome Teste").build())
                                .build();

                when(acompanhanteService.registrarAcompanhante(any(RegistrarAcompanhanteDTO.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/acompanhantes/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.dadoPessoal.nome").value("Nome Teste"));
        }

        @Test
        @DisplayName("Deve deletar acompanhante")
        void deveDeletarAcompanhante() throws Exception {
                doNothing().when(acompanhanteService).deletarAcompanhante(anyString());

                mockMvc.perform(delete("/acompanhantes/uuid-teste"))
                                .andExpect(status().isNoContent());
        }
}
