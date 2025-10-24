package br.com.casadoamor.sgca.entity.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa o registro profissional de um usuário do sistema.
 * Esta entidade é IMUTÁVEL - uma vez criada, não pode ser modificada.
 * Armazena informações como CRM, COREN, CRO, CREFITO, CRN, etc.
 */
@Entity
@Table(name = "auth_usuarios_registros_profissionais",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_registro_profissional_usuario", columnNames = "usuario_id"),
                @UniqueConstraint(name = "uk_registro_profissional_numero_tipo", columnNames = {"tipo_profissional", "numero_registro"})
        },
        indexes = {
                @Index(name = "idx_registro_profissional_tipo", columnList = "tipo_profissional"),
                @Index(name = "idx_registro_profissional_numero", columnList = "numero_registro")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUsuarioRegistroProfissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuário ao qual este registro pertence
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private AuthUsuario usuario;

    /**
     * Tipo de profissional
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_profissional", nullable = false, length = 20)
    private TipoProfissional tipoProfissional;

    /**
     * Número do registro profissional (CRM, COREN, CRO, CREFITO, CRN)
     */
    @Column(name = "numero_registro", nullable = false, length = 50)
    private String numeroRegistro;

    /**
     * RQE - Registro de Qualificação de Especialista (opcional)
     * Usado principalmente por médicos e dentistas
     */
    @Column(name = "rqe", length = 50)
    private String rqe;

    /**
     * Data de criação do registro
     */
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Usuário admin que criou este registro
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por")
    private AuthUsuario criadoPor;

    /**
     * Enum para tipos de profissionais da saúde
     */
    public enum TipoProfissional {
        DENTISTA("CRO - Conselho Regional de Odontologia"),
        ENFERMEIRO("COREN - Conselho Regional de Enfermagem"),
        FISIOTERAPEUTA("CREFITO - Conselho Regional de Fisioterapia e Terapia Ocupacional"),
        MEDICO("CRM - Conselho Regional de Medicina"),
        NUTRICIONISTA("CRN - Conselho Regional de Nutricionistas");

        private final String descricao;

        TipoProfissional(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }

        /**
         * Verifica se este tipo de profissional permite RQE
         */
        public boolean permiteRqe() {
            return this == MEDICO || this == DENTISTA;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
}
