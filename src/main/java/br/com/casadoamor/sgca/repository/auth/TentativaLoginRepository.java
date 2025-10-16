package br.com.casadoamor.sgca.repository.auth;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.auth.TentativaLogin;

/**
 * Repositório para tentativas de login
 */
@Repository
public interface TentativaLoginRepository extends JpaRepository<TentativaLogin, Long> {

    /**
     * Busca tentativas de login de um usuário específico
     */
    List<TentativaLogin> findByUsuarioIdOrderByDataTentativaDesc(Long usuarioId);

    /**
     * Busca tentativas de login por CPF em um período
     */
    List<TentativaLogin> findByCpfAndDataTentativaAfter(String cpf, LocalDateTime dataInicio);

    /**
     * Busca tentativas de login por IP em um período
     */
    List<TentativaLogin> findByIpOrigemAndDataTentativaAfter(String ipOrigem, LocalDateTime dataInicio);

    /**
     * Conta tentativas falhas de um CPF em um período
     */
    long countByCpfAndSucessoAndDataTentativaAfter(String cpf, Boolean sucesso, LocalDateTime dataInicio);

    /**
     * Busca últimas tentativas de login (para relatórios)
     */
    List<TentativaLogin> findTop100ByOrderByDataTentativaDesc();

    /**
     * Busca tentativas de login por período
     */
    List<TentativaLogin> findByDataTentativaBetween(LocalDateTime inicio, LocalDateTime fim);

    /**
     * Busca tentativas de login por período e sucesso
     */
    List<TentativaLogin> findByDataTentativaBetweenAndSucesso(LocalDateTime inicio, LocalDateTime fim, Boolean sucesso);

    /**
     * Busca tentativas de login por CPF
     */
    List<TentativaLogin> findByCpf(String cpf);

    /**
     * Busca tentativas de login por sucesso
     */
    List<TentativaLogin> findBySucesso(Boolean sucesso);
}
