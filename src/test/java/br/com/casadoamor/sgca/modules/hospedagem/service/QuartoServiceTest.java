package br.com.casadoamor.sgca.modules.hospedagem.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.dto.EstatisticasOcupacaoDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.QuartoRequestDTO;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.TipoQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.repository.QuartoRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class QuartoServiceTest {

    @Mock
    private QuartoRepository quartoRepository;

    @InjectMocks
    private QuartoService service;

    private AuthUsuario user;

    @BeforeEach
    void setup() {
        user = AuthUsuario.builder().nome("manager").build();
    }

    @Test
    void cadastrar_success_whenCodeUnique() {
        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Q1")
                .codigo("C1")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .build();

        when(quartoRepository.existsByCodigo("C1")).thenReturn(false);

        Quarto saved = Quarto.builder().uuid("u1").nome("Q1").codigo("C1").capacidadeTotal(4).capacidadeOcupada(0).ativo(true).emManutencao(false).permiteSexoOposto(false).createdBy(user).build();
        when(quartoRepository.save(any())).thenReturn(saved);

        var res = service.cadastrar(dto, user);

        assertThat(res).isNotNull();
        assertThat(res.getUuid()).isEqualTo("u1");
        verify(quartoRepository).save(any());
    }

    @Test
    void cadastrar_throws_whenCodigoExists() {
        QuartoRequestDTO dto = QuartoRequestDTO.builder().nome("Q1").codigo("C1").tipo(TipoQuarto.COMPARTILHADO).ala(AlaQuarto.MISTA).capacidadeTotal(2).build();
        when(quartoRepository.existsByCodigo("C1")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(dto, user)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Já existe um quarto com o código");
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void buscarPorUuid_throws_whenNotFound() {
        when(quartoRepository.findByUuid("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorUuid("x")).isInstanceOf(EntityNotFoundException.class).hasMessageContaining("Quarto não encontrado");
    }

    @Test
    void atualizar_throws_whenCodeConflict() {
        Quarto existing = Quarto.builder().id(2L).uuid("u2").codigo("C2").capacidadeTotal(4).capacidadeOcupada(0).build();
        when(quartoRepository.findByUuid("u2")).thenReturn(Optional.of(existing));

        QuartoRequestDTO dto = QuartoRequestDTO.builder().nome("X").codigo("NEW").tipo(TipoQuarto.COMPARTILHADO).ala(AlaQuarto.MISTA).capacidadeTotal(4).build();
        when(quartoRepository.existsByCodigoAndIdNot("NEW", existing.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar("u2", dto, user)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Já existe outro quarto com o código");
    }

    @Test
    void atualizar_throws_whenReducingCapacityBelowOcupada() {
        Quarto existing = Quarto.builder().id(3L).uuid("u3").codigo("C3").capacidadeTotal(5).capacidadeOcupada(4).build();
        when(quartoRepository.findByUuid("u3")).thenReturn(Optional.of(existing));

        QuartoRequestDTO dto = QuartoRequestDTO.builder().nome("X").codigo("C3").tipo(TipoQuarto.COMPARTILHADO).ala(AlaQuarto.MISTA).capacidadeTotal(2).build();

        assertThatThrownBy(() -> service.atualizar("u3", dto, user)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Não é possível reduzir a capacidade");
    }

    @Test
    void inativar_throws_whenOccupied() {
        Quarto q = Quarto.builder().uuid("u4").capacidadeOcupada(1).build();
        when(quartoRepository.findByUuid("u4")).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> service.inativar("u4", user)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Não é possível inativar um quarto com leitos ocupados");
    }

    @Test
    void inativar_succeeds_whenEmpty() {
        Quarto q = Quarto.builder().uuid("u5").capacidadeOcupada(0).ativo(true).build();
        when(quartoRepository.findByUuid("u5")).thenReturn(Optional.of(q));

        service.inativar("u5", user);

        assertThat(q.getAtivo()).isFalse();
        verify(quartoRepository).save(q);
    }

    @Test
    void deletar_throws_whenOccupied() {
        Quarto q = Quarto.builder().uuid("u6").capacidadeOcupada(2).build();
        when(quartoRepository.findByUuid("u6")).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> service.deletar("u6")).isInstanceOf(IllegalStateException.class).hasMessageContaining("Não é possível deletar um quarto com leitos ocupados");
    }

    @Test
    void obterEstatisticas_handlesNulls_andCalculatesPercentual() {
        when(quartoRepository.contarCapacidadeTotal()).thenReturn(100);
        when(quartoRepository.contarOcupacaoTotal()).thenReturn(25);
        when(quartoRepository.contarVagasDisponiveis()).thenReturn(75);

        // per-ala values return some numbers
        when(quartoRepository.contarCapacidadeTotalPorAla(AlaQuarto.FEMININA)).thenReturn(50);
        when(quartoRepository.contarOcupacaoTotalPorAla(AlaQuarto.FEMININA)).thenReturn(10);
        when(quartoRepository.contarVagasDisponiveisPorAla(AlaQuarto.FEMININA)).thenReturn(40);

        EstatisticasOcupacaoDTO stats = service.obterEstatisticas();

        assertThat(stats.getCapacidadeTotal()).isEqualTo(100);
        assertThat(stats.getOcupacaoTotal()).isEqualTo(25);
        assertThat(stats.getVagasDisponiveis()).isEqualTo(75);
        assertThat(stats.getPercentualOcupacao()).isEqualTo((25 * 100.0) / 100);

        assertThat(stats.getAlaFeminina().getCapacidadeTotal()).isEqualTo(50);
        assertThat(stats.getAlaFeminina().getOcupacaoTotal()).isEqualTo(10);
        assertThat(stats.getAlaFeminina().getVagasDisponiveis()).isEqualTo(40);
    }
}
