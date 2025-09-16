package br.com.casadoamor.sgca.mapper;

import br.com.casadoamor.sgca.dto.UsuarioDto;
import br.com.casadoamor.sgca.dto.UsuarioRequestJson;
import br.com.casadoamor.sgca.entity.Usuario;

public class UsuarioMapper {

    public static UsuarioDto toUsuarioDto(Usuario usuario) {
        return new UsuarioDto(
                usuario.getId(),
                usuario.getCpf(),
                usuario.getSenha(),
                usuario.isAtivo(),
                PessoaFisicaMapper.toPessoaFisicaDto(usuario.getPessoaFisica())
        );
    }

    public static Usuario toUsuario(UsuarioDto usuario) {
        return new Usuario(
                usuario.getId(),
                usuario.getCpf(),
                usuario.getSenha(),
                usuario.isAtivo(),
                PessoaFisicaMapper.toPessoaFisica(usuario.getPessoaFisica())
        );
    }

    public static Usuario toUsuario(UsuarioRequestJson usuario) {
        return new Usuario(
                usuario.getId(),
                usuario.getCpf(),
                usuario.getSenha(),
                usuario.isAtivo(),
                null
        );
    }
}
