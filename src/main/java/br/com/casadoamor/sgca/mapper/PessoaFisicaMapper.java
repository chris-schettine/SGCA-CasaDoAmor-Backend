package br.com.casadoamor.sgca.mapper;

import br.com.casadoamor.sgca.dto.EnderecoDto;
import br.com.casadoamor.sgca.dto.PessoaFisicaDto;
import br.com.casadoamor.sgca.entity.Endereco;
import br.com.casadoamor.sgca.entity.PessoaFisica;

public class PessoaFisicaMapper {

    public static PessoaFisicaDto toPessoaFisicaDto(PessoaFisica pessoaFisica) {
        return new PessoaFisicaDto(
                pessoaFisica.getId(),
                pessoaFisica.getNome(),
                pessoaFisica.getDataNascimento(),
                pessoaFisica.getCpf(),
                pessoaFisica.getRg(),
                pessoaFisica.getNaturalidade(),
                pessoaFisica.getProfissao(),
                pessoaFisica.getTelefone(),
                pessoaFisica.getEmail(),
                toEnderecoDto(pessoaFisica.getEndereco())
        );
    }

    public static PessoaFisica toPessoaFisica(PessoaFisicaDto pessoaFisicaDto) {
        return new PessoaFisica(
                pessoaFisicaDto.getId(),
                pessoaFisicaDto.getNome(),
                pessoaFisicaDto.getDataNascimento(),
                pessoaFisicaDto.getCpf(),
                pessoaFisicaDto.getRg(),
                pessoaFisicaDto.getNaturalidade(),
                pessoaFisicaDto.getProfissao(),
                pessoaFisicaDto.getTelefone(),
                pessoaFisicaDto.getEmail(),
                toEndereco(pessoaFisicaDto.getEndereco())
        );
    }

    public static EnderecoDto toEnderecoDto(Endereco endereco) {
        return new EnderecoDto(
                endereco.getEndereco(),
                endereco.getBairro(),
                endereco.getNumero(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep(),
                endereco.getComplemento()
        );
    }

    public static Endereco toEndereco(EnderecoDto endereco) {
        return new Endereco(
                endereco.getEndereco(),
                endereco.getBairro(),
                endereco.getNumero(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep(),
                endereco.getComplemento()
        );
    }
}
