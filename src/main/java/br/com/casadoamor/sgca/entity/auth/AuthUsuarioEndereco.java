package br.com.casadoamor.sgca.entity.auth;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade para endereços de usuários do sistema (funcionários)
 * Separado de endereços de pacientes para manter contextos distintos
 */
@Entity
@Table(name = "auth_usuarios_enderecos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUsuarioEndereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logradouro", nullable = false, length = 150)
    private String logradouro;

    @Column(name = "numero", length = 10)
    private String numero;

    @Column(name = "complemento", length = 150)
    private String complemento;

    @Column(name = "bairro", nullable = false, length = 100)
    private String bairro;

    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "cep", length = 9)
    private String cep;

    @Column(name = "criado_em", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "criado_por")
    private AuthUsuario criadoPor;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @ManyToOne
    @JoinColumn(name = "atualizado_por")
    private AuthUsuario atualizadoPor;

    @Column(name = "deletado_em")
    private LocalDateTime deletadoEm;

    /**
     * Verifica se o endereço foi deletado (soft delete)
     */
    public boolean isDeletado() {
        return deletadoEm != null;
    }
}
