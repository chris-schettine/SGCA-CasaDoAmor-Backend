package br.com.casadoamor.sgca.modules.paciente.mapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.common.dto.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.common.dto.DadoSocialDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@ExtendWith(MockitoExtension.class)
class PacienteMapperTest {

    @Mock
    private br.com.casadoamor.sgca.modules.common.mapper.EnderecoMapper enderecoMapper;

    @Mock
    private br.com.casadoamor.sgca.modules.common.mapper.DadoPessoalMapper dadoPessoalMapper;

    @Mock
    private ContatoEmergenciaMapper contatoEmergenciaMapper;

    @Mock
    private br.com.casadoamor.sgca.modules.dadoClinico.mapper.DadoClinicoMapper dadoClinicoMapper;

    @Mock
    private br.com.casadoamor.sgca.modules.paciente.mapper.InformacaoHospitalarMapper informacaoHospitalarMapper;

    @Mock
    private br.com.casadoamor.sgca.modules.common.mapper.DadoSocialMapper dadoSocialMapper;

    @InjectMocks
    private PacienteMapper mapper;

    @Test
    void toDTO_withNull_returnsNull() {
        assertThat(mapper.toDTO(null)).isNull();
    }

    @Test
    void toDTO_mapsAllFields() {
        Paciente p = Paciente.builder().email("e@e").build();
        p.setCreatedAt(LocalDateTime.now());
        p.setId("A");

        when(dadoPessoalMapper.mapToDTO(p.getDadoPessoal())).thenReturn(new DadoPessoalDTO());
        when(enderecoMapper.mapToDTO(p.getEndereco())).thenReturn(new EnderecoDTO());
        when(contatoEmergenciaMapper.toDTOList(p.getContatosEmergencia())).thenReturn(List.of());
        when(dadoClinicoMapper.toDTOList(p.getDadosClinicos())).thenReturn(List.of());
        when(informacaoHospitalarMapper.toDTO(p.getInformacaoHospitalar())).thenReturn(null);
        when(dadoSocialMapper.toDTO(p.getDadoSocial())).thenReturn(new DadoSocialDTO());

        PacienteDTO dto = mapper.toDTO(p);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("A");
        assertThat(dto.getEmail()).isEqualTo("e@e");
        assertThat(dto.getContatosDeEmergencia()).isNotNull();
        assertThat(dto.getDadosClinicos()).isNotNull();
    }

    @Test
    void toListDTO_handlesNullList() {
        assertThat(mapper.toListDTO(null)).isNull();
        assertThat(mapper.toListDTO(List.of())).isEmpty();
    }
}
