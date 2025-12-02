package br.com.casadoamor.sgca.modules.hospedagem.controller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.dto.EstatisticasOcupacaoDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.QuartoRequestDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.QuartoResponseDTO;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.service.QuartoService;

@ExtendWith(MockitoExtension.class)
class QuartoControllerTest {

    @Mock
    private QuartoService quartoService;

    @InjectMocks
    private QuartoController controller;

    @Test
    void cadastrar_returnsCreated() {
        QuartoRequestDTO req = QuartoRequestDTO.builder().nome("Q").tipo(null).ala(AlaQuarto.MISTA).capacidadeTotal(2).build();
        AuthUsuario user = AuthUsuario.builder().nome("n").build();

        QuartoResponseDTO resp = QuartoResponseDTO.builder().uuid("u1").nome("Q").build();
        when(quartoService.cadastrar(req, user)).thenReturn(resp);

        ResponseEntity<QuartoResponseDTO> r = controller.cadastrar(req, user);

        assertThat(r.getStatusCodeValue()).isEqualTo(201);
        assertThat(r.getBody()).isEqualTo(resp);
    }

    @Test
    void atualizar_returnsOk() {
        QuartoRequestDTO req = QuartoRequestDTO.builder().nome("Q").tipo(null).ala(AlaQuarto.MISTA).capacidadeTotal(2).build();
        AuthUsuario user = AuthUsuario.builder().nome("n").build();
        QuartoResponseDTO resp = QuartoResponseDTO.builder().uuid("u2").nome("Q").build();

        when(quartoService.atualizar("u2", req, user)).thenReturn(resp);

        ResponseEntity<QuartoResponseDTO> r = controller.atualizar("u2", req, user);

        assertThat(r.getStatusCodeValue()).isEqualTo(200);
        assertThat(r.getBody()).isEqualTo(resp);
    }

    @Test
    void buscarPorUuid_returnsOk() {
        QuartoResponseDTO resp = QuartoResponseDTO.builder().uuid("u3").nome("q3").build();
        when(quartoService.buscarPorUuidDTO("u3")).thenReturn(resp);

        ResponseEntity<QuartoResponseDTO> r = controller.buscarPorUuid("u3");

        assertThat(r.getStatusCodeValue()).isEqualTo(200);
        assertThat(r.getBody()).isEqualTo(resp);
    }

    @Test
    void listarAtivos_forwardToService() {
        when(quartoService.listarAtivos()).thenReturn(List.of());

        assertThat(controller.listarAtivos().getStatusCodeValue()).isEqualTo(200);
        verify(quartoService).listarAtivos();
    }

    @Test
    void obterEstatisticas_delegates() {
        EstatisticasOcupacaoDTO dto = EstatisticasOcupacaoDTO.builder().capacidadeTotal(10).ocupacaoTotal(5).vagasDisponiveis(5).build();
        when(quartoService.obterEstatisticas()).thenReturn(dto);

        var res = controller.obterEstatisticas();

        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        assertThat(res.getBody()).isEqualTo(dto);
    }

    @Test
    void inativarAndDeletar_returnNoContent() {
        AuthUsuario user = AuthUsuario.builder().nome("n").build();

        var r1 = controller.inativar("uabc", user);
        var r2 = controller.deletar("uabc");

        assertThat(r1.getStatusCodeValue()).isEqualTo(204);
        assertThat(r2.getStatusCodeValue()).isEqualTo(204);
        verify(quartoService).inativar("uabc", user);
        verify(quartoService).deletar("uabc");
    }
}
