package br.com.casadoamor.sgca.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de registro profissional
 * Este DTO é usado APENAS na criação - registros profissionais são imutáveis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados do registro profissional (CRM, COREN, CRO, CREFITO, CRN)")
public class RegistroProfissionalDTO {

    @NotNull(message = "Tipo de profissional é obrigatório")
    @Schema(description = "Tipo de profissional", example = "MEDICO", allowableValues = {"DENTISTA", "ENFERMEIRO", "FISIOTERAPEUTA", "MEDICO", "NUTRICIONISTA"})
    private String tipoProfissional;

    @NotBlank(message = "Número do registro profissional é obrigatório")
    @Size(min = 3, max = 50, message = "Número do registro deve ter entre 3 e 50 caracteres")
    @Schema(description = "Número do registro profissional", example = "123456-SP")
    private String numeroRegistro;

    @Size(max = 50, message = "RQE deve ter no máximo 50 caracteres")
    @Schema(description = "RQE - Registro de Qualificação de Especialista (opcional, usado por médicos e dentistas)", example = "12345")
    private String rqe;
}
