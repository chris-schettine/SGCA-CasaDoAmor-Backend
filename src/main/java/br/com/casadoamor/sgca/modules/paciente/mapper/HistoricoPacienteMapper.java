package br.com.casadoamor.sgca.modules.paciente.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.acompanhante.mapper.AcompanhanteMapper;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HistoricoPacienteMapper {
  private final AcompanhanteMapper acompanhanteMapper;
  private final PacienteMapper pacienteMapper;

  public HistoricoPaciente toEntity(Paciente paciente, Acompanhante acompanhante, String descricao) {
    return HistoricoPaciente.builder()
      .paciente(paciente)
      .acompanhante(acompanhante)
      .descricao(descricao)
      .dataRegistro(LocalDateTime.now())
      .build();
  }

  public HistoricoPacienteDTO toHistoricoPacienteDTO(HistoricoPaciente historicoPaciente) {
    if (historicoPaciente == null) {
      return null;
    }

    AcompanhanteDTO acompanhante = null;
    if (historicoPaciente.getAcompanhante() != null) {
      acompanhante = acompanhanteMapper.mapToDTO(historicoPaciente.getAcompanhante());
    }

    return HistoricoPacienteDTO.builder()
      .id(historicoPaciente.getId())
      .descricao(historicoPaciente.getDescricao())
      .dataRegistro(historicoPaciente.getDataRegistro())
      .acompanhante(acompanhante)
      .build();
  }

  public HistoricoAcompanhanteDTO toHistoricoAcompanhanteDTO(HistoricoPaciente historicoPaciente) {
    if (historicoPaciente == null) {
      return null;
    }

    PacienteDTO paciente  = pacienteMapper.toDTO(historicoPaciente.getPaciente());

    return HistoricoAcompanhanteDTO.builder()
      .id(historicoPaciente.getId())
      .descricao(historicoPaciente.getDescricao())
      .dataRegistro(historicoPaciente.getDataRegistro())
      .paciente(paciente)
      .build();
  }
}
