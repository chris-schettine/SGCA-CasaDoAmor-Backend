package br.com.casadoamor.sgca.dto.admin.auditoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para auditoria completa de um usuário
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaUsuarioDTO {
    
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String tipo;
    private Boolean ativo;
    
    // Informações de quem criou/atualizou
    private UsuarioResumoDTO criadoPor;
    private LocalDateTime criadoEm;
    private UsuarioResumoDTO atualizadoPor;
    private LocalDateTime atualizadoEm;
    
    // Perfis atribuídos
    private List<PerfilAuditoriaDTO> perfis;
    
    // Dados pessoais
    private DadosPessoaisAuditoriaDTO dadosPessoais;
    
    // Endereço
    private EnderecoAuditoriaDTO endereco;
    
    // Registro profissional
    private RegistroProfissionalAuditoriaDTO registroProfissional;
    
    /**
     * DTO resumido de usuário para evitar recursividade
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
     * DTO de perfil com auditoria
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerfilAuditoriaDTO {
        private Long id;
        private String nome;
        private String descricao;
        private LocalDateTime atribuidoEm;
        private Integer totalPermissoes;
        private List<PermissaoResumoDTO> permissoes;
    }
    
    /**
     * DTO resumido de permissão
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissaoResumoDTO {
        private Long id;
        private String nome;
        private String descricao;
    }
    
    /**
     * DTO de dados pessoais com auditoria
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DadosPessoaisAuditoriaDTO {
        private UsuarioResumoDTO criadoPor;
        private LocalDateTime criadoEm;
        private UsuarioResumoDTO atualizadoPor;
        private LocalDateTime atualizadoEm;
    }
    
    /**
     * DTO de endereço com auditoria
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnderecoAuditoriaDTO {
        private UsuarioResumoDTO criadoPor;
        private LocalDateTime criadoEm;
        private UsuarioResumoDTO atualizadoPor;
        private LocalDateTime atualizadoEm;
    }
    
    /**
     * DTO de registro profissional com auditoria
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistroProfissionalAuditoriaDTO {
        private String tipoProfissional;
        private String numeroRegistro;
        private String rqe;
        private UsuarioResumoDTO criadoPor;
        private LocalDateTime criadoEm;
    }
}
