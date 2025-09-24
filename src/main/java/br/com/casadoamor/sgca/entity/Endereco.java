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
@Embeddable
public class Endereco {

    private String endereco;
    private String bairro;
    private Integer numero;
    private String cidade;
    private String estado;
    private String cep;
    private String complemento;

}
