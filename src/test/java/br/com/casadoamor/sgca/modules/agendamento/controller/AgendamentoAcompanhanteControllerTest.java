package br.com.casadoamor.sgca.modules.agendamento.controller;

import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoAcompanhanteRequestDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoAcompanhanteResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.ConflictCheckResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Prioridade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.TipoAtendimento;
import br.com.casadoamor.sgca.modules.agendamento.service.AgendamentoAcompanhanteService;
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
class AgendamentoAcompanhanteControllerTest {

    @Mock
    private AgendamentoAcompanhanteService agendamentoAcompanhanteService;

    @InjectMocks
    private AgendamentoAcompanhanteController controller;

    private AgendamentoAcompanhanteResponseDTO createResponseDTO() {
        return AgendamentoAcompanhanteResponseDTO.builder()
                .id(1L)
                .uuid("uuid-123")
                .acompanhanteId("acomp-001")
                .acompanhanteNome("Maria Silva")
                .tipoServicoId(1L)
                .tipoServicoNome("Consulta Médica")
                .profissionalUsuarioId(10L)
                .profissionalNome("Dr. João")
                .pacienteVinculadoId("pac-001")
                .pacienteVinculadoNome("José Silva")
                .quartoNome("Quarto 101")
                .dataHoraInicio(LocalDateTime.of(2025, 12, 3, 10, 0))
                .dataHoraFim(LocalDateTime.of(2025, 12, 3, 10, 30))
                .tipoAtendimento(TipoAtendimento.PRIMEIRA_VEZ)
                .prioridade(Prioridade.NORMAL)
                .status(StatusAgendamento.AGENDADO)
                .confirmadoAcompanhante(false)
                .confirmadoProfissional(false)
                .compareceu(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listar_success() {
        AgendamentoAcompanhanteResponseDTO dto = createResponseDTO();
        when(agendamentoAcompanhanteService.listar(0, 20)).thenReturn(List.of(dto));

        ResponseEntity<List<AgendamentoAcompanhanteResponseDTO>> response = controller.listar(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUuid()).isEqualTo("uuid-123");
        verify(agendamentoAcompanhanteService).listar(0, 20);
    }

    @Test
    void listar_emptyList() {
        when(agendamentoAcompanhanteService.listar(0, 20)).thenReturn(List.of());

        ResponseEntity<List<AgendamentoAcompanhanteResponseDTO>> response = controller.listar(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void criar_success() {
        AgendamentoAcompanhanteRequestDTO requestDTO = new AgendamentoAcompanhanteRequestDTO();
        AgendamentoAcompanhanteResponseDTO responseDTO = createResponseDTO();

        when(agendamentoAcompanhanteService.criar(any())).thenReturn(responseDTO);

        ResponseEntity<AgendamentoAcompanhanteResponseDTO> response = controller.criar(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isEqualTo("uuid-123");
        verify(agendamentoAcompanhanteService).criar(any());
    }

    @Test
    void buscarPorUuid_success() {
        AgendamentoAcompanhanteResponseDTO dto = createResponseDTO();
        when(agendamentoAcompanhanteService.buscarPorUuid("uuid-123")).thenReturn(dto);

        ResponseEntity<AgendamentoAcompanhanteResponseDTO> response = controller.buscarPorUuid("uuid-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAcompanhanteNome()).isEqualTo("Maria Silva");
        verify(agendamentoAcompanhanteService).buscarPorUuid("uuid-123");
    }

    @Test
    void listarPorAcompanhante_success() {
        AgendamentoAcompanhanteResponseDTO dto = createResponseDTO();
        when(agendamentoAcompanhanteService.listarPorAcompanhante("acomp-001")).thenReturn(List.of(dto));

        ResponseEntity<List<AgendamentoAcompanhanteResponseDTO>> response = controller.listarPorAcompanhante("acomp-001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(agendamentoAcompanhanteService).listarPorAcompanhante("acomp-001");
    }

    @Test
    void listarPorProfissional_success() {
        LocalDateTime inicio = LocalDateTime.of(2025, 12, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 12, 31, 23, 59);
        AgendamentoAcompanhanteResponseDTO dto = createResponseDTO();

        when(agendamentoAcompanhanteService.listarPorProfissional(10L, inicio, fim)).thenReturn(List.of(dto));

        ResponseEntity<List<AgendamentoAcompanhanteResponseDTO>> response = 
                controller.listarPorProfissional(10L, inicio, fim);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(agendamentoAcompanhanteService).listarPorProfissional(10L, inicio, fim);
    }

    @Test
    void verificarConflito_noConflict() {
        LocalDateTime inicio = LocalDateTime.of(2025, 12, 3, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 12, 3, 10, 30);

        ConflictCheckResponseDTO conflictDTO = ConflictCheckResponseDTO.builder()
                .temConflito(false)
                .mensagem("Horário disponível")
                .build();

        when(agendamentoAcompanhanteService.verificarConflito(10L, inicio, fim)).thenReturn(conflictDTO);

        ResponseEntity<ConflictCheckResponseDTO> response = controller.verificarConflito(10L, inicio, fim);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTemConflito()).isFalse();
        verify(agendamentoAcompanhanteService).verificarConflito(10L, inicio, fim);
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

        when(agendamentoAcompanhanteService.verificarConflito(10L, inicio, fim)).thenReturn(conflictDTO);

        ResponseEntity<ConflictCheckResponseDTO> response = controller.verificarConflito(10L, inicio, fim);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTemConflito()).isTrue();
        assertThat(response.getBody().getDetails().getTipo()).isEqualTo("AGENDAMENTO");
    }

    @Test
    void confirmar_success() {
        AgendamentoAcompanhanteResponseDTO dto = createResponseDTO();
        dto.setStatus(StatusAgendamento.CONFIRMADO);
        dto.setConfirmadoAcompanhante(true);

        when(agendamentoAcompanhanteService.confirmar("uuid-123", true)).thenReturn(dto);

        ResponseEntity<AgendamentoAcompanhanteResponseDTO> response = controller.confirmar("uuid-123", true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
        assertThat(response.getBody().getConfirmadoAcompanhante()).isTrue();
        verify(agendamentoAcompanhanteService).confirmar("uuid-123", true);
    }

    @Test
    void confirmar_byProfessional() {
        AgendamentoAcompanhanteResponseDTO dto = createResponseDTO();
        dto.setConfirmadoProfissional(true);

        when(agendamentoAcompanhanteService.confirmar("uuid-123", false)).thenReturn(dto);

        ResponseEntity<AgendamentoAcompanhanteResponseDTO> response = controller.confirmar("uuid-123", false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getConfirmadoProfissional()).isTrue();
    }

    @Test
    void cancelar_success() {
        doNothing().when(agendamentoAcompanhanteService).cancelar("uuid-123", "Motivo do cancelamento");

        ResponseEntity<MessageResponseDTO> response = controller.cancelar("uuid-123", "Motivo do cancelamento");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("cancelado com sucesso");
        verify(agendamentoAcompanhanteService).cancelar("uuid-123", "Motivo do cancelamento");
    }

    @Test
    void listarAcompanhantesElegiveis_success() {
        List<?> elegiveis = List.of(
                Map.of("id", "acomp-001", "nome", "Maria Silva"),
                Map.of("id", "acomp-002", "nome", "Ana Santos")
        );

        doReturn(elegiveis).when(agendamentoAcompanhanteService).listarAcompanhantesElegiveis();

        ResponseEntity<?> response = controller.listarAcompanhantesElegiveis();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(agendamentoAcompanhanteService).listarAcompanhantesElegiveis();
    }

    @Test
    void listarProfissionaisElegiveis_success() {
        List<?> profissionais = List.of(
                Map.of("id", 10L, "nome", "Dr. João"),
                Map.of("id", 11L, "nome", "Dra. Ana")
        );

        doReturn(profissionais).when(agendamentoAcompanhanteService).listarProfissionaisElegiveis();

        ResponseEntity<?> response = controller.listarProfissionaisElegiveis();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(agendamentoAcompanhanteService).listarProfissionaisElegiveis();
    }
}
