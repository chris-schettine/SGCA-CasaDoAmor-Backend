package br.com.casadoamor.sgca.modules.paciente.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import br.com.casadoamor.sgca.modules.common.enums.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DadoClinicoDTO {
        String id;
        String diagnostico;
        TipoTratamento tratamento;
        String tratamentoOutroDescricao;
        CondicaoChegada condicaoChegada;
        Boolean usaSonda;
        TipoSondaNasal tipoSondaNasal;
        TipoSondaCirurgica tipoSondaCirurgica;
        TipoSondaVesical tipoSondaVesical;
        String sondaOutraDescricao;
        Boolean usaCurativo;
        Boolean usaOxigenoterapia;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
        TipoSanguineoEnum tipoSanguineo;
}
