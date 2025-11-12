package br.com.casadoamor.sgca.modules.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import br.com.casadoamor.sgca.modules.common.enums.EstadoCivilEnum;
import br.com.casadoamor.sgca.modules.common.enums.SexoEnum;

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

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    @Column(unique = true, length = 10, nullable = false)
    private String rg;

    private String naturalidade;

    private String profissao;

    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", length = 20)
    private EstadoCivilEnum estadoCivil;

    @Enumerated(EnumType.STRING)
    private SexoEnum sexo;
}
