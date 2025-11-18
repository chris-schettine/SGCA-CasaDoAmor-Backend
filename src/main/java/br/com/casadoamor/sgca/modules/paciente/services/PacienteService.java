package br.com.casadoamor.sgca.modules.paciente.services;

import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.common.enums.PacienteStatus;
import br.com.casadoamor.sgca.modules.common.enums.SexoEnum;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarObitoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarPacienteDTO;

public interface PacienteService {
  PacienteDTO registrarPaciente(RegistrarPacienteDTO registrarPacienteDTO);

  PacienteDTO editarPaciente(String id, EditarPacienteDTO editarPacienteDTO);

  public PaginatedResponseDTO<PacienteDTO> pacientesPaginados(
    String searchText, int limit, int offset,
    PacienteStatus status, String diagnostico, String hospitalReferencia,
    String dataCadastroInicio, String dataCadastroFim,
    Integer idadeMin, Integer idadeMax, SexoEnum genero,
    String cidade, String necessidadeEspecial
  );

  PaginatedResponseDTO<HistoricoPacienteDTO> historicoPacientePaginado(String pacienteId, int limit, int offset);

  void deletarPaciente(String id);

  PacienteDTO registrarObito(String id, RegistrarObitoDTO dto);
}
