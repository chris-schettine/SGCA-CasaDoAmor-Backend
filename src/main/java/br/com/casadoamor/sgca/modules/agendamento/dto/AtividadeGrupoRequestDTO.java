package br.com.casadoamor.sgca.modules.agendamento.dto;

import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Modalidade;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para criação/atualização de atividade em grupo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeGrupoRequestDTO {

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 3, max = 255)
    private String titulo;

    @Size(max = 5000)
    private String descricao;

    @NotBlank(message = "Tipo é obrigatório")
    @Size(max = 100)
    private String tipo;

    @NotNull(message = "Data de início é obrigatória")
    @Future(message = "Data de início deve ser futura")
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    private LocalDateTime dataFim;

    @Size(max = 255)
    private String local;

    private Modalidade modalidade;

    @NotNull(message = "Capacidade é obrigatória")
    @Min(value = 1, message = "Capacidade mínima é 1")
    @Max(value = 200, message = "Capacidade máxima é 200")
    private Integer capacidade;

    @Size(max = 500)
    private String publicoAlvo;

    @Min(value = 0, message = "Idade mínima deve ser positiva")
    private Integer idadeMinima;

    @Max(value = 120, message = "Idade máxima não pode exceder 120")
    private Integer idadeMaxima;

    @NotNull(message = "Responsável principal é obrigatório")
    private Long responsavelPrincipalId;

    private Long responsavelApoioId;

    private String materiais; // JSON string

    private String observacoes;

    private Boolean requerInscricao;

    private Boolean inscricoesAbertas;
}
