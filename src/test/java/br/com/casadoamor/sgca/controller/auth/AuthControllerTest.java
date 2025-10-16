package br.com.casadoamor.sgca.controller.auth;

import br.com.casadoamor.sgca.dto.SessaoDTO;
import br.com.casadoamor.sgca.dto.auth.request.LoginRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.RegisterRequestDTO;
import br.com.casadoamor.sgca.dto.auth.response.AuthResponseDTO;
import br.com.casadoamor.sgca.dto.common.MessageResponseDTO;
import br.com.casadoamor.sgca.service.admin.SessaoService;
import br.com.casadoamor.sgca.service.auth.AccountActivationService;
import br.com.casadoamor.sgca.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private AccountActivationService accountActivationService;

    @InjectMocks
    private AuthController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_Success_ReturnsCreated() {
        RegisterRequestDTO req = new RegisterRequestDTO();
        AuthResponseDTO resp = new AuthResponseDTO();
        when(authService.register(any())).thenReturn(resp);

        ResponseEntity<?> result = controller.register(req);

        assertThat(result.getStatusCodeValue()).isEqualTo(201);
        assertThat(result.getBody()).isEqualTo(resp);
        verify(authService).register(req);
    }

    @Test
    void register_Conflict_Returns409() {
        RegisterRequestDTO req = new RegisterRequestDTO();
        when(authService.register(any())).thenThrow(new RuntimeException("conflict"));

        ResponseEntity<?> result = controller.register(req);

        assertThat(result.getStatusCodeValue()).isEqualTo(409);
        verify(authService).register(req);
    }

    @Test
    void login_Success_ReturnsOk() {
        LoginRequestDTO req = new LoginRequestDTO();
        AuthResponseDTO resp = new AuthResponseDTO();
        when(authService.login(any(), any())).thenReturn(resp);

        ResponseEntity<?> result = controller.login(req, null);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(resp);
    }

    @Test
    void login_Unauthorized_Returns401() {
        LoginRequestDTO req = new LoginRequestDTO();
        when(authService.login(any(), any())).thenThrow(new RuntimeException("bad creds"));

        ResponseEntity<?> result = controller.login(req, null);

        assertThat(result.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void listSessions_ReturnsList() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cpf");
        when(authService.findUserIdByCpf("cpf")).thenReturn(Optional.of(1L));

        SessaoDTO dto = new SessaoDTO();
        when(sessaoService.listarSessoesAtivas(1L, "token")).thenReturn(List.of(dto));

        ResponseEntity<List<SessaoDTO>> res = controller.listSessions(auth, "Bearer token");

        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        assertThat(res.getBody()).hasSize(1);
    }

}
