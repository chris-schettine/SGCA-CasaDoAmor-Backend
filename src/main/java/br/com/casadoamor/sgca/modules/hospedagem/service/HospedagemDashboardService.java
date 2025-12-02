package br.com.casadoamor.sgca.modules.hospedagem.service;

import br.com.casadoamor.sgca.modules.agendamento.repository.AgendamentoRepository;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemDashboardStatsDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemDashboardStatsDTO.*;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.repository.HospedagemRepository;
import br.com.casadoamor.sgca.modules.hospedagem.repository.QuartoRepository;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para geração de estatísticas do dashboard de hospedagens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HospedagemDashboardService {

    private final HospedagemRepository hospedagemRepository;
    private final QuartoRepository quartoRepository;
    private final PacienteRepository pacienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    @Transactional(readOnly = true)
    public HospedagemDashboardStatsDTO obterEstatisticas() {
        log.info("Gerando estatísticas do dashboard de hospedagens");

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMesAtual = hoje.withDayOfMonth(1);
        LocalDate fimMesAtual = hoje.withDayOfMonth(hoje.lengthOfMonth());
        
        LocalDate inicioMesAnterior = inicioMesAtual.minusMonths(1);
        LocalDate fimMesAnterior = inicioMesAnterior.withDayOfMonth(inicioMesAnterior.lengthOfMonth());
        
        LocalDate inicio30DiasAtras = hoje.minusDays(30);
        LocalDate proximos7Dias = hoje.plusDays(7);
        LocalDate proximos30Dias = hoje.plusDays(30);

        return HospedagemDashboardStatsDTO.builder()
                // Hospedagens
                .totalHospedagensAtivas(hospedagemRepository.contarHospedagensAtivas())
                .totalHospedagensMesAtual(hospedagemRepository.countByPeriodoEntrada(inicioMesAtual, fimMesAtual))
                .totalHospedagensMesAnterior(hospedagemRepository.countByPeriodoEntrada(inicioMesAnterior, fimMesAnterior))
                .totalHospedagensEncerradasUltimos30Dias(hospedagemRepository.countByPeriodoSaida(inicio30DiasAtras, hoje))
                .totalHospedagensPreviaoVencida((long) hospedagemRepository.findHospedagensComPrevisaoVencida().size())
                .mediaDiasPermanencia(hospedagemRepository.calcularMediaDiasPermanencia())
                .tempoMedioPermanencia(formatarMediaDias(hospedagemRepository.calcularMediaDiasPermanencia()))
                
                // Quartos
                .totalQuartos(quartoRepository.contarTotalQuartos())
                .totalQuartosAtivos(quartoRepository.contarQuartosAtivos())
                .totalQuartosEmManutencao(quartoRepository.contarQuartosEmManutencao())
                .totalLeitosDisponiveis(getLongFromInteger(quartoRepository.contarCapacidadeTotal()))
                .totalLeitosOcupados(getLongFromInteger(quartoRepository.contarOcupacaoTotal()))
                .totalLeitosVagos(getLongFromInteger(quartoRepository.contarVagasDisponiveis()))
                .taxaOcupacaoGlobal(calcularTaxaOcupacao(
                        quartoRepository.contarOcupacaoTotal(),
                        quartoRepository.contarCapacidadeTotal()
                ))
                .ocupacaoPorAla(obterOcupacaoPorAla())
                
                // Pacientes
                .totalPacientes(pacienteRepository.contarTotalPacientes())
                .totalPacientesAtivos(pacienteRepository.contarPacientesAtivos())
                .totalPacientesHospedados(hospedagemRepository.contarHospedagensAtivas())
                .totalNovosPacientesMesAtual(pacienteRepository.contarPacientesPorPeriodo(
                        inicioMesAtual.atStartOfDay(),
                        fimMesAtual.atTime(23, 59, 59)
                ))
                
                // Agendamentos
                .totalAgendamentosHoje(agendamentoRepository.contarAgendamentosHoje())
                .totalAgendamentosSemana(agendamentoRepository.contarAgendamentosSemana())
                .totalAgendamentosPendentes(agendamentoRepository.contarAgendamentosPendentes())
                
                // Previsões
                .previsaoSaidasHoje(hospedagemRepository.countPrevisaoSaidaPara(hoje))
                .previsaoSaidasProximos7Dias(hospedagemRepository.countPrevisaoSaidaEntre(hoje, proximos7Dias))
                .previsaoSaidasProximos30Dias(hospedagemRepository.countPrevisaoSaidaEntre(hoje, proximos30Dias))
                
                // Histórico recente
                .ultimasEntradas(obterUltimasEntradas())
                .ultimasSaidas(obterUltimasSaidas())
                .quartosComMaiorOcupacao(obterQuartosComMaiorOcupacao())
                
                // Tendências
                .crescimentoMesAtual(calcularCrescimento(
                        hospedagemRepository.countByPeriodoEntrada(inicioMesAtual, fimMesAtual),
                        hospedagemRepository.countByPeriodoEntrada(inicioMesAnterior, fimMesAnterior)
                ))
                .hospedagensPorMes(obterHospedagensPorMes())
                
                .build();
    }

    private Map<String, OcupacaoPorAlaDTO> obterOcupacaoPorAla() {
        Map<String, OcupacaoPorAlaDTO> ocupacaoPorAla = new HashMap<>();
        
        for (AlaQuarto ala : AlaQuarto.values()) {
            Integer totalLeitos = quartoRepository.contarCapacidadeTotalPorAla(ala);
            Integer leitosOcupados = quartoRepository.contarOcupacaoTotalPorAla(ala);
            Integer leitosVagos = quartoRepository.contarVagasDisponiveisPorAla(ala);
            
            ocupacaoPorAla.put(ala.name(), OcupacaoPorAlaDTO.builder()
                    .ala(ala.name())
                    .totalLeitos(getLongFromInteger(totalLeitos))
                    .leitosOcupados(getLongFromInteger(leitosOcupados))
                    .leitosVagos(getLongFromInteger(leitosVagos))
                    .taxaOcupacao(calcularTaxaOcupacao(leitosOcupados, totalLeitos))
                    .build());
        }
        
        return ocupacaoPorAla;
    }

    private List<HospedagemResumoDTO> obterUltimasEntradas() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Hospedagem> hospedagens = hospedagemRepository.findUltimasEntradas(pageable);
        
        return hospedagens.stream()
                .map(h -> HospedagemResumoDTO.builder()
                        .uuid(h.getUuid())
                        .nomePaciente(h.getPaciente().getDadoPessoal().getNome())
                        .nomeQuarto(h.getQuarto() != null ? h.getQuarto().getNome() : "N/A")
                        .dataEntrada(h.getDataEntrada())
                        .dataSaida(h.getDataSaida())
                        .status(h.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    private List<HospedagemResumoDTO> obterUltimasSaidas() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Hospedagem> hospedagens = hospedagemRepository.findUltimasSaidas(pageable);
        
        return hospedagens.stream()
                .map(h -> HospedagemResumoDTO.builder()
                        .uuid(h.getUuid())
                        .nomePaciente(h.getPaciente().getDadoPessoal().getNome())
                        .nomeQuarto(h.getQuarto() != null ? h.getQuarto().getNome() : "N/A")
                        .dataEntrada(h.getDataEntrada())
                        .dataSaida(h.getDataSaida())
                        .status(h.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    private List<QuartoOcupacaoDTO> obterQuartosComMaiorOcupacao() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Quarto> quartos = quartoRepository.findQuartosComMaiorOcupacao(pageable);
        
        return quartos.stream()
                .map(q -> QuartoOcupacaoDTO.builder()
                        .uuid(q.getUuid())
                        .nome(q.getNome())
                        .ala(q.getAla().name())
                        .capacidadeTotal(q.getCapacidadeTotal())
                        .capacidadeOcupada(q.getCapacidadeOcupada())
                        .taxaOcupacao(calcularTaxaOcupacao(q.getCapacidadeOcupada(), q.getCapacidadeTotal()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<HospedagensPorMesDTO> obterHospedagensPorMes() {
        LocalDate dataInicio = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        List<Object[]> resultados = hospedagemRepository.findHospedagensPorMes(dataInicio);
        
        return resultados.stream()
                .map(r -> {
                    Integer ano = (Integer) r[0];
                    Integer mes = (Integer) r[1];
                    Long total = ((Number) r[2]).longValue();
                    
                    YearMonth yearMonth = YearMonth.of(ano, mes);
                    String nomeMes = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"));
                    
                    return HospedagensPorMesDTO.builder()
                            .mes(mes)
                            .ano(ano)
                            .nomeMes(nomeMes)
                            .total(total)
                            .totalEntradas(total) // Todas são entradas neste caso
                            .totalSaidas(0L) // Pode ser calculado separadamente se necessário
                            .build();
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calcularTaxaOcupacao(Integer ocupado, Integer total) {
        if (total == null || total == 0 || ocupado == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(ocupado)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularCrescimento(Long valorAtual, Long valorAnterior) {
        if (valorAnterior == null || valorAnterior == 0) {
            return valorAtual != null && valorAtual > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        if (valorAtual == null) {
            return BigDecimal.valueOf(-100);
        }
        
        return BigDecimal.valueOf(valorAtual - valorAnterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(valorAnterior), 2, RoundingMode.HALF_UP);
    }

    private String formatarMediaDias(Double media) {
        if (media == null || media == 0) {
            return "Sem dados";
        }
        long dias = Math.round(media);
        return dias == 1 ? "1 dia" : dias + " dias";
    }

    private Long getLongFromInteger(Integer value) {
        return value != null ? value.longValue() : 0L;
    }
}
