package br.com.casadoamor.sgca.modules.common.mapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import br.com.casadoamor.sgca.modules.common.dto.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO;

class DadoPessoalMapperTest {

  private final DadoPessoalMapper mapper = new DadoPessoalMapper();

  @Test
  void toEntity_cleansCpfRgAndTelefone() {
    DadoPessoalInputDTO in = new DadoPessoalInputDTO();
    in.setNome("Ana");
    in.setCpf("123.456.789-00");
    in.setRg("12.345.678-9");
    in.setTelefone("(11) 99999-8888");
    in.setProfissao("Prof");
    in.setDataNascimento(LocalDate.of(1990, 2, 3));

    DadoPessoal entity = mapper.toEntity(in);

    assertThat(entity.getNome()).isEqualTo("Ana");
    assertThat(entity.getCpf()).isEqualTo("12345678900");
    assertThat(entity.getRg()).isEqualTo("123456789");
    assertThat(entity.getTelefone()).isEqualTo("11999998888");
    assertThat(entity.getProfissao()).isEqualTo("Prof");
    assertThat(entity.getDataNascimento()).isEqualTo(LocalDate.of(1990, 2, 3));
  }

  @Test
  void mapToDTO_mapsAllFields() {
    DadoPessoal e = DadoPessoal.builder()
      .nome("Bruno")
      .cpf("11122233344")
      .rg("12345")
      .telefone("11900011122")
      .profissao("Eng")
      .build();
    e.setId("10");

    DadoPessoalDTO dto = mapper.mapToDTO(e);

    assertThat(dto.getId()).isEqualTo("10");
    assertThat(dto.getNome()).isEqualTo("Bruno");
    assertThat(dto.getCpf()).isEqualTo("11122233344");
    assertThat(dto.getRg()).isEqualTo("12345");
    assertThat(dto.getTelefone()).isEqualTo("11900011122");
  }

  @Test
  void updateEntity_updatesOnlyNonNullAndCleans() {
    DadoPessoal e = DadoPessoal.builder()
      .cpf("00011122233")
      .rg("11122233")
      .telefone("11999990000")
      .nome("Old")
      .profissao("OldProf")
      .build();

    EditarDadoPessoalInputDTO dto = new EditarDadoPessoalInputDTO() {
      @Override public String getCpf() { return "444.555.666-77"; }
      @Override public String getNome() { return "NewName"; }
    };
    // leave profissao null -> should not change

    DadoPessoal updated = mapper.updateEntity(e, dto);

    assertThat(updated.getCpf()).isEqualTo("44455566677");
    assertThat(updated.getNome()).isEqualTo("NewName");
    assertThat(updated.getProfissao()).isEqualTo("OldProf");
  }
}
