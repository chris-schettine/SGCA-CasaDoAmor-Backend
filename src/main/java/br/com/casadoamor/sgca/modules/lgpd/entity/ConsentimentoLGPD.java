package br.com.casadoamor.sgca.modules.lgpd.entity;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade unificada para consentimentos LGPD
 * Utiliza estratégia polimórfica para gerenciar consentimentos de diferentes tipos de entidades
 * (usuários, profissionais, pacientes, etc.)
 */
@Entity
@Table(name = "consentimentos_lgpd", indexes = {
        @Index(name = "idx_consentimentos_tipo_entidade", columnList = "tipo_entidade, entidade_id"),
        @Index(name = "idx_consentimentos_data", columnList = "data_consentimento"),
        @Index(name = "idx_consentimentos_versao", columnList = "versao_termo"),
        @Index(name = "idx_consentimentos_uuid", columnList = "uuid")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentimentoLGPD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    /**
     * Tipo de entidade que consentiu
     * Exemplos: USUARIO, PROFISSIONAL, PACIENTE, VISITANTE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entidade", nullable = false, length = 50)
    private TipoEntidadeLGPD tipoEntidade;

    /**
     * ID da entidade na sua tabela específica
     * Para USUARIO: auth_usuarios.id
     * Para PROFISSIONAL: profissionais.id
     * Para PACIENTE: pacientes.id
     */
    @Column(name = "entidade_id", nullable = false)
    private Long entidadeId;

    // Dados do Termo
    @Column(name = "versao_termo", nullable = false, length = 50)
    private String versaoTermo;

    @Column(name = "escopo", columnDefinition = "TEXT")
    private String escopo;

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
    @JoinColumn(name = "registrado_por_id")
    private AuthUsuario registradoPor;

    // Metadata adicional (JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    // Auditoria (imutável, apenas created)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    private AuthUsuario createdBy;

    @PrePersist
    protected void onCreate() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
        if (this.dataConsentimento == null) {
            this.dataConsentimento = LocalDateTime.now();
        }
    }

    /**
     * Enum para tipos de entidades que podem ter consentimento LGPD
     */
    public enum TipoEntidadeLGPD {
        USUARIO,        // Usuários do sistema (auth_usuarios)
        PROFISSIONAL,   // Profissionais (funcionários e voluntários)
        PACIENTE,       // Pacientes da Casa do Amor
        VISITANTE,      // Visitantes/Acompanhantes
        FORNECEDOR      // Fornecedores/Parceiros (futuro)
    }
}
