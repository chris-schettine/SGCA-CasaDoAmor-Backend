package br.com.casadoamor.sgca.mapper;

import br.com.casadoamor.sgca.dto.ProfissionalSaudeDto;
import br.com.casadoamor.sgca.dto.ProfissionalSaudeRequestJson;
import br.com.casadoamor.sgca.entity.ProfissionalSaude;

public class ProfissionalSaudeMapper {

    public static ProfissionalSaudeDto toProfissionalSaudeDto(ProfissionalSaude profissional) {
        return new ProfissionalSaudeDto(
                profissional.getId(),
                profissional.getTipo(),
                profissional.getDocumento(),
                profissional.getUfDocumento(),
                profissional.getEspecialidade(),
                PessoaFisicaMapper.toPessoaFisicaDto(profissional.getPessoaFisica())
        );
    }

    public static ProfissionalSaude toProfissionalSaude(ProfissionalSaudeDto profissional) {
        return new ProfissionalSaude(
                profissional.getId(),
                profissional.getTipo(),
                profissional.getDocumento(),
                profissional.getUfDocumento(),
                profissional.getEspecialidade(),
                PessoaFisicaMapper.toPessoaFisica(profissional.getPessoaFisica())
        );
    }

    public static ProfissionalSaude toProfissionalSaude(ProfissionalSaudeRequestJson profissional) {
        return new ProfissionalSaude(
                profissional.getId(),
                profissional.getTipo(),
                profissional.getDocumento(),
                profissional.getUfDocumento(),
                profissional.getEspecialidade(),
                null
        );
    }
}
