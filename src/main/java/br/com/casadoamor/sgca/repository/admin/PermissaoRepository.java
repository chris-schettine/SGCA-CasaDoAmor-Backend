package br.com.casadoamor.sgca.repository.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.admin.Permissao;

/**
 * Repository para gerenciamento de Permissões
 */
@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Long> {

    /**
     * Busca permissão por nome
     */
    Optional<Permissao> findByNome(String nome);

    /**
     * Busca permissões não deletadas
     */
    @Query("SELECT p FROM Permissao p WHERE p.deletadoEm IS NULL")
    List<Permissao> findAllAtivas();

    /**
     * Busca permissões por IDs
     */
    @Query("SELECT p FROM Permissao p WHERE p.id IN :ids AND p.deletadoEm IS NULL")
    List<Permissao> findByIdIn(@Param("ids") List<Long> ids);

    /**
     * Verifica se permissão existe por nome
     */
    boolean existsByNome(String nome);

    /**
     * Busca permissões de um perfil
     */
    @Query("SELECT p FROM Permissao p JOIN p.perfis pr WHERE pr.id = :perfilId AND p.deletadoEm IS NULL")
    List<Permissao> findByPerfilId(@Param("perfilId") Long perfilId);
}
