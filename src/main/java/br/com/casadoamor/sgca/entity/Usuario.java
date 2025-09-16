package br.com.casadoamor.sgca.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String cpf;
    @Column(nullable = false)
    private String senha;
    private boolean ativo = true;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pessoa_fisica_id")
    private PessoaFisica pessoaFisica;
}
