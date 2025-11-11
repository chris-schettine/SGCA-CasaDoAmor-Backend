package br.com.casadoamor.sgca.modules.acompanhante.service;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.EditarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.RegistrarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoAcompanhanteDTO;

public interface AcompanhanteService {
  AcompanhanteDTO registrarAcompanhante(RegistrarAcompanhanteDTO dto);
  
  AcompanhanteDTO editarAcompanhante(String id, EditarAcompanhanteDTO dto);

  PaginatedResponseDTO<AcompanhanteDTO> acompanhantesPaginados(String searchText, int limit, int offset);

  PaginatedResponseDTO<HistoricoAcompanhanteDTO> historicoAcompanhantePaginado(String acompanhanteId, int limit, int offset);

  void deletarAcompanhante(String id);

  PaginatedResponseDTO<AcompanhanteDTO> listarAcompanhantesPorPaciente(String pacienteId, int limit, int offset);
}
