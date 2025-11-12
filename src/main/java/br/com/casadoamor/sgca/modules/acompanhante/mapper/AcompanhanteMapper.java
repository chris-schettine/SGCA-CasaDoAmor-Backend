package br.com.casadoamor.sgca.modules.acompanhante.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.RegistrarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.common.dto.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.common.mapper.DadoPessoalMapper;
import br.com.casadoamor.sgca.modules.common.mapper.EnderecoMapper;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AcompanhanteMapper {
  private final DadoPessoalMapper dadoPessoalMapper;
  private final EnderecoMapper enderecoMapper;

  public AcompanhanteDTO mapToDTO(Acompanhante acompanhante) {
    if (acompanhante == null) {
      return null;
    }
    DadoPessoalDTO dadoPessoalDTO = dadoPessoalMapper.mapToDTO(acompanhante.getDadoPessoal());
    EnderecoDTO enderecoDTO = enderecoMapper.mapToDTO(acompanhante.getEndereco());

    return AcompanhanteDTO.builder()
      .id(acompanhante.getId())
      .podeAjudarNaCozinha(acompanhante.getPodeAjudarNaCozinha())
      .dadoPessoal(dadoPessoalDTO)
      .endereco(enderecoDTO)
      .parentesco(acompanhante.getParentesco())
      .ativo(acompanhante.isAtivo())
      .pacienteNome(acompanhante.getPaciente().getDadoPessoal().getNome())
      .build();
  }

  public Acompanhante toEntity(RegistrarAcompanhanteDTO dto, Paciente paciente) {
    return Acompanhante.builder()
      .podeAjudarNaCozinha(dto.getPodeAjudarNaCozinha())
      .dadoPessoal(dadoPessoalMapper.toEntity(dto.getDadoPessoal()))
      .endereco(enderecoMapper.toEntity(dto.getEndereco()))
      .parentesco(dto.getParentesco())
      .paciente(paciente)
      .build();
  }
}
