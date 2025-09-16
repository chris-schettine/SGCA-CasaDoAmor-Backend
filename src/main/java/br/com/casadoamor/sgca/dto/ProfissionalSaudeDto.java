package br.com.casadoamor.sgca.dto;

import br.com.casadoamor.sgca.entity.TipoDocumentoProfissionalSaudeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalSaudeDto {

    private Long id;
    private TipoDocumentoProfissionalSaudeEnum tipo;
    private String documento;
    private String ufDocumento;
    private String especialidade;
    private PessoaFisicaDto pessoaFisica;
}

