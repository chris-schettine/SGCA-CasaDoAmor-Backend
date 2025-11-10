package br.com.casadoamor.sgca.modules.agendamento.entity;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusInscricao;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa a inscrição de um paciente em uma atividade em grupo
 */
@Entity
@Table(name = "inscricoes_atividade",
       uniqueConstraints = @UniqueConstraint(columnNames = {"atividade_id", "paciente_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoAtividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atividade_id", nullable = false)
    private AtividadeGrupo atividade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // Status
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusInscricao status = StatusInscricao.PENDENTE;

    // Datas importantes
    @Column(name = "inscrito_em", nullable = false)
    private LocalDateTime inscritoEm;

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    // Quem realizou a inscrição
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscrito_por")
    private AuthUsuario inscritoPor;

    // Auditoria
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AuthUsuario createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private AuthUsuario updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.inscritoEm == null) {
            this.inscritoEm = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos auxiliares
    public void confirmar() {
        this.status = StatusInscricao.CONFIRMADA;
        this.confirmadoEm = LocalDateTime.now();
    }

    public void cancelar() {
        this.status = StatusInscricao.CANCELADA;
        this.canceladoEm = LocalDateTime.now();
    }

    public void colocarEmListaEspera() {
        this.status = StatusInscricao.LISTA_ESPERA;
    }
}
