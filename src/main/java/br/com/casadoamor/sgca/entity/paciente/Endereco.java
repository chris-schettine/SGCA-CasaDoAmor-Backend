package br.com.casadoamor.sgca.entity.paciente;

import br.com.casadoamor.sgca.entity.common.BaseEntity;
import br.com.casadoamor.sgca.enums.EstadoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "enderecos")
public class Endereco extends BaseEntity {
    @Column(name = "logradouro", length = 150, nullable = false)
    private String logradouro;

    @Column(length = 100, nullable = false)
    private String bairro;

    @Column
    private Integer numero;

    @Column(length = 100, nullable = false)
    private String cidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEnum estado; 

    @Column(length = 10)
    private String cep;

    @Column(length = 150)
    private String complemento;
}
