package br.com.casadoamor.sgca.modules.paciente.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@Component
public class HistoricoPacienteMapper {
  public HistoricoPaciente toEntity(Paciente paciente, Acompanhante acompanhante, String descricao) {
    return HistoricoPaciente.builder()
      .paciente(paciente)
      .acompanhante(acompanhante)
      .descricao(descricao)
      .dataRegistro(LocalDateTime.now())
      .build();
  }
}
