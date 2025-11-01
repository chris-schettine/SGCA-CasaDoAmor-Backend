package br.com.casadoamor.sgca.modules.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioRegistroProfissional;

import java.util.Optional;

/**
 * Repository para registros profissionais de usuários
 */
@Repository
public interface AuthUsuarioRegistroProfissionalRepository extends JpaRepository<AuthUsuarioRegistroProfissional, Long> {

    /**
     * Busca registro profissional por ID do usuário
     */
    Optional<AuthUsuarioRegistroProfissional> findByUsuarioId(Long usuarioId);

    /**
     * Verifica se existe registro profissional para o usuário
     */
    boolean existsByUsuarioId(Long usuarioId);

    /**
     * Busca registro profissional por tipo e número
     */
    Optional<AuthUsuarioRegistroProfissional> findByTipoProfissionalAndNumeroRegistro(
            AuthUsuarioRegistroProfissional.TipoProfissional tipoProfissional,
            String numeroRegistro
    );

    /**
     * Verifica se existe registro profissional com o mesmo tipo e número
     */
    boolean existsByTipoProfissionalAndNumeroRegistro(
            AuthUsuarioRegistroProfissional.TipoProfissional tipoProfissional,
            String numeroRegistro
    );
}
