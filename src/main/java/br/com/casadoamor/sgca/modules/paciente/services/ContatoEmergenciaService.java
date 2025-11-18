package br.com.casadoamor.sgca.modules.paciente.services;

import java.util.List;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarContatoEmergenciaInputDTO;

public interface ContatoEmergenciaService {

  ContatoEmergenciaDTO criar(String pacienteId, ContatoEmergenciaInputDTO dto);

  List<ContatoEmergenciaDTO> listarPorPaciente(String pacienteId);

  ContatoEmergenciaDTO atualizar(String id, EditarContatoEmergenciaInputDTO dto);

  void remover(String id);
}
