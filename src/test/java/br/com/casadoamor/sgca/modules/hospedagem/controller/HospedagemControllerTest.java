package br.com.casadoamor.sgca.modules.hospedagem.controller;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemRequestDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemResponseDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemSaidaDTO;
import br.com.casadoamor.sgca.modules.hospedagem.service.HospedagemService;

@ExtendWith(MockitoExtension.class)
class HospedagemControllerTest {

    @Mock
    private HospedagemService service;

    @InjectMocks
    private HospedagemController controller;

    @Test
    void registrarEntrada_returnsCreated() {
        HospedagemRequestDTO req = HospedagemRequestDTO.builder().pacienteId("p1").dataEntrada(LocalDate.now()).build();
        AuthUsuario user = AuthUsuario.builder().nome("u").build();

        HospedagemResponseDTO resp = HospedagemResponseDTO.builder().uuid("h1").pacienteId("p1").build();
        when(service.registrarEntrada(req, user)).thenReturn(resp);

        ResponseEntity<HospedagemResponseDTO> r = controller.registrarEntrada(req, user);
        assertThat(r.getStatusCodeValue()).isEqualTo(201);
        assertThat(r.getBody()).isEqualTo(resp);
    }

    @Test
    void registrarSaida_andTransferirQuarto_andBuscarPorUuid_andListings_andDeletar() {
        AuthUsuario user = AuthUsuario.builder().nome("u").build();

        // registrarSaida
        HospedagemSaidaDTO saida = HospedagemSaidaDTO.builder().dataSaida(LocalDate.now()).motivoSaida("ok").build();
        HospedagemResponseDTO resp = HospedagemResponseDTO.builder().uuid("h2").build();
        when(service.registrarSaida("h2", saida, user)).thenReturn(resp);

        var r1 = controller.registrarSaida("h2", saida, user);
        assertThat(r1.getStatusCodeValue()).isEqualTo(200);
        assertThat(r1.getBody()).isEqualTo(resp);

        // transferir
        when(service.transferirQuarto("h3", "q1", "motivo", user)).thenReturn(resp);
        var r2 = controller.transferirQuarto("h3", "q1", "motivo", user);
        assertThat(r2.getStatusCodeValue()).isEqualTo(200);

        // buscarPorUuid
        when(service.buscarPorUuidDTO("h4")).thenReturn(resp);
        var r3 = controller.buscarPorUuid("h4");
        assertThat(r3.getStatusCodeValue()).isEqualTo(200);

        // listarAtivas
        when(service.listarAtivas()).thenReturn(List.of(resp));
        var r4 = controller.listarAtivas();
        assertThat(r4.getBody()).hasSize(1);

        // listarPorPaciente
        when(service.listarPorPaciente("p1")).thenReturn(List.of(resp));
        var rp = controller.listarPorPaciente("p1");
        assertThat(rp.getBody()).hasSize(1);

        // listarPorQuarto
        when(service.listarPorQuarto("q1")).thenReturn(List.of(resp));
        var rq = controller.listarPorQuarto("q1");
        assertThat(rq.getBody()).hasSize(1);

        // listarPorPeriodo
        when(service.listarPorPeriodo(LocalDate.now().minusDays(1), LocalDate.now())).thenReturn(List.of(resp));
        var rperiod = controller.listarPorPeriodo(LocalDate.now().minusDays(1), LocalDate.now());
        assertThat(rperiod.getBody()).hasSize(1);

        // listarComPrevisaoVencida
        when(service.listarComPrevisaoVencida()).thenReturn(List.of(resp));
        var r7 = controller.listarComPrevisaoVencida();
        assertThat(r7.getBody()).hasSize(1);

        // listarComPaginacao
        when(service.listarComPaginacaoEFiltros(any(), any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(resp)));
        var pageRes = controller.listarComPaginacao(null, null, null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(pageRes.getBody().getTotalElements()).isEqualTo(1);

        // verificarHospedagemAtiva
        when(service.pacienteTemHospedagemAtiva("p1")).thenReturn(true);
        var v = controller.verificarHospedagemAtiva("p1");
        assertThat(v.getBody()).isTrue();

        // deletar
        doNothing().when(service).deletar("h9");
        var d = controller.deletar("h9");
        assertThat(d.getStatusCodeValue()).isEqualTo(204);

        verify(service).registrarSaida("h2", saida, user);
        verify(service).transferirQuarto("h3", "q1", "motivo", user);
        verify(service).buscarPorUuidDTO("h4");
        verify(service).listarAtivas();
        verify(service).listarPorPaciente("p1");
        verify(service).listarPorQuarto("q1");
        verify(service).listarPorPeriodo(any(), any());
        verify(service).listarComPrevisaoVencida();
        verify(service).listarComPaginacao(any());
        verify(service).pacienteTemHospedagemAtiva("p1");
        verify(service).deletar("h9");
    }
}
