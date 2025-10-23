package br.com.casadoamor.sgca.service.paciente;

import br.com.casadoamor.sgca.dto.common.PaginatedResponseDTO;
import br.com.casadoamor.sgca.dto.paciente.EditarPacienteDTO;
import br.com.casadoamor.sgca.dto.paciente.PacienteDTO;
import br.com.casadoamor.sgca.dto.paciente.RegistrarPacienteDTO;

public interface PacienteService {
  PacienteDTO registrarPaciente(RegistrarPacienteDTO registrarPacienteDTO);

  PacienteDTO editarPaciente(String id, EditarPacienteDTO editarPacienteDTO);

  PaginatedResponseDTO<PacienteDTO> pacientesPaginados (String searchText, int limit, int offset);
}
