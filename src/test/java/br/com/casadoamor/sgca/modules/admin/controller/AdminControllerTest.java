package br.com.casadoamor.sgca.modules.admin.controller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import br.com.casadoamor.sgca.modules.admin.dtos.perfil.CreatePerfilDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.perfil.PerfilDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.permissao.CreatePermissaoDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.permissao.PermissaoDTO;
import br.com.casadoamor.sgca.modules.admin.service.PerfilService;
import br.com.casadoamor.sgca.modules.admin.service.PermissaoService;
import br.com.casadoamor.sgca.modules.admin.service.UserManagementService;
import br.com.casadoamor.sgca.modules.auth.service.AuthService;
import br.com.casadoamor.sgca.modules.common.service.UserPhotoService;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    UserManagementService userManagementService;

    @Mock
    PerfilService perfilService;

    @Mock
    PermissaoService permissaoService;

    @Mock
    AuthService authService;

    @Mock
    UserPhotoService userPhotoService;

    @InjectMocks
    AdminController controller;

    @Mock
    Authentication authentication;

    @Test
    void criarPerfil_success_and_authMissing_returnsBadRequest() {
        CreatePerfilDTO request = new CreatePerfilDTO(); request.setNome("R");
        when(authentication.getName()).thenReturn("cpf1");

        // success case
        when(authService.findUserIdByCpf("cpf1")).thenReturn(Optional.of(99L));
        when(perfilService.criarPerfil(request, 99L)).thenReturn(PerfilDTO.builder().id(10L).nome("R").build());

        ResponseEntity<?> r = controller.criarPerfil(request, authentication);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // failure when admin not found
        when(authService.findUserIdByCpf("cpf1")).thenReturn(Optional.empty());
        ResponseEntity<?> r2 = controller.criarPerfil(request, authentication);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((Object)r2.getBody()).getClass().getSimpleName()).isEqualTo("ErrorResponse");
    }

    @Test
    void listarPerfis_ok_and_buscarPerfil_notFound() {
        when(perfilService.listarPerfis()).thenReturn(List.of(PerfilDTO.builder().id(1L).build()));
        var r = controller.listarPerfis();
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).hasSize(1);

        when(perfilService.buscarPorId(5L)).thenThrow(new RuntimeException("not found"));
        var r2 = controller.buscarPerfil(5L);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(((Object)r2.getBody()).getClass().getSimpleName()).isEqualTo("ErrorResponse");
    }

    @Test
    void criarPermissao_success_and_duplicate_returnsBadRequest() {
        when(authentication.getName()).thenReturn("cpfX");
        CreatePermissaoDTO req = new CreatePermissaoDTO(); req.setNome("P");

        when(authService.findUserIdByCpf("cpfX")).thenReturn(Optional.of(7L));
        when(permissaoService.criarPermissao(req, 7L)).thenReturn(PermissaoDTO.builder().id(3L).nome("P").build());

        ResponseEntity<?> res = controller.criarPermissao(req, authentication);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        when(authService.findUserIdByCpf("cpfX")).thenReturn(Optional.empty());
        ResponseEntity<?> res2 = controller.criarPermissao(req, authentication);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
