package br.com.casadoamor.sgca.modules.admin.controller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import br.com.casadoamor.sgca.modules.admin.dtos.perfil.AtribuirPermissoesDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.perfil.CreatePerfilDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.perfil.PerfilDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.permissao.CreatePermissaoDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.permissao.PermissaoDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.user.AtribuirRolesDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.user.CreateUserDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.user.UserResponseDTO;
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

    private static final String ADMIN_CPF = "12345678900";
    private static final Long ADMIN_ID = 1L;

    @BeforeEach
    void setUp() {
        // Common setup if needed, but most tests will set specific expectations
    }

    // ==================== USUÁRIOS ====================

    @Test
    void criarUsuario_Success() {
        CreateUserDTO request = new CreateUserDTO();
        request.setNome("Test User");

        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(userManagementService.criarUsuario(request, ADMIN_ID))
                .thenReturn(UserResponseDTO.builder().id(2L).nome("Test User").build());

        ResponseEntity<?> response = controller.criarUsuario(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(UserResponseDTO.class);
    }

    @Test
    void criarUsuario_AdminNotFound() {
        CreateUserDTO request = new CreateUserDTO();
        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.criarUsuario(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void criarUsuario_ServiceException() {
        CreateUserDTO request = new CreateUserDTO();
        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(userManagementService.criarUsuario(request, ADMIN_ID))
                .thenThrow(new RuntimeException("Email already exists"));

        ResponseEntity<?> response = controller.criarUsuario(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listarUsuarios_Success() {
        Page<UserResponseDTO> page = new PageImpl<>(List.of(UserResponseDTO.builder().id(1L).build()));
        when(userManagementService.listarUsuarios(any(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<UserResponseDTO>> response = controller.listarUsuarios(Pageable.unpaged(), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void buscarUsuario_Success() {
        when(userManagementService.buscarPorId(1L)).thenReturn(UserResponseDTO.builder().id(1L).build());

        ResponseEntity<?> response = controller.buscarUsuario(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void buscarUsuario_NotFound() {
        when(userManagementService.buscarPorId(1L)).thenThrow(new RuntimeException("User not found"));

        ResponseEntity<?> response = controller.buscarUsuario(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void atualizarUsuario_Success() {
        UpdateUserDTO request = new UpdateUserDTO();
        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(userManagementService.atualizarUsuario(eq(2L), eq(request), eq(ADMIN_ID)))
                .thenReturn(UserResponseDTO.builder().id(2L).build());

        ResponseEntity<?> response = controller.atualizarUsuario(2L, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void atualizarUsuario_AdminNotFound() {
        UpdateUserDTO request = new UpdateUserDTO();
        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.atualizarUsuario(2L, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deletarUsuario_Success() {
        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        doNothing().when(userManagementService).deletarUsuario(2L, ADMIN_ID);

        ResponseEntity<?> response = controller.deletarUsuario(2L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void atribuirPerfis_Success() {
        AtribuirRolesDTO request = new AtribuirRolesDTO();
        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(userManagementService.atribuirPerfis(eq(2L), eq(request), eq(ADMIN_ID)))
                .thenReturn(UserResponseDTO.builder().id(2L).build());

        ResponseEntity<?> response = controller.atribuirPerfis(2L, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void forceLogout_Success() {
        doNothing().when(userManagementService).forceLogout(2L);

        ResponseEntity<?> response = controller.forceLogout(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void toggleUserStatus_Success() {
        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(userManagementService.toggleUserStatus(2L, ADMIN_ID))
                .thenReturn(UserResponseDTO.builder().id(2L).ativo(true).build());

        ResponseEntity<?> response = controller.toggleUserStatus(2L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==================== PERFIS (ROLES) ====================

    @Test
    void criarPerfil_Success() {
        CreatePerfilDTO request = new CreatePerfilDTO();
        request.setNome("ROLE_TEST");

        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(perfilService.criarPerfil(request, ADMIN_ID))
                .thenReturn(PerfilDTO.builder().id(1L).nome("ROLE_TEST").build());

        ResponseEntity<?> response = controller.criarPerfil(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void listarPerfis_Success() {
        when(perfilService.listarPerfis()).thenReturn(List.of(PerfilDTO.builder().id(1L).build()));

        ResponseEntity<List<PerfilDTO>> response = controller.listarPerfis();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void buscarPerfil_Success() {
        when(perfilService.buscarPorId(1L)).thenReturn(PerfilDTO.builder().id(1L).build());

        ResponseEntity<?> response = controller.buscarPerfil(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void buscarPerfil_NotFound() {
        when(perfilService.buscarPorId(1L)).thenThrow(new RuntimeException("Profile not found"));

        ResponseEntity<?> response = controller.buscarPerfil(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void atualizarPerfil_Success() {
        CreatePerfilDTO request = new CreatePerfilDTO();
        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(perfilService.atualizarPerfil(eq(1L), eq(request), eq(ADMIN_ID)))
                .thenReturn(PerfilDTO.builder().id(1L).build());

        ResponseEntity<?> response = controller.atualizarPerfil(1L, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deletarPerfil_Success() {
        doNothing().when(perfilService).deletarPerfil(1L);

        ResponseEntity<?> response = controller.deletarPerfil(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==================== PERMISSÕES ====================

    @Test
    void criarPermissao_Success() {
        CreatePermissaoDTO request = new CreatePermissaoDTO();
        request.setNome("PERM_TEST");

        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(permissaoService.criarPermissao(request, ADMIN_ID))
                .thenReturn(PermissaoDTO.builder().id(1L).nome("PERM_TEST").build());

        ResponseEntity<?> response = controller.criarPermissao(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void listarPermissoes_Success() {
        when(permissaoService.listarPermissoes()).thenReturn(List.of(PermissaoDTO.builder().id(1L).build()));

        ResponseEntity<List<PermissaoDTO>> response = controller.listarPermissoes();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void buscarPermissao_Success() {
        when(permissaoService.buscarPorId(1L)).thenReturn(PermissaoDTO.builder().id(1L).build());

        ResponseEntity<?> response = controller.buscarPermissao(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void buscarPermissao_NotFound() {
        when(permissaoService.buscarPorId(1L)).thenThrow(new RuntimeException("Permission not found"));

        ResponseEntity<?> response = controller.buscarPermissao(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void atualizarPermissao_Success() {
        CreatePermissaoDTO request = new CreatePermissaoDTO();
        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(permissaoService.atualizarPermissao(eq(1L), eq(request), eq(ADMIN_ID)))
                .thenReturn(PermissaoDTO.builder().id(1L).build());

        ResponseEntity<?> response = controller.atualizarPermissao(1L, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==================== GESTÃO DE PERMISSÕES EM PERFIS ====================

    @Test
    void adicionarPermissoesAoPerfil_Success() {
        AtribuirPermissoesDTO request = new AtribuirPermissoesDTO();
        request.setPermissoesIds(List.of(1L, 2L));

        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(perfilService.adicionarPermissoes(eq(1L), eq(request.getPermissoesIds()), eq(ADMIN_ID)))
                .thenReturn(PerfilDTO.builder().id(1L).build());

        ResponseEntity<?> response = controller.adicionarPermissoesAoPerfil(1L, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void removerPermissoesDoPerfil_Success() {
        AtribuirPermissoesDTO request = new AtribuirPermissoesDTO();
        request.setPermissoesIds(List.of(1L));

        when(authentication.getName()).thenReturn(ADMIN_CPF);
        when(authService.findUserIdByCpf(ADMIN_CPF)).thenReturn(Optional.of(ADMIN_ID));
        when(perfilService.removerPermissoes(eq(1L), eq(request.getPermissoesIds()), eq(ADMIN_ID)))
                .thenReturn(PerfilDTO.builder().id(1L).build());

        ResponseEntity<?> response = controller.removerPermissoesDoPerfil(1L, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void obterPermissoesEfetivas_Success() {
        when(userManagementService.obterPermissoesEfetivas(2L))
                .thenReturn(List.of(PermissaoDTO.builder().id(1L).build()));

        ResponseEntity<?> response = controller.obterPermissoesEfetivas(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ==================== FOTOS DE PERFIL ====================

    @Test
    void uploadFoto_Success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(userPhotoService.uploadFoto(2L, file)).thenReturn("http://url.com/foto.jpg");

        ResponseEntity<?> response = controller.uploadFoto(2L, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void uploadFoto_IOException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(userPhotoService.uploadFoto(2L, file)).thenThrow(new java.io.IOException("Error reading file"));

        ResponseEntity<?> response = controller.uploadFoto(2L, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void obterFoto_Success() {
        when(userPhotoService.obterUrlFoto(2L)).thenReturn("http://url.com/foto.jpg");

        ResponseEntity<?> response = controller.obterFoto(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void obterFoto_NotFound() {
        when(userPhotoService.obterUrlFoto(2L)).thenReturn(null);

        ResponseEntity<?> response = controller.obterFoto(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletarFoto_Success() {
        doNothing().when(userPhotoService).deletarFoto(2L);

        ResponseEntity<?> response = controller.deletarFoto(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
