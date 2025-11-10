package br.com.casadoamor.sgca.modules.hospedagem.repository;

import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto;
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
}
