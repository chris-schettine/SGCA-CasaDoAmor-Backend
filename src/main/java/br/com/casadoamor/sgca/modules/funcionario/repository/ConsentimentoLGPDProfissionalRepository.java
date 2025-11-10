package br.com.casadoamor.sgca.modules.funcionario.repository;

import br.com.casadoamor.sgca.modules.funcionario.entity.ConsentimentoLGPDProfissional;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de consentimentos LGPD de profissionais
 */
@Repository
public interface ConsentimentoLGPDProfissionalRepository extends JpaRepository<ConsentimentoLGPDProfissional, Long> {

    /**
     * Lista todos os consentimentos de um profissional
     */
    List<ConsentimentoLGPDProfissional> findByProfissional(Profissional profissional);

    /**
     * Lista consentimentos de um profissional ordenados por data (mais recente primeiro)
     */
    List<ConsentimentoLGPDProfissional> findByProfissionalOrderByDataConsentimentoDesc(Profissional profissional);

    /**
     * Busca o último consentimento de um profissional
     */
    @Query("SELECT c FROM ConsentimentoLGPDProfissional c WHERE c.profissional = :profissional ORDER BY c.dataConsentimento DESC LIMIT 1")
    Optional<ConsentimentoLGPDProfissional> findUltimoConsentimentoPorProfissional(@Param("profissional") Profissional profissional);

    /**
     * Busca o último consentimento válido (concorda=true) de um profissional
     */
    @Query("SELECT c FROM ConsentimentoLGPDProfissional c WHERE c.profissional = :profissional AND c.concorda = true ORDER BY c.dataConsentimento DESC LIMIT 1")
    Optional<ConsentimentoLGPDProfissional> findUltimoConsentimentoValidoPorProfissional(@Param("profissional") Profissional profissional);

    /**
     * Lista consentimentos por versão do termo
     */
    List<ConsentimentoLGPDProfissional> findByVersaoTermo(String versaoTermo);

    /**
     * Busca consentimentos por escopo
     */
    List<ConsentimentoLGPDProfissional> findByEscopo(String escopo);

    /**
     * Lista consentimentos em um período
     */
    @Query("SELECT c FROM ConsentimentoLGPDProfissional c WHERE c.dataConsentimento BETWEEN :dataInicio AND :dataFim")
    List<ConsentimentoLGPDProfissional> findByDataConsentimentoBetween(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    /**
     * Conta consentimentos por versão do termo
     */
    long countByVersaoTermo(String versaoTermo);

    /**
     * Conta consentimentos aceitos (concorda=true)
     */
    long countByConcordaTrue();

    /**
     * Conta consentimentos recusados (concorda=false)
     */
    long countByConcordaFalse();

    /**
     * Verifica se profissional tem consentimento válido
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ConsentimentoLGPDProfissional c WHERE c.profissional = :profissional AND c.concorda = true")
    boolean hasConsentimentoValido(@Param("profissional") Profissional profissional);
}
