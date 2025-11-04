package br.com.casadoamor.sgca.modules.paciente.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.ContatoEmergencia;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@Component
public class ContatoEmergenciaMapper {
  public List<ContatoEmergencia> toEntityList(List<ContatoEmergenciaInputDTO> contatoEmergenciaDTOs, Paciente paciente) {
    if (contatoEmergenciaDTOs == null) {
      return Collections.emptyList();
    }

    return contatoEmergenciaDTOs.stream()
      .map(dto -> ContatoEmergencia.builder()
        .nome(dto.getNome())
        .telefone(dto.getTelefone())
        .email(dto.getEmail())
        .paciente(paciente)
        .build())
      .collect(Collectors.toList());
  }

  public List<ContatoEmergenciaDTO> toDTOList(List<ContatoEmergencia> contatosDeEmergencia) {
    if (contatosDeEmergencia == null) {
      return Collections.emptyList();
    }

    return contatosDeEmergencia.stream()
      .map(contato -> ContatoEmergenciaDTO.builder()
        .id(contato.getId())
        .nome(contato.getNome())
        .telefone(contato.getTelefone())
        .email(contato.getEmail())
        .build())
      .collect(Collectors.toList());
  }
}
