package br.com.casadoamor.sgca.modules.hospedagem.controller;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.dto.*;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.service.QuartoService;
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
 * Controller para gerenciamento de quartos/leitos
 */
@Tag(name = "Quartos", description = "Endpoints para gerenciamento de quartos e leitos")
@RestController
@RequestMapping("/api/quartos")
@RequiredArgsConstructor
public class QuartoController {

    private final QuartoService quartoService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cadastrar novo quarto", description = "Cadastra um novo quarto/leito no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quarto cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Código do quarto já cadastrado")
    })
    @PostMapping
    public ResponseEntity<QuartoResponseDTO> cadastrar(
            @Valid @RequestBody QuartoRequestDTO dto,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        QuartoResponseDTO response = quartoService.cadastrar(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar quarto", description = "Atualiza os dados de um quarto existente (capacidade editável)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quarto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Quarto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou capacidade menor que ocupação atual")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<QuartoResponseDTO> atualizar(
            @PathVariable String uuid,
            @Valid @RequestBody QuartoRequestDTO dto,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        QuartoResponseDTO response = quartoService.atualizar(uuid, dto, usuarioLogado);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Buscar quarto por UUID", description = "Retorna os dados completos de um quarto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quarto encontrado"),
            @ApiResponse(responseCode = "404", description = "Quarto não encontrado")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<QuartoResponseDTO> buscarPorUuid(@PathVariable String uuid) {
        QuartoResponseDTO response = quartoService.buscarPorUuidDTO(uuid);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar quartos com paginação e filtros", 
               description = "Retorna lista paginada de quartos com filtros opcionais por nome, ala, tipo e status")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping
    public ResponseEntity<Page<QuartoResumoDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) AlaQuarto ala,
            @RequestParam(required = false) TipoQuarto tipo,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<QuartoResumoDTO> response = quartoService.listarComPaginacao(nome, ala, tipo, ativo, pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar quartos ativos", description = "Retorna apenas quartos com status ativo")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/ativos")
    public ResponseEntity<List<QuartoResumoDTO>> listarAtivos() {
        List<QuartoResumoDTO> response = quartoService.listarAtivos();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar quartos por ala", description = "Retorna quartos filtrados por ala (FEMININA, MASCULINA, MISTA)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/ala/{ala}")
    public ResponseEntity<List<QuartoResumoDTO>> listarPorAla(@PathVariable AlaQuarto ala) {
        List<QuartoResumoDTO> response = quartoService.listarPorAla(ala);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar quartos com vagas", description = "Retorna apenas quartos com vagas disponíveis")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/disponiveis")
    public ResponseEntity<List<QuartoResumoDTO>> listarComVagas() {
        List<QuartoResumoDTO> response = quartoService.listarComVagas();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar quartos com vagas por ala", description = "Retorna quartos disponíveis filtrados por ala")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/disponiveis/ala/{ala}")
    public ResponseEntity<List<QuartoResumoDTO>> listarComVagasPorAla(@PathVariable AlaQuarto ala) {
        List<QuartoResumoDTO> response = quartoService.listarComVagasPorAla(ala);
        return ResponseEntity.ok(response);
    }



    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Obter estatísticas de ocupação", description = "Retorna estatísticas gerais e por ala da ocupação dos quartos")
    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    @GetMapping("/estatisticas")
    public ResponseEntity<EstatisticasOcupacaoDTO> obterEstatisticas() {
        EstatisticasOcupacaoDTO response = quartoService.obterEstatisticas();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Inativar quarto", description = "Marca um quarto como inativo (não permite se houver leitos ocupados)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quarto inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Quarto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Quarto possui leitos ocupados")
    })
    @PatchMapping("/{uuid}/inativar")
    public ResponseEntity<Void> inativar(
            @PathVariable String uuid,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        quartoService.inativar(uuid, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar quarto", description = "Reativa um quarto inativo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quarto ativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Quarto não encontrado")
    })
    @PatchMapping("/{uuid}/ativar")
    public ResponseEntity<Void> ativar(
            @PathVariable String uuid,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        quartoService.ativar(uuid, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Colocar quarto em manutenção", description = "Marca um quarto como em manutenção (não permite se houver leitos ocupados)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quarto colocado em manutenção com sucesso"),
            @ApiResponse(responseCode = "404", description = "Quarto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Quarto possui leitos ocupados")
    })
    @PatchMapping("/{uuid}/manutencao/ativar")
    public ResponseEntity<Void> ativarManutencao(
            @PathVariable String uuid,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        quartoService.ativarManutencao(uuid, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover quarto de manutenção", description = "Remove o status de manutenção de um quarto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quarto removido de manutenção com sucesso"),
            @ApiResponse(responseCode = "404", description = "Quarto não encontrado")
    })
    @PatchMapping("/{uuid}/manutencao/desativar")
    public ResponseEntity<Void> desativarManutencao(
            @PathVariable String uuid,
            @AuthenticationPrincipal AuthUsuario usuarioLogado) {
        
        quartoService.desativarManutencao(uuid, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar quarto", description = "Remove logicamente um quarto (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quarto deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Quarto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Quarto possui leitos ocupados")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deletar(@PathVariable String uuid) {
        quartoService.deletar(uuid);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar alas disponíveis", description = "Retorna lista de alas (enum) para dropdown")
    @ApiResponse(responseCode = "200", description = "Lista de alas retornada com sucesso")
    @GetMapping("/alas")
    public ResponseEntity<List<AlaQuartoDTO>> listarAlas() {
        List<AlaQuartoDTO> alas = java.util.Arrays.stream(AlaQuarto.values())
                .map(ala -> new AlaQuartoDTO(ala.name(), ala.getDescricao()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(alas);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'AUDITOR')")
    @Operation(summary = "Listar tipos de quarto", description = "Retorna lista de tipos (enum) para dropdown")
    @ApiResponse(responseCode = "200", description = "Lista de tipos retornada com sucesso")
    @GetMapping("/tipos")
    public ResponseEntity<List<TipoQuartoDTO>> listarTipos() {
        List<TipoQuartoDTO> tipos = java.util.Arrays.stream(br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto.values())
                .map(tipo -> new TipoQuartoDTO(tipo.name(), tipo.getDescricao()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(tipos);
    }
}
