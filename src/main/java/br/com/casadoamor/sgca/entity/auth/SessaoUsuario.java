package br.com.casadoamor.sgca.entity.auth;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade para rastrear sessões de usuário (tokens JWT ativos)
 * Permite revogar sessões específicas e implementar logout global
 */
@Entity
@Table(name = "sessoes_usuario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessaoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private AuthUsuario usuario;

    @Column(name = "token_jwt", nullable = false, length = 512)
    private String tokenJwt;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Builder.Default
    @Column(name = "ativo")
    private Boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    /**
     * Verifica se a sessão está expirada
     */
    public boolean isExpirada() {
        return LocalDateTime.now().isAfter(expiraEm);
    }

    /**
     * Verifica se a sessão é válida (ativa e não expirada)
     */
    public boolean isValida() {
        return ativo && !isExpirada();
    }

    /**
     * Revoga a sessão
     */
    public void revogar() {
        this.ativo = false;
    }
}
