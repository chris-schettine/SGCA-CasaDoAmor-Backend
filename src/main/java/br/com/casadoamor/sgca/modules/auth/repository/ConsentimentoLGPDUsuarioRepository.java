package br.com.casadoamor.sgca.modules.auth.repository;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.ConsentimentoLGPDUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de consentimentos LGPD de usuários do sistema
 */
@Repository
public interface ConsentimentoLGPDUsuarioRepository extends JpaRepository<ConsentimentoLGPDUsuario, Long> {

    /**
     * Lista todos os consentimentos de um usuário
     */
    List<ConsentimentoLGPDUsuario> findByUsuario(AuthUsuario usuario);

    /**
     * Lista consentimentos de um usuário ordenados por data (mais recente primeiro)
     */
    List<ConsentimentoLGPDUsuario> findByUsuarioOrderByDataConsentimentoDesc(AuthUsuario usuario);

    /**
     * Busca o primeiro (mais recente) consentimento de um usuário
     */
    Optional<ConsentimentoLGPDUsuario> findFirstByUsuarioOrderByDataConsentimentoDesc(AuthUsuario usuario);

    /**
     * Busca o último consentimento de um usuário
     */
    @Query("SELECT c FROM ConsentimentoLGPDUsuario c WHERE c.usuario = :usuario ORDER BY c.dataConsentimento DESC LIMIT 1")
    Optional<ConsentimentoLGPDUsuario> findUltimoConsentimentoPorUsuario(@Param("usuario") AuthUsuario usuario);

    /**
     * Busca o último consentimento válido (concorda=true) de um usuário
     */
    @Query("SELECT c FROM ConsentimentoLGPDUsuario c WHERE c.usuario = :usuario AND c.concorda = true ORDER BY c.dataConsentimento DESC LIMIT 1")
    Optional<ConsentimentoLGPDUsuario> findUltimoConsentimentoValidoPorUsuario(@Param("usuario") AuthUsuario usuario);

    /**
     * Lista consentimentos por versão do termo
     */
    List<ConsentimentoLGPDUsuario> findByVersaoTermo(String versaoTermo);

    /**
     * Busca consentimentos por escopo
     */
    List<ConsentimentoLGPDUsuario> findByEscopo(String escopo);

    /**
     * Lista consentimentos em um período
     */
    @Query("SELECT c FROM ConsentimentoLGPDUsuario c WHERE c.dataConsentimento BETWEEN :dataInicio AND :dataFim")
    List<ConsentimentoLGPDUsuario> findByDataConsentimentoBetween(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

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
     * Verifica se usuário tem consentimento válido
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ConsentimentoLGPDUsuario c WHERE c.usuario = :usuario AND c.concorda = true")
    boolean hasConsentimentoValido(@Param("usuario") AuthUsuario usuario);
}
