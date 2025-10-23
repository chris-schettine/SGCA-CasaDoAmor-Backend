package br.com.casadoamor.sgca.entity.paciente;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

import br.com.casadoamor.sgca.entity.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "dados_pessoais")
public class DadoPessoal extends BaseEntity {
    @Column(nullable = false)
    private String nome;

    @Column(name = "nome_mae")
    private String nomeMae;

    private Date dataNascimento;

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    @Column(unique = true, length = 10)
    private String rg;

    private String naturalidade;

    private String profissao;

    private String telefone;
}
