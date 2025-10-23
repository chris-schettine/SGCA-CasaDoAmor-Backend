package br.com.casadoamor.sgca.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.auth.AuthUsuarioEndereco;

/**
 * Repositório para endereços de usuários do sistema
 */
@Repository
public interface AuthUsuarioEnderecoRepository extends JpaRepository<AuthUsuarioEndereco, Long> {
}
