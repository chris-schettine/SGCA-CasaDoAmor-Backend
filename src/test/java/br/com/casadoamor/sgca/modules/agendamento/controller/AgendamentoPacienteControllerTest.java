package br.com.casadoamor.sgca.modules.agendamento.controller;

import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoPacienteRequestDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoPacienteResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.ConflictCheckResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Prioridade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.TipoAtendimento;
import br.com.casadoamor.sgca.modules.agendamento.service.AgendamentoPacienteService;
import br.com.casadoamor.sgca.modules.common.dto.MessageResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoPacienteControllerTest {

    @Mock
    private AgendamentoPacienteService agendamentoPacienteService;

    @InjectMocks
    private AgendamentoPacienteController controller;

    private AgendamentoPacienteResponseDTO createResponseDTO() {
        return AgendamentoPacienteResponseDTO.builder()
                .id(1L)
                .uuid("uuid-pac-123")
                .pacienteId("pac-001")
                .pacienteNome("José Silva")
                .tipoServicoId(1L)
                .tipoServicoNome("Consulta Médica")
                .profissionalUsuarioId(10L)
                .profissionalNome("Dr. João")
                .hospedagemId("hosp-001")
                .quartoNome("Quarto 101")
                .dataHoraInicio(LocalDateTime.of(2025, 12, 3, 10, 0))
                .dataHoraFim(LocalDateTime.of(2025, 12, 3, 10, 30))
                .tipoAtendimento(TipoAtendimento.PRIMEIRA_VEZ)
                .prioridade(Prioridade.NORMAL)
                .status(StatusAgendamento.AGENDADO)
                .confirmadoPaciente(false)
                .confirmadoProfissional(false)
                .compareceu(false)
                .geradoAutomaticamente(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listar_success() {
        AgendamentoPacienteResponseDTO dto = createResponseDTO();
        when(agendamentoPacienteService.listar(0, 20)).thenReturn(List.of(dto));

        ResponseEntity<List<AgendamentoPacienteResponseDTO>> response = controller.listar(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUuid()).isEqualTo("uuid-pac-123");
        verify(agendamentoPacienteService).listar(0, 20);
    }

    @Test
    void listar_emptyList() {
        when(agendamentoPacienteService.listar(0, 20)).thenReturn(List.of());

        ResponseEntity<List<AgendamentoPacienteResponseDTO>> response = controller.listar(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void listar_withPagination() {
        AgendamentoPacienteResponseDTO dto = createResponseDTO();
        when(agendamentoPacienteService.listar(1, 10)).thenReturn(List.of(dto));

        ResponseEntity<List<AgendamentoPacienteResponseDTO>> response = controller.listar(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(agendamentoPacienteService).listar(1, 10);
    }

    @Test
    void criar_success() {
        AgendamentoPacienteRequestDTO requestDTO = new AgendamentoPacienteRequestDTO();
        AgendamentoPacienteResponseDTO responseDTO = createResponseDTO();

        when(agendamentoPacienteService.criar(any())).thenReturn(responseDTO);

        ResponseEntity<AgendamentoPacienteResponseDTO> response = controller.criar(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isEqualTo("uuid-pac-123");
        verify(agendamentoPacienteService).criar(any());
    }

    @Test
    void buscarPorUuid_success() {
        AgendamentoPacienteResponseDTO dto = createResponseDTO();
        when(agendamentoPacienteService.buscarPorUuid("uuid-pac-123")).thenReturn(dto);

        ResponseEntity<AgendamentoPacienteResponseDTO> response = controller.buscarPorUuid("uuid-pac-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPacienteNome()).isEqualTo("José Silva");
        verify(agendamentoPacienteService).buscarPorUuid("uuid-pac-123");
    }

    @Test
    void listarPorPaciente_success() {
        AgendamentoPacienteResponseDTO dto = createResponseDTO();
        when(agendamentoPacienteService.listarPorPaciente("pac-001")).thenReturn(List.of(dto));

        ResponseEntity<List<AgendamentoPacienteResponseDTO>> response = controller.listarPorPaciente("pac-001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(agendamentoPacienteService).listarPorPaciente("pac-001");
    }

    @Test
    void listarPorPaciente_emptyList() {
        when(agendamentoPacienteService.listarPorPaciente("pac-999")).thenReturn(List.of());

        ResponseEntity<List<AgendamentoPacienteResponseDTO>> response = controller.listarPorPaciente("pac-999");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void listarPorProfissional_success() {
        LocalDateTime inicio = LocalDateTime.of(2025, 12, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 12, 31, 23, 59);
        AgendamentoPacienteResponseDTO dto = createResponseDTO();

        when(agendamentoPacienteService.listarPorProfissional(10L, inicio, fim)).thenReturn(List.of(dto));

        ResponseEntity<List<AgendamentoPacienteResponseDTO>> response = 
                controller.listarPorProfissional(10L, inicio, fim);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(agendamentoPacienteService).listarPorProfissional(10L, inicio, fim);
    }

    @Test
    void listarPorProfissional_emptyList() {
        LocalDateTime inicio = LocalDateTime.of(2025, 12, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 12, 31, 23, 59);

        when(agendamentoPacienteService.listarPorProfissional(999L, inicio, fim)).thenReturn(List.of());

        ResponseEntity<List<AgendamentoPacienteResponseDTO>> response = 
                controller.listarPorProfissional(999L, inicio, fim);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void verificarConflito_noConflict() {
        LocalDateTime inicio = LocalDateTime.of(2025, 12, 3, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 12, 3, 10, 30);

        ConflictCheckResponseDTO conflictDTO = ConflictCheckResponseDTO.builder()
                .temConflito(false)
                .mensagem("Horário disponível")
                .build();

        when(agendamentoPacienteService.verificarConflito(10L, inicio, fim)).thenReturn(conflictDTO);

        ResponseEntity<ConflictCheckResponseDTO> response = controller.verificarConflito(10L, inicio, fim);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTemConflito()).isFalse();
        assertThat(response.getBody().getMensagem()).isEqualTo("Horário disponível");
        verify(agendamentoPacienteService).verificarConflito(10L, inicio, fim);
    }

    @Test
    void verificarConflito_hasConflict() {
        LocalDateTime inicio = LocalDateTime.of(2025, 12, 3, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 12, 3, 10, 30);

        ConflictCheckResponseDTO.ConflictDetails details = ConflictCheckResponseDTO.ConflictDetails.builder()
                .tipo("AGENDAMENTO")
                .descricao("Já existe agendamento neste horário")
                .conflitanteInicio(inicio)
                .conflitanteFim(fim)
                .profissionalNome("Dr. João")
                .build();

        ConflictCheckResponseDTO conflictDTO = ConflictCheckResponseDTO.builder()
                .temConflito(true)
                .mensagem("Conflito encontrado")
                .details(details)
                .build();

        when(agendamentoPacienteService.verificarConflito(10L, inicio, fim)).thenReturn(conflictDTO);

        ResponseEntity<ConflictCheckResponseDTO> response = controller.verificarConflito(10L, inicio, fim);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTemConflito()).isTrue();
        assertThat(response.getBody().getDetails()).isNotNull();
        assertThat(response.getBody().getDetails().getTipo()).isEqualTo("AGENDAMENTO");
    }

    @Test
    void confirmar_byPatient() {
        AgendamentoPacienteResponseDTO dto = createResponseDTO();
        dto.setStatus(StatusAgendamento.CONFIRMADO);
        dto.setConfirmadoPaciente(true);

        when(agendamentoPacienteService.confirmar("uuid-pac-123", true)).thenReturn(dto);

        ResponseEntity<AgendamentoPacienteResponseDTO> response = controller.confirmar("uuid-pac-123", true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
        assertThat(response.getBody().getConfirmadoPaciente()).isTrue();
        verify(agendamentoPacienteService).confirmar("uuid-pac-123", true);
    }

    @Test
    void confirmar_byProfessional() {
        AgendamentoPacienteResponseDTO dto = createResponseDTO();
        dto.setConfirmadoProfissional(true);

        when(agendamentoPacienteService.confirmar("uuid-pac-123", false)).thenReturn(dto);

        ResponseEntity<AgendamentoPacienteResponseDTO> response = controller.confirmar("uuid-pac-123", false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getConfirmadoProfissional()).isTrue();
    }

    @Test
    void cancelar_success() {
        doNothing().when(agendamentoPacienteService).cancelar("uuid-pac-123", "Paciente solicitou cancelamento");

        ResponseEntity<MessageResponseDTO> response = controller.cancelar("uuid-pac-123", "Paciente solicitou cancelamento");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("cancelado com sucesso");
        verify(agendamentoPacienteService).cancelar("uuid-pac-123", "Paciente solicitou cancelamento");
    }

    @Test
    void listarPacientesElegiveis_success() {
        List<?> elegiveis = List.of(
                Map.of("id", "pac-001", "nome", "José Silva"),
                Map.of("id", "pac-002", "nome", "Maria Santos")
        );

        doReturn(elegiveis).when(agendamentoPacienteService).listarPacientesElegiveis();

        ResponseEntity<?> response = controller.listarPacientesElegiveis();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(agendamentoPacienteService).listarPacientesElegiveis();
    }

    @Test
    void listarProfissionaisElegiveis_success() {
        List<?> profissionais = List.of(
                Map.of("id", 10L, "nome", "Dr. João", "especialidade", "Clínico Geral"),
                Map.of("id", 11L, "nome", "Dra. Ana", "especialidade", "Cardiologista")
        );

        doReturn(profissionais).when(agendamentoPacienteService).listarProfissionaisElegiveis();

        ResponseEntity<?> response = controller.listarProfissionaisElegiveis();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(agendamentoPacienteService).listarProfissionaisElegiveis();
    }

    @Test
    void listarProfissionaisElegiveis_emptyList() {
        List<?> emptyList = List.of();
        doReturn(emptyList).when(agendamentoPacienteService).listarProfissionaisElegiveis();

        ResponseEntity<?> response = controller.listarProfissionaisElegiveis();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(agendamentoPacienteService).listarProfissionaisElegiveis();
    }
}
