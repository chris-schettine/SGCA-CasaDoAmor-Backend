package br.com.casadoamor.sgca.modules.agendamento.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.agendamento.dto.EstatisticasAgendamentoDTO;
import br.com.casadoamor.sgca.modules.agendamento.entity.AgendamentoAcompanhante;
import br.com.casadoamor.sgca.modules.agendamento.entity.AgendamentoPaciente;
import br.com.casadoamor.sgca.modules.agendamento.entity.TipoServico;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.CategoriaServico;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Prioridade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.TipoAtendimento;
import br.com.casadoamor.sgca.modules.agendamento.repository.AgendamentoAcompanhanteRepository;
import br.com.casadoamor.sgca.modules.agendamento.repository.AgendamentoPacienteRepository;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.TipoUsuario;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@ExtendWith(MockitoExtension.class)
class EstatisticasAgendamentoServiceTest {

    @Mock
    private AgendamentoPacienteRepository agendamentoPacienteRepository;

    @Mock
    private AgendamentoAcompanhanteRepository agendamentoAcompanhanteRepository;

    @InjectMocks
    private EstatisticasAgendamentoService estatisticasAgendamentoService;

    private Paciente paciente;
    private Acompanhante acompanhante;
    private AuthUsuario profissional;
    private AuthUsuario profissional2;
    private TipoServico tipoServico;
    private TipoServico tipoServico2;
    private Hospedagem hospedagem;
    private DadoPessoal dadoPessoalPaciente;
    private DadoPessoal dadoPessoalAcompanhante;

    private List<AgendamentoPaciente> agendamentosPacientes;
    private List<AgendamentoAcompanhante> agendamentosAcompanhantes;

    @BeforeEach
    void setup() {
        String pacienteId = UUID.randomUUID().toString();
        String acompanhanteId = UUID.randomUUID().toString();

        dadoPessoalPaciente = new DadoPessoal();
        dadoPessoalPaciente.setId(UUID.randomUUID().toString());
        dadoPessoalPaciente.setNome("João Silva");
        dadoPessoalPaciente.setCpf("12345678901");
        dadoPessoalPaciente.setRg("123456789");
        dadoPessoalPaciente.setDataNascimento(LocalDate.of(1990, 1, 1));

        dadoPessoalAcompanhante = new DadoPessoal();
        dadoPessoalAcompanhante.setId(UUID.randomUUID().toString());
        dadoPessoalAcompanhante.setNome("Maria Santos");
        dadoPessoalAcompanhante.setCpf("98765432100");
        dadoPessoalAcompanhante.setRg("987654321");
        dadoPessoalAcompanhante.setDataNascimento(LocalDate.of(1985, 5, 15));

        paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setEmail("joao@email.com");
        paciente.setDadoPessoal(dadoPessoalPaciente);

        acompanhante = Acompanhante.builder()
                .podeAjudarNaCozinha(true)
                .dadoPessoal(dadoPessoalAcompanhante)
                .ativo(true)
                .parentesco(Parentesco.MAE)
                .paciente(paciente)
                .build();
        acompanhante.setId(acompanhanteId);

        profissional = AuthUsuario.builder()
                .id(1L)
                .nome("Dr. Carlos")
                .cpf("11122233344")
                .email("dr.carlos@email.com")
                .tipo(TipoUsuario.MEDICO)
                .ativo(true)
                .build();

        profissional2 = AuthUsuario.builder()
                .id(2L)
                .nome("Dra. Ana")
                .cpf("55566677788")
                .email("dra.ana@email.com")
                .tipo(TipoUsuario.ENFERMEIRO)
                .ativo(true)
                .build();

        tipoServico = TipoServico.builder()
                .id(1L)
                .codigo("CONS_MED")
                .nome("Consulta Médica")
                .categoria(CategoriaServico.MEDICO)
                .duracaoMinutos(30)
                .ativo(true)
                .build();

        tipoServico2 = TipoServico.builder()
                .id(2L)
                .codigo("EXAM_ENF")
                .nome("Exame de Enfermagem")
                .categoria(CategoriaServico.ENFERMAGEM)
                .duracaoMinutos(15)
                .ativo(true)
                .build();

        hospedagem = Hospedagem.builder()
                .id(1L)
                .uuid(UUID.randomUUID().toString())
                .paciente(paciente)
                .build();

        agendamentosPacientes = new ArrayList<>();
        agendamentosAcompanhantes = new ArrayList<>();
    }

