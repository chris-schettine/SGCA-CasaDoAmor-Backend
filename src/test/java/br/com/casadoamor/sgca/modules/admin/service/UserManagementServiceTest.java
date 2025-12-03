package br.com.casadoamor.sgca.modules.admin.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.casadoamor.sgca.infra.util.CpfUtil;
import br.com.casadoamor.sgca.modules.admin.dtos.user.CreateUserDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.user.UserResponseDTO;
import br.com.casadoamor.sgca.modules.admin.repository.PerfilRepository;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.auth.service.AccountActivationService;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    AuthUsuarioRepository usuarioRepository;

    @Mock
    PerfilRepository perfilRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    SessaoService sessaoService;

    @Mock
    AccountActivationService accountActivationService;

    @Mock
    br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioEnderecoRepository enderecoRepository;

    @Mock
    br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioDadosPessoaisRepository dadosPessoaisRepository;

    @Mock
    br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRegistroProfissionalRepository registroProfissionalRepository;

    @InjectMocks
    UserManagementService service;

    @Test
    void criarUsuario_success_and_handlesInvalidType() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setNome("U");
        dto.setEmail("u@example.com");
        dto.setCpf("123.456.789-00");
        dto.setTelefone("+5511999999999");
        dto.setTipo("recepcionista");

        when(usuarioRepository.findByEmail("u@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCpf(CpfUtil.limparCpf("123.456.789-00"))).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hash");

        AuthUsuario admin = new AuthUsuario();
        admin.setId(99L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(admin));

        when(usuarioRepository.save(any())).thenAnswer(i -> {
            AuthUsuario u = i.getArgument(0);
            u.setId(55L);
            return u;
        });

        // ensure account activation doesn't throw
        doNothing().when(accountActivationService).enviarEmailAtivacao(any(), any());

        UserResponseDTO out = service.criarUsuario(dto, 99L);

        assertThat(out).isNotNull();
        assertThat(out.getId()).isEqualTo(55L);

        // invalid type should throw
        dto.setTipo("INVALIDTYPE");
        when(usuarioRepository.findByEmail("u@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCpf(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarUsuario(dto, 99L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tipo de usuário inválido");
    }

    @Test
    void criarUsuario_duplicateEmailOrCpf_throws() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setEmail("a@a.com");
        dto.setCpf("12345678900");
        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(new AuthUsuario()));

        assertThatThrownBy(() -> service.criarUsuario(dto, 1L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email já cadastrado");

        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCpf(any())).thenReturn(Optional.of(new AuthUsuario()));

        assertThatThrownBy(() -> service.criarUsuario(dto, 1L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CPF já cadastrado");
    }

    @Test
    void listarUsuarios_returnsPageOfDTOs() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        AuthUsuario u1 = new AuthUsuario();
        u1.setId(1L);
        u1.setNome("A");
        u1.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        AuthUsuario u2 = new AuthUsuario();
        u2.setId(2L);
        u2.setNome("B");
        u2.setTipo(AuthUsuario.TipoUsuario.MEDICO);

        org.springframework.data.domain.Page<AuthUsuario> page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(u1, u2));

        when(usuarioRepository.findAll(pageable)).thenReturn(page);

        var result = service.listarUsuarios(pageable);

        assertThat(result).hasSize(2);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("A");
    }

    @Test
    void listarUsuarios_withSearch_returnsFilteredPage() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        AuthUsuario u1 = new AuthUsuario();
        u1.setId(1L);
        u1.setNome("SearchMatch");
        u1.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);

        org.springframework.data.domain.Page<AuthUsuario> page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(u1));

        when(usuarioRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        var result = service.listarUsuarios("Search", pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("SearchMatch");
    }

    @Test
    void buscarPorId_success() {
        AuthUsuario u = new AuthUsuario();
        u.setId(10L);
        u.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(u));

        var result = service.buscarPorId(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void buscarPorId_notFound_throws() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(999L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void atualizarUsuario_success() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        u.setEmail("old@mail.com");
        u.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        AuthUsuario admin = new AuthUsuario();
        admin.setId(99L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(admin));

        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO dto = new br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO();
        dto.setNome("New Name");
        dto.setEmail("new@mail.com");

        var result = service.atualizarUsuario(1L, dto, 99L);

        assertThat(result.getNome()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("new@mail.com");
        assertThat(result.getEmailVerificado()).isFalse();
    }

    @Test
    void atualizarUsuario_selfDeactivation_throws() {
        br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO dto = new br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO();
        dto.setAtivo(false);

        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.atualizarUsuario(1L, dto, 1L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não pode desativar sua própria conta");
    }

    @Test
    void deletarUsuario_disallowsSelfDeletion_and_revokesSessions() {
        // attempt to delete self
        assertThatThrownBy(() -> service.deletarUsuario(1L, 1L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não pode deletar sua própria conta");

        AuthUsuario u = new AuthUsuario();
        u.setId(2L);
        u.setDeletadoEm(null);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));

        doNothing().when(sessaoService).revogarTodasSessoes(2L);

        service.deletarUsuario(2L, 99L);

        verify(sessaoService).revogarTodasSessoes(2L);
        verify(usuarioRepository).save(u);
    }

    @Test
    void atribuirPerfis_success() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        u.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        br.com.casadoamor.sgca.modules.admin.entity.Perfil p = new br.com.casadoamor.sgca.modules.admin.entity.Perfil();
        p.setId(10L);
        when(perfilRepository.findByIdIn(java.util.List.of(10L))).thenReturn(java.util.List.of(p));

        br.com.casadoamor.sgca.modules.admin.dtos.user.AtribuirRolesDTO dto = new br.com.casadoamor.sgca.modules.admin.dtos.user.AtribuirRolesDTO();
        dto.setPerfisIds(java.util.List.of(10L));

        service.atribuirPerfis(1L, dto, 99L);

        assertThat(u.getPerfis()).contains(p);
        verify(usuarioRepository).save(u);
    }

    @Test
    void forceLogout_success() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        service.forceLogout(1L);
        verify(sessaoService).revogarTodasSessoes(1L);
    }

    @Test
    void toggleUserStatus_success() {
        AuthUsuario u = new AuthUsuario();
        u.setId(2L);
        u.setAtivo(true);
        u.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));

        AuthUsuario admin = new AuthUsuario();
        admin.setId(99L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(admin));

        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.toggleUserStatus(2L, 99L);

        assertThat(result.getAtivo()).isFalse();
        verify(sessaoService).revogarTodasSessoes(2L);
    }

    @Test
    void obterPermissoesEfetivas_success() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        br.com.casadoamor.sgca.modules.admin.entity.Perfil p = new br.com.casadoamor.sgca.modules.admin.entity.Perfil();
        br.com.casadoamor.sgca.modules.admin.entity.Permissao perm = new br.com.casadoamor.sgca.modules.admin.entity.Permissao();
        perm.setId(100L);
        perm.setNome("PERM_1");
        p.getPermissoes().add(perm);
        u.getPerfis().add(p);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        var result = service.obterPermissoesEfetivas(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("PERM_1");
    }

    @Test
    void criarUsuario_withDadosPessoaisAndEndereco_success() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setNome("User");
        dto.setEmail("user@test.com");
        dto.setCpf("98765432100");
        dto.setTelefone("+5511988888888");
        dto.setTipo("medico");

        // Dados Pessoais
        br.com.casadoamor.sgca.modules.auth.dtos.AuthUsuarioDadosPessoaisDTO dadosPessoaisDTO =
                new br.com.casadoamor.sgca.modules.auth.dtos.AuthUsuarioDadosPessoaisDTO();
        dadosPessoaisDTO.setNomeMae("Mae");
        dto.setDadosPessoais(dadosPessoaisDTO);

        // Endereço
        br.com.casadoamor.sgca.modules.auth.dtos.AuthUsuarioEnderecoDTO enderecoDTO =
                new br.com.casadoamor.sgca.modules.auth.dtos.AuthUsuarioEnderecoDTO();
        enderecoDTO.setLogradouro("Rua Teste");
        enderecoDTO.setCidade("São Paulo");
        dto.setEndereco(enderecoDTO);

        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCpf(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hash");

        AuthUsuario admin = new AuthUsuario();
        admin.setId(99L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(admin));

        when(usuarioRepository.save(any())).thenAnswer(i -> {
            AuthUsuario u = i.getArgument(0);
            u.setId(77L);
            return u;
        });

        // Mock para salvar dados pessoais
        when(dadosPessoaisRepository.save(any())).thenAnswer(i -> {
            br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioDadosPessoais dp = i.getArgument(0);
            dp.setId(1L);
            return dp;
        });

        // Mock para salvar endereço
        when(enderecoRepository.save(any())).thenAnswer(i -> {
            br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioEndereco end = i.getArgument(0);
            end.setId(1L);
            return end;
        });

        doNothing().when(accountActivationService).enviarEmailAtivacao(any(), any());

        UserResponseDTO out = service.criarUsuario(dto, 99L);

        assertThat(out).isNotNull();
        assertThat(out.getId()).isEqualTo(77L);
        verify(dadosPessoaisRepository).save(any());
        verify(enderecoRepository).save(any());
    }

    @Test
    void atualizarUsuario_duplicateEmail_throws() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        u.setEmail("old@mail.com");
        u.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        // Simula outro usuário com o novo email
        when(usuarioRepository.findByEmail("taken@mail.com")).thenReturn(Optional.of(new AuthUsuario()));

        br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO dto =
                new br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO();
        dto.setEmail("taken@mail.com");

        assertThatThrownBy(() -> service.atualizarUsuario(1L, dto, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email já cadastrado");
    }

    @Test
    void atualizarUsuario_invalidTipo_throws() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        u.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO dto =
                new br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO();
        dto.setTipo("INVALID_TYPE");

        assertThatThrownBy(() -> service.atualizarUsuario(1L, dto, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tipo de usuário inválido");
    }

    @Test
    void atualizarUsuario_withDadosPessoaisUpdate_success() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        u.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);

        // Usuário já tem dados pessoais
        br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioDadosPessoais existingDados =
                new br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioDadosPessoais();
        existingDados.setId(1L);
        u.setDadosPessoais(existingDados);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        AuthUsuario admin = new AuthUsuario();
        admin.setId(99L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(admin));

        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(dadosPessoaisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO dto =
                new br.com.casadoamor.sgca.modules.admin.dtos.user.UpdateUserDTO();

        br.com.casadoamor.sgca.modules.auth.dtos.AuthUsuarioDadosPessoaisDTO dadosDTO =
                new br.com.casadoamor.sgca.modules.auth.dtos.AuthUsuarioDadosPessoaisDTO();
        dadosDTO.setNomePai("Novo Pai");
        dto.setDadosPessoais(dadosDTO);

        var result = service.atualizarUsuario(1L, dto, 99L);

        assertThat(result).isNotNull();
        verify(dadosPessoaisRepository).save(any());
    }

    @Test
    void deletarUsuario_alreadyDeleted_throws() {
        AuthUsuario u = new AuthUsuario();
        u.setId(2L);
        u.setDeletadoEm(java.time.LocalDateTime.now());

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.deletarUsuario(2L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("já foi deletado");
    }

    @Test
    void atribuirPerfis_perfilNotFound_throws() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        // Retorna lista vazia (perfis não encontrados)
        when(perfilRepository.findByIdIn(java.util.List.of(999L))).thenReturn(java.util.List.of());

        br.com.casadoamor.sgca.modules.admin.dtos.user.AtribuirRolesDTO dto =
                new br.com.casadoamor.sgca.modules.admin.dtos.user.AtribuirRolesDTO();
        dto.setPerfisIds(java.util.List.of(999L));

        assertThatThrownBy(() -> service.atribuirPerfis(1L, dto, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Alguns perfis não foram encontrados");
    }

    @Test
    void atribuirPerfis_perfilDeletado_throws() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        br.com.casadoamor.sgca.modules.admin.entity.Perfil deletedPerfil =
                new br.com.casadoamor.sgca.modules.admin.entity.Perfil();
        deletedPerfil.setId(10L);
        deletedPerfil.setNome("DELETED_ROLE");
        deletedPerfil.setDeletadoEm(java.time.LocalDateTime.now());

        when(perfilRepository.findByIdIn(java.util.List.of(10L))).thenReturn(java.util.List.of(deletedPerfil));

        br.com.casadoamor.sgca.modules.admin.dtos.user.AtribuirRolesDTO dto =
                new br.com.casadoamor.sgca.modules.admin.dtos.user.AtribuirRolesDTO();
        dto.setPerfisIds(java.util.List.of(10L));

        assertThatThrownBy(() -> service.atribuirPerfis(1L, dto, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("foi deletado");
    }

    @Test
    void forceLogout_userNotFound_throws() {
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.forceLogout(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void toggleUserStatus_self_throws() {
        assertThatThrownBy(() -> service.toggleUserStatus(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não pode alterar o status da sua própria conta");
    }

    @Test
    void toggleUserStatus_activateUser_noSessionRevoke() {
        AuthUsuario u = new AuthUsuario();
        u.setId(2L);
        u.setAtivo(false); // Usuário inativo
        u.setTipo(AuthUsuario.TipoUsuario.RECEPCIONISTA);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));

        AuthUsuario admin = new AuthUsuario();
        admin.setId(99L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(admin));

        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.toggleUserStatus(2L, 99L);

        assertThat(result.getAtivo()).isTrue();
        // Não deve revogar sessões ao ativar
        verify(sessaoService, org.mockito.Mockito.never()).revogarTodasSessoes(any());
    }

    @Test
    void obterPermissoesEfetivas_filtersDeletedPerfisAndPermissoes() {
        AuthUsuario u = new AuthUsuario();
        u.setId(1L);

        // Perfil ativo com permissão deletada
        br.com.casadoamor.sgca.modules.admin.entity.Perfil p1 =
                new br.com.casadoamor.sgca.modules.admin.entity.Perfil();
        br.com.casadoamor.sgca.modules.admin.entity.Permissao deletedPerm =
                new br.com.casadoamor.sgca.modules.admin.entity.Permissao();
        deletedPerm.setId(1L);
        deletedPerm.setNome("DELETED_PERM");
        deletedPerm.setDeletadoEm(java.time.LocalDateTime.now());
        p1.getPermissoes().add(deletedPerm);

        // Perfil deletado
        br.com.casadoamor.sgca.modules.admin.entity.Perfil deletedPerfil =
                new br.com.casadoamor.sgca.modules.admin.entity.Perfil();
        deletedPerfil.setDeletadoEm(java.time.LocalDateTime.now());
        br.com.casadoamor.sgca.modules.admin.entity.Permissao activePerm =
                new br.com.casadoamor.sgca.modules.admin.entity.Permissao();
        activePerm.setId(2L);
        activePerm.setNome("ACTIVE_PERM");
        deletedPerfil.getPermissoes().add(activePerm);

        u.getPerfis().add(p1);
        u.getPerfis().add(deletedPerfil);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        var result = service.obterPermissoesEfetivas(1L);

        // Ambas devem ser filtradas
        assertThat(result).isEmpty();
    }
}
