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
        dto.setNome("U"); dto.setEmail("u@example.com"); dto.setCpf("123.456.789-00"); dto.setTelefone("+5511999999999"); dto.setTipo("recepcionista");

        when(usuarioRepository.findByEmail("u@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCpf(CpfUtil.limparCpf("123.456.789-00"))).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hash");

        AuthUsuario admin = new AuthUsuario(); admin.setId(99L);
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

        assertThatThrownBy(() -> service.criarUsuario(dto, 99L)).isInstanceOf(RuntimeException.class).hasMessageContaining("Tipo de usuário inválido");
    }

    @Test
    void criarUsuario_duplicateEmailOrCpf_throws() {
        CreateUserDTO dto = new CreateUserDTO(); dto.setEmail("a@a.com"); dto.setCpf("12345678900");
        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(new AuthUsuario()));

        assertThatThrownBy(() -> service.criarUsuario(dto, 1L)).isInstanceOf(RuntimeException.class).hasMessageContaining("Email já cadastrado");

        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCpf(any())).thenReturn(Optional.of(new AuthUsuario()));

        assertThatThrownBy(() -> service.criarUsuario(dto, 1L)).isInstanceOf(RuntimeException.class).hasMessageContaining("CPF já cadastrado");
    }

    @Test
    void deletarUsuario_disallowsSelfDeletion_and_revokesSessions() {
        // attempt to delete self
        assertThatThrownBy(() -> service.deletarUsuario(1L, 1L)).isInstanceOf(RuntimeException.class).hasMessageContaining("não pode deletar sua própria conta");

        AuthUsuario u = new AuthUsuario(); u.setId(2L); u.setDeletadoEm(null);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));

        doNothing().when(sessaoService).revogarTodasSessoes(2L);

        service.deletarUsuario(2L, 99L);

        verify(sessaoService).revogarTodasSessoes(2L);
        verify(usuarioRepository).save(u);
    }
}
