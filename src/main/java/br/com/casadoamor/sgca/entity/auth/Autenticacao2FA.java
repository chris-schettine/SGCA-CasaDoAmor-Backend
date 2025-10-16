package br.com.casadoamor.sgca.entity.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade para autenticação de dois fatores (2FA)
 * Armazena configuração de 2FA por usuário
 */
@Entity
@Table(name = "autenticacao_2fa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Autenticacao2FA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = false;

    @Column(name = "data_habilitacao")
    private LocalDateTime dataHabilitacao;

    @Column(name = "data_desabilitacao")
    private LocalDateTime dataDesabilitacao;

    @Column(name = "codigo_atual", length = 6)
    private String codigoAtual;

    @Column(name = "expiracao_codigo")
    private LocalDateTime expiracaoCodigo;

    @Column(name = "tentativas_falhas", nullable = false)
    private Integer tentativasFalhas = 0;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    /**
     * Verifica se o código está expirado
     */
    public boolean isCodigoExpirado() {
        return expiracaoCodigo != null && LocalDateTime.now().isAfter(expiracaoCodigo);
    }

    /**
     * Verifica se está bloqueado por tentativas excessivas
     */
    public boolean isBloqueado() {
        return bloqueadoAte != null && LocalDateTime.now().isBefore(bloqueadoAte);
    }

    /**
     * Incrementa tentativas falhas
     */
    public void incrementarTentativasFalhas() {
        this.tentativasFalhas++;
        if (this.tentativasFalhas >= 5) {
            // Bloqueia por 15 minutos após 5 tentativas falhas
            this.bloqueadoAte = LocalDateTime.now().plusMinutes(15);
        }
    }

    /**
     * Reseta tentativas falhas após sucesso
     */
    public void resetarTentativasFalhas() {
        this.tentativasFalhas = 0;
        this.bloqueadoAte = null;
    }
}
