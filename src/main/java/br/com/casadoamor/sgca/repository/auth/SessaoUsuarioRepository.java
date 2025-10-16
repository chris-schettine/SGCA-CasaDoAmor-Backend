package br.com.casadoamor.sgca.repository.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.auth.SessaoUsuario;

/**
 * Repositório para sessões de usuário
 */
@Repository
public interface SessaoUsuarioRepository extends JpaRepository<SessaoUsuario, Long> {

    /**
     * Busca sessão pelo token JWT
     */
    Optional<SessaoUsuario> findByTokenJwt(String tokenJwt);

    /**
     * Busca todas as sessões ativas de um usuário
     */
    List<SessaoUsuario> findByUsuarioIdAndAtivo(Long usuarioId, Boolean ativo);

    /**
     * Busca sessões ativas de um usuário
     */
    List<SessaoUsuario> findByUsuarioIdAndAtivoAndExpiraEmAfter(
            Long usuarioId, 
            Boolean ativo, 
            LocalDateTime dataAtual
    );

    /**
     * Conta sessões ativas de um usuário
     */
    long countByUsuarioIdAndAtivoAndExpiraEmAfter(
            Long usuarioId, 
            Boolean ativo, 
            LocalDateTime dataAtual
    );

    /**
     * Busca todas as sessões ativas do sistema
     */
    List<SessaoUsuario> findByAtivoAndExpiraEmAfter(Boolean ativo, LocalDateTime dataAtual);

    /**
     * Deleta sessões expiradas (limpeza periódica)
     */
    void deleteByExpiraEmBefore(LocalDateTime data);
}
