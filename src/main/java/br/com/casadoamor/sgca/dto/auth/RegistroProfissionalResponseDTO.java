package br.com.casadoamor.sgca.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de resposta para registro profissional
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados do registro profissional do usuário")
public class RegistroProfissionalResponseDTO {

    @Schema(description = "ID do registro profissional", example = "1")
    private Long id;

    @Schema(description = "Tipo de profissional", example = "MEDICO")
    private String tipoProfissional;

    @Schema(description = "Descrição do tipo de profissional", example = "CRM - Conselho Regional de Medicina")
    private String descricaoTipo;

    @Schema(description = "Número do registro profissional", example = "123456-SP")
    private String numeroRegistro;

    @Schema(description = "RQE - Registro de Qualificação de Especialista", example = "12345")
    private String rqe;

    @Schema(description = "Data de criação do registro", example = "2025-10-23T18:30:00")
    private LocalDateTime criadoEm;
}
