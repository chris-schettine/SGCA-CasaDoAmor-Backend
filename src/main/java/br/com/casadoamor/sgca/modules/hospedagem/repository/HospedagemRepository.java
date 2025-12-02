package br.com.casadoamor.sgca.modules.hospedagem.repository;

import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de hospedagens
 */
@Repository
public interface HospedagemRepository extends JpaRepository<Hospedagem, Long>, JpaSpecificationExecutor<Hospedagem> {

    // Busca por UUID
    Optional<Hospedagem> findByUuid(String uuid);

    // Busca por paciente
    List<Hospedagem> findByPaciente(Paciente paciente);
    List<Hospedagem> findByPacienteOrderByDataEntradaDesc(Paciente paciente);

    // Busca por quarto
    List<Hospedagem> findByQuarto(Quarto quarto);
    List<Hospedagem> findByQuartoOrderByDataEntradaDesc(Quarto quarto);
    List<Hospedagem> findByQuartoAndStatus(Quarto quarto, StatusHospedagem status);

    // Hospedagem ativa do paciente
    @Query("SELECT h FROM Hospedagem h WHERE h.paciente = :paciente AND h.status = 'ATIVA' " +
           "AND h.dataSaida IS NULL")
    Optional<Hospedagem> findHospedagemAtivaDoPaciente(@Param("paciente") Paciente paciente);

    // Verificar se paciente tem hospedagem ativa
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Hospedagem h " +
           "WHERE h.paciente = :paciente AND h.status = 'ATIVA' AND h.dataSaida IS NULL")
    boolean pacienteTemHospedagemAtiva(@Param("paciente") Paciente paciente);

    // Listagens por status
    List<Hospedagem> findByStatus(StatusHospedagem status);
    List<Hospedagem> findByStatusOrderByDataEntradaDesc(StatusHospedagem status);

    // Hospedagens ativas
    @Query("SELECT h FROM Hospedagem h WHERE h.status = 'ATIVA' AND h.dataSaida IS NULL " +
           "ORDER BY h.dataEntrada DESC")
    List<Hospedagem> findHospedagensAtivas();

    // Hospedagens por período
    @Query("SELECT h FROM Hospedagem h WHERE h.dataEntrada BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY h.dataEntrada DESC")
    List<Hospedagem> findByPeriodoEntrada(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    @Query("SELECT h FROM Hospedagem h WHERE h.dataSaida BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY h.dataSaida DESC")
    List<Hospedagem> findByPeriodoSaida(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    // Hospedagens do mês atual
    @Query("SELECT h FROM Hospedagem h WHERE YEAR(h.dataEntrada) = YEAR(CURRENT_DATE) " +
           "AND MONTH(h.dataEntrada) = MONTH(CURRENT_DATE) ORDER BY h.dataEntrada DESC")
    List<Hospedagem> findHospedagensDoMesAtual();

    // Contagem de hospedagens ativas
    @Query("SELECT COUNT(h) FROM Hospedagem h WHERE h.status = 'ATIVA' AND h.dataSaida IS NULL")
    Long contarHospedagensAtivas();

    // Contagem por quarto
    @Query("SELECT COUNT(h) FROM Hospedagem h WHERE h.quarto = :quarto AND h.status = 'ATIVA' " +
           "AND h.dataSaida IS NULL")
    Long contarHospedagensAtivasPorQuarto(@Param("quarto") Quarto quarto);

    // Relatório: hospedagens por período e status
    @Query("SELECT h FROM Hospedagem h WHERE h.dataEntrada >= :dataInicio " +
           "AND (h.dataSaida IS NULL OR h.dataSaida <= :dataFim) " +
           "AND h.status = :status ORDER BY h.dataEntrada DESC")
    List<Hospedagem> findRelatorioHospedagens(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("status") StatusHospedagem status);

    // Histórico completo do paciente
    @Query("SELECT h FROM Hospedagem h WHERE h.paciente.id = :pacienteId " +
           "ORDER BY h.dataEntrada DESC, h.createdAt DESC")
    List<Hospedagem> findHistoricoPorPacienteId(@Param("pacienteId") String pacienteId);

    // Hospedagens com previsão de saída
    @Query("SELECT h FROM Hospedagem h WHERE h.status = 'ATIVA' AND h.dataSaidaPrevista = :data " +
           "ORDER BY h.dataSaidaPrevista")
    List<Hospedagem> findHospedagensComPrevisaoSaidaPara(@Param("data") LocalDate data);

    // Hospedagens com previsão de saída vencida
    @Query("SELECT h FROM Hospedagem h WHERE h.status = 'ATIVA' AND h.dataSaidaPrevista < CURRENT_DATE " +
           "ORDER BY h.dataSaidaPrevista")
    List<Hospedagem> findHospedagensComPrevisaoVencida();

    // === QUERIES PARA ESTATÍSTICAS ===

    // Contar hospedagens por período
    @Query("SELECT COUNT(h) FROM Hospedagem h WHERE h.dataEntrada BETWEEN :inicio AND :fim")
    Long countByPeriodoEntrada(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(h) FROM Hospedagem h WHERE h.dataSaida BETWEEN :inicio AND :fim " +
           "AND h.status = 'ENCERRADA'")
    Long countByPeriodoSaida(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // Média de dias de permanência
    @Query("SELECT AVG(DATEDIFF(h.dataSaida, h.dataEntrada)) FROM Hospedagem h " +
           "WHERE h.dataSaida IS NOT NULL AND h.status = 'ENCERRADA'")
    Double calcularMediaDiasPermanencia();

    // Previsões de saída
    @Query("SELECT COUNT(h) FROM Hospedagem h WHERE h.status = 'ATIVA' " +
           "AND h.dataSaidaPrevista = :data")
    Long countPrevisaoSaidaPara(@Param("data") LocalDate data);

    @Query("SELECT COUNT(h) FROM Hospedagem h WHERE h.status = 'ATIVA' " +
           "AND h.dataSaidaPrevista BETWEEN :inicio AND :fim")
    Long countPrevisaoSaidaEntre(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // Últimas entradas
    @Query("SELECT h FROM Hospedagem h ORDER BY h.dataEntrada DESC, h.createdAt DESC")
    List<Hospedagem> findUltimasEntradas(Pageable pageable);

    // Últimas saídas
    @Query("SELECT h FROM Hospedagem h WHERE h.dataSaida IS NOT NULL " +
           "ORDER BY h.dataSaida DESC, h.updatedAt DESC")
    List<Hospedagem> findUltimasSaidas(Pageable pageable);

    // Hospedagens por mês (últimos 12 meses)
    @Query("SELECT YEAR(h.dataEntrada) as ano, MONTH(h.dataEntrada) as mes, COUNT(h) as total " +
           "FROM Hospedagem h WHERE h.dataEntrada >= :dataInicio " +
           "GROUP BY YEAR(h.dataEntrada), MONTH(h.dataEntrada) " +
           "ORDER BY YEAR(h.dataEntrada) DESC, MONTH(h.dataEntrada) DESC")
    List<Object[]> findHospedagensPorMes(@Param("dataInicio") LocalDate dataInicio);
}
