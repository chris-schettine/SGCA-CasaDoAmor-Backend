package br.com.casadoamor.sgca.modules.paciente.mapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.ContatoEmergencia;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

class ContatoEmergenciaMapperTest {

  private final ContatoEmergenciaMapper mapper = new ContatoEmergenciaMapper();

  @Test
  void toDTO_null_returnsNull() {
    assertThat(mapper.toDTO(null)).isNull();
  }

  @Test
  void toDTO_mapsFields() {
    Paciente paciente = new Paciente(); paciente.setId("p1");
    ContatoEmergencia c = ContatoEmergencia.builder().nome("N").telefone("123").email("e@e").paciente(paciente).build();
    c.setId("cid");

    ContatoEmergenciaDTO dto = mapper.toDTO(c);
    assertThat(dto.getId()).isEqualTo("cid");
    assertThat(dto.getPacienteId()).isEqualTo("p1");
    assertThat(dto.getNome()).isEqualTo("N");
  }

  @Test
  void toEntityList_and_toDTOList_nullOrValues() {
    Paciente paciente = new Paciente(); paciente.setId("p2");

    List<ContatoEmergencia> empty = mapper.toEntityList(null, paciente);
    assertThat(empty).isEmpty();

    ContatoEmergenciaInputDTO in = ContatoEmergenciaInputDTO.builder().nome("X").email("x@x").telefone("+5511999").build();
    List<ContatoEmergencia> list = mapper.toEntityList(List.of(in), paciente);
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getPaciente()).isEqualTo(paciente);

    List<ContatoEmergenciaDTO> dtoList = mapper.toDTOList(null);
    assertThat(dtoList).isEmpty();

    List<ContatoEmergenciaDTO> dtoList2 = mapper.toDTOList(list);
    assertThat(dtoList2).hasSize(1);
  }

  @Test
  void toEntity_nullAnd_updateEntity() {
    Paciente paciente = new Paciente(); paciente.setId("p3");

    assertThat(mapper.toEntity(null, paciente)).isNull();

    ContatoEmergencia entity = ContatoEmergencia.builder().nome("Old").telefone("111").email("old@e").build();

    EditarContatoEmergenciaInputDTO upd = EditarContatoEmergenciaInputDTO.builder().nome("New").telefone(null).email("new@e").build();
    mapper.updateEntity(entity, upd);

    assertThat(entity.getNome()).isEqualTo("New");
    assertThat(entity.getTelefone()).isEqualTo("111"); // unchanged
    assertThat(entity.getEmail()).isEqualTo("new@e");
  }
}
