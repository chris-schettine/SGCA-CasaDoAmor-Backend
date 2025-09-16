package br.com.casadoamor.sgca.service;

import br.com.casadoamor.sgca.dto.PessoaFisicaDto;

import java.util.List;

public interface PessoaFisicaService {

    PessoaFisicaDto createPessoaFisica(PessoaFisicaDto pessoaFisicaDto);
    PessoaFisicaDto getPessoaFisicaById(Long id);
    List<PessoaFisicaDto> getAllPessoaFisica();
    PessoaFisicaDto updatePessoaFisica(Long id, PessoaFisicaDto pessoaFisicaDto);
    void deletePessoaFisica(Long id);
    PessoaFisicaDto getPessoaFisicaByCpf(String cpf);
    List<PessoaFisicaDto> findByNomeContaining(String nome);
}
