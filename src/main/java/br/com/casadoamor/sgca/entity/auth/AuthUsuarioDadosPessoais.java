package br.com.casadoamor.sgca.entity.auth;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entidade para dados pessoais de usuários do sistema (funcionários)
 * Separado de dados pessoais de pacientes para manter contextos distintos
 */
@Entity
@Table(name = "auth_usuarios_dados_pessoais")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUsuarioDadosPessoais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 20)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 30)
    @Builder.Default
    private Genero genero = Genero.PREFIRO_NAO_INFORMAR;

    @Column(name = "rg", length = 20)
    private String rg;

    @Column(name = "orgao_emissor", length = 10)
    private String orgaoEmissor;

    @Column(name = "naturalidade", length = 100)
    private String naturalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", length = 20)
    private EstadoCivil estadoCivil;

    @Column(name = "nome_mae", length = 255)
    private String nomeMae;

    @Column(name = "nome_pai", length = 255)
    private String nomePai;

    @Column(name = "profissao", length = 100)
    private String profissao;

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
     * Verifica se os dados pessoais foram deletados (soft delete)
     */
    public boolean isDeletado() {
        return deletadoEm != null;
    }

    /**
     * Enum para Sexo Biológico
     */
    public enum Sexo {
        MASCULINO,
        FEMININO
    }

    /**
     * Enum para Identidade de Gênero
     */
    public enum Genero {
        BINARIO,
        NAO_BINARIO,
        TRANSGENERO,
        CISGENERO,
        PREFIRO_NAO_INFORMAR
    }

    /**
     * Enum para Estado Civil
     */
    public enum EstadoCivil {
        SOLTEIRO,
        CASADO,
        DIVORCIADO,
        VIUVO,
        UNIAO_ESTAVEL
    }
}
