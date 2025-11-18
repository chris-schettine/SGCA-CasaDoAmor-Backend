package br.com.casadoamor.sgca.modules.paciente.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.ContatoEmergencia;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@Component
public class ContatoEmergenciaMapper {

  public ContatoEmergenciaDTO toDTO(ContatoEmergencia contato) {
    if (contato == null) return null;

    return ContatoEmergenciaDTO.builder()
        .id(contato.getId())
        .nome(contato.getNome())
        .telefone(contato.getTelefone())
        .email(contato.getEmail())
        .pacienteId(contato.getPaciente().getId())
        .build();
  }

  public List<ContatoEmergencia> toEntityList(List<ContatoEmergenciaInputDTO> dtos, Paciente paciente) {
    if (dtos == null) {
      return Collections.emptyList();
    }

    return dtos.stream()
      .map(dto -> ContatoEmergencia.builder()
        .nome(dto.getNome())
        .telefone(dto.getTelefone())
        .email(dto.getEmail())
        .paciente(paciente)
        .build())
      .collect(Collectors.toList());
  }

  public List<ContatoEmergenciaDTO> toDTOList(List<ContatoEmergencia> contatos) {
    if (contatos == null) {
      return Collections.emptyList();
    }

    return contatos.stream()
      .map(this::toDTO)
      .collect(Collectors.toList());
  }

  public ContatoEmergencia toEntity(ContatoEmergenciaInputDTO dto, Paciente paciente) {
    if (dto == null) return null;

    return ContatoEmergencia.builder()
        .nome(dto.getNome())
        .telefone(dto.getTelefone())
        .email(dto.getEmail())
        .paciente(paciente)
        .build();
  }

  public void updateEntity(ContatoEmergencia entity, EditarContatoEmergenciaInputDTO dto) {
    if (entity == null || dto == null) return;

    if (dto.getNome() != null) {
      entity.setNome(dto.getNome());
    }
    if (dto.getTelefone() != null) {
      entity.setTelefone(dto.getTelefone());
    }
    if (dto.getEmail() != null) {
      entity.setEmail(dto.getEmail());
    }
  }
}
