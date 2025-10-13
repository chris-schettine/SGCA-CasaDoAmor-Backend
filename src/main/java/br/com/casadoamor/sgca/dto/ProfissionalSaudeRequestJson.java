package br.com.casadoamor.sgca.dto;

import br.com.casadoamor.sgca.enums.TipoDocumentoProfissionalSaudeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalSaudeRequestJson {
    private Long id;
    private TipoDocumentoProfissionalSaudeEnum tipo;
    private String documento;
    private String ufDocumento;
    private String especialidade;
    private Long pessoaFisica;
}
