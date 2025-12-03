package br.com.casadoamor.sgca.modules.admin.service;

import br.com.casadoamor.sgca.modules.admin.dtos.auditoria.AuditoriaPerfilDTO;
import br.com.casadoamor.sgca.modules.admin.dtos.auditoria.AuditoriaUsuarioDTO;
import br.com.casadoamor.sgca.modules.admin.entity.Perfil;
import br.com.casadoamor.sgca.modules.admin.repository.PerfilRepository;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaAdminServiceTest {

    @Mock
    private AuthUsuarioRepository authUsuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @InjectMocks
    private AuditoriaAdminService service;

    @Test
    void buscarAuditoriaUsuario_success() {
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setCpf("12345678900");
        usuario.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        usuario.setAtivo(true);
        usuario.setCriadoEm(LocalDateTime.now());

        AuthUsuario criador = new AuthUsuario();
        criador.setId(99L);
        criador.setNome("Creator");
        criador.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        usuario.setCriadoPor(criador);

        when(authUsuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        AuditoriaUsuarioDTO result = service.buscarAuditoriaUsuario(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCriadoPor()).isNotNull();
        assertThat(result.getCriadoPor().getNome()).isEqualTo("Creator");
    }

    @Test
    void buscarAuditoriaUsuario_notFound_throws() {
        when(authUsuarioRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarAuditoriaUsuario(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    void buscarAuditoriaPerfil_success() {
        Perfil perfil = new Perfil();
        perfil.setId(10L);
        perfil.setNome("ROLE_TEST");
        perfil.setDescricao("Test Role");
        perfil.setCriadoEm(LocalDateTime.now());
        perfil.setCriadoPor(99L);

        when(perfilRepository.findById(10L)).thenReturn(Optional.of(perfil));

        AuthUsuario criador = new AuthUsuario();
        criador.setId(99L);
        criador.setNome("Creator");
        criador.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(authUsuarioRepository.findById(99L)).thenReturn(Optional.of(criador));

        AuditoriaPerfilDTO result = service.buscarAuditoriaPerfil(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getCriadoPor()).isNotNull();
        assertThat(result.getCriadoPor().getNome()).isEqualTo("Creator");
    }

    @Test
    void listarAuditoriaPerfis_success() {
        Perfil p1 = new Perfil();
        p1.setId(1L);
        p1.setNome("A");
        Perfil p2 = new Perfil();
        p2.setId(2L);
        p2.setNome("B");

        when(perfilRepository.findAll()).thenReturn(List.of(p1, p2));
        when(authUsuarioRepository.countUsuariosByPerfilId(1L)).thenReturn(5L);
        when(authUsuarioRepository.countUsuariosByPerfilId(2L)).thenReturn(0L);

        List<AuditoriaPerfilDTO> result = service.listarAuditoriaPerfis();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTotalUsuarios()).isEqualTo(5);
    }

    @Test
    void buscarAuditoriaPerfil_notFound_throws() {
        when(perfilRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarAuditoriaPerfil(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Perfil não encontrado");
    }

    @Test
    void buscarAuditoriaUsuario_withDadosPessoaisEnderecoAndRegistroProfissional_success() {
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setCpf("12345678900");
        usuario.setTipo(AuthUsuario.TipoUsuario.MEDICO);
        usuario.setAtivo(true);
        usuario.setCriadoEm(LocalDateTime.now());

        // Dados Pessoais
        br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioDadosPessoais dadosPessoais =
                new br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioDadosPessoais();
        dadosPessoais.setCriadoEm(LocalDateTime.now());
        AuthUsuario criadorDados = new AuthUsuario();
        criadorDados.setId(50L);
        criadorDados.setNome("Criador Dados");
        criadorDados.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        dadosPessoais.setCriadoPor(criadorDados);
        usuario.setDadosPessoais(dadosPessoais);

        // Endereço
        br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioEndereco endereco =
                new br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioEndereco();
        endereco.setCriadoEm(LocalDateTime.now());
        AuthUsuario criadorEndereco = new AuthUsuario();
        criadorEndereco.setId(51L);
        criadorEndereco.setNome("Criador Endereco");
        criadorEndereco.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        endereco.setCriadoPor(criadorEndereco);
        usuario.setEndereco(endereco);

        // Registro Profissional
        br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioRegistroProfissional registro =
                new br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioRegistroProfissional();
        registro.setTipoProfissional(
                br.com.casadoamor.sgca.modules.auth.entity.AuthUsuarioRegistroProfissional.TipoProfissional.MEDICO);
        registro.setNumeroRegistro("CRM123456");
        registro.setCriadoEm(LocalDateTime.now());
        AuthUsuario criadorRegistro = new AuthUsuario();
        criadorRegistro.setId(52L);
        criadorRegistro.setNome("Criador Registro");
        criadorRegistro.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        registro.setCriadoPor(criadorRegistro);
        usuario.setRegistroProfissional(registro);

        when(authUsuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        AuditoriaUsuarioDTO result = service.buscarAuditoriaUsuario(1L);

        assertThat(result).isNotNull();
        assertThat(result.getDadosPessoais()).isNotNull();
        assertThat(result.getDadosPessoais().getCriadoPor().getNome()).isEqualTo("Criador Dados");
        assertThat(result.getEndereco()).isNotNull();
        assertThat(result.getEndereco().getCriadoPor().getNome()).isEqualTo("Criador Endereco");
        assertThat(result.getRegistroProfissional()).isNotNull();
        assertThat(result.getRegistroProfissional().getTipoProfissional()).isEqualTo("MEDICO");
        assertThat(result.getRegistroProfissional().getCriadoPor().getNome()).isEqualTo("Criador Registro");
    }

    @Test
    void buscarAuditoriaUsuario_withPerfis_success() {
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setCpf("12345678900");
        usuario.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        usuario.setAtivo(true);
        usuario.setCriadoEm(LocalDateTime.now());

        // Perfil com permissões
        Perfil perfil = new Perfil();
        perfil.setId(10L);
        perfil.setNome("ADMIN");
        perfil.setDescricao("Admin Role");

        br.com.casadoamor.sgca.modules.admin.entity.Permissao permissao =
                new br.com.casadoamor.sgca.modules.admin.entity.Permissao();
        permissao.setId(100L);
        permissao.setNome("USERS_MANAGE");
        permissao.setDescricao("Manage users");
        perfil.getPermissoes().add(permissao);

        usuario.getPerfis().add(perfil);

        when(authUsuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        AuditoriaUsuarioDTO result = service.buscarAuditoriaUsuario(1L);

        assertThat(result.getPerfis()).hasSize(1);
        assertThat(result.getPerfis().get(0).getNome()).isEqualTo("ADMIN");
        assertThat(result.getPerfis().get(0).getPermissoes()).hasSize(1);
        assertThat(result.getPerfis().get(0).getPermissoes().get(0).getNome()).isEqualTo("USERS_MANAGE");
    }

    @Test
    void buscarAuditoriaPerfil_withPermissoesAndUsuarios_success() {
        Perfil perfil = new Perfil();
        perfil.setId(10L);
        perfil.setNome("ROLE_TEST");
        perfil.setDescricao("Test Role");
        perfil.setCriadoEm(LocalDateTime.now());
        perfil.setCriadoPor(99L);

        // Permissão
        br.com.casadoamor.sgca.modules.admin.entity.Permissao permissao =
                new br.com.casadoamor.sgca.modules.admin.entity.Permissao();
        permissao.setId(1L);
        permissao.setNome("PERM_TEST");
        permissao.setCriadoEm(LocalDateTime.now());
        permissao.setCriadoPor(88L);
        perfil.getPermissoes().add(permissao);

        // Usuário vinculado ao perfil
        AuthUsuario usuarioVinculado = new AuthUsuario();
        usuarioVinculado.setId(1L);
        usuarioVinculado.setNome("User 1");
        usuarioVinculado.setEmail("user1@test.com");
        usuarioVinculado.setTipo(AuthUsuario.TipoUsuario.RECEPCIONISTA);
        perfil.getUsuarios().add(usuarioVinculado);

        when(perfilRepository.findById(10L)).thenReturn(Optional.of(perfil));

        AuthUsuario criador = new AuthUsuario();
        criador.setId(99L);
        criador.setNome("Creator");
        criador.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(authUsuarioRepository.findById(99L)).thenReturn(Optional.of(criador));

        AuthUsuario criadorPerm = new AuthUsuario();
        criadorPerm.setId(88L);
        criadorPerm.setNome("Perm Creator");
        criadorPerm.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(authUsuarioRepository.findById(88L)).thenReturn(Optional.of(criadorPerm));

        AuditoriaPerfilDTO result = service.buscarAuditoriaPerfil(10L);

        assertThat(result.getPermissoes()).hasSize(1);
        assertThat(result.getPermissoes().get(0).getNome()).isEqualTo("PERM_TEST");
        assertThat(result.getPermissoes().get(0).getCriadoPor().getNome()).isEqualTo("Perm Creator");
        assertThat(result.getUsuarios()).hasSize(1);
        assertThat(result.getUsuarios().get(0).getNome()).isEqualTo("User 1");
    }

    @Test
    void listarAuditoriaPerfis_withCriadoPorAndAtualizadoPor() {
        Perfil p1 = new Perfil();
        p1.setId(1L);
        p1.setNome("A");
        p1.setCriadoPor(10L);
        p1.setAtualizadoPor(20L);

        when(perfilRepository.findAll()).thenReturn(List.of(p1));
        when(authUsuarioRepository.countUsuariosByPerfilId(1L)).thenReturn(3L);

        AuthUsuario criador = new AuthUsuario();
        criador.setId(10L);
        criador.setNome("Criador");
        criador.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(authUsuarioRepository.findById(10L)).thenReturn(Optional.of(criador));

        AuthUsuario atualizador = new AuthUsuario();
        atualizador.setId(20L);
        atualizador.setNome("Atualizador");
        atualizador.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        when(authUsuarioRepository.findById(20L)).thenReturn(Optional.of(atualizador));

        List<AuditoriaPerfilDTO> result = service.listarAuditoriaPerfis();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCriadoPor().getNome()).isEqualTo("Criador");
        assertThat(result.get(0).getAtualizadoPor().getNome()).isEqualTo("Atualizador");
    }

    @Test
    void buscarAuditoriaUsuario_withAtualizadoPor_success() {
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(1L);
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setCpf("12345678900");
        usuario.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        usuario.setAtivo(true);
        usuario.setCriadoEm(LocalDateTime.now());

        AuthUsuario atualizador = new AuthUsuario();
        atualizador.setId(50L);
        atualizador.setNome("Atualizador");
        atualizador.setTipo(AuthUsuario.TipoUsuario.ADMINISTRADOR);
        usuario.setAtualizadoPor(atualizador);

        when(authUsuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        AuditoriaUsuarioDTO result = service.buscarAuditoriaUsuario(1L);

        assertThat(result.getAtualizadoPor()).isNotNull();
        assertThat(result.getAtualizadoPor().getNome()).isEqualTo("Atualizador");
    }
}
