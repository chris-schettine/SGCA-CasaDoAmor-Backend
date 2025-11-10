package br.com.casadoamor.sgca.modules.hospedagem.entity;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entidade que representa uma hospedagem de paciente na instituição
 */
@Entity
@Table(name = "hospedagens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE hospedagens SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Hospedagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    private String uuid;

    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarto_id")
    private Quarto quarto;

    // Datas da Hospedagem
    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "data_saida_prevista")
    private LocalDate dataSaidaPrevista;

    @Column(name = "data_saida")
    private LocalDate dataSaida;

    @Column(name = "hora_saida")
    private LocalTime horaSaida;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private StatusHospedagem status = StatusHospedagem.ATIVA;

    @Column(name = "motivo_saida")
    private String motivoSaida;

    // Observações
    @Column(name = "observacoes_entrada", columnDefinition = "TEXT")
    private String observacoesEntrada;

    @Column(name = "observacoes_saida", columnDefinition = "TEXT")
    private String observacoesSaida;

    @Column(name = "observacoes_gerais", columnDefinition = "TEXT")
    private String observacoesGerais;

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Métodos auxiliares
    @PrePersist
    protected void onCreate() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica se a hospedagem está ativa
     */
    public boolean isAtiva() {
        return status == StatusHospedagem.ATIVA && dataSaida == null;
    }

    /**
     * Encerra a hospedagem
     */
    public void encerrar(LocalDate dataSaida, LocalTime horaSaida, String motivo) {
        this.dataSaida = dataSaida;
        this.horaSaida = horaSaida;
        this.motivoSaida = motivo;
        this.status = StatusHospedagem.ENCERRADA;
    }
}
