package br.com.casadoamor.sgca.modules.paciente.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

import br.com.casadoamor.sgca.modules.common.enums.*;

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
