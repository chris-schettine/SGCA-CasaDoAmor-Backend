package br.com.casadoamor.sgca.modules.agendamento.controller;

import br.com.casadoamor.sgca.modules.agendamento.dto.TipoServicoResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.service.TipoServicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoServicoControllerTest {

    @Mock
    private TipoServicoService tipoServicoService;

    @InjectMocks
    private TipoServicoController controller;

    @Test
    void buscarPorId_success() {
        TipoServicoResponseDTO dto = TipoServicoResponseDTO.builder()
                .id(1L)
                .codigo("CONSULTA_MED")
                .nome("Consulta Médica")
                .descricao("Consulta médica geral")
                .duracaoMinutos(30)
                .requerProfissional(true)
                .permiteAcompanhante(false)
                .ativo(true)
                .build();

        when(tipoServicoService.buscarPorId(1L)).thenReturn(dto);

        ResponseEntity<TipoServicoResponseDTO> response = controller.buscarPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getNome()).isEqualTo("Consulta Médica");
        verify(tipoServicoService).buscarPorId(1L);
    }

    @Test
    void listarAtivos_success() {
        TipoServicoResponseDTO dto1 = TipoServicoResponseDTO.builder()
                .id(1L)
                .nome("Consulta Médica")
                .ativo(true)
                .build();
        TipoServicoResponseDTO dto2 = TipoServicoResponseDTO.builder()
                .id(2L)
                .nome("Consulta Odontológica")
                .ativo(true)
                .build();

        when(tipoServicoService.listarAtivos()).thenReturn(List.of(dto1, dto2));

        ResponseEntity<List<TipoServicoResponseDTO>> response = controller.listarAtivos();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getNome()).isEqualTo("Consulta Médica");
        assertThat(response.getBody().get(1).getNome()).isEqualTo("Consulta Odontológica");
        verify(tipoServicoService).listarAtivos();
    }

    @Test
    void listarAtivos_emptyList() {
        when(tipoServicoService.listarAtivos()).thenReturn(List.of());

        ResponseEntity<List<TipoServicoResponseDTO>> response = controller.listarAtivos();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void listarPorProfissional_success() {
        TipoServicoResponseDTO dto = TipoServicoResponseDTO.builder()
                .id(1L)
                .nome("Consulta Médica")
                .requerProfissional(true)
                .build();

        when(tipoServicoService.listarPorProfissional(10L)).thenReturn(List.of(dto));

        ResponseEntity<List<TipoServicoResponseDTO>> response = controller.listarPorProfissional(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getNome()).isEqualTo("Consulta Médica");
        verify(tipoServicoService).listarPorProfissional(10L);
    }

    @Test
    void listarPorProfissional_emptyList() {
        when(tipoServicoService.listarPorProfissional(999L)).thenReturn(List.of());

        ResponseEntity<List<TipoServicoResponseDTO>> response = controller.listarPorProfissional(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
