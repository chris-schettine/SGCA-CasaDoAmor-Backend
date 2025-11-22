package br.com.casadoamor.sgca.modules.common.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import br.com.casadoamor.sgca.modules.common.dto.DadoSocialDTO;
import br.com.casadoamor.sgca.modules.common.dto.DadoSocialInputDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoSocial;

class DadoSocialMapperTest {

  private final DadoSocialMapper mapper = new DadoSocialMapper();

  @Test
  void toEntity_mapsAllFields() {
    DadoSocialInputDTO in = new DadoSocialInputDTO() {
      @Override public java.math.BigDecimal getRendaFamiliar() { return java.math.BigDecimal.valueOf(1000); }
      @Override public String getComposicaoFamiliar() { return "3"; }
      @Override public String getSituacaoMoradia() { return "Alugada"; }
      @Override public String getNecessidadesEspeciais() { return "Nenhuma"; }
    };

    DadoSocial entity = mapper.toEntity(in);

    assertThat(entity.getRendaFamiliar()).isEqualTo("1000");
    assertThat(entity.getComposicaoFamiliar()).isEqualTo("3");
    assertThat(entity.getSituacaoMoradia()).isEqualTo("Alugada");
    assertThat(entity.getNecessidadesEspeciais()).isEqualTo("Nenhuma");
  }

  @Test
  void toDTO_returnsNullForNullInput() {
    DadoSocialDTO dto = mapper.toDTO(null);
    assertThat(dto).isNull();
  }

  @Test
  void toDTO_mapsFields() {
    DadoSocial e = DadoSocial.builder()
      .rendaFamiliar(java.math.BigDecimal.valueOf(200))
      .composicaoFamiliar("5")
      .situacaoMoradia("Casa")
      .necessidadesEspeciais("Transporte")
      .build();

    DadoSocialDTO dto = mapper.toDTO(e);

    assertThat(dto.getRendaFamiliar()).isEqualTo("200");
    assertThat(dto.getComposicaoFamiliar()).isEqualTo("5");
    assertThat(dto.getSituacaoMoradia()).isEqualTo("Casa");
    assertThat(dto.getNecessidadesEspeciais()).isEqualTo("Transporte");
  }
}
