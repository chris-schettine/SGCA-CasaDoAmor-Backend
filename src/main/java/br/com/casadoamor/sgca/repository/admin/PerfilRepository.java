package br.com.casadoamor.sgca.repository.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.admin.Perfil;

/**
 * Repository para gerenciamento de Perfis (Roles)
 */
@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    /**
     * Busca perfil por nome
     */
    Optional<Perfil> findByNome(String nome);

    /**
     * Busca perfis não deletados
     */
    @Query("SELECT p FROM Perfil p WHERE p.deletadoEm IS NULL")
    List<Perfil> findAllAtivos();

    /**
     * Busca perfis por IDs
     */
    @Query("SELECT p FROM Perfil p WHERE p.id IN :ids AND p.deletadoEm IS NULL")
    List<Perfil> findByIdIn(@Param("ids") List<Long> ids);

    /**
     * Verifica se perfil existe por nome
     */
    boolean existsByNome(String nome);

    /**
     * Busca perfis de um usuário
     */
    @Query("SELECT p FROM Perfil p JOIN p.usuarios u WHERE u.id = :usuarioId AND p.deletadoEm IS NULL")
    List<Perfil> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}
