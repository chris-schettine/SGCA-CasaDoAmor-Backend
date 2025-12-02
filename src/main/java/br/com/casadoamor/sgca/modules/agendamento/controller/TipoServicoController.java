package br.com.casadoamor.sgca.modules.agendamento.controller;

import br.com.casadoamor.sgca.modules.agendamento.dto.TipoServicoResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.service.TipoServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento de tipos de serviço
 */
@Tag(name = "Tipos de Serviço", description = "Endpoints para consulta de tipos de serviço disponíveis")
@RestController
@RequestMapping("/api/tipos-servico")
@RequiredArgsConstructor
public class TipoServicoController {

    private final TipoServicoService tipoServicoService;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Buscar tipo de serviço por ID", description = "Retorna os dados de um tipo de serviço específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo de serviço não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TipoServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        TipoServicoResponseDTO response = tipoServicoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar tipos de serviço ativos", description = "Retorna todos os tipos de serviço disponíveis")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<TipoServicoResponseDTO>> listarAtivos() {
        List<TipoServicoResponseDTO> response = tipoServicoService.listarAtivos();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'RECEPCIONISTA')")
    @Operation(summary = "Listar serviços por profissional", description = "Retorna tipos de serviço compatíveis com o tipo de profissional selecionado")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/por-profissional/{profissionalId}")
    public ResponseEntity<List<TipoServicoResponseDTO>> listarPorProfissional(@PathVariable Long profissionalId) {
        List<TipoServicoResponseDTO> response = tipoServicoService.listarPorProfissional(profissionalId);
        return ResponseEntity.ok(response);
    }
}
