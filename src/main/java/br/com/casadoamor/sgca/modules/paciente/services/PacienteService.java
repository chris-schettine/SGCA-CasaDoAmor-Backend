package br.com.casadoamor.sgca.modules.paciente.services;

import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarPacienteDTO;

public interface PacienteService {
  PacienteDTO registrarPaciente(RegistrarPacienteDTO registrarPacienteDTO);

  PacienteDTO editarPaciente(String id, EditarPacienteDTO editarPacienteDTO);

  PaginatedResponseDTO<PacienteDTO> pacientesPaginados (String searchText, int limit, int offset);

  PaginatedResponseDTO<HistoricoPacienteDTO> historicoPacientePaginado(String pacienteId, int limit, int offset);

  void deletarPaciente(String id);
}
