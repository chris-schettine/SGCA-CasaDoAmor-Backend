package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.TipoServico;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.CategoriaServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de Tipos de Serviço
 */
@Repository
public interface TipoServicoRepository extends JpaRepository<TipoServico, Long> {

    /**
     * Busca tipo de serviço por código único
     */
    Optional<TipoServico> findByCodigo(String codigo);

    /**
     * Lista tipos de serviço por categoria
     */
    List<TipoServico> findByCategoria(CategoriaServico categoria);

    /**
     * Lista tipos de serviço ativos
     */
    List<TipoServico> findByAtivoTrue();

    /**
     * Lista tipos de serviço ativos por categoria
     */
    List<TipoServico> findByCategoriaAndAtivoTrue(CategoriaServico categoria);

    /**
     * Lista tipos de serviço que requerem profissional
     */
    List<TipoServico> findByRequerProfissionalTrue();

    /**
     * Lista tipos de serviço que permitem acompanhante
     */
    List<TipoServico> findByPermiteAcompanhanteTrue();

    /**
     * Busca tipos de serviço por nome (LIKE)
     */
    @Query("SELECT t FROM TipoServico t WHERE LOWER(t.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<TipoServico> searchByNome(@Param("nome") String nome);

    /**
     * Busca tipos de serviço ativos por nome (LIKE)
     */
    @Query("SELECT t FROM TipoServico t WHERE LOWER(t.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND t.ativo = true")
    List<TipoServico> searchByNomeAndAtivoTrue(@Param("nome") String nome);

    /**
     * Lista tipos de serviço por duração
     */
    List<TipoServico> findByDuracaoMinutos(Integer duracaoMinutos);

    /**
     * Lista tipos de serviço com duração até um limite
     */
    @Query("SELECT t FROM TipoServico t WHERE t.duracaoMinutos <= :maxDuracao AND t.ativo = true")
    List<TipoServico> findByDuracaoAteLimite(@Param("maxDuracao") Integer maxDuracao);

    /**
     * Conta tipos de serviço por categoria
     */
    long countByCategoria(CategoriaServico categoria);

    /**
     * Conta tipos de serviço ativos
     */
    long countByAtivoTrue();

    /**
     * Verifica se existe tipo de serviço com o código
     */
    boolean existsByCodigo(String codigo);
}
