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
 * Entidade para registrar todas as tentativas de login (sucesso e falhas)
 * Usada para auditoria e detecção de ataques brute force
 */
@Entity
@Table(name = "tentativas_login")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TentativaLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private AuthUsuario usuario;

    @Column(name = "cpf", nullable = false, length = 12)
    private String cpf;

    @Column(name = "ip_origem", nullable = false, length = 45)
    private String ipOrigem;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "data_tentativa", nullable = false)
    private LocalDateTime dataTentativa;

    @Column(name = "sucesso", nullable = false)
    private Boolean sucesso;

    @Column(name = "motivo_falha")
    private String motivoFalha;

    @Builder.Default
    @Column(name = "bloqueado")
    private Boolean bloqueado = false;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        if (dataTentativa == null) {
            dataTentativa = LocalDateTime.now();
        }
    }
}
