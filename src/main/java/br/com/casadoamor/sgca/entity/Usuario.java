package br.com.casadoamor.sgca.entity;


import br.com.casadoamor.sgca.entity.common.BaseEntity;
import br.com.casadoamor.sgca.entity.paciente.DadoPessoal;
import br.com.casadoamor.sgca.entity.paciente.Endereco;
import br.com.casadoamor.sgca.enums.TipoUsuarioEnum;
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
@Table(name = "usuarios")
public class Usuario extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuarioEnum tipoUsuario;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dado_pessoal_id")
    private DadoPessoal dadoPessoal;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;
}
