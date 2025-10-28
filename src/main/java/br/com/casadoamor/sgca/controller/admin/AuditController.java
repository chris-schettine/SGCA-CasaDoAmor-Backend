package br.com.casadoamor.sgca.controller.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.casadoamor.sgca.dto.SessaoDTO;
import br.com.casadoamor.sgca.dto.admin.auditoria.AuditoriaPerfilDTO;
import br.com.casadoamor.sgca.dto.admin.auditoria.AuditoriaUsuarioDTO;
import br.com.casadoamor.sgca.entity.auth.TentativaLogin;
import br.com.casadoamor.sgca.repository.auth.SessaoUsuarioRepository;
import br.com.casadoamor.sgca.repository.auth.TentativaLoginRepository;
import br.com.casadoamor.sgca.service.admin.AuditoriaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller para endpoints de auditoria
 * Requer role ADMINISTRADOR ou AUDITOR
 */
@RestController
@RequestMapping("/admin/audit")
@Validated
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Auditoria", description = "Endpoints de relatórios e auditoria (requer ADMIN ou AUDITOR)")
public class AuditController {

    private final TentativaLoginRepository tentativaLoginRepository;
    private final SessaoUsuarioRepository sessaoRepository;
    private final AuditoriaAdminService auditoriaAdminService;

    /**
     * Relatório de tentativas de login
     * GET /admin/audit/logins
     */
    @GetMapping("/logins")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Relatório de logins", 
               description = "Relatório de tentativas de login com filtros opcionais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN ou AUDITOR")
    })
    public ResponseEntity<?> relatorioLogins(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Boolean sucesso,
            @RequestParam(required = false) String cpf) {
        
        List<TentativaLogin> tentativas;

        if (startDate != null && endDate != null) {
            if (sucesso != null) {
                tentativas = tentativaLoginRepository
                        .findByDataTentativaBetweenAndSucesso(startDate, endDate, sucesso);
            } else {
                tentativas = tentativaLoginRepository
                        .findByDataTentativaBetween(startDate, endDate);
            }
        } else if (cpf != null) {
            tentativas = tentativaLoginRepository.findByCpf(cpf);
        } else if (sucesso != null) {
            tentativas = tentativaLoginRepository.findBySucesso(sucesso);
        } else {
            // Últimas 100 tentativas
            tentativas = tentativaLoginRepository.findTop100ByOrderByDataTentativaDesc();
        }

        // Estatísticas
        long total = tentativas.size();
        long sucessos = tentativas.stream().filter(TentativaLogin::getSucesso).count();
        long falhas = total - sucessos;

        return ResponseEntity.ok(new RelatorioLoginsResponse(total, sucessos, falhas, tentativas));
    }

    /**
     * Relatório de sessões ativas
     * GET /admin/audit/sessions
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Sessões ativas", 
               description = "Lista todas as sessões ativas no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN ou AUDITOR")
    })
    public ResponseEntity<?> sessoesAtivas() {
        var sessoes = sessaoRepository.findByAtivoAndExpiraEmAfter(true, LocalDateTime.now());
        
        var sessoesDTO = sessoes.stream()
                .map(s -> SessaoDTO.builder()
                        .id(s.getId())
                        .ipOrigem(s.getIpOrigem())
                        .userAgent(s.getUserAgent())
                        .criadoEm(s.getCriadoEm())
                        .expiraEm(s.getExpiraEm())
                        .ativo(s.getAtivo())
                        .atual(false)
                        .build())
                .toList();

        return ResponseEntity.ok(new SessoesAtivasResponse(
                (long) sessoesDTO.size(),
                sessoesDTO
        ));
    }

    /**
     * DTO de resposta para relatório de logins
     */
    private record RelatorioLoginsResponse(
            long total,
            long sucessos,
            long falhas,
            List<TentativaLogin> tentativas
    ) {}

    /**
     * DTO de resposta para sessões ativas
     */
    private record SessoesAtivasResponse(
            long totalSessoes,
            List<SessaoDTO> sessoes
    ) {}

    /**
     * Auditoria completa de um usuário
     * GET /admin/audit/usuarios/{id}
     */
    @GetMapping("/usuarios/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Auditoria de usuário",
               description = "Retorna informações completas de auditoria de um usuário: quem criou, quem atribuiu perfis, histórico de alterações")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auditoria retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN ou AUDITOR")
    })
    public ResponseEntity<AuditoriaUsuarioDTO> auditoriaUsuario(@org.springframework.web.bind.annotation.PathVariable Long id) {
        AuditoriaUsuarioDTO auditoria = auditoriaAdminService.buscarAuditoriaUsuario(id);
        return ResponseEntity.ok(auditoria);
    }

    /**
     * Auditoria de um perfil
     * GET /admin/audit/perfis/{id}
     */
    @GetMapping("/perfis/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Auditoria de perfil",
               description = "Retorna informações de auditoria de um perfil: quem criou, quem adicionou permissões, usuários com o perfil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auditoria retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN ou AUDITOR")
    })
    public ResponseEntity<AuditoriaPerfilDTO> auditoriaPerfil(@org.springframework.web.bind.annotation.PathVariable Long id) {
        AuditoriaPerfilDTO auditoria = auditoriaAdminService.buscarAuditoriaPerfil(id);
        return ResponseEntity.ok(auditoria);
    }

    /**
     * Auditoria de todos os perfis
     * GET /admin/audit/perfis
     */
    @GetMapping("/perfis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Auditoria de todos os perfis",
               description = "Retorna informações de auditoria de todos os perfis do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de auditorias retornada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN ou AUDITOR")
    })
    public ResponseEntity<List<AuditoriaPerfilDTO>> listarAuditoriaPerfis() {
        List<AuditoriaPerfilDTO> auditorias = auditoriaAdminService.listarAuditoriaPerfis();
        return ResponseEntity.ok(auditorias);
    }
}
