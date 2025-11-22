package br.com.casadoamor.sgca.modules.common.service.imp;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.acompanhante.mapper.AcompanhanteMapper;
import br.com.casadoamor.sgca.modules.acompanhante.repository.AcompanhanteRepository;
import br.com.casadoamor.sgca.modules.common.dto.BuscaInteligenteResponseDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.mapper.PacienteMapper;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
class BuscaInteligenteServiceImpTest {

  @Mock
  PacienteRepository pacienteRepository;

  @Mock
  AcompanhanteRepository acompanhanteRepository;

  @Mock
  PacienteMapper pacienteMapper;

  @Mock
  AcompanhanteMapper acompanhanteMapper;

  @InjectMocks
  BuscaInteligenteServiceImp service;

  @Test
  void buscar_callsRepositoriesAndReturnsDTOs() {
    String termo = "JoHN-12";
    String termoLimpo = "john12"; // expected cleaning

    Paciente p = new Paciente(); p.setId("p1");
    Acompanhante a = new Acompanhante(); a.setId("a1");

    when(pacienteRepository.buscarInteligente(termo, termoLimpo)).thenReturn(List.of(p));
    when(acompanhanteRepository.buscarInteligente(termo, termoLimpo)).thenReturn(List.of(a));

    PacienteDTO pDto = PacienteDTO.builder().id("p1").build();
    AcompanhanteDTO aDto = AcompanhanteDTO.builder().id("a1").build();

    when(pacienteMapper.toListDTO(List.of(p))).thenReturn(List.of(pDto));
    when(acompanhanteMapper.toListDTO(List.of(a))).thenReturn(List.of(aDto));

    BuscaInteligenteResponseDTO res = service.buscar(termo);

    assertThat(res.getPacientes()).hasSize(1);
    assertThat(res.getAcompanhantes()).hasSize(1);
    assertThat(res.getPacientes().get(0).getId()).isEqualTo("p1");
    assertThat(res.getAcompanhantes().get(0).getId()).isEqualTo("a1");
  }
}
