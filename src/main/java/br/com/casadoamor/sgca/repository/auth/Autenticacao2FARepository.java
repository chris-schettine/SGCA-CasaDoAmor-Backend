package br.com.casadoamor.sgca.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.auth.Autenticacao2FA;

import java.util.Optional;

@Repository
public interface Autenticacao2FARepository extends JpaRepository<Autenticacao2FA, Long> {

    /**
     * Busca configuração 2FA por usuário
     */
    Optional<Autenticacao2FA> findByUsuarioId(Long usuarioId);

    /**
     * Verifica se usuário tem 2FA habilitado
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Autenticacao2FA a " +
           "WHERE a.usuarioId = :usuarioId AND a.habilitado = true")
    boolean existsByUsuarioIdAndHabilitado(Long usuarioId);

    /**
     * Verifica se existe configuração para o usuário
     */
    boolean existsByUsuarioId(Long usuarioId);
}
