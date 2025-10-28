package br.com.casadoamor.sgca.dto.admin.auditoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para auditoria de perfis (roles)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaPerfilDTO {
    
    private Long id;
    private String nome;
    private String descricao;
    
    // Informações de quem criou/atualizou
    private UsuarioResumoDTO criadoPor;
    private LocalDateTime criadoEm;
    private UsuarioResumoDTO atualizadoPor;
    private LocalDateTime atualizadoEm;
    
    // Permissões vinculadas
    private List<PermissaoComAuditoriaDTO> permissoes;
    
    // Usuários que possuem este perfil
    private Integer totalUsuarios;
    private List<UsuarioResumoDTO> usuarios;
    
    /**
     * DTO resumido de usuário
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioResumoDTO {
        private Long id;
        private String nome;
        private String email;
        private String tipo;
    }
    
    /**
     * DTO de permissão com auditoria
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissaoComAuditoriaDTO {
        private Long id;
        private String nome;
        private String descricao;
        private UsuarioResumoDTO criadoPor;
        private LocalDateTime criadoEm;
    }
}
