package br.com.casadoamor.sgca.service;

import br.com.casadoamor.sgca.dto.ProfissionalSaudeDto;
import br.com.casadoamor.sgca.dto.ProfissionalSaudeRequestJson;

import java.util.List;

public interface ProfissionalSaudeService {

    ProfissionalSaudeDto createProfissionalSaude(ProfissionalSaudeRequestJson profissional);
    ProfissionalSaudeDto getProfissionalSaudeById(Long id);
    List<ProfissionalSaudeDto> getAllProfissionalSaude();
    ProfissionalSaudeDto updateProfissionalSaude(Long id, ProfissionalSaudeRequestJson profissional);
    void deleteProfissionalSaude(Long id);
}
