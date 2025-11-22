package br.com.casadoamor.sgca.modules.admin.service;

import java.util.List;

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

import br.com.casadoamor.sgca.modules.admin.dtos.perfil.CreatePerfilDTO;
import br.com.casadoamor.sgca.modules.admin.entity.Perfil;
import br.com.casadoamor.sgca.modules.admin.entity.Permissao;
import br.com.casadoamor.sgca.modules.admin.repository.PerfilRepository;
import br.com.casadoamor.sgca.modules.admin.repository.PermissaoRepository;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    PerfilRepository perfilRepository;

    @Mock
    PermissaoRepository permissaoRepository;

    @InjectMocks
    PerfilService perfilService;

    @Test
    void criarPerfil_success_and_addPermissoes() {
        CreatePerfilDTO dto = new CreatePerfilDTO();
        dto.setNome("ROLE_TEST");
        dto.setDescricao("desc");
        dto.setPermissoesIds(List.of(1L, 2L));

        when(perfilRepository.existsByNome("ROLE_TEST")).thenReturn(false);

        Permissao p1 = Permissao.builder().id(1L).nome("P1").build();
        Permissao p2 = Permissao.builder().id(2L).nome("P2").build();
        when(permissaoRepository.findByIdIn(List.of(1L,2L))).thenReturn(List.of(p1,p2));

        Perfil saved = Perfil.builder().id(10L).nome("ROLE_TEST").descricao("desc").build();
        when(perfilRepository.save(any())).thenReturn(saved);

        var result = perfilService.criarPerfil(dto, 99L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(perfilRepository).save(any());
    }

    @Test
    void criarPerfil_duplicateName_throws() {
        CreatePerfilDTO dto = new CreatePerfilDTO(); dto.setNome("X");
        when(perfilRepository.existsByNome("X")).thenReturn(true);

        assertThatThrownBy(() -> perfilService.criarPerfil(dto, 1L)).isInstanceOf(RuntimeException.class).hasMessageContaining("já existe");
    }

    @Test
    void buscarPorId_throws_whenNotFound_orDeleted() {
        when(perfilRepository.findById(100L)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> perfilService.buscarPorId(100L)).isInstanceOf(RuntimeException.class).hasMessageContaining("Perfil não encontrado");

        Perfil pf = Perfil.builder().id(200L).nome("A").build(); pf.setDeletadoEm(java.time.LocalDateTime.now());
        when(perfilRepository.findById(200L)).thenReturn(java.util.Optional.of(pf));
        assertThatThrownBy(() -> perfilService.buscarPorId(200L)).isInstanceOf(RuntimeException.class).hasMessageContaining("deletado");
    }

    @Test
    void deletarPerfil_throws_whenHasUsers() {
        Perfil pf = Perfil.builder().id(33L).nome("p").build();
        pf.getUsuarios().add(null); // simulate users
        when(perfilRepository.findById(33L)).thenReturn(java.util.Optional.of(pf));

        assertThatThrownBy(() -> perfilService.deletarPerfil(33L)).isInstanceOf(RuntimeException.class).hasMessageContaining("Não é possível deletar perfil");
    }

}
