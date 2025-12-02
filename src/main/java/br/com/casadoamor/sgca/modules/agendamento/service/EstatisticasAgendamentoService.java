package br.com.casadoamor.sgca.modules.agendamento.service;

import br.com.casadoamor.sgca.modules.agendamento.dto.EstatisticasAgendamentoDTO;
import br.com.casadoamor.sgca.modules.agendamento.entity.AgendamentoAcompanhante;
import br.com.casadoamor.sgca.modules.agendamento.entity.AgendamentoPaciente;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Prioridade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.TipoAtendimento;
import br.com.casadoamor.sgca.modules.agendamento.repository.AgendamentoAcompanhanteRepository;
import br.com.casadoamor.sgca.modules.agendamento.repository.AgendamentoPacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstatisticasAgendamentoService {

    private final AgendamentoPacienteRepository agendamentoPacienteRepository;
    private final AgendamentoAcompanhanteRepository agendamentoAcompanhanteRepository;

    @Transactional(readOnly = true)
    public EstatisticasAgendamentoDTO obterEstatisticasGerais() {
        log.info("Obtendo estatísticas gerais de agendamentos");
        
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = LocalDate.now();
        
        // Períodos
        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.atTime(LocalTime.MAX);
        
        LocalDate inicioSemana = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime inicioSemanaDateTime = inicioSemana.atStartOfDay();
        LocalDateTime fimSemanaDateTime = hoje.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
        
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = hoje.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        
        LocalDateTime inicioAno = hoje.withDayOfYear(1).atStartOfDay();
        LocalDateTime fimAno = hoje.with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);
        
        // Buscar agendamentos
        List<AgendamentoPaciente> agendamentosPacientes = agendamentoPacienteRepository.findAll();
        List<AgendamentoAcompanhante> agendamentosAcompanhantes = agendamentoAcompanhanteRepository.findAll();
        
        // Filtrar deletados
        List<AgendamentoPaciente> pacientesAtivos = agendamentosPacientes.stream()
                .filter(a -> a.getDeletedAt() == null)
                .collect(Collectors.toList());
        
        List<AgendamentoAcompanhante> acompanhantesAtivos = agendamentosAcompanhantes.stream()
                .filter(a -> a.getDeletedAt() == null)
                .collect(Collectors.toList());
        
        // ===== ESTATÍSTICAS GERAIS =====
        Long totalAtivos = (long) (pacientesAtivos.size() + acompanhantesAtivos.size());
        
        Long totalHoje = contarPorPeriodo(pacientesAtivos, acompanhantesAtivos, inicioHoje, fimHoje);
        Long totalSemana = contarPorPeriodo(pacientesAtivos, acompanhantesAtivos, inicioSemanaDateTime, fimSemanaDateTime);
        Long totalMes = contarPorPeriodo(pacientesAtivos, acompanhantesAtivos, inicioMes, fimMes);
        Long totalAno = contarPorPeriodo(pacientesAtivos, acompanhantesAtivos, inicioAno, fimAno);
        
        // ===== ESTATÍSTICAS POR STATUS =====
        Long agendados = contarPorStatus(pacientesAtivos, acompanhantesAtivos, StatusAgendamento.AGENDADO);
        Long confirmados = contarPorStatus(pacientesAtivos, acompanhantesAtivos, StatusAgendamento.CONFIRMADO);
        Long emAtendimento = contarPorStatus(pacientesAtivos, acompanhantesAtivos, StatusAgendamento.EM_ATENDIMENTO);
        Long concluidos = contarPorStatus(pacientesAtivos, acompanhantesAtivos, StatusAgendamento.CONCLUIDO);
        Long cancelados = contarPorStatus(pacientesAtivos, acompanhantesAtivos, StatusAgendamento.CANCELADO);
        Long naoCompareceram = contarPorStatus(pacientesAtivos, acompanhantesAtivos, StatusAgendamento.PACIENTE_NAO_COMPARECEU);
        
        // ===== ESTATÍSTICAS POR TIPO =====
        Long totalPacientes = (long) pacientesAtivos.size();
        Long totalAcompanhantes = (long) acompanhantesAtivos.size();
        Long automaticos = pacientesAtivos.stream()
                .filter(AgendamentoPaciente::getGeradoAutomaticamente)
                .count();
        
        // ===== ESTATÍSTICAS POR PRIORIDADE =====
        Long urgentes = contarPorPrioridade(pacientesAtivos, acompanhantesAtivos, Prioridade.URGENTE);
        Long alta = contarPorPrioridade(pacientesAtivos, acompanhantesAtivos, Prioridade.ALTA);
        Long normal = contarPorPrioridade(pacientesAtivos, acompanhantesAtivos, Prioridade.NORMAL);
        Long baixa = contarPorPrioridade(pacientesAtivos, acompanhantesAtivos, Prioridade.BAIXA);
        
        // ===== ESTATÍSTICAS POR TIPO DE ATENDIMENTO =====
        Long primeiraVez = contarPorTipoAtendimento(pacientesAtivos, acompanhantesAtivos, TipoAtendimento.PRIMEIRA_VEZ);
        Long retorno = contarPorTipoAtendimento(pacientesAtivos, acompanhantesAtivos, TipoAtendimento.RETORNO);
        Long emergenciais = contarPorTipoAtendimento(pacientesAtivos, acompanhantesAtivos, TipoAtendimento.EMERGENCIAL);
        Long rotina = contarPorTipoAtendimento(pacientesAtivos, acompanhantesAtivos, TipoAtendimento.ROTINA);
        Long triagem = contarPorTipoAtendimento(pacientesAtivos, acompanhantesAtivos, TipoAtendimento.TRIAGEM);
        
        // ===== CONFIRMAÇÕES =====
        Long pendentesConfirmacao = pacientesAtivos.stream()
                .filter(a -> !a.getConfirmadoPaciente() && !a.getConfirmadoProfissional())
                .count()
                + acompanhantesAtivos.stream()
                .filter(a -> !a.getConfirmadoAcompanhante() && !a.getConfirmadoProfissional())
                .count();
        
        Long confirmadosPaciente = pacientesAtivos.stream()
                .filter(AgendamentoPaciente::getConfirmadoPaciente)
                .count()
                + acompanhantesAtivos.stream()
                .filter(AgendamentoAcompanhante::getConfirmadoAcompanhante)
                .count();
        
        Long confirmadosProfissional = pacientesAtivos.stream()
                .filter(AgendamentoPaciente::getConfirmadoProfissional)
                .count()
                + acompanhantesAtivos.stream()
                .filter(AgendamentoAcompanhante::getConfirmadoProfissional)
                .count();
        
        Long confirmadosAmbos = pacientesAtivos.stream()
                .filter(a -> a.getConfirmadoPaciente() && a.getConfirmadoProfissional())
                .count()
                + acompanhantesAtivos.stream()
                .filter(a -> a.getConfirmadoAcompanhante() && a.getConfirmadoProfissional())
                .count();
        
        // ===== TOP PROFISSIONAIS =====
        List<EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO> topPorAgendamentos = 
                calcularTopProfissionaisPorAgendamentos(pacientesAtivos, acompanhantesAtivos);
        
        List<EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO> topPorConcluidos = 
                calcularTopProfissionaisPorConcluidos(pacientesAtivos, acompanhantesAtivos);
        
        // ===== TOP SERVIÇOS =====
        List<EstatisticasAgendamentoDTO.ServicoEstatisticaDTO> topServicos = 
                calcularTopServicos(pacientesAtivos, acompanhantesAtivos);
        
        // ===== DISTRIBUIÇÃO POR DIA DA SEMANA =====
        Map<String, Long> porDiaSemana = calcularDistribuicaoPorDiaSemana(pacientesAtivos, acompanhantesAtivos);
        
        // ===== DISTRIBUIÇÃO POR HORA =====
        Map<Integer, Long> porHora = calcularDistribuicaoPorHora(pacientesAtivos, acompanhantesAtivos);
        
        // ===== TAXAS =====
        Long totalFinalizados = concluidos + naoCompareceram;
        Double taxaComparecimento = totalFinalizados > 0 ? (concluidos * 100.0) / totalFinalizados : 0.0;
        Double taxaNaoComparecimento = totalFinalizados > 0 ? (naoCompareceram * 100.0) / totalFinalizados : 0.0;
        Double taxaCancelamento = totalAtivos > 0 ? (cancelados * 100.0) / totalAtivos : 0.0;
        
        // ===== TEMPO MÉDIO =====
        Double duracaoMedia = calcularDuracaoMedia(pacientesAtivos, acompanhantesAtivos);
        
        return EstatisticasAgendamentoDTO.builder()
                .totalAgendamentosAtivos(totalAtivos)
                .totalAgendamentosHoje(totalHoje)
                .totalAgendamentosSemana(totalSemana)
                .totalAgendamentosMes(totalMes)
                .totalAgendamentosAno(totalAno)
                .agendamentosAgendados(agendados)
                .agendamentosConfirmados(confirmados)
                .agendamentosEmAtendimento(emAtendimento)
                .agendamentosConcluidos(concluidos)
                .agendamentosCancelados(cancelados)
                .agendamentosNaoCompareceram(naoCompareceram)
                .agendamentosPacientes(totalPacientes)
                .agendamentosAcompanhantes(totalAcompanhantes)
                .agendamentosAutomaticos(automaticos)
                .agendamentosUrgentes(urgentes)
                .agendamentosAltaPrioridade(alta)
                .agendamentosNormalPrioridade(normal)
                .agendamentosBaixaPrioridade(baixa)
                .agendamentosPrimeiraVez(primeiraVez)
                .agendamentosRetorno(retorno)
                .agendamentosEmergenciais(emergenciais)
                .agendamentosRotina(rotina)
                .agendamentosTriagem(triagem)
                .agendamentosPendentesConfirmacao(pendentesConfirmacao)
                .agendamentosConfirmadosPaciente(confirmadosPaciente)
                .agendamentosConfirmadosProfissional(confirmadosProfissional)
                .agendamentosConfirmadosAmbos(confirmadosAmbos)
                .topProfissionaisPorAgendamentos(topPorAgendamentos)
                .topProfissionaisPorConcluidos(topPorConcluidos)
                .topServicosMaisSolicitados(topServicos)
                .agendamentosPorDiaSemana(porDiaSemana)
                .agendamentosPorHora(porHora)
                .taxaComparecimento(Math.round(taxaComparecimento * 100.0) / 100.0)
                .taxaNaoComparecimento(Math.round(taxaNaoComparecimento * 100.0) / 100.0)
                .taxaCancelamento(Math.round(taxaCancelamento * 100.0) / 100.0)
                .duracaoMediaMinutos(Math.round(duracaoMedia * 100.0) / 100.0)
                .tempoMedioEsperaMinutos(0.0)
                .dataHoraConsulta(agora)
                .periodoAnalisado("Geral")
                .build();
    }

    private Long contarPorPeriodo(List<AgendamentoPaciente> pacientes, 
                                   List<AgendamentoAcompanhante> acompanhantes,
                                   LocalDateTime inicio, LocalDateTime fim) {
        long countPacientes = pacientes.stream()
                .filter(a -> !a.getDataHoraInicio().isBefore(inicio) && !a.getDataHoraInicio().isAfter(fim))
                .count();
        
        long countAcompanhantes = acompanhantes.stream()
                .filter(a -> !a.getDataHoraInicio().isBefore(inicio) && !a.getDataHoraInicio().isAfter(fim))
                .count();
        
        return countPacientes + countAcompanhantes;
    }

    private Long contarPorStatus(List<AgendamentoPaciente> pacientes,
                                  List<AgendamentoAcompanhante> acompanhantes,
                                  StatusAgendamento status) {
        long countPacientes = pacientes.stream()
                .filter(a -> a.getStatus() == status)
                .count();
        
        long countAcompanhantes = acompanhantes.stream()
                .filter(a -> a.getStatus() == status)
                .count();
        
        return countPacientes + countAcompanhantes;
    }

    private Long contarPorPrioridade(List<AgendamentoPaciente> pacientes,
                                      List<AgendamentoAcompanhante> acompanhantes,
                                      Prioridade prioridade) {
        long countPacientes = pacientes.stream()
                .filter(a -> a.getPrioridade() == prioridade)
                .count();
        
        long countAcompanhantes = acompanhantes.stream()
                .filter(a -> a.getPrioridade() == prioridade)
                .count();
        
        return countPacientes + countAcompanhantes;
    }

    private Long contarPorTipoAtendimento(List<AgendamentoPaciente> pacientes,
                                          List<AgendamentoAcompanhante> acompanhantes,
                                          TipoAtendimento tipo) {
        long countPacientes = pacientes.stream()
                .filter(a -> a.getTipoAtendimento() == tipo)
                .count();
        
        long countAcompanhantes = acompanhantes.stream()
                .filter(a -> a.getTipoAtendimento() == tipo)
                .count();
        
        return countPacientes + countAcompanhantes;
    }

    private List<EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO> calcularTopProfissionaisPorAgendamentos(
            List<AgendamentoPaciente> pacientes, List<AgendamentoAcompanhante> acompanhantes) {
        
        Map<Long, EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO> profissionaisMap = new HashMap<>();
        
        // Processar pacientes
        for (AgendamentoPaciente agendamento : pacientes) {
            if (agendamento.getProfissionalUsuario() != null) {
                Long profId = agendamento.getProfissionalUsuario().getId();
                profissionaisMap.computeIfAbsent(profId, k -> 
                    EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO.builder()
                            .profissionalId(profId)
                            .profissionalNome(agendamento.getProfissionalUsuario().getNome())
                            .especialidade(agendamento.getProfissionalUsuario().getTipo().name())
                            .totalAgendamentos(0L)
                            .agendamentosConcluidos(0L)
                            .build()
                );
                
                EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO dto = profissionaisMap.get(profId);
                dto.setTotalAgendamentos(dto.getTotalAgendamentos() + 1);
                
                if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
                    dto.setAgendamentosConcluidos(dto.getAgendamentosConcluidos() + 1);
                }
            }
        }
        
        // Processar acompanhantes
        for (AgendamentoAcompanhante agendamento : acompanhantes) {
            if (agendamento.getProfissionalUsuario() != null) {
                Long profId = agendamento.getProfissionalUsuario().getId();
                profissionaisMap.computeIfAbsent(profId, k -> 
                    EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO.builder()
                            .profissionalId(profId)
                            .profissionalNome(agendamento.getProfissionalUsuario().getNome())
                            .especialidade(agendamento.getProfissionalUsuario().getTipo().name())
                            .totalAgendamentos(0L)
                            .agendamentosConcluidos(0L)
                            .build()
                );
                
                EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO dto = profissionaisMap.get(profId);
                dto.setTotalAgendamentos(dto.getTotalAgendamentos() + 1);
                
                if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
                    dto.setAgendamentosConcluidos(dto.getAgendamentosConcluidos() + 1);
                }
            }
        }
        
        // Calcular taxa de conclusão e ordenar
        return profissionaisMap.values().stream()
                .peek(dto -> {
                    double taxa = dto.getTotalAgendamentos() > 0 
                            ? (dto.getAgendamentosConcluidos() * 100.0) / dto.getTotalAgendamentos() 
                            : 0.0;
                    dto.setTaxaConclusao(Math.round(taxa * 100.0) / 100.0);
                })
                .sorted(Comparator.comparing(EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO::getTotalAgendamentos).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO> calcularTopProfissionaisPorConcluidos(
            List<AgendamentoPaciente> pacientes, List<AgendamentoAcompanhante> acompanhantes) {
        
        Map<Long, EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO> profissionaisMap = new HashMap<>();
        
        // Processar pacientes
        for (AgendamentoPaciente agendamento : pacientes) {
            if (agendamento.getProfissionalUsuario() != null && agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
                Long profId = agendamento.getProfissionalUsuario().getId();
                profissionaisMap.computeIfAbsent(profId, k -> 
                    EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO.builder()
                            .profissionalId(profId)
                            .profissionalNome(agendamento.getProfissionalUsuario().getNome())
                            .especialidade(agendamento.getProfissionalUsuario().getTipo().name())
                            .totalAgendamentos(0L)
                            .agendamentosConcluidos(0L)
                            .build()
                );
                
                EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO dto = profissionaisMap.get(profId);
                dto.setAgendamentosConcluidos(dto.getAgendamentosConcluidos() + 1);
            }
        }
        
        // Processar acompanhantes
        for (AgendamentoAcompanhante agendamento : acompanhantes) {
            if (agendamento.getProfissionalUsuario() != null && agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
                Long profId = agendamento.getProfissionalUsuario().getId();
                profissionaisMap.computeIfAbsent(profId, k -> 
                    EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO.builder()
                            .profissionalId(profId)
                            .profissionalNome(agendamento.getProfissionalUsuario().getNome())
                            .especialidade(agendamento.getProfissionalUsuario().getTipo().name())
                            .totalAgendamentos(0L)
                            .agendamentosConcluidos(0L)
                            .build()
                );
                
                EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO dto = profissionaisMap.get(profId);
                dto.setAgendamentosConcluidos(dto.getAgendamentosConcluidos() + 1);
            }
        }
        
        return profissionaisMap.values().stream()
                .sorted(Comparator.comparing(EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO::getAgendamentosConcluidos).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<EstatisticasAgendamentoDTO.ServicoEstatisticaDTO> calcularTopServicos(
            List<AgendamentoPaciente> pacientes, List<AgendamentoAcompanhante> acompanhantes) {
        
        Map<Long, EstatisticasAgendamentoDTO.ServicoEstatisticaDTO> servicosMap = new HashMap<>();
        
        long totalGeral = pacientes.size() + acompanhantes.size();
        
        // Processar pacientes
        for (AgendamentoPaciente agendamento : pacientes) {
            if (agendamento.getTipoServico() != null) {
                Long servicoId = agendamento.getTipoServico().getId();
                servicosMap.computeIfAbsent(servicoId, k -> 
                    EstatisticasAgendamentoDTO.ServicoEstatisticaDTO.builder()
                            .servicoId(servicoId)
                            .servicoNome(agendamento.getTipoServico().getNome())
                            .categoria(agendamento.getTipoServico().getCategoria() != null ? agendamento.getTipoServico().getCategoria().name() : "N/A")
                            .totalAgendamentos(0L)
                            .build()
                );
                
                EstatisticasAgendamentoDTO.ServicoEstatisticaDTO dto = servicosMap.get(servicoId);
                dto.setTotalAgendamentos(dto.getTotalAgendamentos() + 1);
            }
        }
        
        // Processar acompanhantes
        for (AgendamentoAcompanhante agendamento : acompanhantes) {
            if (agendamento.getTipoServico() != null) {
                Long servicoId = agendamento.getTipoServico().getId();
                servicosMap.computeIfAbsent(servicoId, k -> 
                    EstatisticasAgendamentoDTO.ServicoEstatisticaDTO.builder()
                            .servicoId(servicoId)
                            .servicoNome(agendamento.getTipoServico().getNome())
                            .categoria(agendamento.getTipoServico().getCategoria() != null ? agendamento.getTipoServico().getCategoria().name() : "N/A")
                            .totalAgendamentos(0L)
                            .build()
                );
                
                EstatisticasAgendamentoDTO.ServicoEstatisticaDTO dto = servicosMap.get(servicoId);
                dto.setTotalAgendamentos(dto.getTotalAgendamentos() + 1);
            }
        }
        
        return servicosMap.values().stream()
                .peek(dto -> {
                    double percentual = totalGeral > 0 
                            ? (dto.getTotalAgendamentos() * 100.0) / totalGeral 
                            : 0.0;
                    dto.setPercentualTotal(Math.round(percentual * 100.0) / 100.0);
                })
                .sorted(Comparator.comparing(EstatisticasAgendamentoDTO.ServicoEstatisticaDTO::getTotalAgendamentos).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private Map<String, Long> calcularDistribuicaoPorDiaSemana(
            List<AgendamentoPaciente> pacientes, List<AgendamentoAcompanhante> acompanhantes) {
        
        Map<String, Long> distribuicao = new LinkedHashMap<>();
        Locale localePtBr = Locale.forLanguageTag("pt-BR");
        
        // Inicializar com todos os dias
        for (DayOfWeek day : DayOfWeek.values()) {
            distribuicao.put(day.getDisplayName(TextStyle.FULL, localePtBr), 0L);
        }
        
        // Contar pacientes
        pacientes.forEach(a -> {
            String dia = a.getDataHoraInicio().getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, localePtBr);
            distribuicao.merge(dia, 1L, Long::sum);
        });
        
        // Contar acompanhantes
        acompanhantes.forEach(a -> {
            String dia = a.getDataHoraInicio().getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, localePtBr);
            distribuicao.merge(dia, 1L, Long::sum);
        });
        
        return distribuicao;
    }

    private Map<Integer, Long> calcularDistribuicaoPorHora(
            List<AgendamentoPaciente> pacientes, List<AgendamentoAcompanhante> acompanhantes) {
        
        Map<Integer, Long> distribuicao = new TreeMap<>();
        
        // Inicializar com todas as horas
        for (int i = 0; i < 24; i++) {
            distribuicao.put(i, 0L);
        }
        
        // Contar pacientes
        pacientes.forEach(a -> {
            int hora = a.getDataHoraInicio().getHour();
            distribuicao.merge(hora, 1L, Long::sum);
        });
        
        // Contar acompanhantes
        acompanhantes.forEach(a -> {
            int hora = a.getDataHoraInicio().getHour();
            distribuicao.merge(hora, 1L, Long::sum);
        });
        
        return distribuicao;
    }

    private Double calcularDuracaoMedia(List<AgendamentoPaciente> pacientes,
                                        List<AgendamentoAcompanhante> acompanhantes) {
        
        List<Long> duracoes = new ArrayList<>();
        
        // Adicionar durações dos pacientes
        pacientes.stream()
                .filter(a -> a.getDataHoraFim() != null)
                .forEach(a -> {
                    long minutos = java.time.Duration.between(
                            a.getDataHoraInicio(), 
                            a.getDataHoraFim()
                    ).toMinutes();
                    duracoes.add(minutos);
                });
        
        // Adicionar durações dos acompanhantes
        acompanhantes.stream()
                .filter(a -> a.getDataHoraFim() != null)
                .forEach(a -> {
                    long minutos = java.time.Duration.between(
                            a.getDataHoraInicio(), 
                            a.getDataHoraFim()
                    ).toMinutes();
                    duracoes.add(minutos);
                });
        
        if (duracoes.isEmpty()) {
            return 0.0;
        }
        
        return duracoes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }
}
