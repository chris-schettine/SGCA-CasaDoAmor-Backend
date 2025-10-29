package br.com.casadoamor.sgca.dto.paciente;

import br.com.casadoamor.sgca.enums.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DadoClinicoDTO(
        String id,
        String diagnostico,
        TipoTratamento tratamento,
        String tratamentoOutroDescricao,
        CondicaoChegada condicaoChegada,
        Boolean usaSonda,
        TipoSondaNasal tipoSondaNasal,
        TipoSondaCirurgica tipoSondaCirurgica,
        TipoSondaVesical tipoSondaVesical,
        String sondaOutraDescricao,
        Boolean usaCurativo,
        Boolean usaOxigenoterapia,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
