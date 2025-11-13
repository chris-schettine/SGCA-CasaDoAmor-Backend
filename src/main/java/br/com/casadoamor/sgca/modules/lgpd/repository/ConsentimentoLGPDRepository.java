package br.com.casadoamor.sgca.modules.lgpd.repository;

import br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD;
import br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD.TipoEntidadeLGPD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository unificado para gerenciar consentimentos LGPD de todas as entidades
 */
@Repository
public interface ConsentimentoLGPDRepository extends JpaRepository<ConsentimentoLGPD, Long> {

    /**
     * Busca todos os consentimentos de uma entidade específica
     */
    List<ConsentimentoLGPD> findByTipoEntidadeAndEntidadeIdOrderByDataConsentimentoDesc(
            TipoEntidadeLGPD tipoEntidade, 
            Long entidadeId
    );

    /**
     * Busca o consentimento mais recente de uma entidade
     */
    Optional<ConsentimentoLGPD> findFirstByTipoEntidadeAndEntidadeIdOrderByDataConsentimentoDesc(
            TipoEntidadeLGPD tipoEntidade,
            Long entidadeId
    );

    /**
     * Busca consentimentos por versão do termo
     */
    List<ConsentimentoLGPD> findByVersaoTermoOrderByDataConsentimentoDesc(String versaoTermo);

    /**
     * Busca consentimentos por tipo de entidade
     */
    List<ConsentimentoLGPD> findByTipoEntidadeOrderByDataConsentimentoDesc(TipoEntidadeLGPD tipoEntidade);

    /**
     * Busca consentimentos em um período específico
     */
    List<ConsentimentoLGPD> findByDataConsentimentoBetween(
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );

    /**
     * Verifica se existe consentimento válido para uma entidade
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM ConsentimentoLGPD c " +
           "WHERE c.tipoEntidade = :tipoEntidade " +
           "AND c.entidadeId = :entidadeId " +
           "AND c.concorda = true " +
           "AND c.dataConsentimento = (" +
           "    SELECT MAX(c2.dataConsentimento) " +
           "    FROM ConsentimentoLGPD c2 " +
           "    WHERE c2.tipoEntidade = :tipoEntidade " +
           "    AND c2.entidadeId = :entidadeId" +
           ")")
    boolean hasConsentimentoValido(
            @Param("tipoEntidade") TipoEntidadeLGPD tipoEntidade,
            @Param("entidadeId") Long entidadeId
    );

    /**
     * Conta consentimentos por tipo de entidade
     */
    @Query("SELECT COUNT(c) FROM ConsentimentoLGPD c " +
           "WHERE c.tipoEntidade = :tipoEntidade " +
           "AND c.concorda = :concorda")
    Long countByTipoEntidadeAndConcorda(
            @Param("tipoEntidade") TipoEntidadeLGPD tipoEntidade,
            @Param("concorda") Boolean concorda
    );

    /**
     * Busca consentimentos que precisam ser renovados
     */
    @Query("SELECT c FROM ConsentimentoLGPD c " +
           "WHERE c.tipoEntidade = :tipoEntidade " +
           "AND c.versaoTermo != :versaoAtual " +
           "AND c.id IN (" +
           "    SELECT MAX(c2.id) FROM ConsentimentoLGPD c2 " +
           "    WHERE c2.tipoEntidade = :tipoEntidade " +
           "    GROUP BY c2.entidadeId" +
           ")")
    List<ConsentimentoLGPD> findConsentimentosDesatualizados(
            @Param("tipoEntidade") TipoEntidadeLGPD tipoEntidade,
            @Param("versaoAtual") String versaoAtual
    );

    /**
     * Busca consentimentos registrados por um usuário específico
     */
    List<ConsentimentoLGPD> findByRegistradoPorIdOrderByDataConsentimentoDesc(Long registradoPorId);

    /**
     * Conta total de consentimentos por versão do termo
     */
    @Query("SELECT c.versaoTermo, COUNT(c) " +
           "FROM ConsentimentoLGPD c " +
           "GROUP BY c.versaoTermo " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> countByVersaoTermo();

    /**
     * Busca consentimentos criados em um período específico
     */
    List<ConsentimentoLGPD> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );

    /**
     * Busca consentimentos por IP de origem (para auditoria)
     */
    List<ConsentimentoLGPD> findByIpOrigemOrderByDataConsentimentoDesc(String ipOrigem);
}
