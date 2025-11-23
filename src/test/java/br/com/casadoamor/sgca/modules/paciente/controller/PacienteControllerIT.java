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

import br.com.casadoamor.sgca.modules.common.dto.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.services.PacienteService;

@WebMvcTest(controllers = PacienteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PacienteControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private PacienteService pacienteService;

    // security beans
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void registrar_list_and_delete_flow() throws Exception {
        var dadoPessoal = DadoPessoalDTO.builder()
            .nome("Paciente Teste")
            .cpf("11122233344")
            .rg("12345678")
            .build();

        var created = PacienteDTO.builder().id("paciente-1").dadoPessoal(dadoPessoal).email("p@example.com").build();

        // Mock registrar
        org.mockito.Mockito.when(pacienteService.registrarPaciente(org.mockito.ArgumentMatchers.any())).thenReturn(created);

        // Construct a minimal valid JSON payload for registrar paciente (must include mandatory nested fields)
        String payload = "{" +
            "\"dadoPessoal\": { \"nome\": \"Paciente Teste\", \"dataNascimento\": \"1990-01-01\", \"cpf\": \"11122233344\", \"rg\": \"12345678\", \"naturalidade\": \"Cidade\", \"telefone\": \"+5511999999999\", \"sexo\": \"MASCULINO\" }," +
            "\"dadoClinico\": { \"usaSonda\": true, \"usaCurativo\": true, \"usaOxigenoterapia\": false, \"tipoSanguineo\": \"O_POSITIVO\" }," +
            "\"endereco\": { \"logradouro\": \"Rua Teste\", \"numero\": 1, \"bairro\": \"Bairro\", \"cidade\": \"Cidade\", \"estado\": \"SAO_PAULO\", \"cep\": \"12345-000\" }," +
            "\"email\": \"p@example.com\" }";

        String response = mvc.perform(post("/pacientes/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("Paciente Teste");

        // list paginated
        var page = PaginatedResponseDTO.<PacienteDTO>builder().nodes(java.util.List.of(created)).hasNextPage(false).hasPreviousPage(false).totalCount(1).build();
        org.mockito.Mockito.when(pacienteService.pacientesPaginados(
            org.mockito.ArgumentMatchers.isNull(String.class),
            org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.isNull(br.com.casadoamor.sgca.modules.common.enums.PacienteStatus.class),
            org.mockito.ArgumentMatchers.isNull(String.class),
            org.mockito.ArgumentMatchers.isNull(String.class),
            org.mockito.ArgumentMatchers.isNull(String.class),
            org.mockito.ArgumentMatchers.isNull(String.class),
            org.mockito.ArgumentMatchers.isNull(Integer.class),
            org.mockito.ArgumentMatchers.isNull(Integer.class),
            org.mockito.ArgumentMatchers.isNull(br.com.casadoamor.sgca.modules.common.enums.SexoEnum.class),
            org.mockito.ArgumentMatchers.isNull(String.class),
            org.mockito.ArgumentMatchers.isNull(String.class)
        )).thenReturn(page);

        String listJson = mvc.perform(get("/pacientes/")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(listJson).contains("Paciente Teste");

        // delete flow
        org.mockito.Mockito.doNothing().when(pacienteService).deletarPaciente(created.getId());

        mvc.perform(delete("/pacientes/" + created.getId()))
            .andExpect(status().isNoContent());

        org.mockito.Mockito.verify(pacienteService).deletarPaciente(created.getId());
    }
}
