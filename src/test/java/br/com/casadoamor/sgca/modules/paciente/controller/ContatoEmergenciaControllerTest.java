package br.com.casadoamor.sgca.modules.paciente.controller;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.services.ContatoEmergenciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContatoEmergenciaControllerTest {

    @Mock
    private ContatoEmergenciaService contatoEmergenciaService;

    @InjectMocks
    private ContatoEmergenciaController contatoEmergenciaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ContatoEmergenciaDTO contato1;
    private ContatoEmergenciaDTO contato2;
    private ContatoEmergenciaInputDTO inputDTO;
    private EditarContatoEmergenciaInputDTO editarInputDTO;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(contatoEmergenciaController)
                .build();
        objectMapper = new ObjectMapper();

        contato1 = ContatoEmergenciaDTO.builder()
                .id("contato-uuid-1")
                .nome("Maria Silva")
                .email("maria@email.com")
                .telefone("+5511999998888")
                .pacienteId("paciente-uuid-1")
                .build();

        contato2 = ContatoEmergenciaDTO.builder()
                .id("contato-uuid-2")
                .nome("José Santos")
                .email("jose@email.com")
                .telefone("+5511888887777")
                .pacienteId("paciente-uuid-1")
                .build();

        inputDTO = ContatoEmergenciaInputDTO.builder()
                .nome("Maria Silva")
                .email("maria@email.com")
                .telefone("+5511999998888")
                .build();

        editarInputDTO = EditarContatoEmergenciaInputDTO.builder()
                .nome("Maria Silva Atualizado")
                .email("maria.atualizado@email.com")
                .telefone("+5511777776666")
                .build();
    }

    @Nested
    @DisplayName("POST /{pacienteId} - Criar Contato de Emergência")
    class CriarContatoTests {

        @Test
        @DisplayName("Deve criar contato de emergência com sucesso")
        void criar_Sucesso_Returns200() throws Exception {
            when(contatoEmergenciaService.criar(eq("paciente-uuid-1"), any(ContatoEmergenciaInputDTO.class)))
                    .thenReturn(contato1);

            mockMvc.perform(post("/contatos-emergencia/paciente-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("contato-uuid-1"))
                    .andExpect(jsonPath("$.nome").value("Maria Silva"))
                    .andExpect(jsonPath("$.email").value("maria@email.com"))
                    .andExpect(jsonPath("$.telefone").value("+5511999998888"))
                    .andExpect(jsonPath("$.pacienteId").value("paciente-uuid-1"));

            verify(contatoEmergenciaService, times(1)).criar(eq("paciente-uuid-1"), any(ContatoEmergenciaInputDTO.class));
        }

        @Test
        @DisplayName("Deve criar contato com dados mínimos válidos")
        void criar_DadosMinimosValidos_Returns200() throws Exception {
            ContatoEmergenciaInputDTO inputMinimo = ContatoEmergenciaInputDTO.builder()
                    .nome("Maria")
                    .email("maria@email.com")
                    .telefone("1234567890")
                    .build();

            ContatoEmergenciaDTO contatoMinimo = ContatoEmergenciaDTO.builder()
                    .id("contato-uuid-minimo")
                    .nome("Maria")
                    .email("maria@email.com")
                    .telefone("1234567890")
                    .pacienteId("paciente-uuid-1")
                    .build();

            when(contatoEmergenciaService.criar(eq("paciente-uuid-1"), any(ContatoEmergenciaInputDTO.class)))
                    .thenReturn(contatoMinimo);

            mockMvc.perform(post("/contatos-emergencia/paciente-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputMinimo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Maria"));

            verify(contatoEmergenciaService, times(1)).criar(eq("paciente-uuid-1"), any(ContatoEmergenciaInputDTO.class));
        }

        @Test
        @DisplayName("Deve criar contato com telefone válido com +")
        void criar_TelefoneComMais_Returns200() throws Exception {
            ContatoEmergenciaInputDTO inputComMais = ContatoEmergenciaInputDTO.builder()
                    .nome("Maria Silva")
                    .email("maria@email.com")
                    .telefone("+5511999998888")
                    .build();

            when(contatoEmergenciaService.criar(eq("paciente-uuid-1"), any(ContatoEmergenciaInputDTO.class)))
                    .thenReturn(contato1);

            mockMvc.perform(post("/contatos-emergencia/paciente-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputComMais)))
                    .andExpect(status().isOk());

            verify(contatoEmergenciaService, times(1)).criar(eq("paciente-uuid-1"), any(ContatoEmergenciaInputDTO.class));
        }

        @Test
        @DisplayName("Deve criar contato com telefone válido sem +")
        void criar_TelefoneSemMais_Returns200() throws Exception {
            ContatoEmergenciaInputDTO inputSemMais = ContatoEmergenciaInputDTO.builder()
                    .nome("Maria Silva")
                    .email("maria@email.com")
                    .telefone("5511999998888")
                    .build();

            when(contatoEmergenciaService.criar(eq("paciente-uuid-1"), any(ContatoEmergenciaInputDTO.class)))
                    .thenReturn(contato1);

            mockMvc.perform(post("/contatos-emergencia/paciente-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputSemMais)))
                    .andExpect(status().isOk());

            verify(contatoEmergenciaService, times(1)).criar(eq("paciente-uuid-1"), any(ContatoEmergenciaInputDTO.class));
        }

        @Test
        @DisplayName("Deve criar contato para outro paciente")
        void criar_OutroPaciente_Returns200() throws Exception {
            ContatoEmergenciaDTO outroContato = ContatoEmergenciaDTO.builder()
                    .id("contato-uuid-outro")
                    .nome("Outro Contato")
                    .email("outro@email.com")
                    .telefone("+5511888887777")
                    .pacienteId("paciente-uuid-2")
                    .build();

            when(contatoEmergenciaService.criar(eq("paciente-uuid-2"), any(ContatoEmergenciaInputDTO.class)))
                    .thenReturn(outroContato);

            mockMvc.perform(post("/contatos-emergencia/paciente-uuid-2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pacienteId").value("paciente-uuid-2"));

            verify(contatoEmergenciaService, times(1)).criar(eq("paciente-uuid-2"), any(ContatoEmergenciaInputDTO.class));
        }
    }

    @Nested
    @DisplayName("GET /paciente/{pacienteId} - Listar Contatos de Emergência")
    class ListarContatosTests {

        @Test
        @DisplayName("Deve listar contatos de emergência do paciente")
        void listar_ComResultados_Returns200() throws Exception {
            List<ContatoEmergenciaDTO> contatos = Arrays.asList(contato1, contato2);
            when(contatoEmergenciaService.listarPorPaciente("paciente-uuid-1")).thenReturn(contatos);

            mockMvc.perform(get("/contatos-emergencia/paciente/paciente-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value("contato-uuid-1"))
                    .andExpect(jsonPath("$[0].nome").value("Maria Silva"))
                    .andExpect(jsonPath("$[1].id").value("contato-uuid-2"))
                    .andExpect(jsonPath("$[1].nome").value("José Santos"));

            verify(contatoEmergenciaService, times(1)).listarPorPaciente("paciente-uuid-1");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando paciente não tem contatos")
        void listar_SemContatos_Returns200ComListaVazia() throws Exception {
            when(contatoEmergenciaService.listarPorPaciente("paciente-uuid-2"))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/contatos-emergencia/paciente/paciente-uuid-2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(contatoEmergenciaService, times(1)).listarPorPaciente("paciente-uuid-2");
        }

        @Test
        @DisplayName("Deve retornar único contato")
        void listar_UmContato_Returns200() throws Exception {
            when(contatoEmergenciaService.listarPorPaciente("paciente-uuid-1"))
                    .thenReturn(List.of(contato1));

            mockMvc.perform(get("/contatos-emergencia/paciente/paciente-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].email").value("maria@email.com"))
                    .andExpect(jsonPath("$[0].telefone").value("+5511999998888"));

            verify(contatoEmergenciaService, times(1)).listarPorPaciente("paciente-uuid-1");
        }
    }

    @Nested
    @DisplayName("PUT /{id} - Atualizar Contato de Emergência")
    class AtualizarContatoTests {

        @Test
        @DisplayName("Deve atualizar contato de emergência com sucesso")
        void atualizar_Sucesso_Returns200() throws Exception {
            ContatoEmergenciaDTO contatoAtualizado = ContatoEmergenciaDTO.builder()
                    .id("contato-uuid-1")
                    .nome("Maria Silva Atualizado")
                    .email("maria.atualizado@email.com")
                    .telefone("+5511777776666")
                    .pacienteId("paciente-uuid-1")
                    .build();

            when(contatoEmergenciaService.atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class)))
                    .thenReturn(contatoAtualizado);

            mockMvc.perform(put("/contatos-emergencia/contato-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editarInputDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("contato-uuid-1"))
                    .andExpect(jsonPath("$.nome").value("Maria Silva Atualizado"))
                    .andExpect(jsonPath("$.email").value("maria.atualizado@email.com"))
                    .andExpect(jsonPath("$.telefone").value("+5511777776666"));

            verify(contatoEmergenciaService, times(1)).atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class));
        }

        @Test
        @DisplayName("Deve atualizar apenas nome")
        void atualizar_ApenasNome_Returns200() throws Exception {
            EditarContatoEmergenciaInputDTO apenasNome = EditarContatoEmergenciaInputDTO.builder()
                    .nome("Novo Nome")
                    .build();

            ContatoEmergenciaDTO contatoAtualizado = ContatoEmergenciaDTO.builder()
                    .id("contato-uuid-1")
                    .nome("Novo Nome")
                    .email("maria@email.com")
                    .telefone("+5511999998888")
                    .pacienteId("paciente-uuid-1")
                    .build();

            when(contatoEmergenciaService.atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class)))
                    .thenReturn(contatoAtualizado);

            mockMvc.perform(put("/contatos-emergencia/contato-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(apenasNome)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Novo Nome"));

            verify(contatoEmergenciaService, times(1)).atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class));
        }

        @Test
        @DisplayName("Deve atualizar apenas email")
        void atualizar_ApenasEmail_Returns200() throws Exception {
            EditarContatoEmergenciaInputDTO apenasEmail = EditarContatoEmergenciaInputDTO.builder()
                    .email("novo@email.com")
                    .build();

            ContatoEmergenciaDTO contatoAtualizado = ContatoEmergenciaDTO.builder()
                    .id("contato-uuid-1")
                    .nome("Maria Silva")
                    .email("novo@email.com")
                    .telefone("+5511999998888")
                    .pacienteId("paciente-uuid-1")
                    .build();

            when(contatoEmergenciaService.atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class)))
                    .thenReturn(contatoAtualizado);

            mockMvc.perform(put("/contatos-emergencia/contato-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(apenasEmail)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("novo@email.com"));

            verify(contatoEmergenciaService, times(1)).atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class));
        }

        @Test
        @DisplayName("Deve atualizar com telefone válido")
        void atualizar_TelefoneValido_Returns200() throws Exception {
            EditarContatoEmergenciaInputDTO telefoneValido = EditarContatoEmergenciaInputDTO.builder()
                    .telefone("+5511666665555")
                    .build();

            ContatoEmergenciaDTO contatoAtualizado = ContatoEmergenciaDTO.builder()
                    .id("contato-uuid-1")
                    .nome("Maria Silva")
                    .email("maria@email.com")
                    .telefone("+5511666665555")
                    .pacienteId("paciente-uuid-1")
                    .build();

            when(contatoEmergenciaService.atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class)))
                    .thenReturn(contatoAtualizado);

            mockMvc.perform(put("/contatos-emergencia/contato-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(telefoneValido)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.telefone").value("+5511666665555"));

            verify(contatoEmergenciaService, times(1)).atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class));
        }

        @Test
        @DisplayName("Deve atualizar todos os campos")
        void atualizar_TodosCampos_Returns200() throws Exception {
            EditarContatoEmergenciaInputDTO todosCampos = EditarContatoEmergenciaInputDTO.builder()
                    .nome("Nome Completo Novo")
                    .email("novoemail@email.com")
                    .telefone("+5511555554444")
                    .build();

            ContatoEmergenciaDTO contatoAtualizado = ContatoEmergenciaDTO.builder()
                    .id("contato-uuid-1")
                    .nome("Nome Completo Novo")
                    .email("novoemail@email.com")
                    .telefone("+5511555554444")
                    .pacienteId("paciente-uuid-1")
                    .build();

            when(contatoEmergenciaService.atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class)))
                    .thenReturn(contatoAtualizado);

            mockMvc.perform(put("/contatos-emergencia/contato-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(todosCampos)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Nome Completo Novo"))
                    .andExpect(jsonPath("$.email").value("novoemail@email.com"))
                    .andExpect(jsonPath("$.telefone").value("+5511555554444"));

            verify(contatoEmergenciaService, times(1)).atualizar(eq("contato-uuid-1"), any(EditarContatoEmergenciaInputDTO.class));
        }
    }

    @Nested
    @DisplayName("DELETE /{id} - Remover Contato de Emergência")
    class RemoverContatoTests {

        @Test
        @DisplayName("Deve remover contato de emergência com sucesso")
        void remover_Sucesso_Returns204() throws Exception {
            doNothing().when(contatoEmergenciaService).remover("contato-uuid-1");

            mockMvc.perform(delete("/contatos-emergencia/contato-uuid-1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(contatoEmergenciaService, times(1)).remover("contato-uuid-1");
        }

        @Test
        @DisplayName("Deve remover outro contato com sucesso")
        void remover_OutroContato_Returns204() throws Exception {
            doNothing().when(contatoEmergenciaService).remover("contato-uuid-2");

            mockMvc.perform(delete("/contatos-emergencia/contato-uuid-2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(contatoEmergenciaService, times(1)).remover("contato-uuid-2");
        }

        @Test
        @DisplayName("Deve chamar service remover com ID correto")
        void remover_VerificaIdCorreto_Returns204() throws Exception {
            String idContato = "uuid-especifico-123";
            doNothing().when(contatoEmergenciaService).remover(idContato);

            mockMvc.perform(delete("/contatos-emergencia/" + idContato)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(contatoEmergenciaService, times(1)).remover(idContato);
        }
    }
}
