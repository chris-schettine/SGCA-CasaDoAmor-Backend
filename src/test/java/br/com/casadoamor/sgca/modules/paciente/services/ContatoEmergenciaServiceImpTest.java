package br.com.casadoamor.sgca.modules.paciente.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.ContatoEmergencia;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.mapper.ContatoEmergenciaMapper;
import br.com.casadoamor.sgca.modules.paciente.repository.ContatoEmergenciaRepository;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContatoEmergenciaServiceImp Tests")
class ContatoEmergenciaServiceImpTest {

    @Mock
    private ContatoEmergenciaRepository contatoEmergenciaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ContatoEmergenciaMapper contatoEmergenciaMapper;

    @InjectMocks
    private ContatoEmergenciaServiceImp service;

    private Paciente paciente;
    private ContatoEmergencia contato;
    private ContatoEmergenciaDTO contatoDTO;
    private ContatoEmergenciaInputDTO inputDTO;
    private EditarContatoEmergenciaInputDTO editarDTO;

    @BeforeEach
    void setup() {
        paciente = Paciente.builder()
                .email("joao@email.com")
                .build();
        paciente.setId("pac-123");

        contato = ContatoEmergencia.builder()
                .nome("Maria Silva")
                .telefone("+5511999998888")
                .email("maria@email.com")
                .paciente(paciente)
                .build();
        contato.setId("cont-456");

        contatoDTO = ContatoEmergenciaDTO.builder()
                .id("cont-456")
                .nome("Maria Silva")
                .telefone("+5511999998888")
                .email("maria@email.com")
                .pacienteId("pac-123")
                .build();

        inputDTO = ContatoEmergenciaInputDTO.builder()
                .nome("Maria Silva")
                .telefone("+5511999998888")
                .email("maria@email.com")
                .build();

        editarDTO = EditarContatoEmergenciaInputDTO.builder()
                .nome("Maria Santos")
                .telefone("+5511888887777")
                .email("maria.santos@email.com")
                .build();
    }

    // ===================== TESTES DE CRIAÇÃO =====================

    @Nested
    @DisplayName("Testes de criar()")
    class CriarTests {

        @Test
        @DisplayName("Deve criar contato de emergência com sucesso")
        void criar_Success_ReturnsDTO() {
            when(pacienteRepository.findById("pac-123")).thenReturn(Optional.of(paciente));
            when(contatoEmergenciaMapper.toEntity(inputDTO, paciente)).thenReturn(contato);
            when(contatoEmergenciaRepository.save(contato)).thenReturn(contato);
            when(contatoEmergenciaMapper.toDTO(contato)).thenReturn(contatoDTO);

            ContatoEmergenciaDTO result = service.criar("pac-123", inputDTO);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("cont-456");
            assertThat(result.getNome()).isEqualTo("Maria Silva");
            assertThat(result.getTelefone()).isEqualTo("+5511999998888");
            assertThat(result.getEmail()).isEqualTo("maria@email.com");
            assertThat(result.getPacienteId()).isEqualTo("pac-123");

            verify(pacienteRepository).findById("pac-123");
            verify(contatoEmergenciaMapper).toEntity(inputDTO, paciente);
            verify(contatoEmergenciaRepository).save(contato);
            verify(contatoEmergenciaMapper).toDTO(contato);
        }

        @Test
        @DisplayName("Deve lançar exceção quando paciente não encontrado")
        void criar_PacienteNotFound_ThrowsException() {
            when(pacienteRepository.findById("pac-inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criar("pac-inexistente", inputDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Paciente não encontrado");

            verify(pacienteRepository).findById("pac-inexistente");
        }

        @Test
        @DisplayName("Deve verificar chamada ao repositório ao salvar")
        void criar_CallsRepositorySave() {
            when(pacienteRepository.findById("pac-123")).thenReturn(Optional.of(paciente));
            when(contatoEmergenciaMapper.toEntity(inputDTO, paciente)).thenReturn(contato);
            when(contatoEmergenciaRepository.save(any(ContatoEmergencia.class))).thenReturn(contato);
            when(contatoEmergenciaMapper.toDTO(contato)).thenReturn(contatoDTO);

            service.criar("pac-123", inputDTO);

            verify(contatoEmergenciaRepository).save(any(ContatoEmergencia.class));
        }
    }

    // ===================== TESTES DE LISTAGEM =====================

    @Nested
    @DisplayName("Testes de listarPorPaciente()")
    class ListarPorPacienteTests {

        @Test
        @DisplayName("Deve listar contatos do paciente com sucesso")
        void listarPorPaciente_Success_ReturnsList() {
            ContatoEmergencia contato2 = ContatoEmergencia.builder()
                    .nome("José Silva")
                    .telefone("+5511777776666")
                    .email("jose@email.com")
                    .paciente(paciente)
                    .build();
            contato2.setId("cont-789");

            ContatoEmergenciaDTO dto2 = ContatoEmergenciaDTO.builder()
                    .id("cont-789")
                    .nome("José Silva")
                    .telefone("+5511777776666")
                    .email("jose@email.com")
                    .pacienteId("pac-123")
                    .build();

            List<ContatoEmergencia> contatos = Arrays.asList(contato, contato2);
            List<ContatoEmergenciaDTO> dtos = Arrays.asList(contatoDTO, dto2);

            when(contatoEmergenciaRepository.findByPacienteId("pac-123")).thenReturn(contatos);
            when(contatoEmergenciaMapper.toDTOList(contatos)).thenReturn(dtos);

            List<ContatoEmergenciaDTO> result = service.listarPorPaciente("pac-123");

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getNome()).isEqualTo("Maria Silva");
            assertThat(result.get(1).getNome()).isEqualTo("José Silva");

