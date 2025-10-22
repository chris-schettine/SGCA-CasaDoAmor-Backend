package br.com.casadoamor.sgca.entity.admin;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entidade Permissão - representa permissões granulares do sistema
 * Exemplo: PACIENTE_READ, PACIENTE_WRITE, USER_DELETE
 */
@Entity
@Table(name = "permissoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "perfis")
@EqualsAndHashCode(exclude = "perfis")
@JsonIgnoreProperties({"perfis"})
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", unique = true, nullable = false, length = 100)
    private String nome; // Ex: PACIENTE_READ, USER_WRITE

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @ManyToMany(mappedBy = "permissoes")
    @Builder.Default
    private Set<Perfil> perfis = new HashSet<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "criado_por")
    private Long criadoPor;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "atualizado_por")
    private Long atualizadoPor;

    @Column(name = "deletado_em")
    private LocalDateTime deletadoEm;

    /**
     * Verifica se a permissão foi deletada (soft delete)
     */
    public boolean isDeletado() {
        return deletadoEm != null;
    }
}
