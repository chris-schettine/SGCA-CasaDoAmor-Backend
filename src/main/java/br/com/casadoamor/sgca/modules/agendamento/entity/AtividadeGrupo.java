package br.com.casadoamor.sgca.modules.agendamento.entity;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Modalidade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAtividade;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma atividade em grupo (palestras, oficinas, eventos)
 */
@Entity
@Table(name = "atividades_grupo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    private String uuid;

    // Informações Básicas
    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tipo")
    private String tipo; // Palestra, Oficina, Workshop, etc.

    // Data e Hora
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    // Local e Modalidade
    @Column(name = "local")
    private String local;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade")
    private Modalidade modalidade = Modalidade.PRESENCIAL;

    // Capacidade e Público-Alvo
    @Column(name = "capacidade")
    private Integer capacidade;

    @Column(name = "publico_alvo")
    private String publicoAlvo;

    @Column(name = "idade_minima")
    private Integer idadeMinima;

    @Column(name = "idade_maxima")
    private Integer idadeMaxima;

    // Responsáveis
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_principal_id")
    private Profissional responsavelPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_apoio_id")
    private Profissional responsavelApoio;

    // Materiais (JSON)
    @Column(name = "materiais", columnDefinition = "JSON")
    private String materiais;

    // Status
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusAtividade status = StatusAtividade.PLANEJADA;

    // Observações
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "observacoes_pos_atividade", columnDefinition = "TEXT")
    private String observacoesPosAtividade;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    // Inscrições
    @Builder.Default
    @Column(name = "requer_inscricao")
    private Boolean requerInscricao = false;

    @Builder.Default
    @Column(name = "inscricoes_abertas")
    private Boolean inscricoesAbertas = true;

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

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by")
    private AuthUsuario canceledBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.uuid == null || this.uuid.isEmpty()) {
            this.uuid = java.util.UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos auxiliares
    public void cancelar(String motivo, AuthUsuario canceladoPor) {
        this.status = StatusAtividade.CANCELADA;
        this.motivoCancelamento = motivo;
        this.canceledAt = LocalDateTime.now();
        this.canceledBy = canceladoPor;
    }

    public void concluir(String observacoes) {
        this.status = StatusAtividade.CONCLUIDA;
        this.observacoesPosAtividade = observacoes;
    }

    public void fecharInscricoes() {
        this.inscricoesAbertas = false;
    }

    public void abrirInscricoes() {
        this.inscricoesAbertas = true;
    }
}
