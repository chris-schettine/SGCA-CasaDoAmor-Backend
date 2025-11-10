package br.com.casadoamor.sgca.modules.paciente.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.services.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pacientes")
@Tag(name = "Paciente", description = "Endpoints para gerenciar pacientes")
public class PacienteController {
  private final PacienteService pacienteService;

  @PostMapping("/")
  @Operation(summary = "Registrar um novo paciente (Apenas dados brutos)")
  @PreAuthorize("hasAuthority('PACIENTE_CRIAR') or hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  public ResponseEntity<PacienteDTO> registrarPaciente(@Valid @RequestBody RegistrarPacienteDTO registrarPacienteDTO) {
    PacienteDTO paciente = this.pacienteService.registrarPaciente(registrarPacienteDTO);
    return new ResponseEntity<>(paciente, HttpStatus.CREATED);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Editar um paciente existente")
  @PreAuthorize("hasAuthority('PACIENTE_EDITAR') or hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  public ResponseEntity<PacienteDTO> editarPaciente(
    @PathVariable String id,
    @Valid @RequestBody EditarPacienteDTO editarPacienteDTO) {
    PacienteDTO paciente = pacienteService.editarPaciente(id, editarPacienteDTO);
    return new ResponseEntity<>(paciente, HttpStatus.OK);
  }

  @GetMapping("/")
  @PreAuthorize("hasAuthority('PACIENTE_VER') or hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  @Operation(summary = "Listar pacientes com paginação e filtro opcional")
  public PaginatedResponseDTO<PacienteDTO> pacientesPaginados(
    @RequestParam(defaultValue = "10") int limit,
    @RequestParam(defaultValue = "0") int offset,
    @RequestParam(required = false) String searchText
  ) {
    return pacienteService.pacientesPaginados(searchText, limit, offset);
  }

  @GetMapping("/{pacienteId}/historico")
  @PreAuthorize("hasAuthority('PACIENTE_VER') or hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  @Operation(summary = "Listar o histórico de um paciente com paginação")
  public PaginatedResponseDTO<HistoricoPacienteDTO> historicoPaciente(
    @PathVariable String pacienteId,
    @RequestParam(defaultValue = "10") int limit,
    @RequestParam(defaultValue = "0") int offset
  ) {
    return pacienteService.historicoPacientePaginado(pacienteId, limit, offset);
  }
}
