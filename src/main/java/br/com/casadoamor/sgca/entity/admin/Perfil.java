package br.com.casadoamor.sgca.entity.admin;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entidade Perfil (Role) - representa os perfis de acesso do sistema
 * Exemplo: ROLE_ADMIN, ROLE_MEDICO, ROLE_RECEPCIONISTA
 */
@Entity
@Table(name = "perfis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"permissoes", "usuarios"})
@EqualsAndHashCode(exclude = {"permissoes", "usuarios"})
@JsonIgnoreProperties({"usuarios"})
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", unique = true, nullable = false, length = 50)
    private String nome; // Ex: ROLE_ADMIN, ROLE_MEDICO

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "perfis_permissoes",
        joinColumns = @JoinColumn(name = "perfil_id"),
        inverseJoinColumns = @JoinColumn(name = "permissao_id")
    )
    @Builder.Default
    private Set<Permissao> permissoes = new HashSet<>();

    @ManyToMany(mappedBy = "perfis")
    @Builder.Default
    private Set<AuthUsuario> usuarios = new HashSet<>();

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
     * Verifica se o perfil foi deletado (soft delete)
     */
    public boolean isDeletado() {
        return deletadoEm != null;
    }

    /**
     * Adiciona uma permissão ao perfil
     */
    public void adicionarPermissao(Permissao permissao) {
        permissoes.add(permissao);
        permissao.getPerfis().add(this);
    }

    /**
     * Remove uma permissão do perfil
     */
    public void removerPermissao(Permissao permissao) {
        permissoes.remove(permissao);
        permissao.getPerfis().remove(this);
    }
}
