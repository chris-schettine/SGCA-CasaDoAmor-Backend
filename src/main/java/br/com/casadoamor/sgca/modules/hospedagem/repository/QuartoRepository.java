package br.com.casadoamor.sgca.modules.hospedagem.repository;

import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de quartos/leitos
 */
@Repository
public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    // Busca por UUID
    Optional<Quarto> findByUuid(String uuid);

    // Busca por código
    Optional<Quarto> findByCodigo(String codigo);

    // Verificações de existência
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);

    // Listagens
    List<Quarto> findByAtivoTrue();
    List<Quarto> findByAla(AlaQuarto ala);
    List<Quarto> findByTipo(TipoQuarto tipo);
    List<Quarto> findByAtivoTrueAndAla(AlaQuarto ala);
    List<Quarto> findByAtivoTrueAndTipo(TipoQuarto tipo);

    // Busca por nome
    List<Quarto> findByNomeContainingIgnoreCase(String nome);

    // Quartos com vagas disponíveis
    @Query("SELECT q FROM Quarto q WHERE q.ativo = true AND q.emManutencao = false " +
           "AND q.capacidadeOcupada < q.capacidadeTotal")
    List<Quarto> findQuartosComVagas();

    @Query("SELECT q FROM Quarto q WHERE q.ativo = true AND q.emManutencao = false " +
           "AND q.capacidadeOcupada < q.capacidadeTotal AND q.ala = :ala")
    List<Quarto> findQuartosComVagasPorAla(@Param("ala") AlaQuarto ala);

    // Estatísticas
    @Query("SELECT SUM(q.capacidadeTotal) FROM Quarto q WHERE q.ativo = true")
    Integer contarCapacidadeTotal();

    @Query("SELECT SUM(q.capacidadeOcupada) FROM Quarto q WHERE q.ativo = true")
    Integer contarOcupacaoTotal();

    @Query("SELECT SUM(q.capacidadeTotal - q.capacidadeOcupada) FROM Quarto q " +
           "WHERE q.ativo = true AND q.emManutencao = false")
    Integer contarVagasDisponiveis();

    @Query("SELECT SUM(q.capacidadeTotal) FROM Quarto q WHERE q.ativo = true AND q.ala = :ala")
    Integer contarCapacidadeTotalPorAla(@Param("ala") AlaQuarto ala);

    @Query("SELECT SUM(q.capacidadeOcupada) FROM Quarto q WHERE q.ativo = true AND q.ala = :ala")
    Integer contarOcupacaoTotalPorAla(@Param("ala") AlaQuarto ala);

    @Query("SELECT SUM(q.capacidadeTotal - q.capacidadeOcupada) FROM Quarto q " +
           "WHERE q.ativo = true AND q.emManutencao = false AND q.ala = :ala")
    Integer contarVagasDisponiveisPorAla(@Param("ala") AlaQuarto ala);

    // Quartos em manutenção
    List<Quarto> findByEmManutencaoTrue();

    // Andar
    List<Quarto> findByAndar(String andar);
    List<Quarto> findByAtivoTrueAndAndar(String andar);

    // Busca com paginação e múltiplos filtros
    @Query("SELECT q FROM Quarto q WHERE " +
           "(:nome IS NULL OR LOWER(q.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:ala IS NULL OR q.ala = :ala) AND " +
           "(:tipo IS NULL OR q.tipo = :tipo) AND " +
           "(:ativo IS NULL OR q.ativo = :ativo)")
    Page<Quarto> searchQuartos(
            @Param("nome") String nome,
            @Param("ala") AlaQuarto ala,
            @Param("tipo") TipoQuarto tipo,
            @Param("ativo") Boolean ativo,
            Pageable pageable);

    // === ESTATÍSTICAS AVANÇADAS ===
    
    // Contagem de quartos por status
    @Query("SELECT COUNT(q) FROM Quarto q")
    Long contarTotalQuartos();
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ativo = true")
    Long contarQuartosAtivos();
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ativo = false")
    Long contarQuartosInativos();
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.emManutencao = true")
    Long contarQuartosEmManutencao();
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ativo = true AND q.emManutencao = false AND q.capacidadeOcupada < q.capacidadeTotal")
    Long contarQuartosDisponiveisAdmissao();
    
    // Contagem por tipo
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.tipo = :tipo")
    Long contarQuartosPorTipo(@Param("tipo") TipoQuarto tipo);
    
    // Contagem por situação de ocupação
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ativo = true AND q.capacidadeOcupada = q.capacidadeTotal")
    Long contarQuartosLotados();
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ativo = true AND q.capacidadeOcupada = 0")
    Long contarQuartosVazios();
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ativo = true AND q.capacidadeOcupada > 0 AND q.capacidadeOcupada < q.capacidadeTotal")
    Long contarQuartosParcialmenteOcupados();
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.permiteSexoOposto = true")
    Long contarQuartosPermitemSexoOposto();
    
    // === ESTATÍSTICAS POR ALA ===
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ala = :ala")
    Long contarTotalQuartosPorAla(@Param("ala") AlaQuarto ala);
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ala = :ala AND q.ativo = true")
    Long contarQuartosAtivosPorAla(@Param("ala") AlaQuarto ala);
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ala = :ala AND q.ativo = false")
    Long contarQuartosInativosPorAla(@Param("ala") AlaQuarto ala);
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ala = :ala AND q.emManutencao = true")
    Long contarQuartosEmManutencaoPorAla(@Param("ala") AlaQuarto ala);
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ala = :ala AND q.ativo = true AND q.emManutencao = false AND q.capacidadeOcupada < q.capacidadeTotal")
    Long contarQuartosDisponiveisAdmissaoPorAla(@Param("ala") AlaQuarto ala);
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ala = :ala AND q.ativo = true AND q.capacidadeOcupada = q.capacidadeTotal")
    Long contarQuartosLotadosPorAla(@Param("ala") AlaQuarto ala);
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ala = :ala AND q.ativo = true AND q.capacidadeOcupada = 0")
    Long contarQuartosVaziosPorAla(@Param("ala") AlaQuarto ala);
    
    @Query("SELECT COUNT(q) FROM Quarto q WHERE q.ala = :ala AND q.ativo = true AND q.capacidadeOcupada > 0 AND q.capacidadeOcupada < q.capacidadeTotal")
    Long contarQuartosParcialmenteOcupadosPorAla(@Param("ala") AlaQuarto ala);

    // Quartos com maior ocupação (para dashboard)
    @Query("SELECT q FROM Quarto q WHERE q.ativo = true AND q.capacidadeOcupada > 0 " +
           "ORDER BY (q.capacidadeOcupada * 1.0 / q.capacidadeTotal) DESC")
    List<Quarto> findQuartosComMaiorOcupacao(Pageable pageable);
}
