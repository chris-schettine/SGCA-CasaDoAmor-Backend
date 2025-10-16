package br.com.casadoamor.sgca.entity.auth;

import java.time.LocalDateTime;

import br.com.casadoamor.sgca.enums.TipoToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entidade para tokens de recuperação de senha, verificação de email e 2FA
 */
@Entity
@Table(name = "tokens_recuperacao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRecuperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private AuthUsuario usuario;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoToken tipo;

    @Column(name = "expiracao", nullable = false)
    private LocalDateTime expiracao;

    @Builder.Default
    @Column(name = "usado")
    private Boolean usado = false;

    @Column(name = "usado_em")
    private LocalDateTime usadoEm;

    @Column(name = "ip_geracao", length = 45)
    private String ipGeracao;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    /**
     * Verifica se o token está expirado
     */
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(expiracao);
    }

    /**
     * Verifica se o token é válido (não usado e não expirado)
     */
    public boolean isValido() {
        return !usado && !isExpirado();
    }
}
