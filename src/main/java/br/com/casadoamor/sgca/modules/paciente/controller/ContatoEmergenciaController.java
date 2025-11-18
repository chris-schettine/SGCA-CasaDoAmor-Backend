package br.com.casadoamor.sgca.modules.paciente.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.services.ContatoEmergenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/contatos-emergencia")
@RequiredArgsConstructor
public class ContatoEmergenciaController {

  private final ContatoEmergenciaService service;

  @PostMapping("/{pacienteId}")
  @PreAuthorize("hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  public ResponseEntity<ContatoEmergenciaDTO> criar(
          @PathVariable String pacienteId,
          @Valid @RequestBody ContatoEmergenciaInputDTO dto) {

      return ResponseEntity.ok(service.criar(pacienteId, dto));
  }

  @GetMapping("/paciente/{pacienteId}")
  public ResponseEntity<List<ContatoEmergenciaDTO>> listar(
          @PathVariable String pacienteId) {

      return ResponseEntity.ok(service.listarPorPaciente(pacienteId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ContatoEmergenciaDTO> atualizar(
          @PathVariable String id,
          @Valid @RequestBody EditarContatoEmergenciaInputDTO dto) {

      return ResponseEntity.ok(service.atualizar(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remover(@PathVariable String id) {
    service.remover(id);
    return ResponseEntity.noContent().build();
  }
}
