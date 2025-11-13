package br.com.casadoamor.sgca.modules.hospedagem.entity;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um quarto/leito na instituição
 */
@Entity
@Table(name = "quartos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE quartos SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    private String uuid;

    // Dados do Quarto
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @Column(name = "codigo", length = 50, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    @Builder.Default
    private TipoQuarto tipo = TipoQuarto.COMPARTILHADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "ala", nullable = false)
    private AlaQuarto ala;

    @Column(name = "andar", length = 20)
    private String andar;

    // Capacidade
    @Column(name = "capacidade_total", nullable = false)
    @Builder.Default
    private Integer capacidadeTotal = 1;

    @Column(name = "capacidade_ocupada", nullable = false)
    @Builder.Default
    private Integer capacidadeOcupada = 0;

    // Status e Observações
    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "em_manutencao", nullable = false)
    @Builder.Default
    private Boolean emManutencao = false;

    /**
     * Permite pacientes de sexo oposto ao da ala (para acompanhantes e pacientes debilitados)
     * Quartos de 7 camas: permite acompanhante de sexo oposto
     * Quartos de 4 camas: permite sexos opostos (pacientes debilitados)
     * Isolamento: sem restrição de gênero
     */
    @Column(name = "permite_sexo_oposto", nullable = false)
    @Builder.Default
    private Boolean permiteSexoOposto = false;

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
     * Verifica se o quarto tem vagas disponíveis
     */
    public boolean temVagasDisponiveis() {
        return ativo && !emManutencao && capacidadeOcupada < capacidadeTotal;
    }

    /**
     * Retorna a quantidade de vagas disponíveis
     */
    public int getVagasDisponiveis() {
        return capacidadeTotal - capacidadeOcupada;
    }

    /**
     * Incrementa a ocupação do quarto
     */
    public void incrementarOcupacao() {
        if (capacidadeOcupada < capacidadeTotal) {
            this.capacidadeOcupada++;
        }
    }

    /**
     * Decrementa a ocupação do quarto
     */
    public void decrementarOcupacao() {
        if (capacidadeOcupada > 0) {
            this.capacidadeOcupada--;
        }
    }
}
