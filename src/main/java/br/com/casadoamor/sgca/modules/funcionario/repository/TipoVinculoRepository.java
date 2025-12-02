package br.com.casadoamor.sgca.modules.funcionario.repository;

import br.com.casadoamor.sgca.modules.funcionario.entity.TipoVinculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para tipos de vínculo
 */
@Repository
public interface TipoVinculoRepository extends JpaRepository<TipoVinculoEntity, Long> {

    /**
     * Buscar tipo de vínculo por código
     */
    Optional<TipoVinculoEntity> findByCodigo(String codigo);

    /**
     * Listar apenas tipos ativos
     */
    List<TipoVinculoEntity> findByAtivoTrue();

    /**
     * Listar todos ordenados por nome
     */
    List<TipoVinculoEntity> findAllByOrderByNomeAsc();

    /**
     * Listar ativos ordenados por nome
     */
    List<TipoVinculoEntity> findByAtivoTrueOrderByNomeAsc();
}
