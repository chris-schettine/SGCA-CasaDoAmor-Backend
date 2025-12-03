package br.com.casadoamor.sgca.modules.agendamento.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoPacienteRequestDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.AgendamentoPacienteResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.dto.ConflictCheckResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.entity.AgendamentoPaciente;
import br.com.casadoamor.sgca.modules.agendamento.entity.HorarioProfissional;
import br.com.casadoamor.sgca.modules.agendamento.entity.TipoServico;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.CategoriaServico;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Prioridade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.TipoAtendimento;
import br.com.casadoamor.sgca.modules.agendamento.repository.AgendamentoPacienteRepository;
import br.com.casadoamor.sgca.modules.agendamento.repository.BloqueioAgendaRepository;
import br.com.casadoamor.sgca.modules.agendamento.repository.HorarioProfissionalRepository;
import br.com.casadoamor.sgca.modules.agendamento.repository.TipoServicoRepository;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.TipoUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.repository.HospedagemRepository;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgendamentoPacienteServiceTest {

    @Mock
    private AgendamentoPacienteRepository agendamentoPacienteRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private TipoServicoRepository tipoServicoRepository;

    @Mock
    private AuthUsuarioRepository authUsuarioRepository;

    @Mock
    private HospedagemRepository hospedagemRepository;

    @Mock
    private BloqueioAgendaRepository bloqueioAgendaRepository;

    @Mock
    private HorarioProfissionalRepository horarioProfissionalRepository;

    @InjectMocks
    private AgendamentoPacienteService agendamentoPacienteService;

    private Paciente paciente;
    private AuthUsuario profissional;
    private TipoServico tipoServico;
    private Hospedagem hospedagem;
    private AgendamentoPaciente agendamento;
    private AgendamentoPacienteRequestDTO requestDTO;
    private String pacienteId;
    private String agendamentoUuid;
    private String hospedagemUuid;
    private DadoPessoal dadoPessoal;

    @BeforeEach
    void setup() {
        pacienteId = UUID.randomUUID().toString();
        agendamentoUuid = UUID.randomUUID().toString();
        hospedagemUuid = UUID.randomUUID().toString();

        dadoPessoal = new DadoPessoal();
        dadoPessoal.setId(UUID.randomUUID().toString());
        dadoPessoal.setNome("João Silva");
        dadoPessoal.setCpf("12345678901");
        dadoPessoal.setRg("123456789");
        dadoPessoal.setDataNascimento(LocalDate.of(1990, 1, 1));

        paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setEmail("joao@email.com");
        paciente.setDadoPessoal(dadoPessoal);

        profissional = AuthUsuario.builder()
                .id(10L)
                .nome("Dr. Carlos")
                .cpf("98765432100")
                .tipo(TipoUsuario.MEDICO)
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

        hospedagem = Hospedagem.builder()
                .id(1L)
                .uuid(hospedagemUuid)
                .paciente(paciente)
                .build();

        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime fim = inicio.plusMinutes(30);

        agendamento = AgendamentoPaciente.builder()
                .id(1L)
                .uuid(agendamentoUuid)
                .paciente(paciente)
                .profissionalUsuario(profissional)
                .tipoServico(tipoServico)
                .hospedagem(hospedagem)
                .dataHoraInicio(inicio)
                .dataHoraFim(fim)
                .duracaoMinutos(30)
                .status(StatusAgendamento.AGENDADO)
                .tipoAtendimento(TipoAtendimento.ROTINA)
                .prioridade(Prioridade.NORMAL)
                .confirmadoPaciente(false)
                .confirmadoProfissional(false)
                .geradoAutomaticamente(false)
                .build();

        requestDTO = AgendamentoPacienteRequestDTO.builder()
                .pacienteId(pacienteId)
                .profissionalUsuarioId(10L)
                .tipoServicoId(1L)
                .hospedagemId(hospedagemUuid)
                .dataHoraInicio(inicio)
                .dataHoraFim(fim)
                .duracaoMinutos(30)
                .tipoAtendimento(TipoAtendimento.ROTINA)
                .prioridade(Prioridade.NORMAL)
                .build();
    }

    // ===================== TESTES DE CRIAÇÃO =====================

    @Nested
    @DisplayName("Testes de Criação de Agendamento")
    class CriarAgendamentoTests {

        @Test
        @DisplayName("Deve criar agendamento com sucesso")
        void criar_Success_ReturnsResponseDTO() {
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(profissional));
            when(tipoServicoRepository.findById(1L)).thenReturn(Optional.of(tipoServico));
            when(bloqueioAgendaRepository.existsBloqueioNoHorario(any(), any(), any())).thenReturn(false);
            when(horarioProfissionalRepository.findHorarioDisponivel(any(), any(), any()))
                    .thenReturn(Optional.of(new HorarioProfissional()));
            when(agendamentoPacienteRepository.findByProfissionalAndPeriodo(any(), any(), any()))
                    .thenReturn(List.of());
            when(agendamentoPacienteRepository.save(any(AgendamentoPaciente.class))).thenReturn(agendamento);
            when(hospedagemRepository.findHospedagemAtivaDoPaciente(any())).thenReturn(Optional.of(hospedagem));

            AgendamentoPacienteResponseDTO response = agendamentoPacienteService.criar(requestDTO);

            assertThat(response).isNotNull();
            assertThat(response.getUuid()).isEqualTo(agendamentoUuid);
            assertThat(response.getPacienteNome()).isEqualTo("João Silva");
            assertThat(response.getProfissionalNome()).isEqualTo("Dr. Carlos");
            assertThat(response.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);

            verify(agendamentoPacienteRepository).save(any(AgendamentoPaciente.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando paciente não encontrado")
        void criar_PacienteNotFound_ThrowsException() {
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());
            when(bloqueioAgendaRepository.existsBloqueioNoHorario(any(), any(), any())).thenReturn(false);
            when(horarioProfissionalRepository.findHorarioDisponivel(any(), any(), any()))
                    .thenReturn(Optional.of(new HorarioProfissional()));
            when(agendamentoPacienteRepository.findByProfissionalAndPeriodo(any(), any(), any()))
                    .thenReturn(List.of());
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(profissional));

            assertThatThrownBy(() -> agendamentoPacienteService.criar(requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Paciente não encontrado");
        }

        @Test
        @DisplayName("Deve lançar exceção quando profissional não encontrado")
        void criar_ProfissionalNotFound_ThrowsException() {
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoPacienteService.criar(requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Profissional não encontrado");
        }

        @Test
        @DisplayName("Deve lançar exceção quando há conflito de bloqueio")
        void criar_BloqueioExistente_ThrowsException() {
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(profissional));
            when(bloqueioAgendaRepository.existsBloqueioNoHorario(any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> agendamentoPacienteService.criar(requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Conflito");
        }

        @Test
        @DisplayName("Deve lançar exceção quando há conflito de agendamento")
        void criar_ConflitoAgendamento_ThrowsException() {
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(profissional));
            when(bloqueioAgendaRepository.existsBloqueioNoHorario(any(), any(), any())).thenReturn(false);
            when(horarioProfissionalRepository.findHorarioDisponivel(any(), any(), any()))
                    .thenReturn(Optional.of(new HorarioProfissional()));
            when(agendamentoPacienteRepository.findByProfissionalAndPeriodo(any(), any(), any()))
                    .thenReturn(List.of(agendamento));

            assertThatThrownBy(() -> agendamentoPacienteService.criar(requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Conflito");
        }
    }

    // ===================== TESTES DE LISTAGEM =====================

    @Nested
    @DisplayName("Testes de Listagem de Agendamentos")
    class ListarAgendamentosTests {

        @Test
        @DisplayName("Deve listar agendamentos com paginação")
        void listar_Success_ReturnsList() {
            Page<AgendamentoPaciente> page = new PageImpl<>(List.of(agendamento));
            when(agendamentoPacienteRepository.findAll(any(PageRequest.class))).thenReturn(page);
            when(hospedagemRepository.findHospedagemAtivaDoPaciente(any())).thenReturn(Optional.of(hospedagem));

            List<AgendamentoPacienteResponseDTO> response = agendamentoPacienteService.listar(0, 10);

            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getUuid()).isEqualTo(agendamentoUuid);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há agendamentos")
        void listar_Empty_ReturnsEmptyList() {
            Page<AgendamentoPaciente> page = new PageImpl<>(List.of());
            when(agendamentoPacienteRepository.findAll(any(PageRequest.class))).thenReturn(page);

            List<AgendamentoPacienteResponseDTO> response = agendamentoPacienteService.listar(0, 10);

            assertThat(response).isNotNull();
            assertThat(response).isEmpty();
        }
    }

    // ===================== TESTES DE BUSCA POR UUID =====================

    @Nested
    @DisplayName("Testes de Busca por UUID")
    class BuscarPorUuidTests {

        @Test
        @DisplayName("Deve buscar agendamento por UUID com sucesso")
        void buscarPorUuid_Success_ReturnsResponseDTO() {
            when(agendamentoPacienteRepository.findByUuid(agendamentoUuid))
                    .thenReturn(Optional.of(agendamento));
            when(hospedagemRepository.findHospedagemAtivaDoPaciente(any())).thenReturn(Optional.of(hospedagem));

            AgendamentoPacienteResponseDTO response = agendamentoPacienteService.buscarPorUuid(agendamentoUuid);

            assertThat(response).isNotNull();
            assertThat(response.getUuid()).isEqualTo(agendamentoUuid);
            assertThat(response.getPacienteNome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Deve lançar exceção quando agendamento não encontrado")
        void buscarPorUuid_NotFound_ThrowsException() {
            when(agendamentoPacienteRepository.findByUuid("uuid-inexistente"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoPacienteService.buscarPorUuid("uuid-inexistente"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ===================== TESTES DE LISTAGEM POR PACIENTE =====================

    @Nested
    @DisplayName("Testes de Listagem por Paciente")
    class ListarPorPacienteTests {

        @Test
        @DisplayName("Deve listar agendamentos por paciente")
        void listarPorPaciente_Success_ReturnsList() {
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
            when(agendamentoPacienteRepository.findByPaciente(paciente))
                    .thenReturn(List.of(agendamento));
            when(hospedagemRepository.findHospedagemAtivaDoPaciente(any())).thenReturn(Optional.of(hospedagem));

            List<AgendamentoPacienteResponseDTO> response = 
                    agendamentoPacienteService.listarPorPaciente(pacienteId);

            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getPacienteNome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Deve lançar exceção quando paciente não encontrado")
        void listarPorPaciente_PacienteNotFound_ThrowsException() {
            when(pacienteRepository.findById("uuid-inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoPacienteService.listarPorPaciente("uuid-inexistente"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Paciente não encontrado");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando paciente não tem agendamentos")
        void listarPorPaciente_SemAgendamentos_ReturnsEmptyList() {
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
            when(agendamentoPacienteRepository.findByPaciente(paciente)).thenReturn(List.of());

            List<AgendamentoPacienteResponseDTO> response = 
                    agendamentoPacienteService.listarPorPaciente(pacienteId);

            assertThat(response).isEmpty();
        }
    }

    // ===================== TESTES DE LISTAGEM POR PROFISSIONAL =====================

    @Nested
    @DisplayName("Testes de Listagem por Profissional")
    class ListarPorProfissionalTests {

        @Test
        @DisplayName("Deve listar agendamentos por profissional e período")
        void listarPorProfissional_Success_ReturnsList() {
            LocalDateTime inicio = LocalDateTime.now();
            LocalDateTime fim = LocalDateTime.now().plusDays(7);
            
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(profissional));
            when(agendamentoPacienteRepository.findByProfissionalAndPeriodo(profissional, inicio, fim))
                    .thenReturn(List.of(agendamento));
            when(hospedagemRepository.findHospedagemAtivaDoPaciente(any())).thenReturn(Optional.of(hospedagem));

            List<AgendamentoPacienteResponseDTO> response = 
                    agendamentoPacienteService.listarPorProfissional(10L, inicio, fim);

            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getProfissionalNome()).isEqualTo("Dr. Carlos");
        }

        @Test
        @DisplayName("Deve lançar exceção quando profissional não encontrado")
        void listarPorProfissional_NotFound_ThrowsException() {
            LocalDateTime inicio = LocalDateTime.now();
            LocalDateTime fim = LocalDateTime.now().plusDays(7);
            
            when(authUsuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoPacienteService.listarPorProfissional(999L, inicio, fim))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Profissional não encontrado");
        }
    }

    // ===================== TESTES DE CONFIRMAÇÃO =====================

    @Nested
    @DisplayName("Testes de Confirmação de Agendamento")
    class ConfirmarAgendamentoTests {

        @Test
        @DisplayName("Deve confirmar agendamento pelo paciente")
        void confirmar_PeloPaciente_Success() {
            when(agendamentoPacienteRepository.findByUuid(agendamentoUuid))
                    .thenReturn(Optional.of(agendamento));
            when(agendamentoPacienteRepository.save(any(AgendamentoPaciente.class)))
                    .thenAnswer(inv -> {
                        AgendamentoPaciente saved = inv.getArgument(0);
                        saved.setStatus(StatusAgendamento.CONFIRMADO);
                        saved.setConfirmadoPaciente(true);
                        return saved;
                    });
            when(hospedagemRepository.findHospedagemAtivaDoPaciente(any())).thenReturn(Optional.of(hospedagem));

            AgendamentoPacienteResponseDTO response = agendamentoPacienteService.confirmar(agendamentoUuid, true);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
            assertThat(response.getConfirmadoPaciente()).isTrue();
        }

        @Test
        @DisplayName("Deve confirmar agendamento pelo profissional")
        void confirmar_PeloProfissional_Success() {
            when(agendamentoPacienteRepository.findByUuid(agendamentoUuid))
                    .thenReturn(Optional.of(agendamento));
            when(agendamentoPacienteRepository.save(any(AgendamentoPaciente.class)))
                    .thenAnswer(inv -> {
                        AgendamentoPaciente saved = inv.getArgument(0);
                        saved.setStatus(StatusAgendamento.CONFIRMADO);
                        saved.setConfirmadoProfissional(true);
                        return saved;
                    });
            when(hospedagemRepository.findHospedagemAtivaDoPaciente(any())).thenReturn(Optional.of(hospedagem));

            AgendamentoPacienteResponseDTO response = agendamentoPacienteService.confirmar(agendamentoUuid, false);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
            assertThat(response.getConfirmadoProfissional()).isTrue();
        }

        @Test
        @DisplayName("Deve lançar exceção ao confirmar agendamento não encontrado")
        void confirmar_NotFound_ThrowsException() {
            when(agendamentoPacienteRepository.findByUuid("uuid-inexistente"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoPacienteService.confirmar("uuid-inexistente", true))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ===================== TESTES DE CANCELAMENTO =====================

    @Nested
    @DisplayName("Testes de Cancelamento de Agendamento")
    class CancelarAgendamentoTests {

        @Test
        @DisplayName("Deve cancelar agendamento com sucesso")
        void cancelar_Success() {
            when(agendamentoPacienteRepository.findByUuid(agendamentoUuid))
                    .thenReturn(Optional.of(agendamento));
            when(agendamentoPacienteRepository.save(any(AgendamentoPaciente.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            agendamentoPacienteService.cancelar(agendamentoUuid, "Paciente solicitou cancelamento");

            verify(agendamentoPacienteRepository).save(any(AgendamentoPaciente.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao cancelar agendamento não encontrado")
        void cancelar_NotFound_ThrowsException() {
            when(agendamentoPacienteRepository.findByUuid("uuid-inexistente"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoPacienteService.cancelar("uuid-inexistente", "Motivo"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ===================== TESTES DE VERIFICAÇÃO DE CONFLITO =====================

    @Nested
    @DisplayName("Testes de Verificação de Conflito")
    class VerificarConflitoTests {

        @Test
        @DisplayName("Deve retornar sem conflito quando horário disponível")
        void verificarConflito_SemConflito_ReturnsFalse() {
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(profissional));
            when(bloqueioAgendaRepository.existsBloqueioNoHorario(any(), any(), any()))
                    .thenReturn(false);
            when(horarioProfissionalRepository.findHorarioDisponivel(any(), any(), any()))
                    .thenReturn(Optional.of(new HorarioProfissional()));
            when(agendamentoPacienteRepository.findByProfissionalAndPeriodo(any(), any(), any()))
                    .thenReturn(List.of());

            LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            LocalDateTime fim = inicio.plusMinutes(30);

            ConflictCheckResponseDTO resultado = agendamentoPacienteService.verificarConflito(10L, inicio, fim);

            assertThat(resultado.getTemConflito()).isFalse();
            assertThat(resultado.getMensagem()).contains("disponível");
        }

        @Test
        @DisplayName("Deve retornar conflito quando há bloqueio")
        void verificarConflito_ComBloqueio_ReturnsTrue() {
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(profissional));
            when(bloqueioAgendaRepository.existsBloqueioNoHorario(any(), any(), any()))
                    .thenReturn(true);

            LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            LocalDateTime fim = inicio.plusMinutes(30);

            ConflictCheckResponseDTO resultado = agendamentoPacienteService.verificarConflito(10L, inicio, fim);

            assertThat(resultado.getTemConflito()).isTrue();
            assertThat(resultado.getMensagem()).contains("bloqueio");
        }

        @Test
        @DisplayName("Deve retornar conflito quando há agendamento existente")
        void verificarConflito_ComAgendamentoExistente_ReturnsTrue() {
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(profissional));
            when(bloqueioAgendaRepository.existsBloqueioNoHorario(any(), any(), any()))
                    .thenReturn(false);
            when(horarioProfissionalRepository.findHorarioDisponivel(any(), any(), any()))
                    .thenReturn(Optional.of(new HorarioProfissional()));
            when(agendamentoPacienteRepository.findByProfissionalAndPeriodo(any(), any(), any()))
                    .thenReturn(List.of(agendamento));

            LocalDateTime inicio = agendamento.getDataHoraInicio();
            LocalDateTime fim = agendamento.getDataHoraFim();

            ConflictCheckResponseDTO resultado = agendamentoPacienteService.verificarConflito(10L, inicio, fim);

            assertThat(resultado.getTemConflito()).isTrue();
            assertThat(resultado.getMensagem()).contains("agendamento");
        }

        @Test
        @DisplayName("Deve retornar conflito quando fora do horário de trabalho")
        void verificarConflito_ForaHorarioTrabalho_ReturnsTrue() {
            when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(profissional));
            when(bloqueioAgendaRepository.existsBloqueioNoHorario(any(), any(), any()))
                    .thenReturn(false);
            when(horarioProfissionalRepository.findHorarioDisponivel(any(), any(), any()))
                    .thenReturn(Optional.empty());

            LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(22).withMinute(0);
            LocalDateTime fim = inicio.plusMinutes(30);

            ConflictCheckResponseDTO resultado = agendamentoPacienteService.verificarConflito(10L, inicio, fim);

            assertThat(resultado.getTemConflito()).isTrue();
            assertThat(resultado.getMensagem()).contains("horário");
        }
    }

    // ===================== TESTES DE LISTAGEM DE PACIENTES ELEGÍVEIS =====================

    @Nested
    @DisplayName("Testes de Pacientes Elegíveis")
    class PacientesElegiveisTests {

        @Test
        @DisplayName("Deve listar pacientes com hospedagem ativa")
        void listarPacientesElegiveis_Success_ReturnsList() {
            when(hospedagemRepository.findHospedagensAtivas()).thenReturn(List.of(hospedagem));
            when(hospedagemRepository.findHospedagemAtivaDoPaciente(any())).thenReturn(Optional.of(hospedagem));

            var response = agendamentoPacienteService.listarPacientesElegiveis();

            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há hospedagens ativas")
        void listarPacientesElegiveis_Empty_ReturnsEmptyList() {
            when(hospedagemRepository.findHospedagensAtivas()).thenReturn(List.of());

            var response = agendamentoPacienteService.listarPacientesElegiveis();

            assertThat(response).isEmpty();
        }
    }

    // ===================== TESTES DE LISTAGEM DE PROFISSIONAIS ELEGÍVEIS =====================

    @Nested
    @DisplayName("Testes de Profissionais Elegíveis")
    class ProfissionaisElegiveisTests {

        @Test
        @DisplayName("Deve listar profissionais de saúde elegíveis")
        void listarProfissionaisElegiveis_Success_ReturnsList() {
            AuthUsuario medico = AuthUsuario.builder()
                    .id(1L)
                    .nome("Dr. Silva")
                    .tipo(TipoUsuario.MEDICO)
                    .ativo(true)
                    .email("dr.silva@email.com")
                    .build();
            AuthUsuario admin = AuthUsuario.builder()
                    .id(2L)
                    .nome("Admin")
                    .tipo(TipoUsuario.ADMINISTRADOR)
                    .ativo(true)
                    .email("admin@email.com")
                    .build();
            
            when(authUsuarioRepository.findAll()).thenReturn(List.of(medico, admin));

            var response = agendamentoPacienteService.listarProfissionaisElegiveis();

            assertThat(response).isNotNull();
            assertThat(response).hasSize(1); // Apenas o médico
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há profissionais elegíveis")
        void listarProfissionaisElegiveis_Empty_ReturnsEmptyList() {
            when(authUsuarioRepository.findAll()).thenReturn(List.of());

            var response = agendamentoPacienteService.listarProfissionaisElegiveis();

            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("Deve filtrar profissionais inativos")
        void listarProfissionaisElegiveis_FiltraInativos_ReturnsList() {
            AuthUsuario medicoAtivo = AuthUsuario.builder()
                    .id(1L)
                    .nome("Dr. Silva")
                    .tipo(TipoUsuario.MEDICO)
                    .ativo(true)
                    .email("dr.silva@email.com")
                    .build();
            AuthUsuario medicoInativo = AuthUsuario.builder()
                    .id(2L)
                    .nome("Dr. Inativo")
                    .tipo(TipoUsuario.MEDICO)
                    .ativo(false)
                    .email("dr.inativo@email.com")
                    .build();
            
            when(authUsuarioRepository.findAll()).thenReturn(List.of(medicoAtivo, medicoInativo));

            var response = agendamentoPacienteService.listarProfissionaisElegiveis();

            assertThat(response).isNotNull();
            assertThat(response).hasSize(1); // Apenas o médico ativo
        }
    }
}
