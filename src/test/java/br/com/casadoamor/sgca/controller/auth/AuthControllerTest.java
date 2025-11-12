package br.com.casadoamor.sgca.controller.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import br.com.casadoamor.sgca.modules.admin.dtos.user.UserResponseDTO;
import br.com.casadoamor.sgca.modules.admin.service.SessaoService;
import br.com.casadoamor.sgca.modules.auth.controller.AuthController;
import br.com.casadoamor.sgca.modules.auth.controller.AuthController.ResendActivationDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.SessaoDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.ActivateAccountRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.ChangePasswordRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.ForgotPasswordRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.FirstLoginPasswordChangeDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.LoginRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.RegisterRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.ResetPasswordRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.request.VerifyEmailRequestDTO;
import br.com.casadoamor.sgca.modules.auth.dtos.response.AuthResponseDTO;
import br.com.casadoamor.sgca.modules.auth.service.AccountActivationService;
import br.com.casadoamor.sgca.modules.auth.service.AuthService;
import br.com.casadoamor.sgca.modules.common.dto.MessageResponseDTO;

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

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(resp);
        verify(authService).register(req);
    }

    @Test
    void register_Conflict_Returns409() {
        RegisterRequestDTO req = new RegisterRequestDTO();
        when(authService.register(any())).thenThrow(new RuntimeException("conflict"));

        ResponseEntity<?> result = controller.register(req);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(authService).register(req);
    }

    @Test
    void login_Success_ReturnsOk() {
        LoginRequestDTO req = new LoginRequestDTO();
        AuthResponseDTO resp = new AuthResponseDTO();
        when(authService.login(any(), any())).thenReturn(resp);

        ResponseEntity<?> result = controller.login(req, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(resp);
    }

    @Test
    void login_Unauthorized_Returns401() {
        LoginRequestDTO req = new LoginRequestDTO();
        when(authService.login(any(), any())).thenThrow(new RuntimeException("bad creds"));

        ResponseEntity<?> result = controller.login(req, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getCurrentUser_ReturnsProfile() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("12345678901");
    UserResponseDTO profile = UserResponseDTO.builder().id(1L).cpf("12345678901").build();
    when(authService.getUserProfile("12345678901")).thenReturn(profile);

        ResponseEntity<?> res = controller.getCurrentUser(auth);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo(profile);
    }

    @Test
    void getCurrentUser_NotFound_ReturnsNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("00000000000");
        when(authService.getUserProfile("00000000000")).thenThrow(new RuntimeException("missing"));

        ResponseEntity<?> res = controller.getCurrentUser(auth);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void forgotPassword_ReturnsOk() {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("user@example.com");
        MessageResponseDTO message = MessageResponseDTO.success("ok");
        when(authService.forgotPassword(request)).thenReturn(message);

        ResponseEntity<MessageResponseDTO> res = controller.forgotPassword(request);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo(message);
    }

    @Test
    void resetPassword_Success_ReturnsOk() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("token");
        request.setNovaSenha("N3wP@ss1!");
        MessageResponseDTO message = MessageResponseDTO.success("reset");
        when(authService.resetPassword(request)).thenReturn(message);

        ResponseEntity<?> res = controller.resetPassword(request);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo(message);
    }

    @Test
    void resetPassword_Error_ReturnsBadRequest() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("token");
        request.setNovaSenha("N3wP@ss1!");
        when(authService.resetPassword(request)).thenThrow(new RuntimeException("invalid"));

        ResponseEntity<?> res = controller.resetPassword(request);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void verifyEmail_ReturnsOk() {
        VerifyEmailRequestDTO request = new VerifyEmailRequestDTO();
        request.setToken("token");
        MessageResponseDTO message = MessageResponseDTO.success("verified");
        when(authService.verifyEmail(request)).thenReturn(message);

        ResponseEntity<?> res = controller.verifyEmail(request);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo(message);
    }

    @Test
    void changePassword_ReturnsOk() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setSenhaAtual("OldP@ss1!");
        request.setNovaSenha("N3wP@ss1!");
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("12345678901");
        MessageResponseDTO message = MessageResponseDTO.success("changed");
        when(authService.changePassword(request, "12345678901")).thenReturn(message);

        ResponseEntity<?> res = controller.changePassword(request, auth);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo(message);
    }

    @Test
    void listSessions_ReturnsList() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cpf");
        when(authService.findUserIdByCpf("cpf")).thenReturn(Optional.of(1L));

        SessaoDTO dto = new SessaoDTO();
        when(sessaoService.listarSessoesAtivas(1L, "token")).thenReturn(List.of(dto));

        ResponseEntity<List<SessaoDTO>> res = controller.listSessions(auth, "Bearer token");

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSize(1);
    }

    @Test
    void revokeSession_Success_ReturnsOk() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cpf");
        when(authService.findUserIdByCpf("cpf")).thenReturn(Optional.of(1L));

        ResponseEntity<?> res = controller.revokeSession(123L, auth);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(sessaoService).revogarSessao(123L, 1L);
    }

    @Test
    void revokeSession_UserNotFound_ReturnsBadRequest() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cpf");
        when(authService.findUserIdByCpf("cpf")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.revokeSession(123L, auth);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(sessaoService, never()).revogarSessao(anyLong(), anyLong());
    }

    @Test
    void logout_Success_ReturnsOk() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("12345678901");
        when(authService.findUserIdByCpf("12345678901")).thenReturn(Optional.of(5L));

        ResponseEntity<?> res = controller.logout(auth, "Bearer token-value");

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(sessaoService).revogarSessaoPorToken("token-value", 5L);
    }

    @Test
    void logout_Error_ReturnsNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("12345678901");
        when(authService.findUserIdByCpf("12345678901")).thenReturn(Optional.of(5L));
        org.mockito.Mockito.doThrow(new RuntimeException("missing"))
                .when(sessaoService).revogarSessaoPorToken(anyString(), anyLong());

        ResponseEntity<?> res = controller.logout(auth, "Bearer bad-token");

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void activateAccount_ReturnsOk() {
        ActivateAccountRequestDTO request = new ActivateAccountRequestDTO(
                "token",
                "user@example.com",
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");
        MessageResponseDTO message = MessageResponseDTO.success("activated");
        when(accountActivationService.ativarConta(request)).thenReturn(message);

        ResponseEntity<?> res = controller.activateAccount(request);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo(message);
    }

    @Test
    void activateAccount_Error_ReturnsBadRequest() {
        ActivateAccountRequestDTO request = new ActivateAccountRequestDTO(
                "token",
                "user@example.com",
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");
        when(accountActivationService.ativarConta(request)).thenThrow(new RuntimeException("invalid"));

        ResponseEntity<?> res = controller.activateAccount(request);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void resendActivation_ReturnsOk() {
        ResendActivationDTO request = new ResendActivationDTO();
        request.setEmail("user@example.com");
        MessageResponseDTO message = MessageResponseDTO.success("resent");
        when(accountActivationService.reenviarEmailAtivacao("user@example.com")).thenReturn(message);

        ResponseEntity<?> res = controller.resendActivation(request);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo(message);
    }

    @Test
    void firstLoginPasswordChange_ReturnsOk() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("12345678901");
        FirstLoginPasswordChangeDTO request = new FirstLoginPasswordChangeDTO(
                "Temp@123",
                "N3wP@ss1!",
                "N3wP@ss1!");

        ResponseEntity<?> res = controller.firstLoginPasswordChange(request, auth);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(accountActivationService).trocarSenhaTemporaria("12345678901", request);
    }
}
