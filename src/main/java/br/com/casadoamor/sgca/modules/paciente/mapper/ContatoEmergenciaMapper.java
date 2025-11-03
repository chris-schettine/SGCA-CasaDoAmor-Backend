package br.com.casadoamor.sgca.modules.paciente.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.ContatoEmergencia;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@Component
public class ContatoEmergenciaMapper {
  public List<ContatoEmergencia> toEntityList(List<ContatoEmergenciaDTO> contatoEmergenciaDTOs, Paciente paciente) {
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
}
