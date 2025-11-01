package br.com.casadoamor.sgca.modules.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioEndereco;

/**
 * Repositório para endereços de usuários do sistema
 */
@Repository
public interface AuthUsuarioEnderecoRepository extends JpaRepository<AuthUsuarioEndereco, Long> {
}
