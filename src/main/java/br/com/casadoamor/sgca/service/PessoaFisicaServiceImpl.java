package br.com.casadoamor.sgca.service;

import br.com.casadoamor.sgca.dto.PessoaFisicaDto;
import br.com.casadoamor.sgca.entity.PessoaFisica;
import br.com.casadoamor.sgca.exception.ResourceNotFoundException;
import br.com.casadoamor.sgca.mapper.PessoaFisicaMapper;
import br.com.casadoamor.sgca.repository.PessoaFisicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PessoaFisicaServiceImpl implements PessoaFisicaService {

    @Autowired
    private PessoaFisicaRepository pessoaFisicaRepository;

    @Override
    public PessoaFisicaDto createPessoaFisica(PessoaFisicaDto pessoaFisicaDto) {
        pessoaFisicaDto.setCpf(pessoaFisicaDto.getCpf().replace(".", "").replace("-", ""));
        pessoaFisicaDto.getEndereco().setCep(pessoaFisicaDto.getEndereco().getCep().replace("-", ""));

        PessoaFisica pessoa = PessoaFisicaMapper.toPessoaFisica(pessoaFisicaDto);
        return PessoaFisicaMapper.toPessoaFisicaDto(this.pessoaFisicaRepository.save(pessoa));
    }

    @Override
    public PessoaFisicaDto getPessoaFisicaById(Long id) {
        return PessoaFisicaMapper.toPessoaFisicaDto(
                this.pessoaFisicaRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Pessoa Fisica com id " + id + " não encontrada!"))
        );
    }

    @Override
    public List<PessoaFisicaDto> getAllPessoaFisica() {
        return this.pessoaFisicaRepository.findAll()
                .stream().map(PessoaFisicaMapper::toPessoaFisicaDto).collect(Collectors.toList());
    }

    @Override
    public PessoaFisicaDto getPessoaFisicaByCpf(String cpf) {
        String cpfFormatted = cpf.replace(".", "").replace("-", "");
        return PessoaFisicaMapper.toPessoaFisicaDto(this.pessoaFisicaRepository.findByCpf(cpfFormatted)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa Fisica com CPF " + cpf + " não encontrada!")));
    }

    public List<PessoaFisicaDto> findByNomeContaining(String nome) {
        return this.pessoaFisicaRepository.findByNomeContaining(nome).stream()
                .map(PessoaFisicaMapper::toPessoaFisicaDto).collect(Collectors.toList());
    }

    @Override
    public PessoaFisicaDto updatePessoaFisica(Long id, PessoaFisicaDto pessoaFisicaDto) {
        PessoaFisica pessoa = this.pessoaFisicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa Fisica com id " + id + " não encontrada!"));
        pessoa.setNome(pessoaFisicaDto.getNome());
        pessoa.setDataNascimento(pessoaFisicaDto.getDataNascimento());
        pessoa.setCpf(pessoaFisicaDto.getCpf());
        pessoa.setRg(pessoaFisicaDto.getRg());
        pessoa.setTelefone(pessoaFisicaDto.getTelefone());
        pessoa.setEmail(pessoaFisicaDto.getEmail());
        pessoa.getEndereco().setEndereco(pessoaFisicaDto.getEndereco().getEndereco());
        pessoa.getEndereco().setBairro(pessoaFisicaDto.getEndereco().getBairro());
        pessoa.getEndereco().setNumero(pessoaFisicaDto.getEndereco().getNumero());
        pessoa.getEndereco().setCidade(pessoaFisicaDto.getEndereco().getCidade());
        pessoa.getEndereco().setEstado(pessoaFisicaDto.getEndereco().getEstado());
        pessoa.getEndereco().setCep(pessoaFisicaDto.getEndereco().getCep());
        pessoa.getEndereco().setComplemento(pessoaFisicaDto.getEndereco().getComplemento());

        return PessoaFisicaMapper.toPessoaFisicaDto(this.pessoaFisicaRepository.save(pessoa));
    }

    @Override
    public void deletePessoaFisica(Long id) {
        this.pessoaFisicaRepository.deleteById(id);
    }


}
