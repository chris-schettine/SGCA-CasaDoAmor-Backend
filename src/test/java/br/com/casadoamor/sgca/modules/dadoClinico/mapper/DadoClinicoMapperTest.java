package br.com.casadoamor.sgca.modules.dadoClinico.mapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.dadoClinico.entity.DadoClinico;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoClinicoInputDTO;

@ExtendWith(MockitoExtension.class)
class DadoClinicoMapperTest {

    @InjectMocks
    DadoClinicoMapper mapper;

    @Test
    void toEntity_and_toDTO_and_list_null() {
        DadoClinicoInputDTO in = new DadoClinicoInputDTO();
        in.setDiagnostico("dx");
        in.setUsaCurativo(true);
        in.setUsaOxigenoterapia(false);
        in.setTipoSanguineo(br.com.casadoamor.sgca.modules.common.enums.TipoSanguineoEnum.A_POSITIVO);

        DadoClinico ent = mapper.toEntity(in);
        assertThat(ent.getDiagnostico()).isEqualTo("dx");

        DadoClinicoDTO dto = mapper.toDTO(ent);
        assertThat(dto.getDiagnostico()).isEqualTo("dx");

        assertThat(mapper.toDTOList(null)).isNull();
        assertThat(mapper.toDTOList(List.of(ent))).hasSize(1);
    }

    @Test
    void updateEntity_updatesOnlyProvided() {
        DadoClinico ent = new DadoClinico(); ent.setDiagnostico("old"); ent.setUsaCurativo(false);

        EditarDadoClinicoInputDTO upd = new EditarDadoClinicoInputDTO(); upd.setDiagnostico("new"); upd.setUsaCurativo(true);

        mapper.updateEntity(ent, upd);

        assertThat(ent.getDiagnostico()).isEqualTo("new");
        assertThat(ent.getUsaCurativo()).isTrue();
    }
}
