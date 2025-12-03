package br.com.casadoamor.sgca.modules.agendamento.controller;

import br.com.casadoamor.sgca.modules.agendamento.dto.EstatisticasAgendamentoDTO;
import br.com.casadoamor.sgca.modules.agendamento.service.EstatisticasAgendamentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstatisticasAgendamentoControllerTest {

    @Mock
    private EstatisticasAgendamentoService estatisticasAgendamentoService;

    @InjectMocks
    private EstatisticasAgendamentoController controller;

    private EstatisticasAgendamentoDTO createEstatisticasDTO() {
        EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO profissional = 
                EstatisticasAgendamentoDTO.ProfissionalEstatisticaDTO.builder()
                        .profissionalId(10L)
                        .profissionalNome("Dr. João")
                        .especialidade("Clínico Geral")
                        .totalAgendamentos(50L)
                        .agendamentosConcluidos(45L)
                        .taxaConclusao(90.0)
                        .build();

        EstatisticasAgendamentoDTO.ServicoEstatisticaDTO servico = 
                EstatisticasAgendamentoDTO.ServicoEstatisticaDTO.builder()
                        .servicoId(1L)
                        .servicoNome("Consulta Médica")
                        .categoria("SAUDE")
                        .totalAgendamentos(100L)
                        .percentualTotal(25.0)
                        .build();

        return EstatisticasAgendamentoDTO.builder()
                // Estatísticas gerais
                .totalAgendamentosAtivos(150L)
                .totalAgendamentosHoje(10L)
                .totalAgendamentosSemana(45L)
                .totalAgendamentosMes(180L)
                .totalAgendamentosAno(2000L)
                // Por status
                .agendamentosAgendados(50L)
                .agendamentosConfirmados(30L)
                .agendamentosEmAtendimento(5L)
                .agendamentosConcluidos(100L)
                .agendamentosCancelados(15L)
                .agendamentosNaoCompareceram(10L)
                // Por tipo
                .agendamentosPacientes(120L)
                .agendamentosAcompanhantes(30L)
                .agendamentosAutomaticos(20L)
                // Por prioridade
                .agendamentosUrgentes(5L)
                .agendamentosAltaPrioridade(15L)
                .agendamentosNormalPrioridade(100L)
                .agendamentosBaixaPrioridade(30L)
                // Por tipo de atendimento
                .agendamentosPrimeiraVez(40L)
                .agendamentosRetorno(80L)
                .agendamentosEmergenciais(5L)
                .agendamentosRotina(20L)
                .agendamentosTriagem(5L)
                // Confirmações
                .agendamentosPendentesConfirmacao(20L)
                .agendamentosConfirmadosPaciente(50L)
                .agendamentosConfirmadosProfissional(45L)
                .agendamentosConfirmadosAmbos(40L)
                // Top profissionais
                .topProfissionaisPorAgendamentos(List.of(profissional))
                .topProfissionaisPorConcluidos(List.of(profissional))
                // Top serviços
                .topServicosMaisSolicitados(List.of(servico))
                // Distribuição por dia
                .agendamentosPorDiaSemana(Map.of(
                        "SEGUNDA", 30L,
                        "TERCA", 28L,
                        "QUARTA", 32L,
                        "QUINTA", 25L,
                        "SEXTA", 35L
                ))
                // Distribuição por hora
                .agendamentosPorHora(Map.of(
                        8, 10L,
                        9, 20L,
                        10, 25L,
                        11, 20L,
                        14, 30L,
                        15, 25L,
                        16, 20L
                ))
                // Taxas
                .taxaComparecimento(85.5)
                .taxaNaoComparecimento(6.7)
                .taxaCancelamento(7.8)
                // Tempo médio
                .duracaoMediaMinutos(25.5)
                .tempoMedioEsperaMinutos(8.3)
                // Metadados
                .dataHoraConsulta(LocalDateTime.now())
                .periodoAnalisado("2025-01-01 a 2025-12-31")
                .build();
    }

    @Test
    void obterEstatisticasGerais_success() {
        EstatisticasAgendamentoDTO dto = createEstatisticasDTO();
        when(estatisticasAgendamentoService.obterEstatisticasGerais()).thenReturn(dto);

        ResponseEntity<EstatisticasAgendamentoDTO> response = controller.obterEstatisticasGerais();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        // Verifica estatísticas gerais
        assertThat(response.getBody().getTotalAgendamentosAtivos()).isEqualTo(150L);
        assertThat(response.getBody().getTotalAgendamentosHoje()).isEqualTo(10L);
        assertThat(response.getBody().getTotalAgendamentosSemana()).isEqualTo(45L);
        assertThat(response.getBody().getTotalAgendamentosMes()).isEqualTo(180L);
        
        // Verifica estatísticas por status
        assertThat(response.getBody().getAgendamentosAgendados()).isEqualTo(50L);
        assertThat(response.getBody().getAgendamentosConcluidos()).isEqualTo(100L);
        assertThat(response.getBody().getAgendamentosCancelados()).isEqualTo(15L);
        
        // Verifica estatísticas por tipo
        assertThat(response.getBody().getAgendamentosPacientes()).isEqualTo(120L);
        assertThat(response.getBody().getAgendamentosAcompanhantes()).isEqualTo(30L);
        
        // Verifica taxas
        assertThat(response.getBody().getTaxaComparecimento()).isEqualTo(85.5);
        assertThat(response.getBody().getTaxaCancelamento()).isEqualTo(7.8);
        
        // Verifica tempo médio
        assertThat(response.getBody().getDuracaoMediaMinutos()).isEqualTo(25.5);
        
        verify(estatisticasAgendamentoService).obterEstatisticasGerais();
    }

    @Test
    void obterEstatisticasGerais_withTopProfissionais() {
        EstatisticasAgendamentoDTO dto = createEstatisticasDTO();
        when(estatisticasAgendamentoService.obterEstatisticasGerais()).thenReturn(dto);

        ResponseEntity<EstatisticasAgendamentoDTO> response = controller.obterEstatisticasGerais();

        assertThat(response.getBody().getTopProfissionaisPorAgendamentos()).isNotEmpty();
        assertThat(response.getBody().getTopProfissionaisPorAgendamentos().get(0).getProfissionalNome())
                .isEqualTo("Dr. João");
        assertThat(response.getBody().getTopProfissionaisPorAgendamentos().get(0).getTotalAgendamentos())
                .isEqualTo(50L);
        assertThat(response.getBody().getTopProfissionaisPorAgendamentos().get(0).getTaxaConclusao())
                .isEqualTo(90.0);
    }

    @Test
    void obterEstatisticasGerais_withTopServicos() {
        EstatisticasAgendamentoDTO dto = createEstatisticasDTO();
        when(estatisticasAgendamentoService.obterEstatisticasGerais()).thenReturn(dto);

        ResponseEntity<EstatisticasAgendamentoDTO> response = controller.obterEstatisticasGerais();

        assertThat(response.getBody().getTopServicosMaisSolicitados()).isNotEmpty();
        assertThat(response.getBody().getTopServicosMaisSolicitados().get(0).getServicoNome())
                .isEqualTo("Consulta Médica");
        assertThat(response.getBody().getTopServicosMaisSolicitados().get(0).getTotalAgendamentos())
                .isEqualTo(100L);
    }

    @Test
    void obterEstatisticasGerais_withDistribuicaoPorDia() {
        EstatisticasAgendamentoDTO dto = createEstatisticasDTO();
        when(estatisticasAgendamentoService.obterEstatisticasGerais()).thenReturn(dto);

        ResponseEntity<EstatisticasAgendamentoDTO> response = controller.obterEstatisticasGerais();

        assertThat(response.getBody().getAgendamentosPorDiaSemana()).isNotEmpty();
        assertThat(response.getBody().getAgendamentosPorDiaSemana().get("SEXTA")).isEqualTo(35L);
        assertThat(response.getBody().getAgendamentosPorDiaSemana().get("SEGUNDA")).isEqualTo(30L);
    }

    @Test
    void obterEstatisticasGerais_withDistribuicaoPorHora() {
        EstatisticasAgendamentoDTO dto = createEstatisticasDTO();
        when(estatisticasAgendamentoService.obterEstatisticasGerais()).thenReturn(dto);

        ResponseEntity<EstatisticasAgendamentoDTO> response = controller.obterEstatisticasGerais();

        assertThat(response.getBody().getAgendamentosPorHora()).isNotEmpty();
        assertThat(response.getBody().getAgendamentosPorHora().get(14)).isEqualTo(30L);
        assertThat(response.getBody().getAgendamentosPorHora().get(10)).isEqualTo(25L);
    }

    @Test
    void obterEstatisticasGerais_withPrioridadeStats() {
        EstatisticasAgendamentoDTO dto = createEstatisticasDTO();
        when(estatisticasAgendamentoService.obterEstatisticasGerais()).thenReturn(dto);

        ResponseEntity<EstatisticasAgendamentoDTO> response = controller.obterEstatisticasGerais();

        assertThat(response.getBody().getAgendamentosUrgentes()).isEqualTo(5L);
        assertThat(response.getBody().getAgendamentosAltaPrioridade()).isEqualTo(15L);
        assertThat(response.getBody().getAgendamentosNormalPrioridade()).isEqualTo(100L);
        assertThat(response.getBody().getAgendamentosBaixaPrioridade()).isEqualTo(30L);
    }

    @Test
    void obterEstatisticasGerais_withConfirmacaoStats() {
        EstatisticasAgendamentoDTO dto = createEstatisticasDTO();
        when(estatisticasAgendamentoService.obterEstatisticasGerais()).thenReturn(dto);

        ResponseEntity<EstatisticasAgendamentoDTO> response = controller.obterEstatisticasGerais();

        assertThat(response.getBody().getAgendamentosPendentesConfirmacao()).isEqualTo(20L);
        assertThat(response.getBody().getAgendamentosConfirmadosPaciente()).isEqualTo(50L);
        assertThat(response.getBody().getAgendamentosConfirmadosProfissional()).isEqualTo(45L);
        assertThat(response.getBody().getAgendamentosConfirmadosAmbos()).isEqualTo(40L);
    }

    @Test
    void obterEstatisticasGerais_withTipoAtendimentoStats() {
        EstatisticasAgendamentoDTO dto = createEstatisticasDTO();
        when(estatisticasAgendamentoService.obterEstatisticasGerais()).thenReturn(dto);

        ResponseEntity<EstatisticasAgendamentoDTO> response = controller.obterEstatisticasGerais();

        assertThat(response.getBody().getAgendamentosPrimeiraVez()).isEqualTo(40L);
        assertThat(response.getBody().getAgendamentosRetorno()).isEqualTo(80L);
        assertThat(response.getBody().getAgendamentosEmergenciais()).isEqualTo(5L);
        assertThat(response.getBody().getAgendamentosRotina()).isEqualTo(20L);
        assertThat(response.getBody().getAgendamentosTriagem()).isEqualTo(5L);
    }

    @Test
    void obterEstatisticasGerais_emptyStats() {
        EstatisticasAgendamentoDTO emptyDto = EstatisticasAgendamentoDTO.builder()
                .totalAgendamentosAtivos(0L)
                .totalAgendamentosHoje(0L)
                .totalAgendamentosSemana(0L)
                .totalAgendamentosMes(0L)
                .totalAgendamentosAno(0L)
                .agendamentosAgendados(0L)
                .agendamentosConcluidos(0L)
                .agendamentosCancelados(0L)
                .taxaComparecimento(0.0)
                .taxaCancelamento(0.0)
                .dataHoraConsulta(LocalDateTime.now())
                .periodoAnalisado("Nenhum dado disponível")
                .build();

        when(estatisticasAgendamentoService.obterEstatisticasGerais()).thenReturn(emptyDto);

        ResponseEntity<EstatisticasAgendamentoDTO> response = controller.obterEstatisticasGerais();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalAgendamentosAtivos()).isEqualTo(0L);
        assertThat(response.getBody().getTaxaComparecimento()).isEqualTo(0.0);
    }
}
