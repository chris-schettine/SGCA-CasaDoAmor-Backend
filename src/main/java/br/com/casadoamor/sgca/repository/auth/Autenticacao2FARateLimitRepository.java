package br.com.casadoamor.sgca.repository.auth;

import br.com.casadoamor.sgca.entity.auth.Autenticacao2FARateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para controle de rate limiting de 2FA
 */
@Repository
public interface Autenticacao2FARateLimitRepository extends JpaRepository<Autenticacao2FARateLimit, Long> {

    /**
     * Busca controle de rate limit por ID do usuário
     */
    Optional<Autenticacao2FARateLimit> findByUsuarioId(Long usuarioId);

    /**
     * Verifica se existe controle para o usuário
     */
    boolean existsByUsuarioId(Long usuarioId);
}
