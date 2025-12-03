package br.com.casadoamor.sgca.modules.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.modules.admin.dtos.permissao.CreatePermissaoDTO;
import br.com.casadoamor.sgca.modules.admin.entity.Permissao;
import br.com.casadoamor.sgca.modules.admin.repository.PermissaoRepository;

@ExtendWith(MockitoExtension.class)
class PermissaoServiceTest {

    @Mock
    PermissaoRepository permissaoRepository;

    @InjectMocks
    PermissaoService permissaoService;

    @Test
    void criarPermissao_success_and_duplicateThrows() {
        CreatePermissaoDTO dto = new CreatePermissaoDTO();
        dto.setNome("PERM_X"); dto.setDescricao("d");

        when(permissaoRepository.existsByNome("PERM_X")).thenReturn(false);
        Permissao saved = Permissao.builder().id(5L).nome("PERM_X").descricao("d").build();
        when(permissaoRepository.save(any())).thenReturn(saved);

        var out = permissaoService.criarPermissao(dto, 1L);
        assertThat(out.getId()).isEqualTo(5L);

        when(permissaoRepository.existsByNome("PERM_X")).thenReturn(true);
        assertThatThrownBy(() -> permissaoService.criarPermissao(dto, 1L)).isInstanceOf(RuntimeException.class).hasMessageContaining("já existe");
    }

    @Test
    void buscarPorId_notFound_andDeleted_throws() {
        when(permissaoRepository.findById(100L)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> permissaoService.buscarPorId(100L)).isInstanceOf(RuntimeException.class).hasMessageContaining("não encontrada");

        Permissao p = Permissao.builder().id(200L).nome("p").build(); p.setDeletadoEm(java.time.LocalDateTime.now());
        when(permissaoRepository.findById(200L)).thenReturn(java.util.Optional.of(p));
        assertThatThrownBy(() -> permissaoService.buscarPorId(200L)).isInstanceOf(RuntimeException.class).hasMessageContaining("deletada");
    }

    @Test
    void deletarPermissao_throws_whenAssociatedWithPerfis() {
        Permissao p = Permissao.builder().id(3L).nome("a").build(); p.getPerfis().add(null);
        when(permissaoRepository.findById(3L)).thenReturn(java.util.Optional.of(p));

        assertThatThrownBy(() -> permissaoService.deletarPermissao(3L)).isInstanceOf(RuntimeException.class).hasMessageContaining("Não é possível deletar permissão associada a perfis");
    }

    @Test
    void listarPermissoes_success() {
        Permissao p1 = Permissao.builder().id(1L).nome("PERM_A").descricao("Desc A").build();
        Permissao p2 = Permissao.builder().id(2L).nome("PERM_B").descricao("Desc B").build();

        when(permissaoRepository.findAllAtivas()).thenReturn(java.util.List.of(p1, p2));

        var result = permissaoService.listarPermissoes();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNome()).isEqualTo("PERM_A");
        assertThat(result.get(1).getNome()).isEqualTo("PERM_B");
    }

    @Test
    void buscarPorId_success() {
        Permissao p = Permissao.builder().id(10L).nome("PERM_TEST").descricao("Desc").build();
        when(permissaoRepository.findById(10L)).thenReturn(java.util.Optional.of(p));

        var result = permissaoService.buscarPorId(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getNome()).isEqualTo("PERM_TEST");
    }

    @Test
    void atualizarPermissao_success() {
        Permissao p = Permissao.builder().id(1L).nome("PERM_ORIGINAL").descricao("Old desc").build();
        when(permissaoRepository.findById(1L)).thenReturn(java.util.Optional.of(p));

        when(permissaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreatePermissaoDTO dto = new CreatePermissaoDTO();
        dto.setDescricao("New description");

        var result = permissaoService.atualizarPermissao(1L, dto, 99L);

        assertThat(result.getDescricao()).isEqualTo("New description");
        assertThat(result.getNome()).isEqualTo("PERM_ORIGINAL");
        verify(permissaoRepository).save(any());
    }

    @Test
    void atualizarPermissao_notFound_throws() {
        when(permissaoRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        CreatePermissaoDTO dto = new CreatePermissaoDTO();
        dto.setDescricao("New description");

        assertThatThrownBy(() -> permissaoService.atualizarPermissao(999L, dto, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    void atualizarPermissao_deleted_throws() {
        Permissao p = Permissao.builder().id(1L).nome("DELETED").build();
        p.setDeletadoEm(java.time.LocalDateTime.now());
        when(permissaoRepository.findById(1L)).thenReturn(java.util.Optional.of(p));

        CreatePermissaoDTO dto = new CreatePermissaoDTO();
        dto.setDescricao("New description");

        assertThatThrownBy(() -> permissaoService.atualizarPermissao(1L, dto, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("foi deletada");
    }

    @Test
    void deletarPermissao_success() {
        Permissao p = Permissao.builder().id(1L).nome("TO_DELETE").build();
        // Sem perfis associados
        when(permissaoRepository.findById(1L)).thenReturn(java.util.Optional.of(p));
        when(permissaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        permissaoService.deletarPermissao(1L);

        verify(permissaoRepository).save(any());
    }

    @Test
    void deletarPermissao_notFound_throws() {
        when(permissaoRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> permissaoService.deletarPermissao(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    void deletarPermissao_alreadyDeleted_throws() {
        Permissao p = Permissao.builder().id(1L).nome("ALREADY_DELETED").build();
        p.setDeletadoEm(java.time.LocalDateTime.now());
        when(permissaoRepository.findById(1L)).thenReturn(java.util.Optional.of(p));

        assertThatThrownBy(() -> permissaoService.deletarPermissao(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("já foi deletada");
    }
}
