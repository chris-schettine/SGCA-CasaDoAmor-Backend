package br.com.casadoamor.sgca.modules.paciente.mapper;


import java.util.List;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.common.dto.DadoSocialDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.mapper.DadoPessoalMapper;
import br.com.casadoamor.sgca.modules.common.mapper.DadoSocialMapper;
import br.com.casadoamor.sgca.modules.common.mapper.EnderecoMapper;
import br.com.casadoamor.sgca.modules.dadoClinico.mapper.DadoClinicoMapper;
import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.InformacaoHospitalarDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PacienteMapper {
  private final EnderecoMapper enderecoMapper;
  private final DadoPessoalMapper dadoPessoalMapper;
  private final ContatoEmergenciaMapper contatoEmergenciaMapper;
  private final DadoClinicoMapper dadoClinicoMapper;
  private final InformacaoHospitalarMapper informacaoHospitalarMapper;
  private final DadoSocialMapper dadoSocialMapper;

  public Paciente toEntityFromEntities (DadoPessoal dadoPessoal, Endereco endereco) {
    return Paciente.builder()
      .dadoPessoal(dadoPessoal)
      .endereco(endereco)
      .build();
  }

  public PacienteDTO toDTO (Paciente paciente) {
    DadoPessoalDTO dadoPessoal = dadoPessoalMapper.mapToDTO(paciente.getDadoPessoal());
    EnderecoDTO endereco = enderecoMapper.mapToDTO(paciente.getEndereco());
    List<ContatoEmergenciaDTO> contatosDeEmergencia = contatoEmergenciaMapper.toDTOList(paciente.getContatosEmergencia());
    List<DadoClinicoDTO> dadosClinicos = dadoClinicoMapper.toDTOList(paciente.getDadosClinicos());
    InformacaoHospitalarDTO informacaoHospitalar = informacaoHospitalarMapper.toDTO(paciente.getInformacaoHospitalar());
    DadoSocialDTO dadoSocial = dadoSocialMapper.toDTO(paciente.getDadoSocial());

    return PacienteDTO.builder()
      .id(paciente.getId())
      .email(paciente.getEmail())
      .dadoPessoal(dadoPessoal)
      .endereco(endereco)
      .createdAt(paciente.getCreatedAt())
      .imageUrl(null) // TO DO - implementar imagem do paciente
      .contatosDeEmergencia(contatosDeEmergencia)
      .dadosClinicos(dadosClinicos)
      .informacaoHospitalar(informacaoHospitalar)
      .dadoSocial(dadoSocial)
      .build();
  }
}
