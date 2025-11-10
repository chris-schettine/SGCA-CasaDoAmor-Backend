package br.com.casadoamor.sgca.modules.hospedagem.dto;

import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cadastro/atualização de quartos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuartoRequestDTO {

    @NotBlank(message = "Nome do quarto é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Size(max = 50, message = "Código deve ter no máximo 50 caracteres")
    private String codigo;

    @NotNull(message = "Tipo do quarto é obrigatório")
    private TipoQuarto tipo;

    @NotNull(message = "Ala do quarto é obrigatória")
    private AlaQuarto ala;

    @Size(max = 20, message = "Andar deve ter no máximo 20 caracteres")
    private String andar;

    @NotNull(message = "Capacidade total é obrigatória")
    @Min(value = 1, message = "Capacidade deve ser no mínimo 1")
    @Max(value = 20, message = "Capacidade não pode exceder 20 leitos")
    private Integer capacidadeTotal;

    private Boolean ativo;

    private Boolean emManutencao;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;
}
