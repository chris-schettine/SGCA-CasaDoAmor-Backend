package br.com.casadoamor.sgca.modules.hospedagem.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.enums.SexoEnum;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemRequestDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemResponseDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemSaidaDTO;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.repository.HospedagemRepository;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class HospedagemServiceTest {

    @Mock
    private HospedagemRepository hospedagemRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private QuartoService quartoService;

    @InjectMocks
    private HospedagemService service;

    private AuthUsuario user;
    private Paciente pacienteMock;
    private Quarto quartoMock;
    private Hospedagem hospedagemMock;

    @BeforeEach
    void init() {
        user = AuthUsuario.builder().id(1L).nome("host").build();

        DadoPessoal dadoPessoal = new DadoPessoal();
        dadoPessoal.setNome("Paciente Teste");
        dadoPessoal.setCpf("12345678900");
        dadoPessoal.setSexo(SexoEnum.FEMININO);

        pacienteMock = new Paciente();
        pacienteMock.setId("p1");
        pacienteMock.setDadoPessoal(dadoPessoal);

        quartoMock = Quarto.builder()
                .id(1L)
                .uuid("q1")
                .nome("Quarto 101")
                .codigo("FEM-1-001")
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(4)
                .capacidadeOcupada(2)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();

        hospedagemMock = Hospedagem.builder()
                .id(1L)
                .uuid("h1")
                .paciente(pacienteMock)
                .quarto(quartoMock)
                .dataEntrada(LocalDate.now().minusDays(5))
                .horaEntrada(LocalTime.of(10, 0))
                .status(StatusHospedagem.ATIVA)
                .createdBy(user)
                .build();
    }

    // ===================== REGISTRAR ENTRADA =====================

    @Test
    void registrarEntrada_throws_whenPacienteNotFound() {
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder().pacienteId("p1").dataEntrada(LocalDate.now()).build();

        when(pacienteRepository.findById("p1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarEntrada(dto, user)).isInstanceOf(EntityNotFoundException.class).hasMessageContaining("Paciente não encontrado");
    }

    @Test
    void registrarEntrada_throws_whenPacienteAlreadyActive() {
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder().pacienteId("p2").dataEntrada(LocalDate.now()).build();
        Paciente p = new Paciente();
        p.setId("p2");

        when(pacienteRepository.findById("p2")).thenReturn(Optional.of(p));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(p)).thenReturn(true);

        assertThatThrownBy(() -> service.registrarEntrada(dto, user)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Paciente já possui uma hospedagem ativa");
    }

    @Test
    void registrarEntrada_throws_whenQuartoHasNoVagas() {
        Paciente p = new Paciente();
        p.setId("p3");
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder().pacienteId("p3").quartoUuid("q1").dataEntrada(LocalDate.now()).build();

        when(pacienteRepository.findById("p3")).thenReturn(Optional.of(p));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(p)).thenReturn(false);

        Quarto q = Quarto.builder().uuid("q1").capacidadeTotal(2).capacidadeOcupada(2).ativo(true).emManutencao(false).build();
        when(quartoService.buscarPorUuid("q1")).thenReturn(q);

        assertThatThrownBy(() -> service.registrarEntrada(dto, user)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Quarto não possui vagas disponíveis");
    }

    @Test
    void registrarEntrada_success_withQuarto_incrementsOccupancy() {
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("p1")
                .quartoUuid("q1")
                .dataEntrada(LocalDate.now())
                .build();

        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(pacienteMock));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteMock)).thenReturn(false);

        Quarto q = Quarto.builder()
                .uuid("q1")
                .capacidadeTotal(4)
                .capacidadeOcupada(0)
                .ala(AlaQuarto.FEMININA)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoService.buscarPorUuid("q1")).thenReturn(q);

        Hospedagem saved = Hospedagem.builder()
                .uuid("h-new")
                .paciente(pacienteMock)
                .quarto(q)
                .dataEntrada(LocalDate.now())
                .horaEntrada(LocalTime.NOON)
                .status(StatusHospedagem.ATIVA)
                .build();
        when(hospedagemRepository.save(any())).thenReturn(saved);

        var res = service.registrarEntrada(dto, user);

        assertThat(res).isNotNull();
        assertThat(res.getUuid()).isEqualTo("h-new");
        assertThat(q.getCapacidadeOcupada()).isEqualTo(1);
        verify(hospedagemRepository).save(any());
    }

    @Test
    void registrarEntrada_success_withoutQuarto() {
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("p1")
                .quartoUuid(null)
                .dataEntrada(LocalDate.now())
                .horaEntrada(LocalTime.of(14, 30))
                .dataSaidaPrevista(LocalDate.now().plusDays(7))
                .observacoesEntrada("Observação entrada")
                .observacoesGerais("Observação geral")
                .build();

        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(pacienteMock));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteMock)).thenReturn(false);

        Hospedagem saved = Hospedagem.builder()
                .uuid("h-no-quarto")
                .paciente(pacienteMock)
                .quarto(null)
                .dataEntrada(LocalDate.now())
                .horaEntrada(LocalTime.of(14, 30))
                .status(StatusHospedagem.ATIVA)
                .build();
        when(hospedagemRepository.save(any())).thenReturn(saved);

        var res = service.registrarEntrada(dto, user);

        assertThat(res).isNotNull();
        assertThat(res.getQuartoUuid()).isNull();
    }

    @Test
    void registrarEntrada_withNullHoraEntrada_usesCurrentTime() {
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("p1")
                .quartoUuid(null)
                .dataEntrada(LocalDate.now())
                .horaEntrada(null)
                .build();

        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(pacienteMock));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteMock)).thenReturn(false);

        Hospedagem saved = Hospedagem.builder()
                .uuid("h-auto-time")
                .paciente(pacienteMock)
                .dataEntrada(LocalDate.now())
                .horaEntrada(LocalTime.now())
                .status(StatusHospedagem.ATIVA)
                .build();
        when(hospedagemRepository.save(any())).thenReturn(saved);

        var res = service.registrarEntrada(dto, user);

        assertThat(res).isNotNull();
    }

    // ===================== VALIDAR COMPATIBILIDADE GENERO =====================

    @Test
    void registrarEntrada_throws_whenGenderIncompatibleWithAlaFeminina() {
        DadoPessoal dadoMasculino = new DadoPessoal();
        dadoMasculino.setNome("Paciente Masculino");
        dadoMasculino.setSexo(SexoEnum.MASCULINO);

        Paciente pacienteMasculino = new Paciente();
        pacienteMasculino.setId("pm1");
        pacienteMasculino.setDadoPessoal(dadoMasculino);

        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("pm1")
                .quartoUuid("qf1")
                .dataEntrada(LocalDate.now())
                .build();

        when(pacienteRepository.findById("pm1")).thenReturn(Optional.of(pacienteMasculino));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteMasculino)).thenReturn(false);

        Quarto quartoFeminino = Quarto.builder()
                .uuid("qf1")
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(4)
                .capacidadeOcupada(1)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoService.buscarPorUuid("qf1")).thenReturn(quartoFeminino);

        assertThatThrownBy(() -> service.registrarEntrada(dto, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Incompatibilidade de gênero");
    }

    @Test
    void registrarEntrada_throws_whenGenderIncompatibleWithAlaMasculina() {
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("p1")
                .quartoUuid("qm1")
                .dataEntrada(LocalDate.now())
                .build();

        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(pacienteMock));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteMock)).thenReturn(false);

        Quarto quartoMasculino = Quarto.builder()
                .uuid("qm1")
                .ala(AlaQuarto.MASCULINA)
                .capacidadeTotal(4)
                .capacidadeOcupada(1)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoService.buscarPorUuid("qm1")).thenReturn(quartoMasculino);

        assertThatThrownBy(() -> service.registrarEntrada(dto, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Incompatibilidade de gênero");
    }

    @Test
    void registrarEntrada_success_withAlaMista() {
        DadoPessoal dadoMasculino = new DadoPessoal();
        dadoMasculino.setNome("Paciente Misto");
        dadoMasculino.setSexo(SexoEnum.MASCULINO);

        Paciente paciente = new Paciente();
        paciente.setId("px");
        paciente.setDadoPessoal(dadoMasculino);

        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("px")
                .quartoUuid("qmista")
                .dataEntrada(LocalDate.now())
                .build();

        when(pacienteRepository.findById("px")).thenReturn(Optional.of(paciente));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(paciente)).thenReturn(false);

        Quarto quartoMisto = Quarto.builder()
                .uuid("qmista")
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .capacidadeOcupada(1)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoService.buscarPorUuid("qmista")).thenReturn(quartoMisto);

        Hospedagem saved = Hospedagem.builder()
                .uuid("h-mista")
                .paciente(paciente)
                .quarto(quartoMisto)
                .dataEntrada(LocalDate.now())
                .horaEntrada(LocalTime.now())
                .status(StatusHospedagem.ATIVA)
                .build();
        when(hospedagemRepository.save(any())).thenReturn(saved);

        var res = service.registrarEntrada(dto, user);
        assertThat(res).isNotNull();
    }

    @Test
    void registrarEntrada_success_withAlaIsolamento() {
        DadoPessoal dadoMasculino = new DadoPessoal();
        dadoMasculino.setNome("Paciente Isolamento");
        dadoMasculino.setSexo(SexoEnum.MASCULINO);

        Paciente paciente = new Paciente();
        paciente.setId("piso");
        paciente.setDadoPessoal(dadoMasculino);

        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("piso")
                .quartoUuid("qiso")
                .dataEntrada(LocalDate.now())
                .build();

        when(pacienteRepository.findById("piso")).thenReturn(Optional.of(paciente));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(paciente)).thenReturn(false);

        Quarto quartoIsolamento = Quarto.builder()
                .uuid("qiso")
                .ala(AlaQuarto.ISOLAMENTO)
                .capacidadeTotal(1)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoService.buscarPorUuid("qiso")).thenReturn(quartoIsolamento);

        Hospedagem saved = Hospedagem.builder()
                .uuid("h-iso")
                .paciente(paciente)
                .quarto(quartoIsolamento)
                .dataEntrada(LocalDate.now())
                .horaEntrada(LocalTime.now())
                .status(StatusHospedagem.ATIVA)
                .build();
        when(hospedagemRepository.save(any())).thenReturn(saved);

        var res = service.registrarEntrada(dto, user);
        assertThat(res).isNotNull();
    }

    @Test
    void registrarEntrada_success_whenPermiteSexoOposto() {
        DadoPessoal dadoMasculino = new DadoPessoal();
        dadoMasculino.setNome("Paciente Debilitado");
        dadoMasculino.setSexo(SexoEnum.MASCULINO);

        Paciente paciente = new Paciente();
        paciente.setId("pdeb");
        paciente.setDadoPessoal(dadoMasculino);

        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("pdeb")
                .quartoUuid("q4camas")
                .dataEntrada(LocalDate.now())
                .build();

        when(pacienteRepository.findById("pdeb")).thenReturn(Optional.of(paciente));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(paciente)).thenReturn(false);

        Quarto quarto4Camas = Quarto.builder()
                .uuid("q4camas")
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(4)
                .capacidadeOcupada(1)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(true)
                .build();
        when(quartoService.buscarPorUuid("q4camas")).thenReturn(quarto4Camas);

        Hospedagem saved = Hospedagem.builder()
                .uuid("h-deb")
                .paciente(paciente)
                .quarto(quarto4Camas)
                .dataEntrada(LocalDate.now())
                .horaEntrada(LocalTime.now())
                .status(StatusHospedagem.ATIVA)
                .build();
        when(hospedagemRepository.save(any())).thenReturn(saved);

        var res = service.registrarEntrada(dto, user);
        assertThat(res).isNotNull();
    }

    @Test
    void registrarEntrada_throws_whenPacienteHasNoGender() {
        DadoPessoal dadoSemSexo = new DadoPessoal();
        dadoSemSexo.setNome("Paciente Sem Sexo");
        dadoSemSexo.setSexo(null);

        Paciente pacienteSemSexo = new Paciente();
        pacienteSemSexo.setId("pss");
        pacienteSemSexo.setDadoPessoal(dadoSemSexo);

        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("pss")
                .quartoUuid("qf")
                .dataEntrada(LocalDate.now())
                .build();

        when(pacienteRepository.findById("pss")).thenReturn(Optional.of(pacienteSemSexo));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteSemSexo)).thenReturn(false);

        Quarto quartoFeminino = Quarto.builder()
                .uuid("qf")
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(4)
                .capacidadeOcupada(1)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoService.buscarPorUuid("qf")).thenReturn(quartoFeminino);

        assertThatThrownBy(() -> service.registrarEntrada(dto, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Paciente sem gênero cadastrado");
    }

    @Test
    void registrarEntrada_throws_whenPacienteHasNoDadoPessoal() {
        Paciente pacienteSemDados = new Paciente();
        pacienteSemDados.setId("pnd");
        pacienteSemDados.setDadoPessoal(null);

        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("pnd")
                .quartoUuid("qf")
                .dataEntrada(LocalDate.now())
                .build();

        when(pacienteRepository.findById("pnd")).thenReturn(Optional.of(pacienteSemDados));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteSemDados)).thenReturn(false);

        Quarto quartoFeminino = Quarto.builder()
                .uuid("qf")
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(4)
                .capacidadeOcupada(1)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoService.buscarPorUuid("qf")).thenReturn(quartoFeminino);

        assertThatThrownBy(() -> service.registrarEntrada(dto, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Paciente sem gênero cadastrado");
    }

    @Test
    void registrarEntrada_success_withMasculinoInAlaMasculina() {
        DadoPessoal dadoMasculino = new DadoPessoal();
        dadoMasculino.setNome("Paciente Masculino");
        dadoMasculino.setSexo(SexoEnum.MASCULINO);

        Paciente pacienteMasculino = new Paciente();
        pacienteMasculino.setId("pmm");
        pacienteMasculino.setDadoPessoal(dadoMasculino);

        HospedagemRequestDTO dto = HospedagemRequestDTO.builder()
                .pacienteId("pmm")
                .quartoUuid("qm")
                .dataEntrada(LocalDate.now())
                .build();

        when(pacienteRepository.findById("pmm")).thenReturn(Optional.of(pacienteMasculino));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteMasculino)).thenReturn(false);

        Quarto quartoMasculino = Quarto.builder()
                .uuid("qm")
                .ala(AlaQuarto.MASCULINA)
                .capacidadeTotal(4)
                .capacidadeOcupada(1)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoService.buscarPorUuid("qm")).thenReturn(quartoMasculino);

        Hospedagem saved = Hospedagem.builder()
                .uuid("h-mm")
                .paciente(pacienteMasculino)
                .quarto(quartoMasculino)
                .dataEntrada(LocalDate.now())
                .horaEntrada(LocalTime.now())
                .status(StatusHospedagem.ATIVA)
                .build();
        when(hospedagemRepository.save(any())).thenReturn(saved);

        var res = service.registrarEntrada(dto, user);
        assertThat(res).isNotNull();
    }

    // ===================== REGISTRAR SAIDA =====================

    @Test
    void registrarSaida_throws_whenNotActive() {
        Hospedagem h = Hospedagem.builder().uuid("hs1").status(StatusHospedagem.ENCERRADA).build();
        when(hospedagemRepository.findByUuid("hs1")).thenReturn(Optional.of(h));

        assertThatThrownBy(() -> service.registrarSaida("hs1", new HospedagemSaidaDTO(), user)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Hospedagem não está ativa");
    }

    @Test
    void registrarSaida_throws_whenDateBeforeDataEntrada() {
        Hospedagem h = Hospedagem.builder().uuid("hs2").status(StatusHospedagem.ATIVA).dataEntrada(LocalDate.of(2024, 1, 10)).build();
        when(hospedagemRepository.findByUuid("hs2")).thenReturn(Optional.of(h));

        HospedagemSaidaDTO dto = HospedagemSaidaDTO.builder().dataSaida(LocalDate.of(2024, 1, 1)).motivoSaida("fim").build();

        assertThatThrownBy(() -> service.registrarSaida("hs2", dto, user)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Data de saída não pode ser anterior à data de entrada");
    }

    @Test
    void registrarSaida_success_decrementsQuartoAndSaves() {
        Quarto q = Quarto.builder().uuid("q3").capacidadeTotal(2).capacidadeOcupada(2).build();
        Hospedagem h = Hospedagem.builder()
                .uuid("hs3")
                .status(StatusHospedagem.ATIVA)
                .dataEntrada(LocalDate.now().minusDays(2))
                .quarto(q)
                .paciente(pacienteMock)
                .build();

        when(hospedagemRepository.findByUuid("hs3")).thenReturn(Optional.of(h));
        when(hospedagemRepository.save(any())).thenReturn(h);

        HospedagemSaidaDTO dto = HospedagemSaidaDTO.builder()
                .dataSaida(LocalDate.now())
                .horaSaida(LocalTime.of(18, 0))
                .motivoSaida("alta")
                .observacoesSaida("Paciente saiu bem")
                .build();

        var res = service.registrarSaida("hs3", dto, user);

        assertThat(res).isNotNull();
        assertThat(q.getCapacidadeOcupada()).isEqualTo(1);
        verify(hospedagemRepository).save(any());
    }

    @Test
    void registrarSaida_success_withNullHoraSaida() {
        Quarto q = Quarto.builder().uuid("q4").capacidadeTotal(2).capacidadeOcupada(1).build();
        Hospedagem h = Hospedagem.builder()
                .uuid("hs4")
                .status(StatusHospedagem.ATIVA)
                .dataEntrada(LocalDate.now().minusDays(1))
                .quarto(q)
                .paciente(pacienteMock)
                .build();

        when(hospedagemRepository.findByUuid("hs4")).thenReturn(Optional.of(h));
        when(hospedagemRepository.save(any())).thenReturn(h);

        HospedagemSaidaDTO dto = HospedagemSaidaDTO.builder()
                .dataSaida(LocalDate.now())
                .horaSaida(null)
                .motivoSaida("alta")
                .build();

        var res = service.registrarSaida("hs4", dto, user);

        assertThat(res).isNotNull();
    }

    @Test
    void registrarSaida_success_withoutQuarto() {
        Hospedagem h = Hospedagem.builder()
                .uuid("hs5")
                .status(StatusHospedagem.ATIVA)
                .dataEntrada(LocalDate.now().minusDays(1))
                .quarto(null)
                .paciente(pacienteMock)
                .build();

        when(hospedagemRepository.findByUuid("hs5")).thenReturn(Optional.of(h));
        when(hospedagemRepository.save(any())).thenReturn(h);

        HospedagemSaidaDTO dto = HospedagemSaidaDTO.builder()
                .dataSaida(LocalDate.now())
                .motivoSaida("alta")
                .build();

        var res = service.registrarSaida("hs5", dto, user);

        assertThat(res).isNotNull();
    }

    // ===================== TRANSFERIR QUARTO =====================

    @Test
    void transferirQuarto_throws_whenHospedagemNotActive() {
        Hospedagem h = Hospedagem.builder().uuid("ht1").status(StatusHospedagem.ENCERRADA).build();
        when(hospedagemRepository.findByUuid("ht1")).thenReturn(Optional.of(h));

        assertThatThrownBy(() -> service.transferirQuarto("ht1", "novoQuarto", "motivo", user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Apenas hospedagens ativas podem ser transferidas");
    }

    @Test
    void transferirQuarto_throws_whenNovoQuartoHasNoVagas() {
        Hospedagem h = Hospedagem.builder()
                .uuid("ht2")
                .status(StatusHospedagem.ATIVA)
                .paciente(pacienteMock)
                .quarto(quartoMock)
                .build();
        when(hospedagemRepository.findByUuid("ht2")).thenReturn(Optional.of(h));

        Quarto novoQuarto = Quarto.builder()
                .uuid("nq")
                .capacidadeTotal(2)
                .capacidadeOcupada(2)
                .ativo(true)
                .emManutencao(false)
                .build();
        when(quartoService.buscarPorUuid("nq")).thenReturn(novoQuarto);

        assertThatThrownBy(() -> service.transferirQuarto("ht2", "nq", "motivo", user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Novo quarto não possui vagas disponíveis");
    }

    @Test
    void transferirQuarto_success() {
        Quarto quartoOrigem = Quarto.builder()
                .uuid("qo")
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(4)
                .capacidadeOcupada(2)
                .build();

        Hospedagem h = Hospedagem.builder()
                .uuid("ht3")
                .status(StatusHospedagem.ATIVA)
                .paciente(pacienteMock)
                .quarto(quartoOrigem)
                .observacoesGerais("Obs anterior")
                .build();
        when(hospedagemRepository.findByUuid("ht3")).thenReturn(Optional.of(h));

        Quarto novoQuarto = Quarto.builder()
                .uuid("qd")
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .capacidadeOcupada(1)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoService.buscarPorUuid("qd")).thenReturn(novoQuarto);
        when(hospedagemRepository.save(any())).thenReturn(h);

        var res = service.transferirQuarto("ht3", "qd", "Melhor acomodação", user);

        assertThat(res).isNotNull();
        assertThat(quartoOrigem.getCapacidadeOcupada()).isEqualTo(1);
        assertThat(novoQuarto.getCapacidadeOcupada()).isEqualTo(2);
        assertThat(h.getObservacoesGerais()).contains("Transferência: Melhor acomodação");
    }

    @Test
    void transferirQuarto_success_withNullMotivoTransferencia() {
        Hospedagem h = Hospedagem.builder()
                .uuid("ht4")
                .status(StatusHospedagem.ATIVA)
                .paciente(pacienteMock)
                .quarto(quartoMock)
                .observacoesGerais(null)
                .build();
        when(hospedagemRepository.findByUuid("ht4")).thenReturn(Optional.of(h));

        Quarto novoQuarto = Quarto.builder()
                .uuid("qd2")
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .build();
        when(quartoService.buscarPorUuid("qd2")).thenReturn(novoQuarto);
        when(hospedagemRepository.save(any())).thenReturn(h);

        var res = service.transferirQuarto("ht4", "qd2", null, user);

        assertThat(res).isNotNull();
    }

    @Test
    void transferirQuarto_success_fromNullQuarto() {
        Hospedagem h = Hospedagem.builder()
                .uuid("ht5")
                .status(StatusHospedagem.ATIVA)
                .paciente(pacienteMock)
                .quarto(null)
                .build();
        when(hospedagemRepository.findByUuid("ht5")).thenReturn(Optional.of(h));

        Quarto novoQuarto = Quarto.builder()
                .uuid("qd3")
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .build();
        when(quartoService.buscarPorUuid("qd3")).thenReturn(novoQuarto);
        when(hospedagemRepository.save(any())).thenReturn(h);

        var res = service.transferirQuarto("ht5", "qd3", "Primeiro quarto", user);

        assertThat(res).isNotNull();
        assertThat(novoQuarto.getCapacidadeOcupada()).isEqualTo(1);
    }

    // ===================== LISTAR =====================

    @Test
    void listarAtivas_returnsList() {
        when(hospedagemRepository.findHospedagensAtivas()).thenReturn(Arrays.asList(hospedagemMock));

        List<HospedagemResponseDTO> result = service.listarAtivas();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo("h1");
    }

    @Test
    void listarAtivas_returnsEmptyList() {
        when(hospedagemRepository.findHospedagensAtivas()).thenReturn(Collections.emptyList());

        List<HospedagemResponseDTO> result = service.listarAtivas();

        assertThat(result).isEmpty();
    }

    @Test
    void listarPorPaciente_throws_whenPacienteNotFound() {
        when(pacienteRepository.findById("px")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listarPorPaciente("px"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Paciente não encontrado");
    }

    @Test
    void listarPorPaciente_returnsList() {
        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(pacienteMock));
        when(hospedagemRepository.findByPacienteOrderByDataEntradaDesc(pacienteMock))
                .thenReturn(Arrays.asList(hospedagemMock));

        List<HospedagemResponseDTO> result = service.listarPorPaciente("p1");

        assertThat(result).hasSize(1);
    }

    @Test
    void listarPorQuarto_returnsList() {
        when(quartoService.buscarPorUuid("q1")).thenReturn(quartoMock);
        when(hospedagemRepository.findByQuartoOrderByDataEntradaDesc(quartoMock))
                .thenReturn(Arrays.asList(hospedagemMock));

        List<HospedagemResponseDTO> result = service.listarPorQuarto("q1");

        assertThat(result).hasSize(1);
    }

    @Test
    void listarComPaginacao_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hospedagem> page = new PageImpl<>(Arrays.asList(hospedagemMock), pageable, 1);
        when(hospedagemRepository.findAll(pageable)).thenReturn(page);

        Page<HospedagemResponseDTO> result = service.listarComPaginacao(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listarComPaginacaoEFiltros_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hospedagem> page = new PageImpl<>(Arrays.asList(hospedagemMock), pageable, 1);
        when(hospedagemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<HospedagemResponseDTO> result = service.listarComPaginacaoEFiltros(
                "Paciente",
                "Quarto",
                AlaQuarto.FEMININA,
                StatusHospedagem.ATIVA,
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                pageable
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listarPorPeriodo_returnsList() {
        LocalDate inicio = LocalDate.now().minusDays(10);
        LocalDate fim = LocalDate.now();

        when(hospedagemRepository.findByPeriodoEntrada(inicio, fim))
                .thenReturn(Arrays.asList(hospedagemMock));

        List<HospedagemResponseDTO> result = service.listarPorPeriodo(inicio, fim);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarComPrevisaoVencida_returnsList() {
        when(hospedagemRepository.findHospedagensComPrevisaoVencida())
                .thenReturn(Arrays.asList(hospedagemMock));

        List<HospedagemResponseDTO> result = service.listarComPrevisaoVencida();

        assertThat(result).hasSize(1);
    }

    // ===================== BUSCAR =====================

    @Test
    void buscarPorUuid_throws_whenNotFound() {
        when(hospedagemRepository.findByUuid("hx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorUuid("hx"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Hospedagem não encontrada");
    }

    @Test
    void buscarPorUuidDTO_success() {
        when(hospedagemRepository.findByUuid("h1")).thenReturn(Optional.of(hospedagemMock));

        var result = service.buscarPorUuidDTO("h1");

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo("h1");
    }

    // ===================== PACIENTE TEM HOSPEDAGEM ATIVA =====================

    @Test
    void pacienteTemHospedagemAtiva_throws_whenPacienteNotFound() {
        when(pacienteRepository.findById("pnf")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pacienteTemHospedagemAtiva("pnf"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Paciente não encontrado");
    }

    @Test
    void pacienteTemHospedagemAtiva_returnsTrue() {
        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(pacienteMock));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteMock)).thenReturn(true);

        boolean result = service.pacienteTemHospedagemAtiva("p1");

        assertThat(result).isTrue();
    }

    @Test
    void pacienteTemHospedagemAtiva_returnsFalse() {
        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(pacienteMock));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(pacienteMock)).thenReturn(false);

        boolean result = service.pacienteTemHospedagemAtiva("p1");

        assertThat(result).isFalse();
    }

    // ===================== DELETAR =====================

    @Test
    void deletar_success_whenActiveWithQuarto() {
        Quarto q = Quarto.builder().uuid("qd").capacidadeTotal(4).capacidadeOcupada(2).build();
        Hospedagem h = Hospedagem.builder()
                .uuid("hd")
                .status(StatusHospedagem.ATIVA)
                .quarto(q)
                .build();

        when(hospedagemRepository.findByUuid("hd")).thenReturn(Optional.of(h));

        service.deletar("hd");

        assertThat(q.getCapacidadeOcupada()).isEqualTo(1);
        verify(hospedagemRepository).delete(h);
    }

    @Test
    void deletar_success_whenEncerrada() {
        Hospedagem h = Hospedagem.builder()
                .uuid("hde")
                .status(StatusHospedagem.ENCERRADA)
                .quarto(null)
                .build();

        when(hospedagemRepository.findByUuid("hde")).thenReturn(Optional.of(h));

        service.deletar("hde");

        verify(hospedagemRepository).delete(h);
    }

    @Test
    void deletar_success_whenActiveWithoutQuarto() {
        Hospedagem h = Hospedagem.builder()
                .uuid("hdaq")
                .status(StatusHospedagem.ATIVA)
                .quarto(null)
                .build();

        when(hospedagemRepository.findByUuid("hdaq")).thenReturn(Optional.of(h));

        service.deletar("hdaq");

        verify(hospedagemRepository).delete(h);
    }

    // ===================== DTO MAPPING =====================

    @Test
    void toResponseDTO_withNullQuarto() {
        Hospedagem h = Hospedagem.builder()
                .uuid("h-dto-null")
                .paciente(pacienteMock)
                .quarto(null)
                .dataEntrada(LocalDate.now())
                .horaEntrada(LocalTime.of(10, 0))
                .status(StatusHospedagem.ATIVA)
                .createdBy(null)
                .updatedBy(null)
                .build();

        when(hospedagemRepository.findByUuid("h-dto-null")).thenReturn(Optional.of(h));

        var res = service.buscarPorUuidDTO("h-dto-null");

        assertThat(res.getQuartoUuid()).isNull();
        assertThat(res.getQuartoNome()).isNull();
        assertThat(res.getQuartoCodigo()).isNull();
        assertThat(res.getCreatedByNome()).isNull();
        assertThat(res.getUpdatedByNome()).isNull();
    }

    @Test
    void toResponseDTO_withNullDadoPessoal() {
        Paciente pacienteSemDados = new Paciente();
        pacienteSemDados.setId("psd");
        pacienteSemDados.setDadoPessoal(null);

        Hospedagem h = Hospedagem.builder()
                .uuid("h-dto-nd")
                .paciente(pacienteSemDados)
                .quarto(quartoMock)
                .dataEntrada(LocalDate.now())
                .status(StatusHospedagem.ATIVA)
                .createdBy(user)
                .updatedBy(user)
                .build();

        when(hospedagemRepository.findByUuid("h-dto-nd")).thenReturn(Optional.of(h));

        var res = service.buscarPorUuidDTO("h-dto-nd");

        assertThat(res.getPacienteNome()).isEqualTo("N/A");
        assertThat(res.getPacienteCpf()).isNull();
        assertThat(res.getCreatedByNome()).isEqualTo("host");
        assertThat(res.getUpdatedByNome()).isEqualTo("host");
    }

    @Test
    void toResponseDTO_mapsAllFields() {
        when(hospedagemRepository.findByUuid("h1")).thenReturn(Optional.of(hospedagemMock));

        var res = service.buscarPorUuidDTO("h1");

        assertThat(res.getUuid()).isEqualTo("h1");
        assertThat(res.getPacienteId()).isEqualTo("p1");
        assertThat(res.getPacienteNome()).isEqualTo("Paciente Teste");
        assertThat(res.getPacienteCpf()).isEqualTo("12345678900");
        assertThat(res.getQuartoUuid()).isEqualTo("q1");
        assertThat(res.getQuartoNome()).isEqualTo("Quarto 101");
        assertThat(res.getQuartoCodigo()).isEqualTo("FEM-1-001");
        assertThat(res.getDataEntrada()).isEqualTo(LocalDate.now().minusDays(5));
        assertThat(res.getHoraEntrada()).isEqualTo(LocalTime.of(10, 0));
        assertThat(res.getStatus()).isEqualTo(StatusHospedagem.ATIVA);
    }
}
