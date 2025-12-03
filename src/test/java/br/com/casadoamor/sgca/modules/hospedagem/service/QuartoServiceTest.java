package br.com.casadoamor.sgca.modules.hospedagem.service;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.hospedagem.dto.EstatisticasOcupacaoDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.QuartoRequestDTO;
import br.com.casadoamor.sgca.modules.hospedagem.dto.QuartoResumoDTO;
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
    private Quarto quartoMock;

    @BeforeEach
    void setup() {
        user = AuthUsuario.builder().id(1L).nome("manager").build();
        quartoMock = Quarto.builder()
                .id(1L)
                .uuid("uuid-quarto-1")
                .nome("Quarto 101")
                .codigo("FEM-1-001")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.FEMININA)
                .andar("1")
                .capacidadeTotal(4)
                .capacidadeOcupada(2)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .observacoes("Observações do quarto")
                .createdBy(user)
                .build();
    }

    // ===================== CADASTRAR =====================

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

        Quarto saved = Quarto.builder().uuid("u1").nome("Q1").codigo("C1").tipo(TipoQuarto.COMPARTILHADO).ala(AlaQuarto.MISTA).capacidadeTotal(4).capacidadeOcupada(0).ativo(true).emManutencao(false).permiteSexoOposto(false).createdBy(user).build();
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
    void cadastrar_generatesCode_whenCodeIsNull() {
        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Quarto Auto")
                .codigo(null)
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.FEMININA)
                .andar("2")
                .capacidadeTotal(4)
                .build();

        when(quartoRepository.existsByCodigo("FEM-2-001")).thenReturn(false);

        Quarto saved = Quarto.builder()
                .uuid("auto-uuid")
                .nome("Quarto Auto")
                .codigo("FEM-2-001")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.FEMININA)
                .andar("2")
                .capacidadeTotal(4)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .createdBy(user)
                .build();
        when(quartoRepository.save(any())).thenReturn(saved);

        var res = service.cadastrar(dto, user);

        assertThat(res).isNotNull();
        assertThat(res.getCodigo()).isEqualTo("FEM-2-001");
    }

    @Test
    void cadastrar_generatesCode_whenCodeIsEmpty() {
        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Quarto Auto 2")
                .codigo("   ")
                .tipo(TipoQuarto.INDIVIDUAL)
                .ala(AlaQuarto.MASCULINA)
                .andar("3")
                .capacidadeTotal(1)
                .build();

        when(quartoRepository.existsByCodigo("MASC-3-001")).thenReturn(false);

        Quarto saved = Quarto.builder()
                .uuid("auto-uuid-2")
                .nome("Quarto Auto 2")
                .codigo("MASC-3-001")
                .tipo(TipoQuarto.INDIVIDUAL)
                .ala(AlaQuarto.MASCULINA)
                .capacidadeTotal(1)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .createdBy(user)
                .build();
        when(quartoRepository.save(any())).thenReturn(saved);

        var res = service.cadastrar(dto, user);

        assertThat(res).isNotNull();
    }

    @Test
    void cadastrar_generatesCodeWithMista() {
        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Quarto Misto")
                .codigo(null)
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.MISTA)
                .andar("1")
                .capacidadeTotal(6)
                .build();

        when(quartoRepository.existsByCodigo("MIST-1-001")).thenReturn(false);

        Quarto saved = Quarto.builder()
                .uuid("mist-uuid")
                .nome("Quarto Misto")
                .codigo("MIST-1-001")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(6)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoRepository.save(any())).thenReturn(saved);

        var res = service.cadastrar(dto, user);
        assertThat(res).isNotNull();
    }

    @Test
    void cadastrar_generatesCodeWithIsolamento() {
        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Quarto Isolamento")
                .codigo(null)
                .tipo(TipoQuarto.ISOLAMENTO)
                .ala(AlaQuarto.ISOLAMENTO)
                .andar("0")
                .capacidadeTotal(1)
                .build();

        when(quartoRepository.existsByCodigo("ISO-0-001")).thenReturn(false);

        Quarto saved = Quarto.builder()
                .uuid("iso-uuid")
                .nome("Quarto Isolamento")
                .codigo("ISO-0-001")
                .tipo(TipoQuarto.ISOLAMENTO)
                .ala(AlaQuarto.ISOLAMENTO)
                .capacidadeTotal(1)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoRepository.save(any())).thenReturn(saved);

        var res = service.cadastrar(dto, user);
        assertThat(res).isNotNull();
    }

    @Test
    void cadastrar_generatesCodeIncrementing_whenFirstCodeExists() {
        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Quarto")
                .codigo(null)
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.FEMININA)
                .andar("1")
                .capacidadeTotal(4)
                .build();

        when(quartoRepository.existsByCodigo("FEM-1-001")).thenReturn(true);
        when(quartoRepository.existsByCodigo("FEM-1-002")).thenReturn(false);

        Quarto saved = Quarto.builder()
                .uuid("inc-uuid")
                .nome("Quarto")
                .codigo("FEM-1-002")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(4)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoRepository.save(any())).thenReturn(saved);

        var res = service.cadastrar(dto, user);
        assertThat(res).isNotNull();
    }

    @Test
    void cadastrar_generatesCodeWithNullAndar() {
        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Quarto Sem Andar")
                .codigo(null)
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.FEMININA)
                .andar(null)
                .capacidadeTotal(4)
                .build();

        when(quartoRepository.existsByCodigo("FEM-0-001")).thenReturn(false);

        Quarto saved = Quarto.builder()
                .uuid("null-andar-uuid")
                .nome("Quarto Sem Andar")
                .codigo("FEM-0-001")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(4)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoRepository.save(any())).thenReturn(saved);

        var res = service.cadastrar(dto, user);
        assertThat(res).isNotNull();
    }

    @Test
    void cadastrar_withAllOptionalFieldsNull() {
        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Quarto Minimal")
                .codigo("MIN-001")
                .tipo(TipoQuarto.INDIVIDUAL)
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(1)
                .ativo(null)
                .emManutencao(null)
                .permiteSexoOposto(null)
                .build();

        when(quartoRepository.existsByCodigo("MIN-001")).thenReturn(false);

        Quarto saved = Quarto.builder()
                .uuid("min-uuid")
                .nome("Quarto Minimal")
                .codigo("MIN-001")
                .tipo(TipoQuarto.INDIVIDUAL)
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(1)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoRepository.save(any())).thenReturn(saved);

        var res = service.cadastrar(dto, user);
        assertThat(res).isNotNull();
        assertThat(res.getAtivo()).isTrue();
        assertThat(res.getEmManutencao()).isFalse();
    }

    // ===================== BUSCAR =====================

    @Test
    void buscarPorUuid_throws_whenNotFound() {
        when(quartoRepository.findByUuid("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorUuid("x")).isInstanceOf(EntityNotFoundException.class).hasMessageContaining("Quarto não encontrado");
    }

    @Test
    void buscarPorUuid_success() {
        when(quartoRepository.findByUuid("uuid-quarto-1")).thenReturn(Optional.of(quartoMock));

        var result = service.buscarPorUuid("uuid-quarto-1");

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo("uuid-quarto-1");
    }

    @Test
    void buscarPorUuidDTO_success() {
        when(quartoRepository.findByUuid("uuid-quarto-1")).thenReturn(Optional.of(quartoMock));

        var result = service.buscarPorUuidDTO("uuid-quarto-1");

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo("uuid-quarto-1");
        assertThat(result.getNome()).isEqualTo("Quarto 101");
    }

    // ===================== ATUALIZAR =====================

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
    void atualizar_success_whenNoCodeChange() {
        Quarto existing = Quarto.builder()
                .id(4L)
                .uuid("u4")
                .codigo("SAME-CODE")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .capacidadeOcupada(1)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoRepository.findByUuid("u4")).thenReturn(Optional.of(existing));
        when(quartoRepository.save(any())).thenReturn(existing);

        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Novo Nome")
                .codigo("SAME-CODE")
                .tipo(TipoQuarto.INDIVIDUAL)
                .ala(AlaQuarto.FEMININA)
                .capacidadeTotal(4)
                .ativo(true)
                .emManutencao(true)
                .permiteSexoOposto(true)
                .build();

        var res = service.atualizar("u4", dto, user);

        assertThat(res).isNotNull();
        verify(quartoRepository).save(any());
    }

    @Test
    void atualizar_success_withNewCode() {
        Quarto existing = Quarto.builder()
                .id(5L)
                .uuid("u5")
                .codigo("OLD-CODE")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoRepository.findByUuid("u5")).thenReturn(Optional.of(existing));
        when(quartoRepository.existsByCodigoAndIdNot("NEW-CODE", 5L)).thenReturn(false);
        when(quartoRepository.save(any())).thenReturn(existing);

        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Updated")
                .codigo("NEW-CODE")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(6)
                .build();

        var res = service.atualizar("u5", dto, user);

        assertThat(res).isNotNull();
        verify(quartoRepository).save(any());
    }

    @Test
    void atualizar_withNullOptionalFields_keepsExistingValues() {
        Quarto existing = Quarto.builder()
                .id(6L)
                .uuid("u6")
                .codigo("C6")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(true)
                .permiteSexoOposto(true)
                .build();
        when(quartoRepository.findByUuid("u6")).thenReturn(Optional.of(existing));
        when(quartoRepository.save(any())).thenReturn(existing);

        QuartoRequestDTO dto = QuartoRequestDTO.builder()
                .nome("Updated Name")
                .codigo("C6")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .ativo(null)
                .emManutencao(null)
                .permiteSexoOposto(null)
                .build();

        var res = service.atualizar("u6", dto, user);

        assertThat(res).isNotNull();
    }

    // ===================== LISTAR =====================

    @Test
    void listarTodos_returnsAllQuartos() {
        List<Quarto> quartos = Arrays.asList(quartoMock,
                Quarto.builder().uuid("u2").nome("Q2").codigo("C2").tipo(TipoQuarto.INDIVIDUAL).ala(AlaQuarto.MASCULINA).capacidadeTotal(1).capacidadeOcupada(0).ativo(true).emManutencao(false).permiteSexoOposto(false).build());
        when(quartoRepository.findAll()).thenReturn(quartos);

        List<QuartoResumoDTO> result = service.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNome()).isEqualTo("Quarto 101");
    }

    @Test
    void listarTodos_returnsEmptyList() {
        when(quartoRepository.findAll()).thenReturn(Collections.emptyList());

        List<QuartoResumoDTO> result = service.listarTodos();

        assertThat(result).isEmpty();
    }

    @Test
    void listarAtivos_returnsOnlyActiveQuartos() {
        List<Quarto> quartos = Arrays.asList(quartoMock);
        when(quartoRepository.findByAtivoTrue()).thenReturn(quartos);

        List<QuartoResumoDTO> result = service.listarAtivos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAtivo()).isTrue();
    }

    @Test
    void listarPorAla_returnsQuartosByAla() {
        List<Quarto> quartos = Arrays.asList(quartoMock);
        when(quartoRepository.findByAla(AlaQuarto.FEMININA)).thenReturn(quartos);

        List<QuartoResumoDTO> result = service.listarPorAla(AlaQuarto.FEMININA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAla().getValor()).isEqualTo("FEMININA");
    }

    @Test
    void listarComVagas_returnsQuartosWithAvailableSlots() {
        Quarto quartoComVaga = Quarto.builder()
                .uuid("vaga-uuid")
                .nome("Quarto Com Vaga")
                .codigo("V001")
                .tipo(TipoQuarto.COMPARTILHADO)
                .ala(AlaQuarto.MISTA)
                .capacidadeTotal(4)
                .capacidadeOcupada(2)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .build();
        when(quartoRepository.findQuartosComVagas()).thenReturn(Arrays.asList(quartoComVaga));

        List<QuartoResumoDTO> result = service.listarComVagas();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVagasDisponiveis()).isEqualTo(2);
    }

    @Test
    void listarComVagasPorAla_returnsQuartosWithAvailableSlotsByAla() {
        when(quartoRepository.findQuartosComVagasPorAla(AlaQuarto.MASCULINA)).thenReturn(Arrays.asList(quartoMock));

        List<QuartoResumoDTO> result = service.listarComVagasPorAla(AlaQuarto.MASCULINA);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarComPaginacao_returnsPagedQuartos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Quarto> page = new PageImpl<>(Arrays.asList(quartoMock), pageable, 1);
        when(quartoRepository.searchQuartos(eq("Quarto"), eq(AlaQuarto.FEMININA), eq(TipoQuarto.COMPARTILHADO), eq(true), eq(pageable)))
                .thenReturn(page);

        Page<QuartoResumoDTO> result = service.listarComPaginacao("Quarto", AlaQuarto.FEMININA, TipoQuarto.COMPARTILHADO, true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("Quarto 101");
    }

    @Test
    void listarComPaginacao_withNullFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Quarto> page = new PageImpl<>(Arrays.asList(quartoMock), pageable, 1);
        when(quartoRepository.searchQuartos(eq(null), eq(null), eq(null), eq(null), eq(pageable)))
                .thenReturn(page);

        Page<QuartoResumoDTO> result = service.listarComPaginacao(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ===================== INATIVAR / ATIVAR =====================

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
        assertThat(q.getUpdatedBy()).isEqualTo(user);
        verify(quartoRepository).save(q);
    }

    @Test
    void ativar_success() {
        Quarto q = Quarto.builder().uuid("u-ativar").capacidadeOcupada(0).ativo(false).build();
        when(quartoRepository.findByUuid("u-ativar")).thenReturn(Optional.of(q));

        service.ativar("u-ativar", user);

        assertThat(q.getAtivo()).isTrue();
        assertThat(q.getUpdatedBy()).isEqualTo(user);
        verify(quartoRepository).save(q);
    }

    // ===================== MANUTENÇÃO =====================

    @Test
    void ativarManutencao_throws_whenOccupied() {
        Quarto q = Quarto.builder().uuid("u-manut").capacidadeOcupada(1).build();
        when(quartoRepository.findByUuid("u-manut")).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> service.ativarManutencao("u-manut", user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não é possível colocar em manutenção um quarto com leitos ocupados");
    }

    @Test
    void ativarManutencao_success_whenEmpty() {
        Quarto q = Quarto.builder().uuid("u-manut-ok").capacidadeOcupada(0).emManutencao(false).build();
        when(quartoRepository.findByUuid("u-manut-ok")).thenReturn(Optional.of(q));

        service.ativarManutencao("u-manut-ok", user);

        assertThat(q.getEmManutencao()).isTrue();
        assertThat(q.getUpdatedBy()).isEqualTo(user);
        verify(quartoRepository).save(q);
    }

    @Test
    void desativarManutencao_success() {
        Quarto q = Quarto.builder().uuid("u-desmanut").emManutencao(true).build();
        when(quartoRepository.findByUuid("u-desmanut")).thenReturn(Optional.of(q));

        service.desativarManutencao("u-desmanut", user);

        assertThat(q.getEmManutencao()).isFalse();
        assertThat(q.getUpdatedBy()).isEqualTo(user);
        verify(quartoRepository).save(q);
    }

    // ===================== DELETAR =====================

    @Test
    void deletar_throws_whenOccupied() {
        Quarto q = Quarto.builder().uuid("u6").capacidadeOcupada(2).build();
        when(quartoRepository.findByUuid("u6")).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> service.deletar("u6")).isInstanceOf(IllegalStateException.class).hasMessageContaining("Não é possível deletar um quarto com leitos ocupados");
    }

    @Test
    void deletar_success_whenEmpty() {
        Quarto q = Quarto.builder().uuid("u-del").capacidadeOcupada(0).build();
        when(quartoRepository.findByUuid("u-del")).thenReturn(Optional.of(q));

        service.deletar("u-del");

        verify(quartoRepository).delete(q);
    }

    // ===================== ESTATÍSTICAS =====================

    @Test
    void obterEstatisticas_handlesNulls_andCalculatesPercentual() {
        when(quartoRepository.contarCapacidadeTotal()).thenReturn(100);
        when(quartoRepository.contarOcupacaoTotal()).thenReturn(25);
        when(quartoRepository.contarVagasDisponiveis()).thenReturn(75);

        when(quartoRepository.contarTotalQuartos()).thenReturn(10L);
        when(quartoRepository.contarQuartosAtivos()).thenReturn(8L);
        when(quartoRepository.contarQuartosInativos()).thenReturn(2L);
        when(quartoRepository.contarQuartosEmManutencao()).thenReturn(1L);
        when(quartoRepository.contarQuartosDisponiveisAdmissao()).thenReturn(5L);

        when(quartoRepository.contarQuartosPorTipo(TipoQuarto.INDIVIDUAL)).thenReturn(3L);
        when(quartoRepository.contarQuartosPorTipo(TipoQuarto.COMPARTILHADO)).thenReturn(6L);
        when(quartoRepository.contarQuartosPorTipo(TipoQuarto.ISOLAMENTO)).thenReturn(1L);

        when(quartoRepository.contarQuartosLotados()).thenReturn(2L);
        when(quartoRepository.contarQuartosVazios()).thenReturn(3L);
        when(quartoRepository.contarQuartosParcialmenteOcupados()).thenReturn(5L);
        when(quartoRepository.contarQuartosPermitemSexoOposto()).thenReturn(4L);

        // per-ala values return some numbers
        when(quartoRepository.contarCapacidadeTotalPorAla(AlaQuarto.FEMININA)).thenReturn(50);
        when(quartoRepository.contarOcupacaoTotalPorAla(AlaQuarto.FEMININA)).thenReturn(10);
        when(quartoRepository.contarVagasDisponiveisPorAla(AlaQuarto.FEMININA)).thenReturn(40);
        when(quartoRepository.contarTotalQuartosPorAla(AlaQuarto.FEMININA)).thenReturn(5L);
        when(quartoRepository.contarQuartosAtivosPorAla(AlaQuarto.FEMININA)).thenReturn(4L);
        when(quartoRepository.contarQuartosInativosPorAla(AlaQuarto.FEMININA)).thenReturn(1L);
        when(quartoRepository.contarQuartosEmManutencaoPorAla(AlaQuarto.FEMININA)).thenReturn(0L);
        when(quartoRepository.contarQuartosDisponiveisAdmissaoPorAla(AlaQuarto.FEMININA)).thenReturn(3L);
        when(quartoRepository.contarQuartosLotadosPorAla(AlaQuarto.FEMININA)).thenReturn(1L);
        when(quartoRepository.contarQuartosVaziosPorAla(AlaQuarto.FEMININA)).thenReturn(1L);
        when(quartoRepository.contarQuartosParcialmenteOcupadosPorAla(AlaQuarto.FEMININA)).thenReturn(3L);

        EstatisticasOcupacaoDTO stats = service.obterEstatisticas();

        assertThat(stats.getCapacidadeTotal()).isEqualTo(100);
        assertThat(stats.getOcupacaoTotal()).isEqualTo(25);
        assertThat(stats.getVagasDisponiveis()).isEqualTo(75);
        assertThat(stats.getPercentualOcupacao()).isEqualTo((25 * 100.0) / 100);

        assertThat(stats.getTotalQuartos()).isEqualTo(10);
        assertThat(stats.getQuartosAtivos()).isEqualTo(8);
        assertThat(stats.getQuartosInativos()).isEqualTo(2);
        assertThat(stats.getQuartosEmManutencao()).isEqualTo(1);
        assertThat(stats.getQuartosDisponiveisAdmissao()).isEqualTo(5);

        assertThat(stats.getQuartosIndividuais()).isEqualTo(3);
        assertThat(stats.getQuartosCompartilhados()).isEqualTo(6);
        assertThat(stats.getQuartosIsolamento()).isEqualTo(1);

        assertThat(stats.getQuartosLotados()).isEqualTo(2);
        assertThat(stats.getQuartosVazios()).isEqualTo(3);
        assertThat(stats.getQuartosParcialmenteOcupados()).isEqualTo(5);
        assertThat(stats.getQuartosPermitemSexoOposto()).isEqualTo(4);

        assertThat(stats.getAlaFeminina().getCapacidadeTotal()).isEqualTo(50);
        assertThat(stats.getAlaFeminina().getOcupacaoTotal()).isEqualTo(10);
        assertThat(stats.getAlaFeminina().getVagasDisponiveis()).isEqualTo(40);
    }

    @Test
    void obterEstatisticas_handlesAllNulls() {
        when(quartoRepository.contarCapacidadeTotal()).thenReturn(null);
        when(quartoRepository.contarOcupacaoTotal()).thenReturn(null);
        when(quartoRepository.contarVagasDisponiveis()).thenReturn(null);

        when(quartoRepository.contarTotalQuartos()).thenReturn(null);
        when(quartoRepository.contarQuartosAtivos()).thenReturn(null);
        when(quartoRepository.contarQuartosInativos()).thenReturn(null);
        when(quartoRepository.contarQuartosEmManutencao()).thenReturn(null);
        when(quartoRepository.contarQuartosDisponiveisAdmissao()).thenReturn(null);

        when(quartoRepository.contarQuartosPorTipo(TipoQuarto.INDIVIDUAL)).thenReturn(null);
        when(quartoRepository.contarQuartosPorTipo(TipoQuarto.COMPARTILHADO)).thenReturn(null);
        when(quartoRepository.contarQuartosPorTipo(TipoQuarto.ISOLAMENTO)).thenReturn(null);

        when(quartoRepository.contarQuartosLotados()).thenReturn(null);
        when(quartoRepository.contarQuartosVazios()).thenReturn(null);
        when(quartoRepository.contarQuartosParcialmenteOcupados()).thenReturn(null);
        when(quartoRepository.contarQuartosPermitemSexoOposto()).thenReturn(null);

        when(quartoRepository.contarCapacidadeTotalPorAla(any())).thenReturn(null);
        when(quartoRepository.contarOcupacaoTotalPorAla(any())).thenReturn(null);
        when(quartoRepository.contarVagasDisponiveisPorAla(any())).thenReturn(null);
        when(quartoRepository.contarTotalQuartosPorAla(any())).thenReturn(null);
        when(quartoRepository.contarQuartosAtivosPorAla(any())).thenReturn(null);
        when(quartoRepository.contarQuartosInativosPorAla(any())).thenReturn(null);
        when(quartoRepository.contarQuartosEmManutencaoPorAla(any())).thenReturn(null);
        when(quartoRepository.contarQuartosDisponiveisAdmissaoPorAla(any())).thenReturn(null);
        when(quartoRepository.contarQuartosLotadosPorAla(any())).thenReturn(null);
        when(quartoRepository.contarQuartosVaziosPorAla(any())).thenReturn(null);
        when(quartoRepository.contarQuartosParcialmenteOcupadosPorAla(any())).thenReturn(null);

        EstatisticasOcupacaoDTO stats = service.obterEstatisticas();

        assertThat(stats.getCapacidadeTotal()).isEqualTo(0);
        assertThat(stats.getOcupacaoTotal()).isEqualTo(0);
        assertThat(stats.getVagasDisponiveis()).isEqualTo(0);
        assertThat(stats.getPercentualOcupacao()).isEqualTo(0.0);
        assertThat(stats.getTotalQuartos()).isEqualTo(0);
    }

    @Test
    void obterEstatisticas_zeroCapacidade_returnsZeroPercentual() {
        when(quartoRepository.contarCapacidadeTotal()).thenReturn(0);
        when(quartoRepository.contarOcupacaoTotal()).thenReturn(0);
        when(quartoRepository.contarVagasDisponiveis()).thenReturn(0);

        when(quartoRepository.contarTotalQuartos()).thenReturn(0L);
        when(quartoRepository.contarQuartosAtivos()).thenReturn(0L);
        when(quartoRepository.contarQuartosInativos()).thenReturn(0L);
        when(quartoRepository.contarQuartosEmManutencao()).thenReturn(0L);
        when(quartoRepository.contarQuartosDisponiveisAdmissao()).thenReturn(0L);

        when(quartoRepository.contarQuartosPorTipo(any())).thenReturn(0L);

        when(quartoRepository.contarQuartosLotados()).thenReturn(0L);
        when(quartoRepository.contarQuartosVazios()).thenReturn(0L);
        when(quartoRepository.contarQuartosParcialmenteOcupados()).thenReturn(0L);
        when(quartoRepository.contarQuartosPermitemSexoOposto()).thenReturn(0L);

        when(quartoRepository.contarCapacidadeTotalPorAla(any())).thenReturn(0);
        when(quartoRepository.contarOcupacaoTotalPorAla(any())).thenReturn(0);
        when(quartoRepository.contarVagasDisponiveisPorAla(any())).thenReturn(0);
        when(quartoRepository.contarTotalQuartosPorAla(any())).thenReturn(0L);
        when(quartoRepository.contarQuartosAtivosPorAla(any())).thenReturn(0L);
        when(quartoRepository.contarQuartosInativosPorAla(any())).thenReturn(0L);
        when(quartoRepository.contarQuartosEmManutencaoPorAla(any())).thenReturn(0L);
        when(quartoRepository.contarQuartosDisponiveisAdmissaoPorAla(any())).thenReturn(0L);
        when(quartoRepository.contarQuartosLotadosPorAla(any())).thenReturn(0L);
        when(quartoRepository.contarQuartosVaziosPorAla(any())).thenReturn(0L);
        when(quartoRepository.contarQuartosParcialmenteOcupadosPorAla(any())).thenReturn(0L);

        EstatisticasOcupacaoDTO stats = service.obterEstatisticas();

        assertThat(stats.getPercentualOcupacao()).isEqualTo(0.0);
        assertThat(stats.getAlaFeminina().getPercentualOcupacao()).isEqualTo(0.0);
    }

    // ===================== DTO MAPPING =====================

    @Test
    void toResponseDTO_withNullCreatedByAndUpdatedBy() {
        Quarto quarto = Quarto.builder()
                .uuid("dto-test")
                .nome("Quarto DTO")
                .codigo("DTO-001")
                .tipo(TipoQuarto.INDIVIDUAL)
                .ala(AlaQuarto.MISTA)
                .andar("1")
                .capacidadeTotal(1)
                .capacidadeOcupada(0)
                .ativo(true)
                .emManutencao(false)
                .permiteSexoOposto(false)
                .createdBy(null)
                .updatedBy(null)
                .build();
        when(quartoRepository.findByUuid("dto-test")).thenReturn(Optional.of(quarto));

        var res = service.buscarPorUuidDTO("dto-test");

        assertThat(res.getCreatedByNome()).isNull();
        assertThat(res.getUpdatedByNome()).isNull();
    }

    @Test
    void toResumoDTO_mapsProperly() {
        when(quartoRepository.findAll()).thenReturn(Arrays.asList(quartoMock));

        var result = service.listarTodos();

        assertThat(result).hasSize(1);
        QuartoResumoDTO dto = result.get(0);
        assertThat(dto.getUuid()).isEqualTo("uuid-quarto-1");
        assertThat(dto.getNome()).isEqualTo("Quarto 101");
        assertThat(dto.getCodigo()).isEqualTo("FEM-1-001");
        assertThat(dto.getTipo().getValor()).isEqualTo("COMPARTILHADO");
        assertThat(dto.getAla().getValor()).isEqualTo("FEMININA");
        assertThat(dto.getAndar()).isEqualTo("1");
        assertThat(dto.getCapacidadeTotal()).isEqualTo(4);
        assertThat(dto.getCapacidadeOcupada()).isEqualTo(2);
        assertThat(dto.getVagasDisponiveis()).isEqualTo(2);
        assertThat(dto.getAtivo()).isTrue();
        assertThat(dto.getEmManutencao()).isFalse();
        assertThat(dto.getPermiteSexoOposto()).isFalse();
    }
}
