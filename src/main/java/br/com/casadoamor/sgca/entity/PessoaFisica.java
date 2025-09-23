package br.com.casadoamor.sgca.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pessoa_fisica")
public class PessoaFisica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    private Date dataNascimento;
    @Column(unique = true, nullable = false, length = 11)
    private String cpf;
    @Column(unique = true, length = 10)
    private String rg;
    private String naturalidade;
    private String profissao;
    private String telefone;
    private String email;
    @Embedded
    private Endereco endereco;
}
