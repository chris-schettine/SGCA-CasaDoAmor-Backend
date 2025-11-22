package br.com.casadoamor.sgca.modules.acompanhante.mapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.RegistrarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.common.dto.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.mapper.DadoPessoalMapper;
import br.com.casadoamor.sgca.modules.common.mapper.EnderecoMapper;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@ExtendWith(MockitoExtension.class)
class AcompanhanteMapperTest {

  @Mock
  DadoPessoalMapper dadoPessoalMapper;

  @Mock
  EnderecoMapper enderecoMapper;

  @InjectMocks
  AcompanhanteMapper mapper;

  @Test
  void mapToDTO_handlesNull() {
    assertThat(mapper.mapToDTO(null)).isNull();
  }

  @Test
  void mapToDTO_returnsPopulatedDTO() {
    Acompanhante a = Acompanhante.builder().podeAjudarNaCozinha(true).ativo(true).parentesco(null).build();
    DadoPessoal dp = new DadoPessoal(); dp.setNome("PacienteName");
    Paciente p = new Paciente(); p.setId("p1"); p.setDadoPessoal(dp);
    a.setPaciente(p);

    DadoPessoalDTO dpDto = new DadoPessoalDTO(); dpDto.setNome("AcompNome");
    when(dadoPessoalMapper.mapToDTO(a.getDadoPessoal())).thenReturn(dpDto);
    when(enderecoMapper.mapToDTO(a.getEndereco())).thenReturn(null);

    var dto = mapper.mapToDTO(a);

    assertThat(dto).isNotNull();
    assertThat(dto.getPacienteNome()).isEqualTo("PacienteName");
  }

  @Test
  void toEntity_and_toListDTO() {
    RegistrarAcompanhanteDTO dto = new RegistrarAcompanhanteDTO();
    // use setters from lombok @Data
    dto.setDadoPessoal(null);
    dto.setEndereco(null);
    dto.setPodeAjudarNaCozinha(true);

    Paciente paciente = new Paciente(); paciente.setId("p2"); paciente.setDadoPessoal(new DadoPessoal()); paciente.getDadoPessoal().setNome("PacName");

    when(dadoPessoalMapper.toEntity(null)).thenReturn(new DadoPessoal());
    when(enderecoMapper.toEntity((EnderecoInputDTO) null)).thenReturn(new Endereco());

    Acompanhante entity = mapper.toEntity(dto, paciente);
    assertThat(entity.getPaciente()).isEqualTo(paciente);

    var listDto = mapper.toListDTO(List.of(entity));
    assertThat(listDto).hasSize(1);
  }
}
