package br.com.casadoamor.sgca.modules.common.controller;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.common.dto.BuscaInteligenteResponseDTO;
import br.com.casadoamor.sgca.modules.common.dto.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.common.enums.PacienteStatus;
import br.com.casadoamor.sgca.modules.common.service.BuscaInteligenteService;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BuscaInteligenteControllerTest {

    @Mock
    private BuscaInteligenteService buscaInteligenteService;

    @InjectMocks
    private BuscaInteligenteController buscaInteligenteController;

    private MockMvc mockMvc;

    private PacienteDTO paciente1;
    private PacienteDTO paciente2;
    private AcompanhanteDTO acompanhante1;
    private AcompanhanteDTO acompanhante2;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(buscaInteligenteController).build();

        paciente1 = PacienteDTO.builder()
                .id("paciente-uuid-1")
                .dadoPessoal(DadoPessoalDTO.builder()
                        .nome("João Silva")
                        .cpf("12345678901")
                        .build())
                .email("joao@email.com")
                .status(PacienteStatus.ATIVO)
                .createdAt(LocalDateTime.now())
                .build();

        paciente2 = PacienteDTO.builder()
                .id("paciente-uuid-2")
                .dadoPessoal(DadoPessoalDTO.builder()
                        .nome("Maria João Santos")
                        .cpf("98765432109")
                        .build())
                .email("maria@email.com")
                .status(PacienteStatus.ATIVO)
                .createdAt(LocalDateTime.now())
                .build();

        acompanhante1 = AcompanhanteDTO.builder()
                .id("acompanhante-uuid-1")
                .dadoPessoal(DadoPessoalDTO.builder()
                        .nome("João Acompanhante")
                        .cpf("11122233344")
                        .build())
                .parentesco(Parentesco.IRMAO)
                .ativo(true)
                .pacienteNome("Maria Silva")
                .build();

        acompanhante2 = AcompanhanteDTO.builder()
                .id("acompanhante-uuid-2")
                .dadoPessoal(DadoPessoalDTO.builder()
                        .nome("Ana Costa")
                        .cpf("55566677788")
                        .build())
                .parentesco(Parentesco.MAE)
                .ativo(true)
                .pacienteNome("Pedro Costa")
                .podeAjudarNaCozinha(true)
                .build();
    }

    @Nested
    @DisplayName("GET /busca-inteligente - Buscar")
    class BuscarTests {

        @Test
        @DisplayName("Deve retornar pacientes e acompanhantes quando termo encontra resultados")
        void buscar_ComResultados_Returns200() throws Exception {
            BuscaInteligenteResponseDTO response = BuscaInteligenteResponseDTO.builder()
                    .pacientes(Arrays.asList(paciente1, paciente2))
                    .acompanhantes(Arrays.asList(acompanhante1))
                    .build();

            when(buscaInteligenteService.buscar("João")).thenReturn(response);

            mockMvc.perform(get("/busca-inteligente")
                            .param("termo", "João")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pacientes").isArray())
                    .andExpect(jsonPath("$.pacientes.length()").value(2))
                    .andExpect(jsonPath("$.pacientes[0].id").value("paciente-uuid-1"))
                    .andExpect(jsonPath("$.pacientes[0].dadoPessoal.nome").value("João Silva"))
                    .andExpect(jsonPath("$.pacientes[1].id").value("paciente-uuid-2"))
                    .andExpect(jsonPath("$.acompanhantes").isArray())
                    .andExpect(jsonPath("$.acompanhantes.length()").value(1))
                    .andExpect(jsonPath("$.acompanhantes[0].id").value("acompanhante-uuid-1"));

            verify(buscaInteligenteService, times(1)).buscar("João");
        }

        @Test
        @DisplayName("Deve retornar apenas pacientes quando termo encontra apenas pacientes")
        void buscar_ApenasPacientes_Returns200() throws Exception {
            BuscaInteligenteResponseDTO response = BuscaInteligenteResponseDTO.builder()
                    .pacientes(List.of(paciente1))
                    .acompanhantes(Collections.emptyList())
                    .build();

            when(buscaInteligenteService.buscar("Silva")).thenReturn(response);

            mockMvc.perform(get("/busca-inteligente")
                            .param("termo", "Silva")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pacientes.length()").value(1))
                    .andExpect(jsonPath("$.pacientes[0].dadoPessoal.nome").value("João Silva"))
                    .andExpect(jsonPath("$.acompanhantes.length()").value(0));

            verify(buscaInteligenteService, times(1)).buscar("Silva");
        }

        @Test
        @DisplayName("Deve retornar apenas acompanhantes quando termo encontra apenas acompanhantes")
        void buscar_ApenasAcompanhantes_Returns200() throws Exception {
            BuscaInteligenteResponseDTO response = BuscaInteligenteResponseDTO.builder()
                    .pacientes(Collections.emptyList())
                    .acompanhantes(Arrays.asList(acompanhante1, acompanhante2))
                    .build();

            when(buscaInteligenteService.buscar("Costa")).thenReturn(response);

            mockMvc.perform(get("/busca-inteligente")
                            .param("termo", "Costa")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pacientes.length()").value(0))
                    .andExpect(jsonPath("$.acompanhantes.length()").value(2))
                    .andExpect(jsonPath("$.acompanhantes[0].parentesco").value("IRMAO"))
                    .andExpect(jsonPath("$.acompanhantes[1].parentesco").value("MAE"));

            verify(buscaInteligenteService, times(1)).buscar("Costa");
        }

        @Test
        @DisplayName("Deve retornar listas vazias quando termo não encontra resultados")
        void buscar_SemResultados_Returns200ComListasVazias() throws Exception {
            BuscaInteligenteResponseDTO response = BuscaInteligenteResponseDTO.builder()
                    .pacientes(Collections.emptyList())
                    .acompanhantes(Collections.emptyList())
                    .build();

            when(buscaInteligenteService.buscar("TermoInexistente")).thenReturn(response);

            mockMvc.perform(get("/busca-inteligente")
                            .param("termo", "TermoInexistente")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pacientes").isArray())
                    .andExpect(jsonPath("$.pacientes.length()").value(0))
                    .andExpect(jsonPath("$.acompanhantes").isArray())
                    .andExpect(jsonPath("$.acompanhantes.length()").value(0));

            verify(buscaInteligenteService, times(1)).buscar("TermoInexistente");
        }

        @Test
        @DisplayName("Deve buscar por CPF")
        void buscar_PorCpf_Returns200() throws Exception {
            BuscaInteligenteResponseDTO response = BuscaInteligenteResponseDTO.builder()
                    .pacientes(List.of(paciente1))
                    .acompanhantes(Collections.emptyList())
                    .build();

            when(buscaInteligenteService.buscar("12345678901")).thenReturn(response);

            mockMvc.perform(get("/busca-inteligente")
                            .param("termo", "12345678901")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pacientes.length()").value(1))
                    .andExpect(jsonPath("$.pacientes[0].dadoPessoal.cpf").value("12345678901"));

            verify(buscaInteligenteService, times(1)).buscar("12345678901");
        }

        @Test
        @DisplayName("Deve buscar termo com espaços")
        void buscar_ComEspacos_Returns200() throws Exception {
            BuscaInteligenteResponseDTO response = BuscaInteligenteResponseDTO.builder()
                    .pacientes(List.of(paciente2))
                    .acompanhantes(Collections.emptyList())
                    .build();

            when(buscaInteligenteService.buscar("Maria João")).thenReturn(response);

            mockMvc.perform(get("/busca-inteligente")
                            .param("termo", "Maria João")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pacientes.length()").value(1))
                    .andExpect(jsonPath("$.pacientes[0].dadoPessoal.nome").value("Maria João Santos"));

            verify(buscaInteligenteService, times(1)).buscar("Maria João");
        }

        @Test
        @DisplayName("Deve validar detalhes do acompanhante retornado")
        void buscar_ValidaDetalhesAcompanhante_Returns200() throws Exception {
            BuscaInteligenteResponseDTO response = BuscaInteligenteResponseDTO.builder()
                    .pacientes(Collections.emptyList())
                    .acompanhantes(List.of(acompanhante2))
                    .build();

            when(buscaInteligenteService.buscar("Ana")).thenReturn(response);

            mockMvc.perform(get("/busca-inteligente")
                            .param("termo", "Ana")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.acompanhantes[0].id").value("acompanhante-uuid-2"))
                    .andExpect(jsonPath("$.acompanhantes[0].dadoPessoal.nome").value("Ana Costa"))
                    .andExpect(jsonPath("$.acompanhantes[0].parentesco").value("MAE"))
                    .andExpect(jsonPath("$.acompanhantes[0].ativo").value(true))
                    .andExpect(jsonPath("$.acompanhantes[0].pacienteNome").value("Pedro Costa"))
                    .andExpect(jsonPath("$.acompanhantes[0].podeAjudarNaCozinha").value(true));

            verify(buscaInteligenteService, times(1)).buscar("Ana");
        }

        @Test
        @DisplayName("Deve validar detalhes do paciente retornado")
        void buscar_ValidaDetalhesPaciente_Returns200() throws Exception {
            BuscaInteligenteResponseDTO response = BuscaInteligenteResponseDTO.builder()
                    .pacientes(List.of(paciente1))
                    .acompanhantes(Collections.emptyList())
                    .build();

            when(buscaInteligenteService.buscar("joao@email.com")).thenReturn(response);

            mockMvc.perform(get("/busca-inteligente")
                            .param("termo", "joao@email.com")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pacientes[0].id").value("paciente-uuid-1"))
                    .andExpect(jsonPath("$.pacientes[0].email").value("joao@email.com"))
                    .andExpect(jsonPath("$.pacientes[0].status").value("ATIVO"));

            verify(buscaInteligenteService, times(1)).buscar("joao@email.com");
        }

        @Test
        @DisplayName("Deve retornar erro 400 quando termo não é fornecido")
        void buscar_SemTermo_Returns400() throws Exception {
            mockMvc.perform(get("/busca-inteligente")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(buscaInteligenteService, never()).buscar(any());
        }

        @Test
        @DisplayName("Deve buscar termo com caracteres especiais")
        void buscar_ComCaracteresEspeciais_Returns200() throws Exception {
            BuscaInteligenteResponseDTO response = BuscaInteligenteResponseDTO.builder()
                    .pacientes(Collections.emptyList())
                    .acompanhantes(Collections.emptyList())
                    .build();

            when(buscaInteligenteService.buscar("José")).thenReturn(response);

            mockMvc.perform(get("/busca-inteligente")
                            .param("termo", "José")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(buscaInteligenteService, times(1)).buscar("José");
        }
    }
}
