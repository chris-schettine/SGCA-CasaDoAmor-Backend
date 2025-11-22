package br.com.casadoamor.sgca.modules.paciente.mapper;

import java.time.LocalDateTime;

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
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@ExtendWith(MockitoExtension.class)
class HistoricoPacienteMapperTest {

  @Mock
  AcompanhanteMapper acompanhanteMapper;

  @Mock
  PacienteMapper pacienteMapper;

  @InjectMocks
  HistoricoPacienteMapper mapper;

  @Test
  void toEntity_populatesFields() {
    Paciente p = new Paciente(); p.setId("p1");
    Acompanhante a = new Acompanhante(); a.setId("a1");

    HistoricoPaciente h = mapper.toEntity(p, a, "desc");
    assertThat(h.getPaciente()).isEqualTo(p);
    assertThat(h.getAcompanhante()).isEqualTo(a);
    assertThat(h.getDescricao()).isEqualTo("desc");
    assertThat(h.getDataRegistro()).isNotNull();
  }

  @Test
  void toHistoricoPacienteDTO_nullOrWithAcompanhante() {
    assertThat(mapper.toHistoricoPacienteDTO(null)).isNull();

    HistoricoPaciente h = new HistoricoPaciente(); h.setId("h1"); h.setDescricao("d"); h.setDataRegistro(LocalDateTime.now());
    AcompanhanteDTO acompDto = AcompanhanteDTO.builder().id("a1").build();
    h.setAcompanhante(new Acompanhante());
    when(acompanhanteMapper.mapToDTO(h.getAcompanhante())).thenReturn(acompDto);

    HistoricoPacienteDTO dto = mapper.toHistoricoPacienteDTO(h);
    assertThat(dto.getId()).isEqualTo("h1");
    assertThat(dto.getAcompanhante()).isNotNull();
  }

  @Test
  void toHistoricoAcompanhanteDTO_usesPacienteMapper() {
    HistoricoPaciente h = new HistoricoPaciente(); h.setId("h2"); h.setDescricao("dx"); h.setDataRegistro(LocalDateTime.now());
    Paciente p = new Paciente(); p.setId("p2");
    h.setPaciente(p);

    var pDto = br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO.builder().id("p2").build();
    when(pacienteMapper.toDTO(p)).thenReturn(pDto);

    HistoricoAcompanhanteDTO dto = mapper.toHistoricoAcompanhanteDTO(h);
    assertThat(dto).isNotNull();
    assertThat(dto.getPaciente()).isNotNull();
  }
}
