package br.com.casadoamor.sgca.modules.funcionario.entity;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa um profissional (funcionário ou voluntário)
 * Unifica funcionários contratados e voluntários da instituição
 */
@Entity
@Table(name = "profissionais")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE profissionais SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    private String uuid;

    // Dados Pessoais
    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cpf", length = 11)
    private String cpf;

    @Column(name = "cpf_criptografado", columnDefinition = "VARBINARY(512)")
    private byte[] cpfCriptografado;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "email")
    private String email;

    // Tipo de vínculo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_vinculo_id", referencedColumnName = "id")
    private TipoVinculoEntity tipoVinculo;

    @Column(name = "tipo_vinculo_backup")
    private String tipoVinculoBackup;

    // Categoria
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaProfissional categoria;

    @Column(name = "area_atuacao", length = 100)
    private String areaAtuacao;

    @Column(name = "especialidade")
    private String especialidade;

    @Column(name = "numero_registro", length = 50)
    private String numeroRegistro;

    @Column(name = "uf_registro", length = 2)
    private String ufRegistro;

    // Dados Trabalhistas (apenas para FUNCIONARIO)
    @Column(name = "data_admissao")
    private LocalDate dataAdmissao;

    @Column(name = "data_desligamento")
    private LocalDate dataDesligamento;

    @Column(name = "tipo_contrato", length = 50)
    private String tipoContrato;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "dados_bancarios_criptografados", columnDefinition = "VARBINARY(1024)")
    private byte[] dadosBancariosCriptografados;

    // Disponibilidade (principalmente para VOLUNTARIO)
    @Column(name = "disponibilidade", columnDefinition = "JSON")
    private String disponibilidade;

    // Relacionamento com Endereço
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id", referencedColumnName = "id")
    private Endereco endereco;

    // Foto de Perfil
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "foto_path", length = 255)
    private String fotoPath;

    @Column(name = "foto_atualizada_em")
    private LocalDateTime fotoAtualizadaEm;

    // Status
    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    // Auditoria
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private AuthUsuario createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private AuthUsuario updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
}
