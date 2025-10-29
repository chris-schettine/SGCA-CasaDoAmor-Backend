package br.com.casadoamor.sgca.service.auth;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.casadoamor.sgca.entity.auth.HistoricoSenha;
import br.com.casadoamor.sgca.repository.auth.HistoricoSenhaRepository;

class HistoricoSenhaServiceTest {

    @Mock
    private HistoricoSenhaRepository historicoRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private HistoricoSenhaService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        try {
            var f = service.getClass().getDeclaredField("quantidadeSenhasVerificar");
            f.setAccessible(true);
            f.setInt(service, 5);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    void salvarHistorico_CallsSave() {
        when(historicoRepo.save(any(HistoricoSenha.class))).thenAnswer(i -> i.getArgument(0));

        service.salvarHistorico(1L, "hash1");

        verify(historicoRepo).save(any(HistoricoSenha.class));
    }

    @Test
    void senhaJaUsada_NoHistory_ReturnsFalse() {
        when(historicoRepo.findTopNByUsuarioIdOrderByCriadoEmDesc(anyLong(), any(PageRequest.class))).thenReturn(List.of());

        boolean used = service.senhaJaUsada(1L, "plain");

        assertThat(used).isFalse();
    }

    @Test
    void senhaJaUsada_MatchFound_ReturnsTrue() {
        HistoricoSenha h = HistoricoSenha.builder().id(1L).usuarioId(1L).senhaHash("hash").build();
        when(historicoRepo.findTopNByUsuarioIdOrderByCriadoEmDesc(anyLong(), any(PageRequest.class))).thenReturn(List.of(h));
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);

        boolean used = service.senhaJaUsada(1L, "plain");

        assertThat(used).isTrue();
    }

    @Test
    void buscarHistorico_ReturnsList() {
        HistoricoSenha h = HistoricoSenha.builder().id(2L).usuarioId(1L).senhaHash("h").build();
        when(historicoRepo.findTopNByUsuarioIdOrderByCriadoEmDesc(anyLong(), any(PageRequest.class))).thenReturn(List.of(h));

        var list = service.buscarHistorico(1L, 5);

        assertThat(list).hasSize(1);
    }

    @Test
    void contarHistorico_ReturnsCount() {
        when(historicoRepo.countByUsuarioId(1L)).thenReturn(3L);

        long c = service.contarHistorico(1L);

        assertThat(c).isEqualTo(3L);
    }
}
