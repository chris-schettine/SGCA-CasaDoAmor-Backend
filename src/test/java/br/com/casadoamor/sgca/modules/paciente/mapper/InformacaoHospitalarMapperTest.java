package br.com.casadoamor.sgca.modules.paciente.mapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import br.com.casadoamor.sgca.modules.paciente.dtos.InformacaoHospitalarDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.InformacaoHospitalarInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.InformacaoHospitalar;

class InformacaoHospitalarMapperTest {

  private final InformacaoHospitalarMapper mapper = new InformacaoHospitalarMapper();

  @Test
  void toDTO_null_returnsNull() {
    assertThat(mapper.toDTO(null)).isNull();
  }

  @Test
  void toDTO_and_toEntity_mapsFields() {
    InformacaoHospitalar e = InformacaoHospitalar.builder()
      .nomeHospitalReferencia("Hosp")
      .medicoResponsavel("Dr X")
      .setorAla("Ala 1")
      .dataInternacao(LocalDate.of(2020,1,2))
      .build();

    InformacaoHospitalarDTO dto = mapper.toDTO(e);
    assertThat(dto.getNomeHospitalReferencia()).isEqualTo("Hosp");
    assertThat(dto.getMedicoResponsavel()).isEqualTo("Dr X");

    InformacaoHospitalarInputDTO in = new InformacaoHospitalarInputDTO() {
      @Override public String getNomeHospitalReferencia() { return "H2"; }
      @Override public String getMedicoResponsavel() { return "Dr Y"; }
      @Override public String getSetorAla() { return "Ala2"; }
      @Override public LocalDate getDataInternacao() { return LocalDate.of(2021,3,4); }
    };

    InformacaoHospitalar ent = mapper.toEntity(in);
    assertThat(ent.getNomeHospitalReferencia()).isEqualTo("H2");
    assertThat(ent.getDataInternacao()).isEqualTo(LocalDate.of(2021,3,4));
  }
}
