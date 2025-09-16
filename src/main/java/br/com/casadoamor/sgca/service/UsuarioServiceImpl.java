package br.com.casadoamor.sgca.service;

import br.com.casadoamor.sgca.dto.UsuarioDto;
import br.com.casadoamor.sgca.dto.UsuarioRequestJson;
import br.com.casadoamor.sgca.entity.Usuario;
import br.com.casadoamor.sgca.exception.ResourceNotFoundException;
import br.com.casadoamor.sgca.mapper.PessoaFisicaMapper;
import br.com.casadoamor.sgca.mapper.ProfissionalSaudeMapper;
import br.com.casadoamor.sgca.mapper.UsuarioMapper;
import br.com.casadoamor.sgca.repository.PessoaFisicaRepository;
import br.com.casadoamor.sgca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PessoaFisicaRepository pessoaFisicaRepository;

    @Override
    public UsuarioDto createUsuario(UsuarioRequestJson requestJson) {
        requestJson.setCpf(requestJson.getCpf().replace(".", "").replace("-", ""));
        Usuario usuario = UsuarioMapper.toUsuario(requestJson);

        usuario.setPessoaFisica(this.pessoaFisicaRepository.findById(requestJson.getPessoaFisica()).orElseThrow(() ->
                new ResourceNotFoundException("Pessoa Fisica com id " + requestJson.getPessoaFisica() + "não encontrada!")));

        return UsuarioMapper.toUsuarioDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDto getUsuarioById(Long id) {
        return UsuarioMapper.toUsuarioDto(
                this.usuarioRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Usuário com id " + id + "não encontrado!"))
        );
    }

    @Override
    public UsuarioDto getUsuarioByCpf(String cpf) {
        String cpfFormatted = cpf.replace(".", "").replace("-", "");
        return UsuarioMapper.toUsuarioDto(this.usuarioRepository.findByCpf(cpfFormatted)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com CPF " + cpf + " não encontrada!")));
    }

    @Override
    public List<UsuarioDto> getAllUsuario() {
        return this.usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toUsuarioDto).collect(Collectors.toList());
    }

    @Override
    public UsuarioDto updateUsuario(Long id, UsuarioDto usuarioDto) {
        Usuario user = this.usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário com id " + id + "não encontrado!"));
        user.setCpf(usuarioDto.getCpf());
        user.setSenha(usuarioDto.getSenha());
        user.setAtivo(usuarioDto.isAtivo());
        user.setPessoaFisica(PessoaFisicaMapper.toPessoaFisica(usuarioDto.getPessoaFisica()));
        return UsuarioMapper.toUsuarioDto(this.usuarioRepository.save(user));
    }

    @Override
    public void deleteUsuario(Long id) {
        this.usuarioRepository.deleteById(id);
    }
}