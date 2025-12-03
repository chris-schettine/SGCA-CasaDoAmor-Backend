package br.com.casadoamor.sgca.modules.dadoClinico.controller;

import br.com.casadoamor.sgca.modules.common.enums.*;
import br.com.casadoamor.sgca.modules.dadoClinico.service.DadoClinicoService;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoClinicoInputDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DadoClinicoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DadoClinicoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private DadoClinicoService dadoClinicoService;

        // JwtAuthenticationFilter dependencies
        @MockBean
        private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

        @MockBean
        private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

        @MockBean
        private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

        private static final String PACIENTE_ID = UUID.randomUUID().toString();
        private static final String DADO_CLINICO_ID = UUID.randomUUID().toString();

        private DadoClinicoDTO criarDadoClinicoDTO() {
                return DadoClinicoDTO.builder()
                                .id(DADO_CLINICO_ID)
                                .diagnostico("Câncer de Mama")
                                .tratamento(TipoTratamento.QUIMIOTERAPIA)
                                .condicaoChegada(CondicaoChegada.AMBULANCIA)
                                .usaSonda(false)
                                .usaCurativo(false)
                                .usaOxigenoterapia(true)
                                .tipoSanguineo(TipoSanguineoEnum.A_POSITIVO)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
        }

        private DadoClinicoInputDTO criarDadoClinicoInputDTO() {
                return new DadoClinicoInputDTO(
                                "Câncer de Mama",
                                TipoTratamento.QUIMIOTERAPIA,
                                null,
                                CondicaoChegada.AMBULANCIA,
                                false,
                                null,
                                null,
                                null,
                                null,
                                false,
                                true,
                                TipoSanguineoEnum.A_POSITIVO);
        }

        @Nested
        @DisplayName("Testes de criação de dados clínicos")
        class CriarDadoClinicoTests {

                @Test
                @DisplayName("Deve criar dado clínico com sucesso")
                void deveCriarDadoClinico() throws Exception {
                        DadoClinicoInputDTO request = criarDadoClinicoInputDTO();
                        DadoClinicoDTO response = criarDadoClinicoDTO();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(DADO_CLINICO_ID))
                                        .andExpect(jsonPath("$.diagnostico").value("Câncer de Mama"))
                                        .andExpect(jsonPath("$.tratamento").value("QUIMIOTERAPIA"))
                                        .andExpect(jsonPath("$.tipoSanguineo").value("A_POSITIVO"));

                        verify(dadoClinicoService).criarDadoClinico(eq(PACIENTE_ID), any(DadoClinicoInputDTO.class));
                }

                @Test
                @DisplayName("Deve criar dado clínico com todos os campos de sonda preenchidos")
                void deveCriarDadoClinicoComSonda() throws Exception {
                        DadoClinicoInputDTO request = new DadoClinicoInputDTO(
                                        "Câncer de Pulmão",
                                        TipoTratamento.RADIOTERAPIA,
                                        null,
                                        CondicaoChegada.MACA,
                                        true,
                                        TipoSondaNasal.SNG,
                                        TipoSondaCirurgica.G,
                                        TipoSondaVesical.FOLEY,
                                        "Sonda especial",
                                        true,
                                        true,
                                        TipoSanguineoEnum.O_NEGATIVO);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .diagnostico("Câncer de Pulmão")
                                        .tratamento(TipoTratamento.RADIOTERAPIA)
                                        .condicaoChegada(CondicaoChegada.MACA)
                                        .usaSonda(true)
                                        .tipoSondaNasal(TipoSondaNasal.SNG)
                                        .tipoSondaCirurgica(TipoSondaCirurgica.G)
                                        .tipoSondaVesical(TipoSondaVesical.FOLEY)
                                        .sondaOutraDescricao("Sonda especial")
                                        .usaCurativo(true)
                                        .usaOxigenoterapia(true)
                                        .tipoSanguineo(TipoSanguineoEnum.O_NEGATIVO)
                                        .build();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.usaSonda").value(true))
                                        .andExpect(jsonPath("$.tipoSondaNasal").value("SNG"))
                                        .andExpect(jsonPath("$.tipoSondaCirurgica").value("G"))
                                        .andExpect(jsonPath("$.tipoSondaVesical").value("FOLEY"));
                }

                @Test
                @DisplayName("Deve criar dado clínico com tratamento OUTRO e descrição")
                void deveCriarDadoClinicoComTratamentoOutro() throws Exception {
                        DadoClinicoInputDTO request = new DadoClinicoInputDTO(
                                        "Diagnóstico complexo",
                                        TipoTratamento.OUTRO,
                                        "Tratamento experimental",
                                        CondicaoChegada.CADEIRA_RODAS,
                                        false,
                                        null,
                                        null,
                                        null,
                                        null,
                                        false,
                                        false,
                                        TipoSanguineoEnum.AB_POSITIVO);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .diagnostico("Diagnóstico complexo")
                                        .tratamento(TipoTratamento.OUTRO)
                                        .tratamentoOutroDescricao("Tratamento experimental")
                                        .condicaoChegada(CondicaoChegada.CADEIRA_RODAS)
                                        .tipoSanguineo(TipoSanguineoEnum.AB_POSITIVO)
                                        .build();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.tratamento").value("OUTRO"))
                                        .andExpect(jsonPath("$.tratamentoOutroDescricao").value("Tratamento experimental"));
                }

                @Test
                @DisplayName("Deve retornar erro quando paciente não é encontrado")
                void deveRetornarErroQuandoPacienteNaoEncontrado() throws Exception {
                        DadoClinicoInputDTO request = criarDadoClinicoInputDTO();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenThrow(new RuntimeException("Paciente não encontrado com ID: " + PACIENTE_ID));

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isInternalServerError());
                }

                @Test
                @DisplayName("Deve criar dado clínico com todos os tipos de condição de chegada")
                void deveCriarDadoClinicoComCondicaoNenhuma() throws Exception {
                        DadoClinicoInputDTO request = new DadoClinicoInputDTO(
                                        "Diagnóstico",
                                        TipoTratamento.AMBOS,
                                        null,
                                        CondicaoChegada.NENHUMA,
                                        false,
                                        null,
                                        null,
                                        null,
                                        null,
                                        false,
                                        false,
                                        TipoSanguineoEnum.B_NEGATIVO);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .condicaoChegada(CondicaoChegada.NENHUMA)
                                        .tratamento(TipoTratamento.AMBOS)
                                        .tipoSanguineo(TipoSanguineoEnum.B_NEGATIVO)
                                        .build();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.condicaoChegada").value("NENHUMA"))
                                        .andExpect(jsonPath("$.tratamento").value("AMBOS"));
                }
        }

        @Nested
        @DisplayName("Testes de atualização de dados clínicos")
        class AtualizarDadoClinicoTests {

                @Test
                @DisplayName("Deve atualizar dado clínico com sucesso")
                void deveAtualizarDadoClinico() throws Exception {
                        EditarDadoClinicoInputDTO request = new EditarDadoClinicoInputDTO(
                                        "Diagnóstico Atualizado",
                                        TipoTratamento.RADIOTERAPIA,
                                        null,
                                        CondicaoChegada.CADEIRA_RODAS,
                                        true,
                                        TipoSondaNasal.SNE,
                                        null,
                                        null,
                                        null,
                                        true,
                                        true,
                                        TipoSanguineoEnum.B_POSITIVO);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .diagnostico("Diagnóstico Atualizado")
                                        .tratamento(TipoTratamento.RADIOTERAPIA)
                                        .tipoSanguineo(TipoSanguineoEnum.B_POSITIVO)
                                        .build();

                        when(dadoClinicoService.atualizarDadoClinico(anyString(), any(EditarDadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/{id}/pacientes/{pacienteId}", DADO_CLINICO_ID, PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.diagnostico").value("Diagnóstico Atualizado"))
                                        .andExpect(jsonPath("$.tratamento").value("RADIOTERAPIA"));

                        verify(dadoClinicoService).atualizarDadoClinico(eq(DADO_CLINICO_ID), any(EditarDadoClinicoInputDTO.class));
                }

                @Test
                @DisplayName("Deve retornar erro ao atualizar dado clínico inexistente")
                void deveRetornarErroAoAtualizarDadoClinicoInexistente() throws Exception {
                        EditarDadoClinicoInputDTO request = new EditarDadoClinicoInputDTO(
                                        "Diagnóstico",
                                        TipoTratamento.QUIMIOTERAPIA,
                                        null,
                                        CondicaoChegada.AMBULANCIA,
                                        false,
                                        null,
                                        null,
                                        null,
                                        null,
                                        false,
                                        false,
                                        TipoSanguineoEnum.A_POSITIVO);

                        when(dadoClinicoService.atualizarDadoClinico(anyString(), any(EditarDadoClinicoInputDTO.class)))
                                        .thenThrow(new RuntimeException("Dado clínico não encontrado com ID: " + DADO_CLINICO_ID));

                        mockMvc.perform(post("/dados-clinicos/{id}/pacientes/{pacienteId}", DADO_CLINICO_ID, PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isInternalServerError());
                }

                @Test
                @DisplayName("Deve atualizar apenas campos específicos do dado clínico")
                void deveAtualizarApenasAlgunsCampos() throws Exception {
                        EditarDadoClinicoInputDTO request = new EditarDadoClinicoInputDTO(
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        true,
                                        null,
                                        null);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .usaCurativo(true)
                                        .build();

                        when(dadoClinicoService.atualizarDadoClinico(anyString(), any(EditarDadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/{id}/pacientes/{pacienteId}", DADO_CLINICO_ID, PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.usaCurativo").value(true));
                }
        }

        @Nested
        @DisplayName("Testes de listagem de dados clínicos")
        class ListarDadosClinicosTests {

                @Test
                @DisplayName("Deve listar dados clínicos por paciente")
                void deveListarDadosClinicos() throws Exception {
                        when(dadoClinicoService.buscarDadosClinicosPorPaciente(anyString()))
                                        .thenReturn(Collections.emptyList());

                        mockMvc.perform(get("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$", hasSize(0)));

                        verify(dadoClinicoService).buscarDadosClinicosPorPaciente(PACIENTE_ID);
                }

                @Test
                @DisplayName("Deve retornar lista com múltiplos dados clínicos")
                void deveRetornarListaComMultiplosDadosClinicos() throws Exception {
                        DadoClinicoDTO dado1 = DadoClinicoDTO.builder()
                                        .id(UUID.randomUUID().toString())
                                        .diagnostico("Diagnóstico 1")
                                        .tratamento(TipoTratamento.QUIMIOTERAPIA)
                                        .tipoSanguineo(TipoSanguineoEnum.A_POSITIVO)
                                        .createdAt(LocalDateTime.now().minusDays(10))
                                        .build();

                        DadoClinicoDTO dado2 = DadoClinicoDTO.builder()
                                        .id(UUID.randomUUID().toString())
                                        .diagnostico("Diagnóstico 2")
                                        .tratamento(TipoTratamento.RADIOTERAPIA)
                                        .tipoSanguineo(TipoSanguineoEnum.A_POSITIVO)
                                        .createdAt(LocalDateTime.now().minusDays(5))
                                        .build();

                        DadoClinicoDTO dado3 = DadoClinicoDTO.builder()
                                        .id(UUID.randomUUID().toString())
                                        .diagnostico("Diagnóstico 3")
                                        .tratamento(TipoTratamento.AMBOS)
                                        .tipoSanguineo(TipoSanguineoEnum.A_POSITIVO)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        List<DadoClinicoDTO> listaDados = Arrays.asList(dado3, dado2, dado1);

                        when(dadoClinicoService.buscarDadosClinicosPorPaciente(anyString()))
                                        .thenReturn(listaDados);

                        mockMvc.perform(get("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$", hasSize(3)))
                                        .andExpect(jsonPath("$[0].diagnostico").value("Diagnóstico 3"))
                                        .andExpect(jsonPath("$[1].diagnostico").value("Diagnóstico 2"))
                                        .andExpect(jsonPath("$[2].diagnostico").value("Diagnóstico 1"));
                }
        }

        @Nested
        @DisplayName("Testes de busca de dado clínico atual")
        class BuscarDadoClinicoAtualTests {

                @Test
                @DisplayName("Deve buscar dado clínico atual com sucesso")
                void deveBuscarDadoClinicoAtual() throws Exception {
                        DadoClinicoDTO response = criarDadoClinicoDTO();

                        when(dadoClinicoService.buscarDadoClinicoAtual(anyString()))
                                        .thenReturn(response);

                        mockMvc.perform(get("/dados-clinicos/pacientes/{pacienteId}/atual", PACIENTE_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(DADO_CLINICO_ID))
                                        .andExpect(jsonPath("$.diagnostico").value("Câncer de Mama"));

                        verify(dadoClinicoService).buscarDadoClinicoAtual(PACIENTE_ID);
                }

                @Test
                @DisplayName("Deve retornar erro quando nenhum dado clínico é encontrado para o paciente")
                void deveRetornarErroQuandoNenhumDadoClinicoEncontrado() throws Exception {
                        when(dadoClinicoService.buscarDadoClinicoAtual(anyString()))
                                        .thenThrow(new RuntimeException("Nenhum dado clínico encontrado para o paciente: " + PACIENTE_ID));

                        mockMvc.perform(get("/dados-clinicos/pacientes/{pacienteId}/atual", PACIENTE_ID))
                                        .andExpect(status().isInternalServerError());
                }

                @Test
                @DisplayName("Deve retornar dado clínico atual com todos os campos preenchidos")
                void deveRetornarDadoClinicoAtualCompleto() throws Exception {
                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .diagnostico("Câncer de Próstata")
                                        .tratamento(TipoTratamento.AMBOS)
                                        .tratamentoOutroDescricao(null)
                                        .condicaoChegada(CondicaoChegada.MACA)
                                        .usaSonda(true)
                                        .tipoSondaNasal(TipoSondaNasal.OROGASTRICA)
                                        .tipoSondaCirurgica(TipoSondaCirurgica.GJ)
                                        .tipoSondaVesical(TipoSondaVesical.CISTOSTOMIA)
                                        .sondaOutraDescricao("Descrição adicional")
                                        .usaCurativo(true)
                                        .usaOxigenoterapia(true)
                                        .tipoSanguineo(TipoSanguineoEnum.AB_NEGATIVO)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                        when(dadoClinicoService.buscarDadoClinicoAtual(anyString()))
                                        .thenReturn(response);

                        mockMvc.perform(get("/dados-clinicos/pacientes/{pacienteId}/atual", PACIENTE_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.diagnostico").value("Câncer de Próstata"))
                                        .andExpect(jsonPath("$.tratamento").value("AMBOS"))
                                        .andExpect(jsonPath("$.condicaoChegada").value("MACA"))
                                        .andExpect(jsonPath("$.usaSonda").value(true))
                                        .andExpect(jsonPath("$.tipoSondaNasal").value("OROGASTRICA"))
                                        .andExpect(jsonPath("$.tipoSondaCirurgica").value("GJ"))
                                        .andExpect(jsonPath("$.tipoSondaVesical").value("CISTOSTOMIA"))
                                        .andExpect(jsonPath("$.usaCurativo").value(true))
                                        .andExpect(jsonPath("$.usaOxigenoterapia").value(true))
                                        .andExpect(jsonPath("$.tipoSanguineo").value("AB_NEGATIVO"));
                }
        }

        @Nested
        @DisplayName("Testes de busca de dado clínico por ID")
        class BuscarPorIdTests {

                @Test
                @DisplayName("Deve buscar dado clínico por ID com sucesso")
                void deveBuscarPorId() throws Exception {
                        DadoClinicoDTO response = criarDadoClinicoDTO();

                        when(dadoClinicoService.buscarPorId(anyString()))
                                        .thenReturn(response);

                        mockMvc.perform(get("/dados-clinicos/{id}/pacientes/{pacienteId}", DADO_CLINICO_ID, PACIENTE_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(DADO_CLINICO_ID))
                                        .andExpect(jsonPath("$.diagnostico").value("Câncer de Mama"));

                        verify(dadoClinicoService).buscarPorId(DADO_CLINICO_ID);
                }

                @Test
                @DisplayName("Deve retornar erro quando dado clínico não é encontrado por ID")
                void deveRetornarErroQuandoDadoClinicoNaoEncontrado() throws Exception {
                        when(dadoClinicoService.buscarPorId(anyString()))
                                        .thenThrow(new RuntimeException("Dado clínico não encontrado com ID: " + DADO_CLINICO_ID));

                        mockMvc.perform(get("/dados-clinicos/{id}/pacientes/{pacienteId}", DADO_CLINICO_ID, PACIENTE_ID))
                                        .andExpect(status().isInternalServerError());
                }

                @Test
                @DisplayName("Deve retornar dado clínico com sonda vesical OUTRA")
                void deveRetornarDadoClinicoComSondaVesicalOutra() throws Exception {
                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .diagnostico("Diagnóstico teste")
                                        .usaSonda(true)
                                        .tipoSondaVesical(TipoSondaVesical.OUTRA)
                                        .sondaOutraDescricao("Sonda personalizada")
                                        .tipoSanguineo(TipoSanguineoEnum.O_POSITIVO)
                                        .build();

                        when(dadoClinicoService.buscarPorId(anyString()))
                                        .thenReturn(response);

                        mockMvc.perform(get("/dados-clinicos/{id}/pacientes/{pacienteId}", DADO_CLINICO_ID, PACIENTE_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.tipoSondaVesical").value("OUTRA"))
                                        .andExpect(jsonPath("$.sondaOutraDescricao").value("Sonda personalizada"));
                }
        }

        @Nested
        @DisplayName("Testes de tipos sanguíneos")
        class TiposSanguineosTests {

                @Test
                @DisplayName("Deve criar dado clínico com tipo sanguíneo O_POSITIVO")
                void deveCriarComTipoSanguineoOPositivo() throws Exception {
                        DadoClinicoInputDTO request = new DadoClinicoInputDTO(
                                        "Diagnóstico",
                                        TipoTratamento.QUIMIOTERAPIA,
                                        null,
                                        CondicaoChegada.AMBULANCIA,
                                        false,
                                        null,
                                        null,
                                        null,
                                        null,
                                        false,
                                        false,
                                        TipoSanguineoEnum.O_POSITIVO);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .tipoSanguineo(TipoSanguineoEnum.O_POSITIVO)
                                        .build();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.tipoSanguineo").value("O_POSITIVO"));
                }

                @Test
                @DisplayName("Deve criar dado clínico com tipo sanguíneo AB_NEGATIVO")
                void deveCriarComTipoSanguineoABNegativo() throws Exception {
                        DadoClinicoInputDTO request = new DadoClinicoInputDTO(
                                        "Diagnóstico",
                                        TipoTratamento.RADIOTERAPIA,
                                        null,
                                        CondicaoChegada.CADEIRA_RODAS,
                                        false,
                                        null,
                                        null,
                                        null,
                                        null,
                                        false,
                                        false,
                                        TipoSanguineoEnum.AB_NEGATIVO);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .tipoSanguineo(TipoSanguineoEnum.AB_NEGATIVO)
                                        .build();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.tipoSanguineo").value("AB_NEGATIVO"));
                }
        }

        @Nested
        @DisplayName("Testes de tipos de sonda")
        class TiposSondaTests {

                @Test
                @DisplayName("Deve criar dado clínico com sonda nasal SNE")
                void deveCriarComSondaNasalSNE() throws Exception {
                        DadoClinicoInputDTO request = new DadoClinicoInputDTO(
                                        "Diagnóstico",
                                        TipoTratamento.QUIMIOTERAPIA,
                                        null,
                                        CondicaoChegada.AMBULANCIA,
                                        true,
                                        TipoSondaNasal.SNE,
                                        null,
                                        null,
                                        null,
                                        false,
                                        false,
                                        TipoSanguineoEnum.A_POSITIVO);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .usaSonda(true)
                                        .tipoSondaNasal(TipoSondaNasal.SNE)
                                        .build();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.tipoSondaNasal").value("SNE"));
                }

                @Test
                @DisplayName("Deve criar dado clínico com sonda cirúrgica J (Jejunostomia)")
                void deveCriarComSondaCirurgicaJ() throws Exception {
                        DadoClinicoInputDTO request = new DadoClinicoInputDTO(
                                        "Diagnóstico",
                                        TipoTratamento.QUIMIOTERAPIA,
                                        null,
                                        CondicaoChegada.MACA,
                                        true,
                                        null,
                                        TipoSondaCirurgica.J,
                                        null,
                                        null,
                                        false,
                                        false,
                                        TipoSanguineoEnum.B_POSITIVO);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .usaSonda(true)
                                        .tipoSondaCirurgica(TipoSondaCirurgica.J)
                                        .build();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.tipoSondaCirurgica").value("J"));
                }

                @Test
                @DisplayName("Deve criar dado clínico sem sonda vesical (NAO)")
                void deveCriarSemSondaVesical() throws Exception {
                        DadoClinicoInputDTO request = new DadoClinicoInputDTO(
                                        "Diagnóstico",
                                        TipoTratamento.QUIMIOTERAPIA,
                                        null,
                                        CondicaoChegada.NENHUMA,
                                        true,
                                        null,
                                        null,
                                        TipoSondaVesical.NAO,
                                        null,
                                        false,
                                        false,
                                        TipoSanguineoEnum.A_NEGATIVO);

                        DadoClinicoDTO response = DadoClinicoDTO.builder()
                                        .id(DADO_CLINICO_ID)
                                        .usaSonda(true)
                                        .tipoSondaVesical(TipoSondaVesical.NAO)
                                        .build();

                        when(dadoClinicoService.criarDadoClinico(anyString(), any(DadoClinicoInputDTO.class)))
                                        .thenReturn(response);

                        mockMvc.perform(post("/dados-clinicos/pacientes/{pacienteId}", PACIENTE_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.tipoSondaVesical").value("NAO"));
                }
        }
}
