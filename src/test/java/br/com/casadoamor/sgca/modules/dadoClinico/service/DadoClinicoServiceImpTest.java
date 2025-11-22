package br.com.casadoamor.sgca.modules.dadoClinico.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.dadoClinico.entity.DadoClinico;
import br.com.casadoamor.sgca.modules.dadoClinico.mapper.DadoClinicoMapper;
import br.com.casadoamor.sgca.modules.dadoClinico.repository.DadoClinicoRepository;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoClinicoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
class DadoClinicoServiceImpTest {

  @Mock
  DadoClinicoRepository dadoClinicoRepository;

  @Mock
  PacienteRepository pacienteRepository;

  @Mock
  DadoClinicoMapper dadoClinicoMapper;

  @InjectMocks
  DadoClinicoServiceImp service;

  @Test
  void criarDadoClinico_success() {
    String pacienteId = "p1";
    Paciente paciente = new Paciente(); paciente.setId(pacienteId);

    when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

    DadoClinicoInputDTO input = new DadoClinicoInputDTO();
    DadoClinico entity = new DadoClinico();
    when(dadoClinicoMapper.toEntity(input)).thenReturn(entity);

    DadoClinico saved = new DadoClinico(); saved.setId("dc1");
    when(dadoClinicoRepository.save(entity)).thenReturn(saved);

    DadoClinicoDTO dto = DadoClinicoDTO.builder().id("dc1").build();
    when(dadoClinicoMapper.toDTO(saved)).thenReturn(dto);

    DadoClinicoDTO result = service.criarDadoClinico(pacienteId, input);

    assertThat(result.getId()).isEqualTo("dc1");
  }

  @Test
  void criarDadoClinico_patientMissing_throws() {
    when(pacienteRepository.findById("not-found")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.criarDadoClinico("not-found", new DadoClinicoInputDTO())).isInstanceOf(RuntimeException.class).hasMessageContaining("Paciente não encontrado");
  }

  @Test
  void atualizarDadoClinico_success() {
    DadoClinico existing = new DadoClinico(); existing.setId("dc2");
    when(dadoClinicoRepository.findById("dc2")).thenReturn(Optional.of(existing));

    EditarDadoClinicoInputDTO dto = new EditarDadoClinicoInputDTO();
    // mapper.updateEntity will be called — but we mock save and toDTO
    when(dadoClinicoRepository.save(existing)).thenReturn(existing);
    DadoClinicoDTO out = DadoClinicoDTO.builder().id("dc2").build();
    when(dadoClinicoMapper.toDTO(existing)).thenReturn(out);

    DadoClinicoDTO result = service.atualizarDadoClinico("dc2", dto);

    assertThat(result.getId()).isEqualTo("dc2");
  }

  @Test
  void atualizarDadoClinico_notFound_throws() {
    when(dadoClinicoRepository.findById("nx")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.atualizarDadoClinico("nx", new EditarDadoClinicoInputDTO())).isInstanceOf(RuntimeException.class).hasMessageContaining("Dado clínico não encontrado");
  }

  @Test
  void buscarDadosClinicosPorPaciente_and_buscarDadoClinicoAtual_and_buscarPorId() {
    DadoClinico d1 = new DadoClinico(); d1.setId("d1");
    DadoClinico d2 = new DadoClinico(); d2.setId("d2");

    when(dadoClinicoRepository.findByPacienteId("p1")).thenReturn(List.of(d1, d2));
    DadoClinicoDTO dto1 = DadoClinicoDTO.builder().id("d1").build();
    DadoClinicoDTO dto2 = DadoClinicoDTO.builder().id("d2").build();
    when(dadoClinicoMapper.toDTO(d1)).thenReturn(dto1);
    when(dadoClinicoMapper.toDTO(d2)).thenReturn(dto2);

    var list = service.buscarDadosClinicosPorPaciente("p1");
    assertThat(list).hasSize(2);

    when(dadoClinicoRepository.findFirstByPacienteIdOrderByCreatedAtDesc("p1")).thenReturn(Optional.of(d2));
    DadoClinicoDTO latest = service.buscarDadoClinicoAtual("p1");
    assertThat(latest.getId()).isEqualTo("d2");

    when(dadoClinicoRepository.findById("d2")).thenReturn(Optional.of(d2));
    DadoClinicoDTO byId = service.buscarPorId("d2");
    assertThat(byId.getId()).isEqualTo("d2");

    when(dadoClinicoRepository.findById("not")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.buscarPorId("not")).isInstanceOf(RuntimeException.class).hasMessageContaining("não encontrado");
  }
}
