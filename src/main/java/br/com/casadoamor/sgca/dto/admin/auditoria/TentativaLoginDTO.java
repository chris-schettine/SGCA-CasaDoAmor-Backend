package br.com.casadoamor.sgca.dto.admin.auditoria;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para tentativas de login com informações do usuário
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TentativaLoginDTO {
    
    private Long id;
    private String cpf;
    private String ipOrigem;
    private String userAgent;
    private LocalDateTime dataTentativa;
    private Boolean sucesso;
    private String motivoFalha;
    private Boolean bloqueado;
    
    // Informações do usuário (se encontrado)
    private UsuarioTentativaDTO usuario;
    
    /**
     * DTO resumido do usuário na tentativa de login
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioTentativaDTO {
        private Long id;
        private String nome;
        private String email;
        private String tipo;
        private Boolean ativo;
        private Boolean bloqueado;
    }
}
