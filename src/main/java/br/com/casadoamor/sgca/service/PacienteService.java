package br.com.casadoamor.sgca.service;

import java.util.UUID;

import br.com.casadoamor.sgca.dto.EditarPacienteDTO;
import br.com.casadoamor.sgca.dto.PacienteDTO;
import br.com.casadoamor.sgca.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.dto.RegistrarPacienteDTO;

public interface PacienteService {
  PacienteDTO registrarPaciente(RegistrarPacienteDTO registrarPacienteDTO);

  PacienteDTO editarPaciente(UUID id, EditarPacienteDTO editarPacienteDTO);

  PaginatedResponseDTO<PacienteDTO> pacientesPaginados (String searchText, int limit, int offset);
}