            verify(contatoEmergenciaRepository).findByPacienteId("pac-123");
            verify(contatoEmergenciaMapper).toDTOList(contatos);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando paciente não tem contatos")
        void listarPorPaciente_NoContacts_ReturnsEmptyList() {
            when(contatoEmergenciaRepository.findByPacienteId("pac-123")).thenReturn(Collections.emptyList());
            when(contatoEmergenciaMapper.toDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<ContatoEmergenciaDTO> result = service.listarPorPaciente("pac-123");

            assertThat(result).isEmpty();
            verify(contatoEmergenciaRepository).findByPacienteId("pac-123");
        }

        @Test
        @DisplayName("Deve retornar lista vazia para paciente inexistente")
        void listarPorPaciente_NonExistentPatient_ReturnsEmptyList() {
            when(contatoEmergenciaRepository.findByPacienteId("pac-inexistente")).thenReturn(Collections.emptyList());
            when(contatoEmergenciaMapper.toDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<ContatoEmergenciaDTO> result = service.listarPorPaciente("pac-inexistente");

            assertThat(result).isEmpty();
        }
    }

    // ===================== TESTES DE ATUALIZAÇÃO =====================

    @Nested
    @DisplayName("Testes de atualizar()")
    class AtualizarTests {

        @Test
        @DisplayName("Deve atualizar contato com sucesso")
        void atualizar_Success_ReturnsUpdatedDTO() {
            ContatoEmergenciaDTO updatedDTO = ContatoEmergenciaDTO.builder()
                    .id("cont-456")
                    .nome("Maria Santos")
                    .telefone("+5511888887777")
                    .email("maria.santos@email.com")
                    .pacienteId("pac-123")
                    .build();

            when(contatoEmergenciaRepository.findById("cont-456")).thenReturn(Optional.of(contato));
            when(contatoEmergenciaRepository.save(contato)).thenReturn(contato);
            when(contatoEmergenciaMapper.toDTO(contato)).thenReturn(updatedDTO);

            ContatoEmergenciaDTO result = service.atualizar("cont-456", editarDTO);

            assertThat(result).isNotNull();
            assertThat(result.getNome()).isEqualTo("Maria Santos");
            assertThat(result.getTelefone()).isEqualTo("+5511888887777");
            assertThat(result.getEmail()).isEqualTo("maria.santos@email.com");

            verify(contatoEmergenciaRepository).findById("cont-456");
            verify(contatoEmergenciaMapper).updateEntity(contato, editarDTO);
            verify(contatoEmergenciaRepository).save(contato);
        }

        @Test
        @DisplayName("Deve lançar exceção quando contato não encontrado")
        void atualizar_ContatoNotFound_ThrowsException() {
            when(contatoEmergenciaRepository.findById("cont-inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar("cont-inexistente", editarDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Contato não encontrado");

            verify(contatoEmergenciaRepository).findById("cont-inexistente");
        }

        @Test
        @DisplayName("Deve atualizar parcialmente quando apenas nome fornecido")
        void atualizar_PartialUpdate_OnlyName() {
            EditarContatoEmergenciaInputDTO partialDTO = EditarContatoEmergenciaInputDTO.builder()
                    .nome("Novo Nome")
                    .build();

            when(contatoEmergenciaRepository.findById("cont-456")).thenReturn(Optional.of(contato));
            when(contatoEmergenciaRepository.save(contato)).thenReturn(contato);
            when(contatoEmergenciaMapper.toDTO(contato)).thenReturn(contatoDTO);

            service.atualizar("cont-456", partialDTO);

            verify(contatoEmergenciaMapper).updateEntity(contato, partialDTO);
            verify(contatoEmergenciaRepository).save(contato);
        }

        @Test
        @DisplayName("Deve atualizar parcialmente quando apenas telefone fornecido")
        void atualizar_PartialUpdate_OnlyTelefone() {
            EditarContatoEmergenciaInputDTO partialDTO = EditarContatoEmergenciaInputDTO.builder()
                    .telefone("+5511555554444")
                    .build();

            when(contatoEmergenciaRepository.findById("cont-456")).thenReturn(Optional.of(contato));
            when(contatoEmergenciaRepository.save(contato)).thenReturn(contato);
            when(contatoEmergenciaMapper.toDTO(contato)).thenReturn(contatoDTO);

            service.atualizar("cont-456", partialDTO);

            verify(contatoEmergenciaMapper).updateEntity(contato, partialDTO);
            verify(contatoEmergenciaRepository).save(contato);
        }
    }

    // ===================== TESTES DE REMOÇÃO =====================

    @Nested
    @DisplayName("Testes de remover()")
    class RemoverTests {

        @Test
        @DisplayName("Deve remover contato com sucesso")
        void remover_Success() {
            when(contatoEmergenciaRepository.findById("cont-456")).thenReturn(Optional.of(contato));

            service.remover("cont-456");

            verify(contatoEmergenciaRepository).findById("cont-456");
            verify(contatoEmergenciaRepository).delete(contato);
        }

        @Test
        @DisplayName("Deve lançar exceção quando contato não encontrado para remoção")
        void remover_ContatoNotFound_ThrowsException() {
            when(contatoEmergenciaRepository.findById("cont-inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.remover("cont-inexistente"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Contato não encontrado");

            verify(contatoEmergenciaRepository).findById("cont-inexistente");
        }

        @Test
        @DisplayName("Deve chamar delete no repositório ao remover")
        void remover_CallsRepositoryDelete() {
            when(contatoEmergenciaRepository.findById("cont-456")).thenReturn(Optional.of(contato));

            service.remover("cont-456");

            verify(contatoEmergenciaRepository).delete(contato);
        }
    }
}
