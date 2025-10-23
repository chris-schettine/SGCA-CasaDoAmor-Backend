package br.com.casadoamor.sgca.dto.paciente;

import java.util.Date;

import lombok.Builder;

@Builder
public record PacienteDTO(
    String id,
    String nome,
    String cpf,
    String rg,
    Date dataNascimento,
    String naturalidade,
    String profissao,
    String telefone,
    String logradouro,
    Integer numero,
    String complemento,
    String bairro,
    String cidade,
    String estado,
    String cep
) {
}
