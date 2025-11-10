package br.com.casadoamor.sgca.modules.funcionario.repository;

import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de Profissionais (funcionários e voluntários)
 */
@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    /**
     * Busca profissional por UUID
     */
    Optional<Profissional> findByUuid(String uuid);

    /**
     * Busca profissional por CPF (hash/criptografado)
     */
    Optional<Profissional> findByCpfCriptografado(byte[] cpfCriptografado);

    /**
     * Busca profissional por número de registro profissional
     */
    Optional<Profissional> findByNumeroRegistroAndUfRegistro(String numeroRegistro, String ufRegistro);

    /**
     * Lista profissionais por categoria (FUNCIONARIO ou VOLUNTARIO)
     */
    List<Profissional> findByCategoria(CategoriaProfissional categoria);

    /**
     * Lista profissionais ativos por categoria
     */
    List<Profissional> findByCategoriaAndAtivoTrue(CategoriaProfissional categoria);

    /**
     * Lista profissionais por área de atuação
     */
    List<Profissional> findByAreaAtuacao(String areaAtuacao);

    /**
     * Lista profissionais por área de atuação e status ativo
     */
    List<Profissional> findByAreaAtuacaoAndAtivoTrue(String areaAtuacao);

    /**
     * Lista profissionais por especialidade
     */
    List<Profissional> findByEspecialidade(String especialidade);

    /**
     * Lista profissionais por especialidade e status ativo
     */
    List<Profissional> findByEspecialidadeAndAtivoTrue(String especialidade);

    /**
     * Lista profissionais ativos
     */
    List<Profissional> findByAtivoTrue();

    /**
     * Lista profissionais inativos
     */
    List<Profissional> findByAtivoFalse();

    /**
     * Busca profissionais por nome (LIKE)
     */
    @Query("SELECT p FROM Profissional p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Profissional> searchByNome(@Param("nome") String nome);

    /**
     * Busca profissionais ativos por nome (LIKE)
     */
    @Query("SELECT p FROM Profissional p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND p.ativo = true")
    List<Profissional> searchByNomeAndAtivoTrue(@Param("nome") String nome);

    /**
     * Lista profissionais ativos com paginação
     */
    Page<Profissional> findByAtivoTrue(Pageable pageable);

    /**
     * Lista profissionais por categoria com paginação
     */
    Page<Profissional> findByCategoria(CategoriaProfissional categoria, Pageable pageable);

    /**
     * Busca profissionais admitidos em um período
     */
    @Query("SELECT p FROM Profissional p WHERE p.dataAdmissao BETWEEN :dataInicio AND :dataFim")
    List<Profissional> findByDataAdmissaoBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    /**
     * Busca profissionais desligados em um período
     */
    @Query("SELECT p FROM Profissional p WHERE p.dataDesligamento BETWEEN :dataInicio AND :dataFim")
    List<Profissional> findByDataDesligamentoBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    /**
     * Conta profissionais por categoria
     */
    long countByCategoria(CategoriaProfissional categoria);

    /**
     * Conta profissionais ativos por categoria
     */
    long countByCategoriaAndAtivoTrue(CategoriaProfissional categoria);

    /**
     * Conta profissionais por área de atuação
     */
    long countByAreaAtuacao(String areaAtuacao);

    /**
     * Verifica se existe profissional com o número de registro
     */
    boolean existsByNumeroRegistroAndUfRegistro(String numeroRegistro, String ufRegistro);

    /**
     * Busca profissionais disponíveis para uma área específica
     * (Análise de JSON disponibilidade seria feita em serviço)
     */
    @Query("SELECT p FROM Profissional p WHERE p.areaAtuacao = :areaAtuacao AND p.ativo = true")
    List<Profissional> findProfissionaisDisponiveisPorArea(@Param("areaAtuacao") String areaAtuacao);
}
