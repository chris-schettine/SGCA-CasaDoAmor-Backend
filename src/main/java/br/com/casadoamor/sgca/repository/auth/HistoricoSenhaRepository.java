package br.com.casadoamor.sgca.repository.auth;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.auth.HistoricoSenha;

import java.util.List;

/**
 * Repository para operações com histórico de senhas.
 */
@Repository
public interface HistoricoSenhaRepository extends JpaRepository<HistoricoSenha, Long> {

    /**
     * Busca as últimas N senhas de um usuário, ordenadas da mais recente para a mais antiga.
     *
     * @param usuarioId ID do usuário
     * @param pageable  Paginação com limite de resultados
     * @return Lista das últimas senhas do usuário
     */
    @Query("SELECT h FROM HistoricoSenha h WHERE h.usuarioId = :usuarioId ORDER BY h.criadoEm DESC")
    List<HistoricoSenha> findTopNByUsuarioIdOrderByCriadoEmDesc(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /**
     * Conta quantas senhas um usuário possui no histórico.
     *
     * @param usuarioId ID do usuário
     * @return Quantidade de senhas no histórico
     */
    long countByUsuarioId(Long usuarioId);
}
