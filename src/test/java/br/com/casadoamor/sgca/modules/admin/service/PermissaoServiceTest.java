package br.com.casadoamor.sgca.modules.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
}
