package br.com.casadoamor.sgca.controller.paciente;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.casadoamor.sgca.dto.common.PaginatedResponseDTO;
import br.com.casadoamor.sgca.dto.paciente.EditarPacienteDTO;
import br.com.casadoamor.sgca.dto.paciente.PacienteDTO;
import br.com.casadoamor.sgca.dto.paciente.RegistrarPacienteDTO;
import br.com.casadoamor.sgca.service.paciente.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
  @Operation(summary = "Registrar um novo paciente")
  // TODO - ADICIONAR AUTENTICAÇÃO PARA PERMITIR APENAS RECEPCIONISTAS
  public ResponseEntity<PacienteDTO> registrarPaciente(@Valid @RequestBody  RegistrarPacienteDTO RegistrarPacienteDTO) {
    PacienteDTO paciente = this.pacienteService.registrarPaciente(RegistrarPacienteDTO);
    return new ResponseEntity<>(paciente, HttpStatus.OK);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Editar um paciente existente")
  // TODO - ADICIONAR AUTENTICAÇÃO PARA PERMITIR APENAS RECEPCIONISTAS
  public ResponseEntity<PacienteDTO> editarPaciente(
    @PathVariable UUID id,
    @Valid @RequestBody EditarPacienteDTO editarPacienteDTO) {
    PacienteDTO paciente = pacienteService.editarPaciente(id, editarPacienteDTO);
    return new ResponseEntity<>(paciente, HttpStatus.OK);
  }

  @GetMapping("/")
    // TODO - ADICIONAR AUTENTICAÇÃO PARA PERMITIR APENAS RECEPCIONISTAS
  @Operation(summary = "Listar pacientes com paginação e filtro opcional")
  public PaginatedResponseDTO<PacienteDTO> pacientesPaginados(
    @RequestParam(defaultValue = "10") int limit,
    @RequestParam(defaultValue = "0") int offset,
    @RequestParam(required = false) String searchText
  ) {
    return pacienteService.pacientesPaginados(searchText, limit, offset);
  }
}
