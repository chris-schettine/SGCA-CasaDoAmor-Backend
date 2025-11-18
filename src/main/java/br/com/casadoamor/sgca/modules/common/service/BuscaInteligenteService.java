package br.com.casadoamor.sgca.modules.common.service;

import br.com.casadoamor.sgca.modules.common.dto.BuscaInteligenteResponseDTO;

public interface BuscaInteligenteService {
  BuscaInteligenteResponseDTO buscar(String termo);
}
