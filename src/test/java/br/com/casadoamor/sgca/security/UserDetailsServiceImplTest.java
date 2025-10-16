package br.com.casadoamor.sgca.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;

class UserDetailsServiceImplTest {

    @Mock
    private AuthUsuarioRepository repository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void UserDetailsServiceImpl_loadUserByUsername_ExistingUser_ReturnsUserDetails() {
        AuthUsuario u = AuthUsuario.builder()
                .cpf("12345678900")
                .senhaHash("hashedpwd")
                .ativo(true)
                .tipo(AuthUsuario.TipoUsuario.RECEPCIONISTA)
                .lockedUntil(null)
                .build();

        when(repository.findByCpf("12345678900")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("12345678900");

        assertNotNull(details);
        assertEquals("12345678900", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("RECEPCIONISTA")));
    }

    @Test
    void UserDetailsServiceImpl_loadUserByUsername_NonExistingUser_ThrowsUsernameNotFound() {
        when(repository.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("00000000000"));
    }

    @Test
    void UserDetailsServiceImpl_loadUserByUsername_InactiveUser_ThrowsUsernameNotFound() {
        AuthUsuario u = AuthUsuario.builder()
                .cpf("22233344455")
                .senhaHash("h")
                .ativo(false)
                .tipo(AuthUsuario.TipoUsuario.RECEPCIONISTA)
                .build();

        when(repository.findByCpf("22233344455")).thenReturn(Optional.of(u));

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("22233344455"));
    }

    @Test
    void UserDetailsServiceImpl_loadUserByUsername_LockedUser_ThrowsUsernameNotFound() {
        AuthUsuario u = AuthUsuario.builder()
                .cpf("99988877766")
                .senhaHash("h")
                .ativo(true)
                .lockedUntil(LocalDateTime.now().plusDays(1))
                .tipo(AuthUsuario.TipoUsuario.RECEPCIONISTA)
                .build();

        when(repository.findByCpf("99988877766")).thenReturn(Optional.of(u));

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("99988877766"));
    }
}
