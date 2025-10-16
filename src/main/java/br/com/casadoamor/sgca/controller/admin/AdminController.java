package br.com.casadoamor.sgca.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.casadoamor.sgca.dto.admin.perfil.CreatePerfilDTO;
import br.com.casadoamor.sgca.dto.admin.perfil.PerfilDTO;
import br.com.casadoamor.sgca.dto.admin.permissao.CreatePermissaoDTO;
import br.com.casadoamor.sgca.dto.admin.permissao.PermissaoDTO;
import br.com.casadoamor.sgca.dto.admin.user.AtribuirRolesDTO;
import br.com.casadoamor.sgca.dto.admin.user.CreateUserDTO;
import br.com.casadoamor.sgca.dto.admin.user.UpdateUserDTO;
import br.com.casadoamor.sgca.dto.admin.user.UserResponseDTO;
import br.com.casadoamor.sgca.dto.common.MessageResponseDTO;
import br.com.casadoamor.sgca.service.admin.PerfilService;
import br.com.casadoamor.sgca.service.admin.PermissaoService;
import br.com.casadoamor.sgca.service.admin.UserManagementService;
import br.com.casadoamor.sgca.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller para endpoints administrativos
 * Todos os endpoints requerem role ADMINISTRADOR
 */
@RestController
@RequestMapping("/admin")
@Validated
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administração", description = "Endpoints para gerenciamento do sistema (requer ADMIN)")
public class AdminController {

    private final UserManagementService userManagementService;
    private final PerfilService perfilService;
    private final PermissaoService permissaoService;
    private final AuthService authService;
    private final br.com.casadoamor.sgca.service.imp.UserPhotoService userPhotoService;

    // ==================== USUÁRIOS ====================

