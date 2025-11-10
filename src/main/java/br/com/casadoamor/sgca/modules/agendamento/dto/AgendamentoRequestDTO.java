package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Modalidade;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para criação de agendamento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoRequestDTO {

    @NotNull(message = "Tipo de serviço é obrigatório")
    private Long tipoServicoId;

    @NotBlank(message = "UUID do paciente é obrigatório")
    private String pacienteUuid;

    private Long profissionalId;

    private String acompanhanteUuid;

    @NotNull(message = "Data de início é obrigatória")
    @Future(message = "Data de início deve ser futura")
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    private LocalDateTime dataFim;

    @Size(max = 255)
    private String local;

    private Modalidade modalidade;

    @Size(max = 5000)
    private String observacoes;
}
