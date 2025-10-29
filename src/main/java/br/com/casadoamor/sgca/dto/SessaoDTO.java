package br.com.casadoamor.sgca.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para informações de sessão de usuário
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessaoDTO {

    private Long id;
    private String ipOrigem;
    private String userAgent;
    private LocalDateTime criadoEm;
    private LocalDateTime expiraEm;
    private Boolean ativo;
    private Boolean atual; // Indica se é a sessão atual do usuário
    
    // Informações do usuário (adicionado para auditoria)
    private UsuarioSessaoDTO usuario;
    
    /**
     * DTO resumido do usuário na sessão
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioSessaoDTO {
        private Long id;
        private String nome;
        private String email;
        private String cpf;
        private String tipo;
    }
}