    private AgendamentoPaciente criarAgendamentoPaciente(
            Long id, 
            StatusAgendamento status, 
            Prioridade prioridade,
            TipoAtendimento tipoAtendimento,
            AuthUsuario prof,
            TipoServico servico,
            LocalDateTime inicio,
            boolean geradoAutomaticamente,
            boolean confirmadoPaciente,
            boolean confirmadoProfissional) {
        
        return AgendamentoPaciente.builder()
                .id(id)
                .uuid(UUID.randomUUID().toString())
                .paciente(paciente)
                .profissionalUsuario(prof)
                .tipoServico(servico)
                .hospedagem(hospedagem)
                .dataHoraInicio(inicio)
                .dataHoraFim(inicio.plusMinutes(30))
                .duracaoMinutos(30)
                .status(status)
                .tipoAtendimento(tipoAtendimento)
                .prioridade(prioridade)
                .confirmadoPaciente(confirmadoPaciente)
                .confirmadoProfissional(confirmadoProfissional)
                .geradoAutomaticamente(geradoAutomaticamente)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AgendamentoAcompanhante criarAgendamentoAcompanhante(
            Long id,
            StatusAgendamento status,
            Prioridade prioridade,
            TipoAtendimento tipoAtendimento,
            AuthUsuario prof,
            TipoServico servico,
            LocalDateTime inicio,
            boolean confirmadoAcompanhante,
            boolean confirmadoProfissional) {
        
        return AgendamentoAcompanhante.builder()
                .id(id)
                .uuid(UUID.randomUUID().toString())
                .acompanhante(acompanhante)
                .profissionalUsuario(prof)
                .tipoServico(servico)
                .pacienteVinculado(paciente)
                .dataHoraInicio(inicio)
                .dataHoraFim(inicio.plusMinutes(30))
                .duracaoMinutos(30)
                .status(status)
                .tipoAtendimento(tipoAtendimento)
                .prioridade(prioridade)
                .confirmadoAcompanhante(confirmadoAcompanhante)
                .confirmadoProfissional(confirmadoProfissional)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ===================== TESTES BÁSICOS =====================

    @Nested
    @DisplayName("Testes Básicos de Estatísticas")
    class EstatisticasBasicasTests {

        @Test
        @DisplayName("Deve retornar estatísticas vazias quando não há agendamentos")
        void obterEstatisticasGerais_SemAgendamentos_ReturnsZeros() {
            when(agendamentoPacienteRepository.findAll()).thenReturn(List.of());
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas).isNotNull();
            assertThat(estatisticas.getTotalAgendamentosAtivos()).isEqualTo(0L);
            assertThat(estatisticas.getAgendamentosPacientes()).isEqualTo(0L);
            assertThat(estatisticas.getAgendamentosAcompanhantes()).isEqualTo(0L);
            assertThat(estatisticas.getTaxaComparecimento()).isEqualTo(0.0);
            assertThat(estatisticas.getDataHoraConsulta()).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar total correto de agendamentos ativos")
        void obterEstatisticasGerais_ComAgendamentos_ReturnsTotais() {
            LocalDateTime hoje = LocalDateTime.now();
            
            AgendamentoPaciente ap1 = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente ap2 = criarAgendamentoPaciente(
                2L, StatusAgendamento.CONFIRMADO, Prioridade.ALTA, 
                TipoAtendimento.RETORNO, profissional, tipoServico, 
                hoje, false, true, true);

            AgendamentoAcompanhante aa1 = criarAgendamentoAcompanhante(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional2, tipoServico2, 
                hoje, false, false);

            when(agendamentoPacienteRepository.findAll()).thenReturn(List.of(ap1, ap2));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of(aa1));

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getTotalAgendamentosAtivos()).isEqualTo(3L);
            assertThat(estatisticas.getAgendamentosPacientes()).isEqualTo(2L);
            assertThat(estatisticas.getAgendamentosAcompanhantes()).isEqualTo(1L);
        }
    }

    // ===================== TESTES DE CONTAGEM POR STATUS =====================

    @Nested
    @DisplayName("Testes de Contagem por Status")
    class ContagemPorStatusTests {

