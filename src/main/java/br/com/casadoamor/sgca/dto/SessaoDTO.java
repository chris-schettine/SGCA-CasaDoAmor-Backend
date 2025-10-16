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
}
