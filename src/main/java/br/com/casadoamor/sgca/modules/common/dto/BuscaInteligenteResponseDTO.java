package br.com.casadoamor.sgca.modules.common.dto;

import java.util.List;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuscaInteligenteResponseDTO {
    private List<PacienteDTO> pacientes;
    private List<AcompanhanteDTO> acompanhantes;
}
