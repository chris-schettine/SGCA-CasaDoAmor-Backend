package br.com.casadoamor.sgca.modules.funcionario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.casadoamor.sgca.modules.funcionario.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
