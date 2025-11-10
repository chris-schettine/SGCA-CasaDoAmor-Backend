package br.com.casadoamor.sgca.modules.funcionario.controller;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResumoDTO;
import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import br.com.casadoamor.sgca.modules.funcionario.service.ProfissionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento de profissionais (funcionários e voluntários)
 */
@Tag(name = "Profissionais", description = "Endpoints para gerenciamento de funcionários e voluntários")
@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cadastrar novo profissional", description = "Cadastra um novo funcionário ou voluntário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profissional cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "CPF ou registro profissional já cadastrado")
    })
    @PostMapping
    public ResponseEntity<ProfissionalResponseDTO> cadastrar(
            @Valid @RequestBody ProfissionalRequestDTO dto,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        ProfissionalResponseDTO response = profissionalService.cadastrar(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar profissional", description = "Atualiza os dados de um profissional existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profissional atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<ProfissionalResponseDTO> atualizar(
            @PathVariable String uuid,
            @Valid @RequestBody ProfissionalRequestDTO dto,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        ProfissionalResponseDTO response = profissionalService.atualizar(uuid, dto, usuarioLogado);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Buscar profissional por UUID", description = "Retorna os dados completos de um profissional")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profissional encontrado"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<ProfissionalResponseDTO> buscarPorUuid(@PathVariable String uuid) {
        ProfissionalResponseDTO response = profissionalService.buscarPorUuid(uuid);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar todos os profissionais", description = "Retorna lista resumida de todos os profissionais")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<ProfissionalResumoDTO>> listarTodos() {
        List<ProfissionalResumoDTO> response = profissionalService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar profissionais por categoria", description = "Retorna profissionais filtrados por categoria (FUNCIONARIO ou VOLUNTARIO)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProfissionalResumoDTO>> listarPorCategoria(
            @PathVariable CategoriaProfissional categoria) {
        
        List<ProfissionalResumoDTO> response = profissionalService.listarPorCategoria(categoria);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar profissionais ativos", description = "Retorna apenas profissionais com status ativo")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/ativos")
    public ResponseEntity<List<ProfissionalResumoDTO>> listarAtivos() {
        List<ProfissionalResumoDTO> response = profissionalService.listarAtivos();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Buscar profissionais por nome", description = "Busca profissionais cujo nome contém o texto fornecido")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/buscar")
    public ResponseEntity<List<ProfissionalResumoDTO>> buscarPorNome(
            @RequestParam String nome) {
        
        List<ProfissionalResumoDTO> response = profissionalService.buscarPorNome(nome);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar profissionais com paginação", description = "Retorna lista paginada de profissionais")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping("/paginated")
    public ResponseEntity<Page<ProfissionalResumoDTO>> listarComPaginacao(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<ProfissionalResumoDTO> response = profissionalService.listarComPaginacao(pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Inativar profissional", description = "Marca um profissional como inativo sem deletá-lo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Profissional inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    @PatchMapping("/{uuid}/inativar")
    public ResponseEntity<Void> inativar(
            @PathVariable String uuid,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        profissionalService.inativar(uuid, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar profissional", description = "Remove logicamente um profissional (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Profissional deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deletar(@PathVariable String uuid) {
        profissionalService.deletar(uuid);
        return ResponseEntity.noContent().build();
    }
}
