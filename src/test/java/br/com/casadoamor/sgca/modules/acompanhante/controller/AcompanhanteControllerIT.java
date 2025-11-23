package br.com.casadoamor.sgca.modules.acompanhante.controller;

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

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.service.AcompanhanteService;
import br.com.casadoamor.sgca.modules.common.dto.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.common.enums.EstadoEnum;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;

@WebMvcTest(controllers = AcompanhanteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AcompanhanteControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AcompanhanteService acompanhanteService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void registrar_and_list_and_delete_flow() throws Exception {
        // prepare a DTO to be returned by the mocked service
        var dadoPessoalDto = DadoPessoalDTO.builder().nome("AcName").cpf("11122233344").rg("7654321").dataNascimento(java.time.LocalDate.of(1995,5,5)).telefone("+551198888777").build();
        var enderecoDto = EnderecoDTO.builder().logradouro("Rua A").bairro("B").cidade("C").estado(EstadoEnum.SAO_PAULO).cep("22222-222").build();
        var created = AcompanhanteDTO.builder().id("created-id-1").podeAjudarNaCozinha(true).dadoPessoal(dadoPessoalDto).endereco(enderecoDto).parentesco(null).ativo(true).pacienteNome("Paciente X").build();

        // Build payload for registrar acompanhante
        String payload = "{" +
            "\"podeAjudarNaCozinha\": true," +
            "\"dadoPessoal\": {\"nome\": \"AcName\", \"cpf\": \"11122233344\", \"rg\": \"7654321\", \"dataNascimento\": \"1995-05-05\", \"telefone\": \"+551198888777\", \"naturalidade\": \"N\", \"profissao\": \"P\"}," +
            "\"endereco\": {\"logradouro\": \"Rua A\", \"bairro\": \"B\", \"cidade\": \"C\", \"estado\": \"SP\", \"cep\": \"22222-222\"}," +
            "\"parentesco\": \"OUTRO\"," +
            "\"pacienteId\": \"some-paciente-id\" }";

        org.mockito.Mockito.when(acompanhanteService.registrarAcompanhante(org.mockito.ArgumentMatchers.any())).thenReturn(created);

        String response = mvc.perform(post("/acompanhantes/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // assert created and present
        assertThat(response).contains("AcName");

        // fetch list by paciente
        var page = PaginatedResponseDTO.<AcompanhanteDTO>builder().nodes(java.util.List.of(created)).hasNextPage(false).hasPreviousPage(false).totalCount(1).build();
        org.mockito.Mockito.when(acompanhanteService.listarAcompanhantesPorPaciente(org.mockito.ArgumentMatchers.eq("some-paciente-id"), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(page);

        String listJson = mvc.perform(get("/acompanhantes/paciente/some-paciente-id")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(listJson).contains("AcName");

        // delete the created acompanhante - we rely on the mocked service to handle deletion
        org.mockito.Mockito.doNothing().when(acompanhanteService).deletarAcompanhante(created.getId());

        mvc.perform(delete("/acompanhantes/" + created.getId()))
            .andExpect(status().isNoContent());

        org.mockito.Mockito.verify(acompanhanteService).deletarAcompanhante(created.getId());
    }
}
