package br.com.casadoamor.sgca.modules.paciente.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarObitoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.services.PacienteService;

@ExtendWith(MockitoExtension.class)
class PacienteControllerTest {

    @Mock
    private PacienteService pacienteService;

    @InjectMocks
    private PacienteController controller;

    @Test
    void registrarPaciente_returnsCreated() {
        RegistrarPacienteDTO req = new RegistrarPacienteDTO();
        PacienteDTO dto = PacienteDTO.builder().id("p1").email("x@x").build();

        when(pacienteService.registrarPaciente(req)).thenReturn(dto);

        ResponseEntity<PacienteDTO> r = controller.registrarPaciente(req);

        assertThat(r.getStatusCodeValue()).isEqualTo(201);
        assertThat(r.getBody()).isEqualTo(dto);
    }

    @Test
    void editarPaciente_andRegistrarObito_andDelete_delegates() {
        EditarPacienteDTO edit = new EditarPacienteDTO();
        PacienteDTO edited = PacienteDTO.builder().id("p2").email("b@b").build();
        when(pacienteService.editarPaciente("id", edit)).thenReturn(edited);

        ResponseEntity<PacienteDTO> r = controller.editarPaciente("id", edit);
        assertThat(r.getStatusCodeValue()).isEqualTo(200);
        assertThat(r.getBody()).isEqualTo(edited);

        RegistrarObitoDTO obito = new RegistrarObitoDTO();
        when(pacienteService.registrarObito("id", obito)).thenReturn(edited);
        ResponseEntity<PacienteDTO> r2 = controller.registrarObito("id", obito);
        assertThat(r2.getStatusCodeValue()).isEqualTo(200);

        // delete
        doNothing().when(pacienteService).deletarPaciente("id");
        assertThat(controller.deletarPaciente("id").getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void pacientesPaginados_andHistorico_forwardToService() {
        when(pacienteService.pacientesPaginados(null, 10, 0, null, null, null, null, null, null, null, null, null, null)).thenReturn(null);
        controller.pacientesPaginados(10,0,null,null,null,null,null,null,null,null,null,null,null);
        verify(pacienteService).pacientesPaginados(null, 10, 0, null, null, null, null, null, null, null, null, null, null);

        when(pacienteService.historicoPacientePaginado("p", 10,0)).thenReturn(null);
        controller.historicoPaciente("p", 10,0);
        verify(pacienteService).historicoPacientePaginado("p", 10,0);
    }
}
