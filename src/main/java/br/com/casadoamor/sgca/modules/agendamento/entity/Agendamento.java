package br.com.casadoamor.sgca.modules.agendamento.entity;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Modalidade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa um agendamento de serviço individual
 */
@Entity
@Table(name = "agendamentos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    private String uuid;

    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servico_id", nullable = false)
    private TipoServico tipoServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id")
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhante_id")
    private Acompanhante acompanhante;

    // Data e Hora
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    // Status
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    // Local e Modalidade
    @Column(name = "local")
    private String local;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade")
    private Modalidade modalidade = Modalidade.PRESENCIAL;

    // Observações
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Column(name = "observacoes_atendimento", columnDefinition = "TEXT")
    private String observacoesAtendimento;

    // Histórico
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_original_id")
    private Agendamento agendamentoOriginal;

    // Lembretes
    @Builder.Default
    @Column(name = "lembrete_enviado")
    private Boolean lembreteEnviado = false;

    @Column(name = "lembrete_enviado_em")
    private LocalDateTime lembreteEnviadoEm;

    @Builder.Default
    @Column(name = "confirmacao_paciente")
    private Boolean confirmacaoPaciente = false;

    @Column(name = "confirmacao_paciente_em")
    private LocalDateTime confirmacaoPacienteEm;

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
        this.status = StatusAgendamento.CANCELADO;
        this.motivoCancelamento = motivo;
        this.canceledAt = LocalDateTime.now();
        this.canceledBy = canceladoPor;
    }

    public void remarcar(LocalDateTime novaDataInicio, LocalDateTime novaDataFim) {
        this.status = StatusAgendamento.REMARCADO;
        this.dataInicio = novaDataInicio;
        this.dataFim = novaDataFim;
    }

    public void confirmar() {
        this.status = StatusAgendamento.CONFIRMADO;
        this.confirmacaoPaciente = true;
        this.confirmacaoPacienteEm = LocalDateTime.now();
    }
}
