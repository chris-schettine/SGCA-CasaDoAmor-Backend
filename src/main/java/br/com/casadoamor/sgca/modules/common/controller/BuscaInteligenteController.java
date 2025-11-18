package br.com.casadoamor.sgca.modules.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.com.casadoamor.sgca.modules.common.dto.BuscaInteligenteResponseDTO;
import br.com.casadoamor.sgca.modules.common.service.BuscaInteligenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/busca-inteligente")
@Tag(name = "Busca inteligente")
public class BuscaInteligenteController {

  private final BuscaInteligenteService buscaInteligenteService;

  @GetMapping
  @Operation(summary = "Busca unificada de assistidos e acompanhantes")
  @PreAuthorize("hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
  public ResponseEntity<BuscaInteligenteResponseDTO> buscar(
          @RequestParam String termo
  ) {
      return ResponseEntity.ok(buscaInteligenteService.buscar(termo));
  }
}
