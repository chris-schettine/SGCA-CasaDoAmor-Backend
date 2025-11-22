package br.com.casadoamor.sgca.modules.hospedagem.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemRequestDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.HospedagemSaidaDTO;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
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

    @BeforeEach
    void init() {
        user = AuthUsuario.builder().nome("host").build();
    }

    @Test
    void registrarEntrada_throws_whenPacienteNotFound() {
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder().pacienteId("p1").dataEntrada(LocalDate.now()).build();

        when(pacienteRepository.findById("p1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarEntrada(dto, user)).isInstanceOf(EntityNotFoundException.class).hasMessageContaining("Paciente não encontrado");
    }

    @Test
    void registrarEntrada_throws_whenPacienteAlreadyActive() {
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder().pacienteId("p2").dataEntrada(LocalDate.now()).build();
        Paciente p = new Paciente(); p.setId("p2");

        when(pacienteRepository.findById("p2")).thenReturn(Optional.of(p));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(p)).thenReturn(true);

        assertThatThrownBy(() -> service.registrarEntrada(dto, user)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Paciente já possui uma hospedagem ativa");
    }

    @Test
    void registrarEntrada_throws_whenQuartoHasNoVagas() {
        Paciente p = new Paciente(); p.setId("p3");
        HospedagemRequestDTO dto = HospedagemRequestDTO.builder().pacienteId("p3").quartoUuid("q1").dataEntrada(LocalDate.now()).build();

        when(pacienteRepository.findById("p3")).thenReturn(Optional.of(p));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(p)).thenReturn(false);

        Quarto q = Quarto.builder().uuid("q1").capacidadeTotal(2).capacidadeOcupada(2).build();
        when(quartoService.buscarPorUuid("q1")).thenReturn(q);

        assertThatThrownBy(() -> service.registrarEntrada(dto, user)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Quarto não possui vagas disponíveis");
    }

    @Test
    void registrarEntrada_success_withQuarto_incrementsOccupancy() {
        Paciente p = new Paciente(); p.setId("p4");
        p.setDadoPessoal(new DadoPessoal()); p.getDadoPessoal().setNome("Paciente");

        HospedagemRequestDTO dto = HospedagemRequestDTO.builder().pacienteId("p4").quartoUuid("q2").dataEntrada(LocalDate.now()).build();

        when(pacienteRepository.findById("p4")).thenReturn(Optional.of(p));
        when(hospedagemRepository.pacienteTemHospedagemAtiva(p)).thenReturn(false);

        Quarto q = Quarto.builder().uuid("q2").capacidadeTotal(2).capacidadeOcupada(0).ala(AlaQuarto.MISTA).permiteSexoOposto(false).build();
        when(quartoService.buscarPorUuid("q2")).thenReturn(q);

        Hospedagem saved = Hospedagem.builder().uuid("h1").paciente(p).quarto(q).dataEntrada(LocalDate.now()).horaEntrada(LocalTime.NOON).build();
        when(hospedagemRepository.save(any())).thenReturn(saved);

        var res = service.registrarEntrada(dto, user);

        assertThat(res).isNotNull();
        assertThat(res.getUuid()).isEqualTo("h1");
        // quarto occupancy incremented from 0 to 1
        assertThat(q.getCapacidadeOcupada()).isEqualTo(1);
        verify(hospedagemRepository).save(any());
    }

    @Test
    void registrarSaida_throws_whenNotActive() {
        Hospedagem h = Hospedagem.builder().uuid("hs1").status(br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem.ENCERRADA).build();
        when(hospedagemRepository.findByUuid("hs1")).thenReturn(Optional.of(h));

        assertThatThrownBy(() -> service.registrarSaida("hs1", new HospedagemSaidaDTO(), user)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Hospedagem não está ativa");
    }

    @Test
    void registrarSaida_throws_whenDateBeforeDataEntrada() {
        Hospedagem h = Hospedagem.builder().uuid("hs2").status(br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem.ATIVA).dataEntrada(LocalDate.of(2024,1,10)).build();
        when(hospedagemRepository.findByUuid("hs2")).thenReturn(Optional.of(h));

        HospedagemSaidaDTO dto = HospedagemSaidaDTO.builder().dataSaida(LocalDate.of(2024,1,1)).motivoSaida("fim").build();

        assertThatThrownBy(() -> service.registrarSaida("hs2", dto, user)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Data de saída não pode ser anterior à data de entrada");
    }

    @Test
    void registrarSaida_success_decrementsQuartoAndSaves() {
        Quarto q = Quarto.builder().uuid("q3").capacidadeTotal(2).capacidadeOcupada(2).build();
        Paciente p = new Paciente(); p.setId("p5");
        Hospedagem h = Hospedagem.builder().uuid("hs3").status(br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem.ATIVA).dataEntrada(LocalDate.now().minusDays(2)).quarto(q).paciente(p).build();

        when(hospedagemRepository.findByUuid("hs3")).thenReturn(Optional.of(h));
        when(hospedagemRepository.save(any())).thenReturn(h);

        HospedagemSaidaDTO dto = HospedagemSaidaDTO.builder().dataSaida(LocalDate.now()).motivoSaida("alta").build();

        var res = service.registrarSaida("hs3", dto, user);

        assertThat(res).isNotNull();
        assertThat(q.getCapacidadeOcupada()).isEqualTo(1); // decremented
        verify(hospedagemRepository).save(any());
    }
}
