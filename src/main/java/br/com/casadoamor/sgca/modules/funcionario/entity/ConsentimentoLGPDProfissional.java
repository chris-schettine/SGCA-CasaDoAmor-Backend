package br.com.casadoamor.sgca.modules.funcionario.entity;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa o consentimento LGPD de um profissional (funcionário ou voluntário)
 * Esta tabela mantém um registro imutável de todos os consentimentos para auditoria
 */
@Entity
@Table(name = "consentimentos_lgpd_profissionais")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentimentoLGPDProfissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Profissional
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    // Dados do Termo
    @Column(name = "versao_termo", nullable = false, length = 50)
    private String versaoTermo; // Ex: "v1.0", "v2.0"

    @Column(name = "escopo", length = 255)
    private String escopo; // Descreve o que o consentimento cobre (nullable conforme migration)

    @Column(name = "concorda", nullable = false)
    private Boolean concorda;

    @Column(name = "data_consentimento", nullable = false)
    private LocalDateTime dataConsentimento;

    // Rastreamento
    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por")
    private AuthUsuario registradoPor;

    // Metadata adicional (JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    // Auditoria (imutável, apenas created)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", updatable = false)
    private AuthUsuario createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.dataConsentimento == null) {
            this.dataConsentimento = LocalDateTime.now();
        }
    }
}
