package br.com.casadoamor.sgca.modules.agendamento.entity;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Modalidade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Prioridade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.TipoAtendimento;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Entidade que representa agendamentos de serviços para acompanhantes
 */
@Entity
@Table(name = "agendamentos_acompanhantes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE agendamentos_acompanhantes SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class AgendamentoAcompanhante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    private String uuid;

    // Relacionamentos principais
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhante_id", nullable = false)
    private Acompanhante acompanhante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servico_id", nullable = false)
    private TipoServico tipoServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_usuario_id")
    private AuthUsuario profissionalUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_vinculado_id")
    private Paciente pacienteVinculado;

    // Data e Hora
    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    // Status do Agendamento
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    // Tipo de Agendamento
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_atendimento", nullable = false)
    private TipoAtendimento tipoAtendimento = TipoAtendimento.ROTINA;

    // Modalidade
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade")
    private Modalidade modalidade = Modalidade.PRESENCIAL;

    // Local
    @Column(name = "local_atendimento")
    private String localAtendimento;

    // Prioridade
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade")
    private Prioridade prioridade = Prioridade.NORMAL;

    // Observações
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "observacoes_profissional", columnDefinition = "TEXT")
    private String observacoesProfissional;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    // Remarcação
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_original_id")
    private AgendamentoAcompanhante agendamentoOriginal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novo_agendamento_id")
    private AgendamentoAcompanhante novoAgendamento;

    // Confirmações e Lembretes
    @Builder.Default
    @Column(name = "confirmado_acompanhante")
    private Boolean confirmadoAcompanhante = false;

    @Column(name = "confirmado_acompanhante_em")
    private LocalDateTime confirmadoAcompanhanteEm;

    @Builder.Default
    @Column(name = "confirmado_profissional")
    private Boolean confirmadoProfissional = false;

    @Column(name = "confirmado_profissional_em")
    private LocalDateTime confirmadoProfissionalEm;

    @Builder.Default
    @Column(name = "lembrete_enviado")
    private Boolean lembreteEnviado = false;

    @Column(name = "lembrete_enviado_em")
    private LocalDateTime lembreteEnviadoEm;

    // Resultado do Atendimento
    @Column(name = "compareceu")
    private Boolean compareceu;

    @Column(name = "hora_chegada")
    private LocalDateTime horaChegada;

    @Column(name = "hora_inicio_atendimento")
    private LocalDateTime horaInicioAtendimento;

    @Column(name = "hora_fim_atendimento")
    private LocalDateTime horaFimAtendimento;

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.uuid == null) {
            this.uuid = java.util.UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
