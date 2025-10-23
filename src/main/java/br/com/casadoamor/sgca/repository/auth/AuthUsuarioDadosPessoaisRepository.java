package br.com.casadoamor.sgca.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.auth.AuthUsuarioDadosPessoais;

/**
 * Repositório para dados pessoais de usuários do sistema
 */
@Repository
public interface AuthUsuarioDadosPessoaisRepository extends JpaRepository<AuthUsuarioDadosPessoais, Long> {
}
