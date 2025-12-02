package br.com.casadoamor.sgca.modules.paciente.service;

import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.acompanhante.repository.AcompanhanteRepository;
import br.com.casadoamor.sgca.modules.common.enums.PacienteStatus;
import br.com.casadoamor.sgca.modules.dadoClinico.entity.DadoClinico;
import br.com.casadoamor.sgca.modules.paciente.dto.EstatisticasPacienteAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstatisticasPacienteAcompanhanteService {

    private final PacienteRepository pacienteRepository;
    private final AcompanhanteRepository acompanhanteRepository;

    public EstatisticasPacienteAcompanhanteDTO obterEstatisticasGerais() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = agora.toLocalDate();
        LocalDate inicioSemana = hoje.minusDays(7);
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate inicioAno = hoje.withDayOfYear(1);

        // Buscar todos os pacientes e acompanhantes
        List<Paciente> todosPacientes = pacienteRepository.findAll();
        List<Acompanhante> todosAcompanhantes = acompanhanteRepository.findAll();

        // Filtrar pacientes não deletados
        List<Paciente> pacientesAtivos = todosPacientes.stream()
                .filter(p -> p.getDeletedAt() == null)
                .collect(Collectors.toList());

        // Filtrar acompanhantes não deletados
        List<Acompanhante> acompanhantesAtivos = todosAcompanhantes.stream()
                .filter(a -> a.getDeletedAt() == null)
                .collect(Collectors.toList());

        // === ESTATÍSTICAS GERAIS DE PACIENTES ===
        long totalPacientes = todosPacientes.size();
        long pacientesAtivosCount = pacientesAtivos.size();
        long pacientesInativosCount = totalPacientes - pacientesAtivosCount;

        long pacientesRegistradosHoje = pacientesAtivos.stream()
                .filter(p -> p.getCreatedAt().toLocalDate().equals(hoje))
                .count();

        long pacientesRegistradosSemana = pacientesAtivos.stream()
                .filter(p -> !p.getCreatedAt().toLocalDate().isBefore(inicioSemana))
                .count();

        long pacientesRegistradosMes = pacientesAtivos.stream()
                .filter(p -> !p.getCreatedAt().toLocalDate().isBefore(inicioMes))
                .count();

        long pacientesRegistradosAno = pacientesAtivos.stream()
                .filter(p -> !p.getCreatedAt().toLocalDate().isBefore(inicioAno))
                .count();

        // === ESTATÍSTICAS POR STATUS DE PACIENTE ===
        // PacienteStatus only has: ATIVO, INATIVO, FALECIDO
        Map<PacienteStatus, Long> pacientesPorStatus = pacientesAtivos.stream()
                .collect(Collectors.groupingBy(Paciente::getStatus, Collectors.counting()));

        long pacientesAtivosPorStatus = pacientesPorStatus.getOrDefault(PacienteStatus.ATIVO, 0L);
        long pacientesFalecidos = pacientesPorStatus.getOrDefault(PacienteStatus.FALECIDO, 0L);
        
        // Calculate these based on business logic (can be adjusted later)
        long pacientesEmTratamento = pacientesAtivosPorStatus;
        long pacientesCurados = 0L; // No enum value for this
        long pacientesEmObservacao = 0L; // No enum value for this

        // === ESTATÍSTICAS GERAIS DE ACOMPANHANTES ===
        long totalAcompanhantes = todosAcompanhantes.size();
        long acompanhantesAtivosCount = acompanhantesAtivos.size();
        long acompanhantesInativosCount = totalAcompanhantes - acompanhantesAtivosCount;

        long acompanhantesRegistradosHoje = acompanhantesAtivos.stream()
                .filter(a -> a.getCreatedAt().toLocalDate().equals(hoje))
                .count();

        long acompanhantesRegistradosSemana = acompanhantesAtivos.stream()
                .filter(a -> !a.getCreatedAt().toLocalDate().isBefore(inicioSemana))
                .count();

        long acompanhantesRegistradosMes = acompanhantesAtivos.stream()
                .filter(a -> !a.getCreatedAt().toLocalDate().isBefore(inicioMes))
                .count();

        long acompanhantesRegistradosAno = acompanhantesAtivos.stream()
                .filter(a -> !a.getCreatedAt().toLocalDate().isBefore(inicioAno))
                .count();

        // === ESTATÍSTICAS DE RELACIONAMENTO ===
        Map<String, Long> acompanhantesPorPaciente = acompanhantesAtivos.stream()
                .collect(Collectors.groupingBy(a -> a.getPaciente().getId(), Collectors.counting()));

        double mediaAcompanhantesPorPaciente = pacientesAtivosCount > 0 ?
                (double) acompanhantesAtivosCount / pacientesAtivosCount : 0.0;

        long pacientesSemAcompanhante = pacientesAtivos.stream()
                .filter(p -> acompanhantesPorPaciente.getOrDefault(p.getId(), 0L) == 0)
                .count();

        long pacientesComUmAcompanhante = acompanhantesPorPaciente.values().stream()
                .filter(count -> count == 1)
                .count();

        long pacientesComMultiplosAcompanhantes = acompanhantesPorPaciente.values().stream()
                .filter(count -> count > 1)
                .count();

        long maxAcompanhantesPorPaciente = acompanhantesPorPaciente.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        // === DISTRIBUIÇÃO POR PARENTESCO ===
        Map<String, Long> acompanhantesPorParentesco = acompanhantesAtivos.stream()
                .collect(Collectors.groupingBy(a -> a.getParentesco().toString(), Collectors.counting()));

        EstatisticasPacienteAcompanhanteDTO.ParentescoEstatisticaDTO parentescoMaisComum = null;
        if (!acompanhantesPorParentesco.isEmpty()) {
            Map.Entry<String, Long> maisComunEntry = acompanhantesPorParentesco.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (maisComunEntry != null) {
                parentescoMaisComum = EstatisticasPacienteAcompanhanteDTO.ParentescoEstatisticaDTO.builder()
                        .parentesco(maisComunEntry.getKey())
                        .quantidade(maisComunEntry.getValue())
                        .percentualTotal(acompanhantesAtivosCount > 0 ?
                                (maisComunEntry.getValue() * 100.0) / acompanhantesAtivosCount : 0.0)
                        .build();
            }
        }

        // === DADOS CLÍNICOS ===
        long pacientesComDadosClinicos = pacientesAtivos.stream()
                .filter(p -> p.getDadosClinicos() != null && !p.getDadosClinicos().isEmpty())
                .count();

        long pacientesSemDadosClinicos = pacientesAtivosCount - pacientesComDadosClinicos;

        long pacientesComSonda = pacientesAtivos.stream()
                .filter(p -> p.getDadosClinicos() != null && 
                        p.getDadosClinicos().stream().anyMatch(DadoClinico::getUsaSonda))
                .count();

        long pacientesComCurativo = pacientesAtivos.stream()
                .filter(p -> p.getDadosClinicos() != null && 
                        p.getDadosClinicos().stream().anyMatch(DadoClinico::getUsaCurativo))
                .count();

        Map<String, Long> pacientesPorTipoSonda = new HashMap<>();
        pacientesAtivos.stream()
                .flatMap(p -> p.getDadosClinicos().stream())
                .filter(dc -> dc.getUsaSonda())
                .forEach(dc -> {
                    if (dc.getTipoSondaNasal() != null) {
                        pacientesPorTipoSonda.merge(dc.getTipoSondaNasal().toString(), 1L, Long::sum);
                    }
                    if (dc.getTipoSondaCirurgica() != null) {
                        pacientesPorTipoSonda.merge(dc.getTipoSondaCirurgica().toString(), 1L, Long::sum);
                    }
                    if (dc.getTipoSondaVesical() != null) {
                        pacientesPorTipoSonda.merge(dc.getTipoSondaVesical().toString(), 1L, Long::sum);
                    }
                });

        // === INFORMAÇÕES HOSPITALARES ===
        long pacientesComInformacaoHospitalar = pacientesAtivos.stream()
                .filter(p -> p.getInformacaoHospitalar() != null)
                .count();

        long pacientesSemInformacaoHospitalar = pacientesAtivosCount - pacientesComInformacaoHospitalar;

        // === ACOMPANHANTES QUE PODEM AJUDAR NA COZINHA ===
        long acompanhantesPodemAjudarCozinha = acompanhantesAtivos.stream()
                .filter(Acompanhante::getPodeAjudarNaCozinha)
                .count();

        long acompanhantesNaoPodemAjudarCozinha = acompanhantesAtivosCount - acompanhantesPodemAjudarCozinha;

        double percentualAjudamCozinha = acompanhantesAtivosCount > 0 ?
                (acompanhantesPodemAjudarCozinha * 100.0) / acompanhantesAtivosCount : 0.0;

        // === CONTATOS DE EMERGÊNCIA ===
        long pacientesComContatoEmergencia = pacientesAtivos.stream()
                .filter(p -> p.getContatosEmergencia() != null && !p.getContatosEmergencia().isEmpty())
                .count();

        long pacientesSemContatoEmergencia = pacientesAtivosCount - pacientesComContatoEmergencia;

        double mediaContatosEmergenciaPorPaciente = pacientesAtivosCount > 0 ?
                pacientesAtivos.stream()
                        .mapToInt(p -> p.getContatosEmergencia() != null ? p.getContatosEmergencia().size() : 0)
                        .average()
                        .orElse(0.0) : 0.0;

        // === DISTRIBUIÇÃO GEOGRÁFICA ===
        Map<String, Long> pacientesPorEstado = pacientesAtivos.stream()
                .filter(p -> p.getEndereco() != null && p.getEndereco().getEstado() != null)
                .collect(Collectors.groupingBy(p -> p.getEndereco().getEstado().toString(), Collectors.counting()));

        Map<String, Long> pacientesPorCidade = pacientesAtivos.stream()
                .filter(p -> p.getEndereco() != null && p.getEndereco().getCidade() != null)
                .collect(Collectors.groupingBy(p -> p.getEndereco().getCidade(), Collectors.counting()));

        Map<String, Long> acompanhantesPorEstado = acompanhantesAtivos.stream()
                .filter(a -> a.getEndereco() != null && a.getEndereco().getEstado() != null)
                .collect(Collectors.groupingBy(a -> a.getEndereco().getEstado().toString(), Collectors.counting()));

        Map<String, Long> acompanhantesPorCidade = acompanhantesAtivos.stream()
                .filter(a -> a.getEndereco() != null && a.getEndereco().getCidade() != null)
                .collect(Collectors.groupingBy(a -> a.getEndereco().getCidade(), Collectors.counting()));

        // === TOP LISTAS ===
        List<EstatisticasPacienteAcompanhanteDTO.CidadeEstatisticaDTO> topCidadesComMaisPacientes =
                calcularTopCidades(pacientesPorCidade, pacientesAtivosCount, true);

        List<EstatisticasPacienteAcompanhanteDTO.CidadeEstatisticaDTO> topCidadesComMaisAcompanhantes =
                calcularTopCidades(acompanhantesPorCidade, acompanhantesAtivosCount, false);

        // === TAXAS E PERCENTUAIS ===
        double taxaPacientesAtivos = totalPacientes > 0 ?
                (pacientesAtivosCount * 100.0) / totalPacientes : 0.0;

        double taxaAcompanhantesAtivos = totalAcompanhantes > 0 ?
                (acompanhantesAtivosCount * 100.0) / totalAcompanhantes : 0.0;

        double taxaPacientesComAcompanhante = pacientesAtivosCount > 0 ?
                ((pacientesAtivosCount - pacientesSemAcompanhante) * 100.0) / pacientesAtivosCount : 0.0;

        double taxaPacientesComDadosClinicosPerc = pacientesAtivosCount > 0 ?
                (pacientesComDadosClinicos * 100.0) / pacientesAtivosCount : 0.0;

        // === TENDÊNCIAS TEMPORAIS ===
        List<EstatisticasPacienteAcompanhanteDTO.RegistroMensalDTO> registrosPacientesPorMes =
                calcularRegistrosPorMes(pacientesAtivos);

        List<EstatisticasPacienteAcompanhanteDTO.RegistroMensalDTO> registrosAcompanhantesPorMes =
                calcularRegistrosPorMes(todosAcompanhantes.stream()
                        .map(a -> (Object) a)
                        .collect(Collectors.toList()));

        return EstatisticasPacienteAcompanhanteDTO.builder()
                // Estatísticas gerais de pacientes
                .totalPacientes(totalPacientes)
                .pacientesAtivos(pacientesAtivosCount)
                .pacientesInativos(pacientesInativosCount)
                .pacientesRegistradosHoje(pacientesRegistradosHoje)
                .pacientesRegistradosSemana(pacientesRegistradosSemana)
                .pacientesRegistradosMes(pacientesRegistradosMes)
                .pacientesRegistradosAno(pacientesRegistradosAno)
                // Estatísticas por status de paciente
                .pacientesEmTratamento(pacientesEmTratamento)
                .pacientesCurados(pacientesCurados)
                .pacientesEmObservacao(pacientesEmObservacao)
                .pacientesFalecidos(pacientesFalecidos)
                // Estatísticas gerais de acompanhantes
                .totalAcompanhantes(totalAcompanhantes)
                .acompanhantesAtivos(acompanhantesAtivosCount)
                .acompanhantesInativos(acompanhantesInativosCount)
                .acompanhantesRegistradosHoje(acompanhantesRegistradosHoje)
                .acompanhantesRegistradosSemana(acompanhantesRegistradosSemana)
                .acompanhantesRegistradosMes(acompanhantesRegistradosMes)
                .acompanhantesRegistradosAno(acompanhantesRegistradosAno)
                // Estatísticas de relacionamento
                .mediaAcompanhantesPorPaciente(Math.round(mediaAcompanhantesPorPaciente * 100.0) / 100.0)
                .pacientesSemAcompanhante(pacientesSemAcompanhante)
                .pacientesComUmAcompanhante(pacientesComUmAcompanhante)
                .pacientesComMultiplosAcompanhantes(pacientesComMultiplosAcompanhantes)
                .maxAcompanhantesPorPaciente(maxAcompanhantesPorPaciente)
                // Distribuição por parentesco
                .acompanhantesPorParentesco(acompanhantesPorParentesco)
                .parentescoMaisComum(parentescoMaisComum)
                // Dados clínicos
                .pacientesComDadosClinicos(pacientesComDadosClinicos)
                .pacientesSemDadosClinicos(pacientesSemDadosClinicos)
                .pacientesComSonda(pacientesComSonda)
                .pacientesComCurativo(pacientesComCurativo)
                .pacientesPorTipoSonda(pacientesPorTipoSonda)
                // Informações hospitalares
                .pacientesComInformacaoHospitalar(pacientesComInformacaoHospitalar)
                .pacientesSemInformacaoHospitalar(pacientesSemInformacaoHospitalar)
                // Acompanhantes que podem ajudar na cozinha
                .acompanhantesPodemAjudarCozinha(acompanhantesPodemAjudarCozinha)
                .acompanhantesNaoPodemAjudarCozinha(acompanhantesNaoPodemAjudarCozinha)
                .percentualAjudamCozinha(Math.round(percentualAjudamCozinha * 100.0) / 100.0)
                // Contatos de emergência
                .pacientesComContatoEmergencia(pacientesComContatoEmergencia)
                .pacientesSemContatoEmergencia(pacientesSemContatoEmergencia)
                .mediaContatosEmergenciaPorPaciente(Math.round(mediaContatosEmergenciaPorPaciente * 100.0) / 100.0)
                // Distribuição geográfica
                .pacientesPorEstado(pacientesPorEstado)
                .pacientesPorCidade(pacientesPorCidade)
                .acompanhantesPorEstado(acompanhantesPorEstado)
                .acompanhantesPorCidade(acompanhantesPorCidade)
                // Top listas
                .topCidadesComMaisPacientes(topCidadesComMaisPacientes)
                .topCidadesComMaisAcompanhantes(topCidadesComMaisAcompanhantes)
                // Taxas e percentuais
                .taxaPacientesAtivos(Math.round(taxaPacientesAtivos * 100.0) / 100.0)
                .taxaAcompanhantesAtivos(Math.round(taxaAcompanhantesAtivos * 100.0) / 100.0)
                .taxaPacientesComAcompanhante(Math.round(taxaPacientesComAcompanhante * 100.0) / 100.0)
                .taxaPacientesComDadosClinicos(Math.round(taxaPacientesComDadosClinicosPerc * 100.0) / 100.0)
                // Tendências temporais
                .registrosPacientesPorMes(registrosPacientesPorMes)
                .registrosAcompanhantesPorMes(registrosAcompanhantesPorMes)
                // Metadados
                .dataHoraConsulta(agora)
                .periodoAnalisado("Últimos 12 meses")
                .build();
    }

    private List<EstatisticasPacienteAcompanhanteDTO.CidadeEstatisticaDTO> calcularTopCidades(
            Map<String, Long> cidadesMap, long total, boolean isPaciente) {
        return cidadesMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> EstatisticasPacienteAcompanhanteDTO.CidadeEstatisticaDTO.builder()
                        .cidade(entry.getKey())
                        .estado("") // Estado seria necessário mapear da lista original
                        .totalPacientes(isPaciente ? entry.getValue() : 0L)
                        .totalAcompanhantes(!isPaciente ? entry.getValue() : 0L)
                        .percentualTotal(total > 0 ? Math.round((entry.getValue() * 10000.0) / total) / 100.0 : 0.0)
                        .build())
                .collect(Collectors.toList());
    }

    private List<EstatisticasPacienteAcompanhanteDTO.RegistroMensalDTO> calcularRegistrosPorMes(List<?> entidades) {
        LocalDate hoje = LocalDate.now();
        LocalDate umAnoAtras = hoje.minusMonths(12);

        Map<String, EstatisticasPacienteAcompanhanteDTO.RegistroMensalDTO> registrosPorMes = new HashMap<>();

        entidades.stream()
                .filter(e -> {
                    if (e instanceof Paciente) {
                        LocalDateTime createdAt = ((Paciente) e).getCreatedAt();
                        return createdAt != null && !createdAt.toLocalDate().isBefore(umAnoAtras);
                    } else if (e instanceof Acompanhante) {
                        LocalDateTime createdAt = ((Acompanhante) e).getCreatedAt();
                        return createdAt != null && !createdAt.toLocalDate().isBefore(umAnoAtras);
                    }
                    return false;
                })
                .forEach(e -> {
                    final LocalDateTime createdAt;
                    if (e instanceof Paciente) {
                        createdAt = ((Paciente) e).getCreatedAt();
                    } else if (e instanceof Acompanhante) {
                        createdAt = ((Acompanhante) e).getCreatedAt();
                    } else {
                        return;
                    }

                    if (createdAt != null) {
                        int ano = createdAt.getYear();
                        int mes = createdAt.getMonthValue();
                        String chave = ano + "-" + String.format("%02d", mes);

                        registrosPorMes.computeIfAbsent(chave, k ->
                                EstatisticasPacienteAcompanhanteDTO.RegistroMensalDTO.builder()
                                        .ano(ano)
                                        .mes(mes)
                                        .mesNome(createdAt.getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR")))
                                        .totalRegistros(0L)
                                        .build()
                        );

                        registrosPorMes.get(chave).setTotalRegistros(
                                registrosPorMes.get(chave).getTotalRegistros() + 1
                        );
                    }
                });

        return registrosPorMes.values().stream()
                .sorted(Comparator.comparing(EstatisticasPacienteAcompanhanteDTO.RegistroMensalDTO::getAno)
                        .thenComparing(EstatisticasPacienteAcompanhanteDTO.RegistroMensalDTO::getMes))
                .collect(Collectors.toList());
    }
}