        @Test
        @DisplayName("Deve contar agendamentos por status corretamente")
        void obterEstatisticasGerais_ContaPorStatus_ReturnsCorretos() {
            LocalDateTime hoje = LocalDateTime.now();
            
            AgendamentoPaciente agendado = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente confirmado = criarAgendamentoPaciente(
                2L, StatusAgendamento.CONFIRMADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, true, true);
            
            AgendamentoPaciente concluido = criarAgendamentoPaciente(
                3L, StatusAgendamento.CONCLUIDO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje.minusDays(1), false, true, true);
            
            AgendamentoPaciente cancelado = criarAgendamentoPaciente(
                4L, StatusAgendamento.CANCELADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(agendado, confirmado, concluido, cancelado));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getAgendamentosAgendados()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosConfirmados()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosConcluidos()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosCancelados()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve contar agendamentos em atendimento e não compareceram")
        void obterEstatisticasGerais_ContaStatusEspeciais_ReturnsCorretos() {
            LocalDateTime hoje = LocalDateTime.now();
            
            AgendamentoPaciente emAtendimento = criarAgendamentoPaciente(
                1L, StatusAgendamento.EM_ATENDIMENTO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, true, true);
            
            AgendamentoPaciente naoCompareceu = criarAgendamentoPaciente(
                2L, StatusAgendamento.PACIENTE_NAO_COMPARECEU, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje.minusDays(1), false, true, true);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(emAtendimento, naoCompareceu));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getAgendamentosEmAtendimento()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosNaoCompareceram()).isEqualTo(1L);
        }
    }

    // ===================== TESTES DE CONTAGEM POR PRIORIDADE =====================

    @Nested
    @DisplayName("Testes de Contagem por Prioridade")
    class ContagemPorPrioridadeTests {

        @Test
        @DisplayName("Deve contar agendamentos por prioridade corretamente")
        void obterEstatisticasGerais_ContaPorPrioridade_ReturnsCorretos() {
            LocalDateTime hoje = LocalDateTime.now();
            
            AgendamentoPaciente urgente = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.URGENTE, 
                TipoAtendimento.EMERGENCIAL, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente alta = criarAgendamentoPaciente(
                2L, StatusAgendamento.AGENDADO, Prioridade.ALTA, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente normal = criarAgendamentoPaciente(
                3L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente baixa = criarAgendamentoPaciente(
                4L, StatusAgendamento.AGENDADO, Prioridade.BAIXA, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(urgente, alta, normal, baixa));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getAgendamentosUrgentes()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosAltaPrioridade()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosNormalPrioridade()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosBaixaPrioridade()).isEqualTo(1L);
        }
    }

    // ===================== TESTES DE CONTAGEM POR TIPO DE ATENDIMENTO =====================

    @Nested
    @DisplayName("Testes de Contagem por Tipo de Atendimento")
    class ContagemPorTipoAtendimentoTests {

        @Test
        @DisplayName("Deve contar agendamentos por tipo de atendimento")
        void obterEstatisticasGerais_ContaPorTipoAtendimento_ReturnsCorretos() {
            LocalDateTime hoje = LocalDateTime.now();
            
            AgendamentoPaciente primeiraVez = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.PRIMEIRA_VEZ, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente retorno = criarAgendamentoPaciente(
                2L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.RETORNO, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente emergencial = criarAgendamentoPaciente(
                3L, StatusAgendamento.AGENDADO, Prioridade.URGENTE, 
                TipoAtendimento.EMERGENCIAL, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente rotina = criarAgendamentoPaciente(
                4L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente triagem = criarAgendamentoPaciente(
                5L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.TRIAGEM, profissional, tipoServico, 
                hoje, false, false, false);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(primeiraVez, retorno, emergencial, rotina, triagem));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getAgendamentosPrimeiraVez()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosRetorno()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosEmergenciais()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosRotina()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosTriagem()).isEqualTo(1L);
        }
    }

    // ===================== TESTES DE CONFIRMAÇÕES =====================

    @Nested
    @DisplayName("Testes de Confirmações")
    class ConfirmacoesTests {

        @Test
        @DisplayName("Deve contar confirmações corretamente")
        void obterEstatisticasGerais_ContaConfirmacoes_ReturnsCorretos() {
            LocalDateTime hoje = LocalDateTime.now();
            
            // Pendente confirmação (ambos false)
            AgendamentoPaciente pendente = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            // Confirmado pelo paciente
            AgendamentoPaciente confirmadoPaciente = criarAgendamentoPaciente(
                2L, StatusAgendamento.CONFIRMADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, true, false);
            
            // Confirmado pelo profissional
            AgendamentoPaciente confirmadoProf = criarAgendamentoPaciente(
                3L, StatusAgendamento.CONFIRMADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, true);
            
            // Confirmado por ambos
            AgendamentoPaciente confirmadoAmbos = criarAgendamentoPaciente(
                4L, StatusAgendamento.CONFIRMADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, true, true);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(pendente, confirmadoPaciente, confirmadoProf, confirmadoAmbos));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getAgendamentosPendentesConfirmacao()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosConfirmadosPaciente()).isEqualTo(2L); // confirmadoPaciente + confirmadoAmbos
            assertThat(estatisticas.getAgendamentosConfirmadosProfissional()).isEqualTo(2L); // confirmadoProf + confirmadoAmbos
            assertThat(estatisticas.getAgendamentosConfirmadosAmbos()).isEqualTo(1L);
        }
    }

    // ===================== TESTES DE AGENDAMENTOS AUTOMÁTICOS =====================

    @Nested
    @DisplayName("Testes de Agendamentos Automáticos")
    class AgendamentosAutomaticosTests {

        @Test
        @DisplayName("Deve contar agendamentos automáticos")
        void obterEstatisticasGerais_ContaAutomaticos_ReturnsCorretos() {
            LocalDateTime hoje = LocalDateTime.now();
            
            AgendamentoPaciente automatico = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, true, false, false);
            
            AgendamentoPaciente manual = criarAgendamentoPaciente(
                2L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(automatico, manual));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getAgendamentosAutomaticos()).isEqualTo(1L);
        }
    }

    // ===================== TESTES DE TAXAS =====================

    @Nested
    @DisplayName("Testes de Taxas")
    class TaxasTests {

        @Test
        @DisplayName("Deve calcular taxa de comparecimento corretamente")
        void obterEstatisticasGerais_CalculaTaxaComparecimento_ReturnsCorreto() {
            LocalDateTime ontem = LocalDateTime.now().minusDays(1);
            
            // 3 concluídos
            AgendamentoPaciente concluido1 = criarAgendamentoPaciente(
                1L, StatusAgendamento.CONCLUIDO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                ontem, false, true, true);
            
            AgendamentoPaciente concluido2 = criarAgendamentoPaciente(
                2L, StatusAgendamento.CONCLUIDO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                ontem, false, true, true);
            
            AgendamentoPaciente concluido3 = criarAgendamentoPaciente(
                3L, StatusAgendamento.CONCLUIDO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                ontem, false, true, true);
            
            // 1 não compareceu
            AgendamentoPaciente naoCompareceu = criarAgendamentoPaciente(
                4L, StatusAgendamento.PACIENTE_NAO_COMPARECEU, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                ontem, false, true, true);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(concluido1, concluido2, concluido3, naoCompareceu));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            // Taxa de comparecimento: 3 / 4 = 75%
            assertThat(estatisticas.getTaxaComparecimento()).isEqualTo(75.0);
            // Taxa de não comparecimento: 1 / 4 = 25%
            assertThat(estatisticas.getTaxaNaoComparecimento()).isEqualTo(25.0);
        }

        @Test
        @DisplayName("Deve calcular taxa de cancelamento corretamente")
        void obterEstatisticasGerais_CalculaTaxaCancelamento_ReturnsCorreto() {
            LocalDateTime hoje = LocalDateTime.now();
            
            // 1 cancelado de 5 total = 20%
            AgendamentoPaciente agendado1 = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente agendado2 = criarAgendamentoPaciente(
                2L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente confirmado = criarAgendamentoPaciente(
                3L, StatusAgendamento.CONFIRMADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, true, true);
            
            AgendamentoPaciente concluido = criarAgendamentoPaciente(
                4L, StatusAgendamento.CONCLUIDO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje.minusDays(1), false, true, true);
            
            AgendamentoPaciente cancelado = criarAgendamentoPaciente(
                5L, StatusAgendamento.CANCELADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(agendado1, agendado2, confirmado, concluido, cancelado));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            // Taxa de cancelamento: 1 / 5 = 20%
            assertThat(estatisticas.getTaxaCancelamento()).isEqualTo(20.0);
        }
    }

    // ===================== TESTES DE DURAÇÃO MÉDIA =====================

    @Nested
    @DisplayName("Testes de Duração Média")
    class DuracaoMediaTests {

        @Test
        @DisplayName("Deve calcular duração média corretamente")
        void obterEstatisticasGerais_CalculaDuracaoMedia_ReturnsCorreto() {
            LocalDateTime hoje = LocalDateTime.now();
            
            // Agendamento de 30 minutos
            AgendamentoPaciente ap1 = criarAgendamentoPaciente(
                1L, StatusAgendamento.CONCLUIDO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje.minusDays(1), false, true, true);
            
            // Agendamento de 60 minutos
            AgendamentoPaciente ap2 = AgendamentoPaciente.builder()
                .id(2L)
                .uuid(UUID.randomUUID().toString())
                .paciente(paciente)
                .profissionalUsuario(profissional)
                .tipoServico(tipoServico)
                .hospedagem(hospedagem)
                .dataHoraInicio(hoje.minusDays(1).withHour(14))
                .dataHoraFim(hoje.minusDays(1).withHour(15)) // 60 minutos
                .duracaoMinutos(60)
                .status(StatusAgendamento.CONCLUIDO)
                .tipoAtendimento(TipoAtendimento.ROTINA)
                .prioridade(Prioridade.NORMAL)
                .confirmadoPaciente(true)
                .confirmadoProfissional(true)
                .geradoAutomaticamente(false)
                .createdAt(LocalDateTime.now())
                .build();

            when(agendamentoPacienteRepository.findAll()).thenReturn(List.of(ap1, ap2));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            // Duração média: (30 + 60) / 2 = 45 minutos
            assertThat(estatisticas.getDuracaoMediaMinutos()).isEqualTo(45.0);
        }
    }

    // ===================== TESTES DE FILTRAGEM DE DELETADOS =====================

    @Nested
    @DisplayName("Testes de Filtragem de Deletados")
    class FiltragemDeletadosTests {

        @Test
        @DisplayName("Deve filtrar agendamentos deletados")
        void obterEstatisticasGerais_FiltraDeletados_ReturnsCorretos() {
            LocalDateTime hoje = LocalDateTime.now();
            
            AgendamentoPaciente ativo = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoPaciente deletado = criarAgendamentoPaciente(
                2L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            deletado.setDeletedAt(LocalDateTime.now());

            when(agendamentoPacienteRepository.findAll()).thenReturn(List.of(ativo, deletado));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            // Apenas 1 ativo
            assertThat(estatisticas.getTotalAgendamentosAtivos()).isEqualTo(1L);
        }
    }

    // ===================== TESTES DE TOP PROFISSIONAIS =====================

    @Nested
    @DisplayName("Testes de Top Profissionais")
    class TopProfissionaisTests {

        @Test
        @DisplayName("Deve calcular top profissionais por agendamentos")
        void obterEstatisticasGerais_CalculaTopProfissionais_ReturnsCorretos() {
            LocalDateTime hoje = LocalDateTime.now();
            
            // Profissional 1: 3 agendamentos
            AgendamentoPaciente ap1 = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            AgendamentoPaciente ap2 = criarAgendamentoPaciente(
                2L, StatusAgendamento.CONCLUIDO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje.minusDays(1), false, true, true);
            AgendamentoPaciente ap3 = criarAgendamentoPaciente(
                3L, StatusAgendamento.CONCLUIDO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje.minusDays(2), false, true, true);
            
            // Profissional 2: 1 agendamento
            AgendamentoPaciente ap4 = criarAgendamentoPaciente(
                4L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional2, tipoServico2, 
                hoje, false, false, false);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(ap1, ap2, ap3, ap4));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getTopProfissionaisPorAgendamentos()).isNotEmpty();
            assertThat(estatisticas.getTopProfissionaisPorAgendamentos().get(0).getProfissionalId())
                    .isEqualTo(profissional.getId());
            assertThat(estatisticas.getTopProfissionaisPorAgendamentos().get(0).getTotalAgendamentos())
                    .isEqualTo(3L);
        }
    }

    // ===================== TESTES DE TOP SERVIÇOS =====================

    @Nested
    @DisplayName("Testes de Top Serviços")
    class TopServicosTests {

        @Test
        @DisplayName("Deve calcular top serviços mais solicitados")
        void obterEstatisticasGerais_CalculaTopServicos_ReturnsCorretos() {
            LocalDateTime hoje = LocalDateTime.now();
            
            // TipoServico 1: 2 agendamentos
            AgendamentoPaciente ap1 = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            AgendamentoPaciente ap2 = criarAgendamentoPaciente(
                2L, StatusAgendamento.CONCLUIDO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje.minusDays(1), false, true, true);
            
            // TipoServico 2: 1 agendamento
            AgendamentoPaciente ap3 = criarAgendamentoPaciente(
                3L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico2, 
                hoje, false, false, false);

            when(agendamentoPacienteRepository.findAll())
                    .thenReturn(List.of(ap1, ap2, ap3));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getTopServicosMaisSolicitados()).isNotEmpty();
            assertThat(estatisticas.getTopServicosMaisSolicitados().get(0).getServicoId())
                    .isEqualTo(tipoServico.getId());
            assertThat(estatisticas.getTopServicosMaisSolicitados().get(0).getTotalAgendamentos())
                    .isEqualTo(2L);
        }
    }

    // ===================== TESTES DE DISTRIBUIÇÃO POR DIA DA SEMANA =====================

    @Nested
    @DisplayName("Testes de Distribuição por Dia da Semana")
    class DistribuicaoPorDiaSemanaTests {

        @Test
        @DisplayName("Deve calcular distribuição por dia da semana")
        void obterEstatisticasGerais_CalculaDistribuicaoDiaSemana_ReturnsMap() {
            LocalDateTime hoje = LocalDateTime.now();
            
            AgendamentoPaciente ap1 = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);

            when(agendamentoPacienteRepository.findAll()).thenReturn(List.of(ap1));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getAgendamentosPorDiaSemana()).isNotNull();
            assertThat(estatisticas.getAgendamentosPorDiaSemana()).hasSize(7);
        }
    }

    // ===================== TESTES DE DISTRIBUIÇÃO POR HORA =====================

    @Nested
    @DisplayName("Testes de Distribuição por Hora")
    class DistribuicaoPorHoraTests {

        @Test
        @DisplayName("Deve calcular distribuição por hora")
        void obterEstatisticasGerais_CalculaDistribuicaoPorHora_ReturnsMap() {
            LocalDateTime hoje = LocalDateTime.now().withHour(14);
            
            AgendamentoPaciente ap1 = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);

            when(agendamentoPacienteRepository.findAll()).thenReturn(List.of(ap1));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of());

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getAgendamentosPorHora()).isNotNull();
            assertThat(estatisticas.getAgendamentosPorHora()).hasSize(24);
            assertThat(estatisticas.getAgendamentosPorHora().get(14)).isEqualTo(1L);
        }
    }

    // ===================== TESTES COMBINADOS =====================

    @Nested
    @DisplayName("Testes Combinados (Pacientes + Acompanhantes)")
    class TestesCombinados {

        @Test
        @DisplayName("Deve somar estatísticas de pacientes e acompanhantes")
        void obterEstatisticasGerais_SomaPacientesEAcompanhantes_ReturnsCorreto() {
            LocalDateTime hoje = LocalDateTime.now();
            
            AgendamentoPaciente ap = criarAgendamentoPaciente(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional, tipoServico, 
                hoje, false, false, false);
            
            AgendamentoAcompanhante aa = criarAgendamentoAcompanhante(
                1L, StatusAgendamento.AGENDADO, Prioridade.NORMAL, 
                TipoAtendimento.ROTINA, profissional2, tipoServico2, 
                hoje, false, false);

            when(agendamentoPacienteRepository.findAll()).thenReturn(List.of(ap));
            when(agendamentoAcompanhanteRepository.findAll()).thenReturn(List.of(aa));

            EstatisticasAgendamentoDTO estatisticas = estatisticasAgendamentoService.obterEstatisticasGerais();

            assertThat(estatisticas.getTotalAgendamentosAtivos()).isEqualTo(2L);
            assertThat(estatisticas.getAgendamentosPacientes()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosAcompanhantes()).isEqualTo(1L);
            assertThat(estatisticas.getAgendamentosAgendados()).isEqualTo(2L);
        }
    }
}
