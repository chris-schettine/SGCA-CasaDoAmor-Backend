package br.com.casadoamor.sgca.repository.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.auth.TokenRecuperacao;
import br.com.casadoamor.sgca.enums.TipoToken;

/**
 * Repositório para tokens de recuperação
 */
@Repository
public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Long> {

    /**
     * Busca token pelo hash
     */
    Optional<TokenRecuperacao> findByTokenHash(String tokenHash);

    /**
     * Busca token pelo hash e tipo
     */
    Optional<TokenRecuperacao> findByTokenHashAndTipo(String tokenHash, TipoToken tipo);

    /**
     * Busca tokens de um usuário por tipo
     */
    List<TokenRecuperacao> findByUsuarioIdAndTipo(Long usuarioId, TipoToken tipo);

    /**
     * Busca tokens por usuário e tipo (para invalidação)
     */
    List<TokenRecuperacao> findByUsuarioAndTipo(br.com.casadoamor.sgca.entity.auth.AuthUsuario usuario, TipoToken tipo);

    /**
     * Busca tokens válidos (não usados e não expirados) de um usuário
     */
    List<TokenRecuperacao> findByUsuarioIdAndUsadoAndExpiracaoAfter(
            Long usuarioId, 
            Boolean usado, 
            LocalDateTime dataAtual
    );

    /**
     * Deleta tokens expirados (limpeza periódica)
     */
    void deleteByExpiracaoBefore(LocalDateTime data);
}
