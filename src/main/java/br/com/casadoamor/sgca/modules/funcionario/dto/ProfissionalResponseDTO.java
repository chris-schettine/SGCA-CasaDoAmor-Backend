package br.com.casadoamor.sgca.modules.funcionario.dto;

import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para resposta completa de profissional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalResponseDTO {

    private String uuid;
    private String nome;
    private String telefone;
    private String email;
    private CategoriaProfissional categoria;
    private String areaAtuacao;
    private String especialidade;
    private String numeroRegistro;
    private String ufRegistro;
    private LocalDate dataAdmissao;
    private LocalDate dataDesligamento;
    private String tipoContrato;
    private Integer cargaHoraria;
    private String cargo;
    private String departamento;
    private String disponibilidade; // JSON string
    private String enderecoId;
    private Boolean ativo;
    private String observacoes;
    private LocalDateTime createdAt;
    private String createdByNome;
    private LocalDateTime updatedAt;
    private String updatedByNome;
}
