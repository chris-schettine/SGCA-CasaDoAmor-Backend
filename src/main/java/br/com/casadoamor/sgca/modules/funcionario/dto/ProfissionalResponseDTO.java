package br.com.casadoamor.sgca.modules.funcionario.dto;

import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
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
    private String cpf;
    private String telefone;
    private String email;
    private TipoVinculoDTO tipoVinculo; // Objeto completo do tipo de vínculo
    private CategoriaProfissionalDTO categoria;
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
    
    // Relacionamentos
    private EnderecoDTO endereco; // Objeto completo do endereço
    
    private Boolean ativo;
    private String observacoes;
    
    // Foto de perfil
    private String fotoUrl;
    private String fotoPath;
    private LocalDateTime fotoAtualizadaEm;
    
    // Auditoria
    private LocalDateTime createdAt;
    private UsuarioResumoDTO createdBy; // Objeto completo do usuário que criou
    private LocalDateTime updatedAt;
    private UsuarioResumoDTO updatedBy; // Objeto completo do usuário que atualizou
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioResumoDTO {
        private String uuid;
        private String nome;
        private String email;
        private String tipo;
    }
}
