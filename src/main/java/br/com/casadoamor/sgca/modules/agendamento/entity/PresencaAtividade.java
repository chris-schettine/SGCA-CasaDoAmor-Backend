package br.com.casadoamor.sgca.modules.agendamento.entity;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.NivelParticipacao;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa a presença de um paciente em uma atividade em grupo
 */
@Entity
@Table(name = "presencas_atividade",
       uniqueConstraints = @UniqueConstraint(columnNames = {"atividade_id", "paciente_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresencaAtividade {

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

    // Presença
    @Builder.Default
    @Column(name = "presente", nullable = false)
    private Boolean presente = false;

    @Column(name = "horario_chegada")
    private LocalDateTime horarioChegada;

    @Column(name = "horario_saida")
    private LocalDateTime horarioSaida;

    // Participação
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_participacao")
    private NivelParticipacao nivelParticipacao;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos auxiliares
    public void registrarChegada() {
        this.presente = true;
        this.horarioChegada = LocalDateTime.now();
    }

    public void registrarSaida() {
        this.horarioSaida = LocalDateTime.now();
    }
}