    /**
     * Cria um novo usuário
     * POST /admin/users
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário e envia senha temporária por email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "409", description = "Email ou CPF já cadastrado")
    })
    public ResponseEntity<?> criarUsuario(@Valid @RequestBody CreateUserDTO request,
                                          Authentication authentication) {
        try {
            String cpf = authentication.getName();
            Long adminId = authService.findUserIdByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

            UserResponseDTO response = userManagementService.criarUsuario(request, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lista todos os usuários com paginação
     * GET /admin/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN")
    })
    public ResponseEntity<Page<UserResponseDTO>> listarUsuarios(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UserResponseDTO> usuarios = userManagementService.listarUsuarios(pageable);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Busca usuário por ID
     * GET /admin/users/{id}
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Buscar usuário", description = "Busca usuário por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> buscarUsuario(@PathVariable Long id) {
        try {
            UserResponseDTO usuario = userManagementService.buscarPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Atualiza dados do usuário
     * PUT /admin/users/{id}
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar usuário", description = "Atualiza dados do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id,
                                              @Valid @RequestBody UpdateUserDTO request,
                                              Authentication authentication) {
        try {
            String cpf = authentication.getName();
            Long adminId = authService.findUserIdByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

            UserResponseDTO response = userManagementService.atualizarUsuario(id, request, adminId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Deleta usuário (soft delete)
     * DELETE /admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar usuário", description = "Deleta usuário (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário deletado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id,
                                            Authentication authentication) {
        try {
            String cpf = authentication.getName();
            Long adminId = authService.findUserIdByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

            userManagementService.deletarUsuario(id, adminId);
            return ResponseEntity.ok(MessageResponseDTO.success("Usuário deletado com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Atribui perfis a um usuário
     * POST /admin/users/{id}/roles
     */
    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atribuir perfis", description = "Atribui perfis (roles) a um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfis atribuídos"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário ou perfil não encontrado")
    })
    public ResponseEntity<?> atribuirPerfis(@PathVariable Long id,
                                            @Valid @RequestBody AtribuirRolesDTO request,
                                            Authentication authentication) {
        try {
            String cpf = authentication.getName();
            Long adminId = authService.findUserIdByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

            UserResponseDTO response = userManagementService.atribuirPerfis(id, request, adminId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Force logout - revoga todas as sessões do usuário
     * POST /admin/users/{id}/force-logout
     */
    @PostMapping("/users/{id}/force-logout")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Force logout", description = "Revoga todas as sessões ativas do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessões revogadas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> forceLogout(@PathVariable Long id) {
        try {
            userManagementService.forceLogout(id);
            return ResponseEntity.ok(MessageResponseDTO.success("Todas as sessões do usuário foram revogadas"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==================== PERFIS (ROLES) ====================

    /**
     * Cria um novo perfil
     * POST /admin/roles
     */
    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar perfil", description = "Cria um novo perfil (role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Perfil criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "409", description = "Perfil já existe")
    })
    public ResponseEntity<?> criarPerfil(@Valid @RequestBody CreatePerfilDTO request,
                                         Authentication authentication) {
        try {
            String cpf = authentication.getName();
            Long adminId = authService.findUserIdByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

            PerfilDTO response = perfilService.criarPerfil(request, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lista todos os perfis
     * GET /admin/roles
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar perfis", description = "Lista todos os perfis (roles)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN")
    })
    public ResponseEntity<List<PerfilDTO>> listarPerfis() {
        List<PerfilDTO> perfis = perfilService.listarPerfis();
        return ResponseEntity.ok(perfis);
    }

    /**
     * Busca perfil por ID
     * GET /admin/roles/{id}
     */
    @GetMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Buscar perfil", description = "Busca perfil por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    public ResponseEntity<?> buscarPerfil(@PathVariable Long id) {
        try {
            PerfilDTO perfil = perfilService.buscarPorId(id);
            return ResponseEntity.ok(perfil);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Atualiza um perfil
     * PUT /admin/roles/{id}
     */
    @PutMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar perfil", description = "Atualiza dados do perfil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    public ResponseEntity<?> atualizarPerfil(@PathVariable Long id,
                                             @Valid @RequestBody CreatePerfilDTO request,
                                             Authentication authentication) {
        try {
            String cpf = authentication.getName();
            Long adminId = authService.findUserIdByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

            PerfilDTO response = perfilService.atualizarPerfil(id, request, adminId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Deleta um perfil
     * DELETE /admin/roles/{id}
     */
    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar perfil", description = "Deleta perfil (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil deletado"),
            @ApiResponse(responseCode = "400", description = "Perfil em uso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    public ResponseEntity<?> deletarPerfil(@PathVariable Long id) {
        try {
            perfilService.deletarPerfil(id);
            return ResponseEntity.ok(MessageResponseDTO.success("Perfil deletado com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==================== PERMISSÕES ====================

    /**
     * Cria uma nova permissão
     * POST /admin/permissions
     */
    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar permissão", description = "Cria uma nova permissão")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Permissão criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "409", description = "Permissão já existe")
    })
    public ResponseEntity<?> criarPermissao(@Valid @RequestBody CreatePermissaoDTO request,
                                            Authentication authentication) {
        try {
            String cpf = authentication.getName();
            Long adminId = authService.findUserIdByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

            PermissaoDTO response = permissaoService.criarPermissao(request, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lista todas as permissões
     * GET /admin/permissions
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar permissões", description = "Lista todas as permissões")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN")
    })
    public ResponseEntity<List<PermissaoDTO>> listarPermissoes() {
        List<PermissaoDTO> permissoes = permissaoService.listarPermissoes();
        return ResponseEntity.ok(permissoes);
    }

    /**
     * Busca permissão por ID
     * GET /admin/permissions/{id}
     */
    @GetMapping("/permissions/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Buscar permissão", description = "Busca permissão por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissão encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada")
    })
    public ResponseEntity<?> buscarPermissao(@PathVariable Long id) {
        try {
            PermissaoDTO permissao = permissaoService.buscarPorId(id);
            return ResponseEntity.ok(permissao);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Atualiza uma permissão
     * PUT /admin/permissions/{id}
     */
    @PutMapping("/permissions/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar permissão", description = "Atualiza dados da permissão")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissão atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMIN"),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada")
    })
    public ResponseEntity<?> atualizarPermissao(@PathVariable Long id,
                                                @Valid @RequestBody CreatePermissaoDTO request,
                                                Authentication authentication) {
        try {
            String cpf = authentication.getName();
            Long adminId = authService.findUserIdByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

            PermissaoDTO response = permissaoService.atualizarPermissao(id, request, adminId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==================== FOTOS DE PERFIL ====================

    /**
     * Upload de foto de perfil do usuário
     * POST /admin/users/{id}/foto
     */
    @PostMapping("/{id}/foto")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Upload de foto de perfil", description = "Faz upload da foto de perfil de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> uploadFoto(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String fotoUrl = userPhotoService.uploadFoto(id, file);
            return ResponseEntity.ok(new UploadFotoResponse(fotoUrl, "Foto atualizada com sucesso"));
        } catch (java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Obtém a URL da foto de perfil do usuário
     * GET /admin/users/{id}/foto
     */
    @GetMapping("/{id}/foto")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Obter URL da foto", description = "Retorna a URL da foto de perfil do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado ou sem foto")
    })
    public ResponseEntity<?> obterFoto(@PathVariable Long id) {
        try {
            String fotoUrl = userPhotoService.obterUrlFoto(id);
            if (fotoUrl == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Usuário não possui foto"));
            }
            return ResponseEntity.ok(new UrlFotoResponse(fotoUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Deleta a foto de perfil do usuário
     * DELETE /admin/users/{id}/foto
     */
    @DeleteMapping("/{id}/foto")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar foto de perfil", description = "Remove a foto de perfil do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> deletarFoto(@PathVariable Long id) {
        try {
            userPhotoService.deletarFoto(id);
            return ResponseEntity.ok(new UploadFotoResponse(null, "Foto deletada com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Classe interna para resposta de upload de foto
     */
    @SuppressWarnings("unused")
    private static class UploadFotoResponse {
        private final String fotoUrl;
        private final String message;

        public UploadFotoResponse(String fotoUrl, String message) {
            this.fotoUrl = fotoUrl;
            this.message = message;
        }

        public String getFotoUrl() {
            return fotoUrl;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Classe interna para resposta de URL da foto
     */
    @SuppressWarnings("unused")
    private static class UrlFotoResponse {
        private final String fotoUrl;

        public UrlFotoResponse(String fotoUrl) {
            this.fotoUrl = fotoUrl;
        }

        public String getFotoUrl() {
            return fotoUrl;
        }
    }

    /**
     * Classe interna para respostas de erro
     */
    @SuppressWarnings("unused")
    private static class ErrorResponse {
        private final String message;
        private final long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
