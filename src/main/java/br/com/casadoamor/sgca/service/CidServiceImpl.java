package br.com.casadoamor.sgca.service;

import br.com.casadoamor.sgca.dto.CidDto;
import br.com.casadoamor.sgca.mapper.CidMapper;
import br.com.casadoamor.sgca.repository.CidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CidServiceImpl implements CidService {

    @Autowired
    private CidRepository cidRepository;


    @Override
    public List<CidDto> getCidById(String id) {
        id = id.replace(".", "");
        return this.cidRepository.findCidBySubcatContaining(id).stream()
                .map(CidMapper::toCidDto).collect(Collectors.toList());
    }

    @Override
    public List<CidDto> getCidByDescricao(String descricao) {
        return this.cidRepository.findCidByDescricaoContaining(descricao).stream()
                .map(CidMapper::toCidDto).collect(Collectors.toList());
    }

    @Override
    public List<CidDto> getAllCid() {
        return this.cidRepository.findAll().stream()
                .map(CidMapper::toCidDto).collect(Collectors.toList());
    }
}