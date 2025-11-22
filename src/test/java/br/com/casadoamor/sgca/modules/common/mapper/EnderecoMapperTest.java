package br.com.casadoamor.sgca.modules.common.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarEnderecoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoInputDTO;

class EnderecoMapperTest {

  private final EnderecoMapper mapper = new EnderecoMapper();

  @Test
  void toEntity_mapsAllFields() {
    EnderecoInputDTO in = EnderecoInputDTO.builder()
      .cep("12345-678")
      .logradouro("Rua A")
      .numero(10)
      .complemento("apt")
      .bairro("Bairro")
      .cidade("Cidade")
      .estado(null)
      .build();

    Endereco entity = mapper.toEntity(in);

    assertThat(entity.getCep()).isEqualTo("12345-678");
    assertThat(entity.getLogradouro()).isEqualTo("Rua A");
    assertThat(entity.getNumero()).isEqualTo(10);
    assertThat(entity.getComplemento()).isEqualTo("apt");
    assertThat(entity.getBairro()).isEqualTo("Bairro");
    assertThat(entity.getCidade()).isEqualTo("Cidade");
  }

  @Test
  void mapToDTO_mapsAllFields() {
    Endereco e = Endereco.builder()
      .cep("98765-432")
      .logradouro("Av B")
      .numero(5)
      .complemento(null)
      .bairro("Central")
      .cidade("Town")
      .estado(null)
      .build();
    e.setId("abc");

    EnderecoDTO dto = mapper.mapToDTO(e);

    assertThat(dto.getId()).isEqualTo("abc");
    assertThat(dto.getCep()).isEqualTo("98765-432");
    assertThat(dto.getLogradouro()).isEqualTo("Av B");
    assertThat(dto.getNumero()).isEqualTo(5);
    assertThat(dto.getBairro()).isEqualTo("Central");
  }

  @Test
  void updateEntity_updatesOnlyNonNull() {
    Endereco e = Endereco.builder()
      .cep("1")
      .logradouro("old")
      .numero(1)
      .complemento("oldc")
      .bairro("oldb")
      .cidade("oldcity")
      .estado(null)
      .build();

    EditarEnderecoInputDTO dto = new EditarEnderecoInputDTO() {
      @Override public String getCep() { return "99999-000"; }
      @Override public String getCidade() { return "NewCity"; }
      // other getters remain null
    };

    Endereco updated = mapper.updateEntity(e, dto);

    assertThat(updated.getCep()).isEqualTo("99999-000");
    assertThat(updated.getCidade()).isEqualTo("NewCity");
    assertThat(updated.getLogradouro()).isEqualTo("old");
  }
}
