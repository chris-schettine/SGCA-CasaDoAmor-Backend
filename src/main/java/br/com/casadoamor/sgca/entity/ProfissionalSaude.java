package br.com.casadoamor.sgca.entity;


import br.com.casadoamor.sgca.enums.TipoDocumentoProfissionalSaudeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profissional_saude")
public class ProfissionalSaude extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumentoProfissionalSaudeEnum tipo;

    @Column(nullable = false)
    private String documento;

    @Column(length = 2, nullable = false)
    private String ufDocumento;
    
    private String especialidade;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dado_pessoal_id",  nullable = false)
    private DadoPessoal dadoPessoal;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;
}
