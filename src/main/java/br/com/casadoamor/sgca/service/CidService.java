package br.com.casadoamor.sgca.service;

import br.com.casadoamor.sgca.dto.CidDto;

import java.util.List;

public interface CidService {
    List<CidDto> getCidById(String id);
    List<CidDto> getCidByDescricao(String descricao);
    List<CidDto> getAllCid();
}
