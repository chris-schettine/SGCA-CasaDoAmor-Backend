package br.com.casadoamor.sgca.controller;

import br.com.casadoamor.sgca.dto.paciente.DadoClinicoDTO;
import br.com.casadoamor.sgca.dto.paciente.DadoClinicoInputDTO;
import br.com.casadoamor.sgca.service.DadoClinicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes/{pacienteId}/dados-clinicos")
@RequiredArgsConstructor
@Tag(name = "Dados Clínicos", description = "Endpoints para gerenciar dados clínicos dos pacientes")
@SecurityRequirement(name = "bearerAuth")
public class DadoClinicoController {

  private final DadoClinicoService dadoClinicoService;

  @PostMapping
  @Operation(summary = "Criar novo registro de dados clínicos",
      description = "Cria um novo registro de dados clínicos para o paciente. " +
          "Os registros são cumulativos, mantendo histórico.")
  public ResponseEntity<DadoClinicoDTO> criarDadoClinico(
      @PathVariable String pacienteId,
      @Valid @RequestBody DadoClinicoInputDTO dto) {
    DadoClinicoDTO criado = dadoClinicoService.criarDadoClinico(pacienteId, dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(criado);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar dados clínicos",
      description = "Atualiza um registro específico de dados clínicos")
  public ResponseEntity<DadoClinicoDTO> atualizarDadoClinico(
      @PathVariable String pacienteId,
      @PathVariable String id,
      @Valid @RequestBody DadoClinicoInputDTO dto) {
    DadoClinicoDTO atualizado = dadoClinicoService.atualizarDadoClinico(id, dto);
    return ResponseEntity.ok(atualizado);
  }

  @GetMapping
  @Operation(summary = "Listar histórico de dados clínicos",
      description = "Retorna todos os registros de dados clínicos do paciente, ordenados do mais recente ao mais antigo")
  public ResponseEntity<List<DadoClinicoDTO>> listarDadosClinicos(
      @PathVariable String pacienteId) {
    List<DadoClinicoDTO> dadosClinicos = dadoClinicoService.buscarDadosClinicosPorPaciente(pacienteId);
    return ResponseEntity.ok(dadosClinicos);
  }

  @GetMapping("/atual")
  @Operation(summary = "Buscar dados clínicos atuais",
      description = "Retorna o registro mais recente de dados clínicos do paciente")
  public ResponseEntity<DadoClinicoDTO> buscarDadoClinicoAtual(
      @PathVariable String pacienteId) {
    DadoClinicoDTO dadoClinico = dadoClinicoService.buscarDadoClinicoAtual(pacienteId);
    return ResponseEntity.ok(dadoClinico);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar dados clínicos por ID",
      description = "Retorna um registro específico de dados clínicos")
  public ResponseEntity<DadoClinicoDTO> buscarPorId(
      @PathVariable String pacienteId,
      @PathVariable String id) {
    DadoClinicoDTO dadoClinico = dadoClinicoService.buscarPorId(id);
    return ResponseEntity.ok(dadoClinico);
  }
}
