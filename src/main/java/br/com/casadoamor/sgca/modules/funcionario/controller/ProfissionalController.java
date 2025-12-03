package br.com.casadoamor.sgca.modules.funcionario.controller;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.funcionario.dto.CategoriaProfissionalDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResumoDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.TipoVinculoDTO;
import br.com.casadoamor.sgca.modules.funcionario.entity.TipoVinculoEntity;
import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import br.com.casadoamor.sgca.modules.funcionario.repository.TipoVinculoRepository;
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
    private final TipoVinculoRepository tipoVinculoRepository;

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
    @Operation(summary = "Listar todos os profissionais", description = "Retorna lista resumida de todos os profissionais. Aceita parâmetros opcionais para busca.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<ProfissionalResumoDTO>> listarTodos(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false, defaultValue = "false") Boolean apenasAtivos) {
        
        // Se termo foi fornecido, faz busca; caso contrário, lista todos
        List<ProfissionalResumoDTO> response;
        if (termo != null && !termo.trim().isEmpty()) {
            response = apenasAtivos 
                ? profissionalService.buscarAtivosPorMultiplosCampos(termo)
                : profissionalService.buscarPorMultiplosCampos(termo);
        } else {
            response = apenasAtivos 
                ? profissionalService.listarAtivos()
                : profissionalService.listarTodos();
        }
        
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
    @Operation(summary = "Pesquisar profissionais", description = "Busca profissionais por nome, CPF, registro profissional ou categoria")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/pesquisar")
    public ResponseEntity<List<ProfissionalResumoDTO>> pesquisar(
            @RequestParam String termo,
            @RequestParam(required = false, defaultValue = "false") Boolean apenasAtivos) {
        
        List<ProfissionalResumoDTO> response = apenasAtivos 
            ? profissionalService.buscarAtivosPorMultiplosCampos(termo)
            : profissionalService.buscarPorMultiplosCampos(termo);
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
    @Operation(summary = "Alternar status do profissional", description = "Alterna entre ativo e inativo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    @PostMapping("/{uuid}/inativar")
    @PatchMapping("/{uuid}/inativar")
    public ResponseEntity<Void> toggleStatus(
            @PathVariable String uuid,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        profissionalService.toggleStatus(uuid, usuarioLogado);
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

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar tipos de vínculo", description = "Retorna os tipos de vínculo disponíveis para dropdown (da tabela do banco)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })

    @GetMapping("/tipos-vinculo")
    public ResponseEntity<List<TipoVinculoDTO>> listarTiposVinculo() {
        List<TipoVinculoEntity> tiposEntity = tipoVinculoRepository.findByAtivoTrueOrderByNomeAsc();
        
        List<TipoVinculoDTO> tipos = tiposEntity.stream()
                .map(tipo -> TipoVinculoDTO.builder()
                        .id(tipo.getId())
                        .codigo(tipo.getCodigo())
                        .nome(tipo.getNome())
                        .ativo(tipo.getAtivo())
                        .build())
                .toList();
        return ResponseEntity.ok(tipos);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar categorias profissionais", description = "Retorna as categorias/áreas de atuação disponíveis para dropdown")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaProfissionalDTO>> listarCategorias() {
        List<CategoriaProfissionalDTO> categorias = java.util.Arrays.stream(CategoriaProfissional.values())
                .map(cat -> new CategoriaProfissionalDTO(cat.name(), cat.getDescricao()))
                .toList();
        return ResponseEntity.ok(categorias);
    }
    
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Estatísticas do dashboard", description = "Retorna estatísticas importantes sobre profissionais para exibição no dashboard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    @GetMapping("/dashboard/stats")
    public ResponseEntity<br.com.casadoamor.sgca.modules.funcionario.dto.DashboardStatsDTO> getDashboardStats() {
        br.com.casadoamor.sgca.modules.funcionario.dto.DashboardStatsDTO stats = profissionalService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
