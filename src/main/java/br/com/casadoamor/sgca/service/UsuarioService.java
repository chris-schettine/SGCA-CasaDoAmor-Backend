package br.com.casadoamor.sgca.service;

import br.com.casadoamor.sgca.dto.UsuarioDto;
import br.com.casadoamor.sgca.dto.UsuarioRequestJson;

import java.util.List;

public interface UsuarioService {
    UsuarioDto createUsuario(UsuarioRequestJson usuarioRequestJson);
    UsuarioDto getUsuarioById(Long id);
    UsuarioDto getUsuarioByCpf(String cpf);
    List<UsuarioDto> getAllUsuario();
    UsuarioDto updateUsuario(Long id, UsuarioDto usuarioDto);
    void deleteUsuario(Long id);
}
