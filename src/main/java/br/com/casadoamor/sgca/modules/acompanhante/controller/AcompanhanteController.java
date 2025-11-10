package br.com.casadoamor.sgca.modules.acompanhante.controller;

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

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.EditarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.RegistrarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.service.AcompanhanteService;
import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoPacienteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/acompanhantes")
@Tag(name = "Acompanhante", description = "Endpoints para gerenciar acompanhantes")
public class AcompanhanteController {
  private final AcompanhanteService acompanhanteService;

  @PostMapping("/")
  @Operation(summary = "Registrar um novo acompanhante")
  @PreAuthorize("hasAuthority('ACOMPANHANTES_CRIAR') or hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  public ResponseEntity<AcompanhanteDTO> registrarAcompanhante(@Valid @RequestBody RegistrarAcompanhanteDTO registrarAcompanhanteDTO) {
    AcompanhanteDTO acompanhante = this.acompanhanteService.registrarAcompanhante(registrarAcompanhanteDTO);
    return new ResponseEntity<>(acompanhante, HttpStatus.OK);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Editar um acompanhante existente")
  @PreAuthorize("hasAuthority('ACOMPANHANTES_EDITAR') or hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  public ResponseEntity<AcompanhanteDTO> editarAcompanhante(
    @PathVariable String id,
    @Valid @RequestBody EditarAcompanhanteDTO editarAcompanhanteDTO) {
    AcompanhanteDTO acompanhante = acompanhanteService.editarAcompanhante(id, editarAcompanhanteDTO);
    return new ResponseEntity<>(acompanhante, HttpStatus.OK);
  }

  @GetMapping("/")
  @Operation(summary = "Listar acompanhantes com paginação e filtro opcional")
  @PreAuthorize("hasAuthority('ACOMPANHANTES_VER') or hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  public ResponseEntity<PaginatedResponseDTO<AcompanhanteDTO>> acompanhantesPaginados(
    @RequestParam(defaultValue = "10") int limit,
    @RequestParam(defaultValue = "0") int offset,
    @RequestParam(required = false) String searchText
  ) {
    PaginatedResponseDTO<AcompanhanteDTO> page = acompanhanteService.acompanhantesPaginados(searchText, limit, offset);
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @GetMapping("/{acompanhanteId}/historico")
  @PreAuthorize("hasAuthority('ACOMPANHANTES_VER') or hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  @Operation(summary = "Listar o histórico de um acompanhante com paginação")
  public PaginatedResponseDTO<HistoricoAcompanhanteDTO> historicoAcompanhante(
    @PathVariable String acompanhanteId,
    @RequestParam(defaultValue = "10") int limit,
    @RequestParam(defaultValue = "0") int offset
  ) {
    return acompanhanteService.historicoAcompanhantePaginado(acompanhanteId, limit, offset);
  }
}
