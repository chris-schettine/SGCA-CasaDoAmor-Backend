package br.com.casadoamor.sgca.modules.hospedagem.service;

import br.com.casadoamor.sgca.modules.agendamento.repository.AgendamentoRepository;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemDashboardStatsDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemDashboardStatsDTO.*;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.repository.HospedagemRepository;
import br.com.casadoamor.sgca.modules.hospedagem.repository.QuartoRepository;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HospedagemDashboardServiceTest {

    @InjectMocks
    private HospedagemDashboardService hospedagemDashboardService;

    @Mock
    private HospedagemRepository hospedagemRepository;

    @Mock
    private QuartoRepository quartoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Test
    @DisplayName("Should return dashboard statistics successfully with populated data")
    void shouldReturnDashboardStatisticsWithData() {
        // Setup Data
        Hospedagem hospedagem = new Hospedagem();
        hospedagem.setUuid(UUID.randomUUID().toString());
        hospedagem.setStatus(StatusHospedagem.ATIVA);
        hospedagem.setDataEntrada(LocalDate.now());

        Paciente paciente = new Paciente();
        DadoPessoal dadoPessoal = new DadoPessoal();
        dadoPessoal.setNome("Test Patient");
        paciente.setDadoPessoal(dadoPessoal);
        hospedagem.setPaciente(paciente);

        Quarto quarto = new Quarto();
        quarto.setNome("Quarto 1");
        quarto.setAla(AlaQuarto.MASCULINA);
        quarto.setCapacidadeTotal(2);
        quarto.setCapacidadeOcupada(1);
        hospedagem.setQuarto(quarto);

        // Mocking HospedagemRepository
        when(hospedagemRepository.contarHospedagensAtivas()).thenReturn(10L);
        when(hospedagemRepository.countByPeriodoEntrada(any(), any())).thenReturn(5L);
        when(hospedagemRepository.countByPeriodoSaida(any(), any())).thenReturn(3L);
        when(hospedagemRepository.findHospedagensComPrevisaoVencida()).thenReturn(Collections.emptyList());
        when(hospedagemRepository.calcularMediaDiasPermanencia()).thenReturn(5.5);
        when(hospedagemRepository.countPrevisaoSaidaPara(any())).thenReturn(2L);
        when(hospedagemRepository.countPrevisaoSaidaEntre(any(), any())).thenReturn(4L);
        when(hospedagemRepository.findUltimasEntradas(any(Pageable.class))).thenReturn(List.of(hospedagem));
        when(hospedagemRepository.findUltimasSaidas(any(Pageable.class))).thenReturn(List.of(hospedagem));


        when(hospedagemRepository.findHospedagensPorMes(any())).thenReturn(Arrays.<Object[]>asList(new Object[]{(Integer) 2023, (Integer) 10, (Long) 15L}));

        // Mocking QuartoRepository
        when(quartoRepository.contarTotalQuartos()).thenReturn(20L);
        when(quartoRepository.contarQuartosAtivos()).thenReturn(18L);
        when(quartoRepository.contarQuartosEmManutencao()).thenReturn(2L);
        when(quartoRepository.contarCapacidadeTotal()).thenReturn(40);
        when(quartoRepository.contarOcupacaoTotal()).thenReturn(20);
        when(quartoRepository.contarVagasDisponiveis()).thenReturn(20);
        when(quartoRepository.findQuartosComMaiorOcupacao(any(Pageable.class))).thenReturn(List.of(quarto));

        when(quartoRepository.contarCapacidadeTotalPorAla(any())).thenReturn(10);
        when(quartoRepository.contarOcupacaoTotalPorAla(any())).thenReturn(5);
        when(quartoRepository.contarVagasDisponiveisPorAla(any())).thenReturn(5);

        // Mocking PacienteRepository
        when(pacienteRepository.contarTotalPacientes()).thenReturn(50L);
        when(pacienteRepository.contarPacientesAtivos()).thenReturn(45L);
        when(pacienteRepository.contarPacientesPorPeriodo(any(), any())).thenReturn(2L);

        // Mocking AgendamentoRepository
        when(agendamentoRepository.contarAgendamentosHoje()).thenReturn(1L);
        when(agendamentoRepository.contarAgendamentosSemana()).thenReturn(5L);
        when(agendamentoRepository.contarAgendamentosPendentes()).thenReturn(3L);

        HospedagemDashboardStatsDTO result = hospedagemDashboardService.obterEstatisticas();

        assertNotNull(result);
        assertEquals(10L, result.getTotalHospedagensAtivas());
        assertEquals("6 dias", result.getTempoMedioPermanencia());
        assertEquals(new BigDecimal("50.00"), result.getTaxaOcupacaoGlobal());
        assertFalse(result.getUltimasEntradas().isEmpty());
        assertFalse(result.getUltimasSaidas().isEmpty());
        assertFalse(result.getQuartosComMaiorOcupacao().isEmpty());
        assertFalse(result.getHospedagensPorMes().isEmpty());
        assertNotNull(result.getOcupacaoPorAla());
        assertFalse(result.getOcupacaoPorAla().isEmpty());
    }

    @Test
    @DisplayName("Should handle null or zero values gracefully")
    void shouldHandleNullOrZeroValues() {
        // Mocking HospedagemRepository with nulls/zeros
        when(hospedagemRepository.contarHospedagensAtivas()).thenReturn(0L);
        when(hospedagemRepository.countByPeriodoEntrada(any(), any())).thenReturn(0L);
        when(hospedagemRepository.countByPeriodoSaida(any(), any())).thenReturn(0L);
        when(hospedagemRepository.findHospedagensComPrevisaoVencida()).thenReturn(Collections.emptyList());
        when(hospedagemRepository.calcularMediaDiasPermanencia()).thenReturn(null);
        when(hospedagemRepository.countPrevisaoSaidaPara(any())).thenReturn(0L);
        when(hospedagemRepository.countPrevisaoSaidaEntre(any(), any())).thenReturn(0L);
        when(hospedagemRepository.findUltimasEntradas(any(Pageable.class))).thenReturn(Collections.emptyList());
        when(hospedagemRepository.findUltimasSaidas(any(Pageable.class))).thenReturn(Collections.emptyList());
        when(hospedagemRepository.findHospedagensPorMes(any())).thenReturn(Collections.emptyList());

        // Mocking QuartoRepository with nulls/zeros
        when(quartoRepository.contarTotalQuartos()).thenReturn(0L);
        when(quartoRepository.contarQuartosAtivos()).thenReturn(0L);
        when(quartoRepository.contarQuartosEmManutencao()).thenReturn(0L);
        when(quartoRepository.contarCapacidadeTotal()).thenReturn(0);
        when(quartoRepository.contarOcupacaoTotal()).thenReturn(0);
        when(quartoRepository.contarVagasDisponiveis()).thenReturn(0);
        when(quartoRepository.findQuartosComMaiorOcupacao(any(Pageable.class))).thenReturn(Collections.emptyList());

        when(quartoRepository.contarCapacidadeTotalPorAla(any())).thenReturn(0);
        when(quartoRepository.contarOcupacaoTotalPorAla(any())).thenReturn(0);
        when(quartoRepository.contarVagasDisponiveisPorAla(any())).thenReturn(0);

        // Mocking PacienteRepository
        when(pacienteRepository.contarTotalPacientes()).thenReturn(0L);
        when(pacienteRepository.contarPacientesAtivos()).thenReturn(0L);
        when(pacienteRepository.contarPacientesPorPeriodo(any(), any())).thenReturn(0L);

        // Mocking AgendamentoRepository
        when(agendamentoRepository.contarAgendamentosHoje()).thenReturn(0L);
        when(agendamentoRepository.contarAgendamentosSemana()).thenReturn(0L);
        when(agendamentoRepository.contarAgendamentosPendentes()).thenReturn(0L);

        HospedagemDashboardStatsDTO result = hospedagemDashboardService.obterEstatisticas();

        assertNotNull(result);
        assertEquals(0L, result.getTotalHospedagensAtivas());
        assertEquals("Sem dados", result.getTempoMedioPermanencia());
        assertEquals(BigDecimal.ZERO, result.getTaxaOcupacaoGlobal());
        assertEquals(BigDecimal.ZERO, result.getCrescimentoMesAtual());
    }

    @Test
    @DisplayName("Should calculate growth correctly")
    void shouldCalculateGrowth() {
        // Mocking for growth calculation
        // Current month: 10, Previous month: 5 -> Growth 100%
        when(hospedagemRepository.countByPeriodoEntrada(any(), any()))
                .thenReturn(10L) // Current month
                .thenReturn(5L); // Previous month (called inside obterEstatisticas in order)

        // We need to be careful with the order of calls or use specific matchers if
        // possible,
        // but the service calls countByPeriodoEntrada twice with different dates.
        // Let's mock based on the logic that the service calls:
        // 1. countByPeriodoEntrada(inicioMesAtual, fimMesAtual)
        // 2. countByPeriodoEntrada(inicioMesAnterior, fimMesAnterior)

        // To be safe and avoid strict order dependency issues if implementation
        // changes,
        // we could use ArgumentCaptor or more specific matchers, but for now let's
        // assume the service logic holds.
        // However, since we are mocking the same method with different arguments, we
        // should try to be specific if possible.
        // But the arguments are local variables in the service method (dates).
        // So we will rely on the sequence or just return fixed values and assert the
        // calculation.

        // Let's refine the mock to be safe:
        // The service calls:
        // .totalHospedagensMesAtual(...) -> calls countByPeriodoEntrada(current)
        // .totalHospedagensMesAnterior(...) -> calls countByPeriodoEntrada(previous)
        // .crescimentoMesAtual(...) -> calls countByPeriodoEntrada(current) AND
        // countByPeriodoEntrada(previous) again?
        // Wait, let's check the service code.

        /*
         * .totalHospedagensMesAtual(hospedagemRepository.countByPeriodoEntrada(
         * inicioMesAtual, fimMesAtual))
         * .totalHospedagensMesAnterior(hospedagemRepository.countByPeriodoEntrada(
         * inicioMesAnterior, fimMesAnterior))
         * ...
         * .crescimentoMesAtual(calcularCrescimento(
         * hospedagemRepository.countByPeriodoEntrada(inicioMesAtual, fimMesAtual),
         * hospedagemRepository.countByPeriodoEntrada(inicioMesAnterior, fimMesAnterior)
         * ))
         */

        // It calls the repository methods multiple times.
        // So we can just mock the return values.

        when(hospedagemRepository.countByPeriodoEntrada(any(), any())).thenAnswer(invocation -> {
            LocalDate start = invocation.getArgument(0);
            if (start.getDayOfMonth() == 1 && start.getMonthValue() == LocalDate.now().getMonthValue()) {
                return 10L; // Current Month
            } else {
                return 5L; // Previous Month
            }
        });

        // Provide other necessary mocks to avoid NPEs
        when(hospedagemRepository.contarHospedagensAtivas()).thenReturn(0L);
        when(hospedagemRepository.countByPeriodoSaida(any(), any())).thenReturn(0L);
        when(hospedagemRepository.findHospedagensComPrevisaoVencida()).thenReturn(Collections.emptyList());
        when(hospedagemRepository.calcularMediaDiasPermanencia()).thenReturn(0.0);
        when(hospedagemRepository.countPrevisaoSaidaPara(any())).thenReturn(0L);
        when(hospedagemRepository.countPrevisaoSaidaEntre(any(), any())).thenReturn(0L);
        when(hospedagemRepository.findUltimasEntradas(any(Pageable.class))).thenReturn(Collections.emptyList());
        when(hospedagemRepository.findUltimasSaidas(any(Pageable.class))).thenReturn(Collections.emptyList());
        when(hospedagemRepository.findHospedagensPorMes(any())).thenReturn(Collections.emptyList());

        when(quartoRepository.contarTotalQuartos()).thenReturn(0L);
        when(quartoRepository.contarQuartosAtivos()).thenReturn(0L);
        when(quartoRepository.contarQuartosEmManutencao()).thenReturn(0L);
        when(quartoRepository.contarCapacidadeTotal()).thenReturn(0);
        when(quartoRepository.contarOcupacaoTotal()).thenReturn(0);
        when(quartoRepository.contarVagasDisponiveis()).thenReturn(0);
        when(quartoRepository.findQuartosComMaiorOcupacao(any(Pageable.class))).thenReturn(Collections.emptyList());
        when(quartoRepository.contarCapacidadeTotalPorAla(any())).thenReturn(0);
        when(quartoRepository.contarOcupacaoTotalPorAla(any())).thenReturn(0);
        when(quartoRepository.contarVagasDisponiveisPorAla(any())).thenReturn(0);

        when(pacienteRepository.contarTotalPacientes()).thenReturn(0L);
        when(pacienteRepository.contarPacientesAtivos()).thenReturn(0L);
        when(pacienteRepository.contarPacientesPorPeriodo(any(), any())).thenReturn(0L);

        when(agendamentoRepository.contarAgendamentosHoje()).thenReturn(0L);
        when(agendamentoRepository.contarAgendamentosSemana()).thenReturn(0L);
        when(agendamentoRepository.contarAgendamentosPendentes()).thenReturn(0L);

        HospedagemDashboardStatsDTO result = hospedagemDashboardService.obterEstatisticas();

        // (10 - 5) / 5 * 100 = 100%
        assertEquals(new BigDecimal("100.00"), result.getCrescimentoMesAtual());
    }

    @Test
    @DisplayName("Should handle single day average duration")
    void shouldHandleSingleDayAverageDuration() {
        // Provide other necessary mocks to avoid NPEs
        when(hospedagemRepository.contarHospedagensAtivas()).thenReturn(0L);
        when(hospedagemRepository.countByPeriodoEntrada(any(), any())).thenReturn(0L);
        when(hospedagemRepository.countByPeriodoSaida(any(), any())).thenReturn(0L);
        when(hospedagemRepository.findHospedagensComPrevisaoVencida()).thenReturn(Collections.emptyList());
        when(hospedagemRepository.countPrevisaoSaidaPara(any())).thenReturn(0L);
        when(hospedagemRepository.countPrevisaoSaidaEntre(any(), any())).thenReturn(0L);
        when(hospedagemRepository.findUltimasEntradas(any(Pageable.class))).thenReturn(Collections.emptyList());
        when(hospedagemRepository.findUltimasSaidas(any(Pageable.class))).thenReturn(Collections.emptyList());
        when(hospedagemRepository.findHospedagensPorMes(any())).thenReturn(Collections.emptyList());

        when(quartoRepository.contarTotalQuartos()).thenReturn(0L);
        when(quartoRepository.contarQuartosAtivos()).thenReturn(0L);
        when(quartoRepository.contarQuartosEmManutencao()).thenReturn(0L);
        when(quartoRepository.contarCapacidadeTotal()).thenReturn(0);
        when(quartoRepository.contarOcupacaoTotal()).thenReturn(0);
        when(quartoRepository.contarVagasDisponiveis()).thenReturn(0);
        when(quartoRepository.findQuartosComMaiorOcupacao(any(Pageable.class))).thenReturn(Collections.emptyList());
        when(quartoRepository.contarCapacidadeTotalPorAla(any())).thenReturn(0);
        when(quartoRepository.contarOcupacaoTotalPorAla(any())).thenReturn(0);
        when(quartoRepository.contarVagasDisponiveisPorAla(any())).thenReturn(0);

        when(pacienteRepository.contarTotalPacientes()).thenReturn(0L);
        when(pacienteRepository.contarPacientesAtivos()).thenReturn(0L);
        when(pacienteRepository.contarPacientesPorPeriodo(any(), any())).thenReturn(0L);

        when(agendamentoRepository.contarAgendamentosHoje()).thenReturn(0L);
        when(agendamentoRepository.contarAgendamentosSemana()).thenReturn(0L);
        when(agendamentoRepository.contarAgendamentosPendentes()).thenReturn(0L);

        // Mock specific behavior
        when(hospedagemRepository.calcularMediaDiasPermanencia()).thenReturn(1.0);

        HospedagemDashboardStatsDTO result = hospedagemDashboardService.obterEstatisticas();

        assertEquals("1 dia", result.getTempoMedioPermanencia());
    }
}
