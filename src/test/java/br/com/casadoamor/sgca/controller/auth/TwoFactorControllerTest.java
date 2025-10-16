package br.com.casadoamor.sgca.controller.auth;

import br.com.casadoamor.sgca.dto.auth.response.AuthResponseDTO;
import br.com.casadoamor.sgca.dto.common.MessageResponseDTO;
import br.com.casadoamor.sgca.dto.twofactor.Enable2FADTO;
import br.com.casadoamor.sgca.dto.twofactor.Setup2FADTO;
import br.com.casadoamor.sgca.dto.twofactor.Verify2FADTO;
import br.com.casadoamor.sgca.service.auth.AuthService;
import br.com.casadoamor.sgca.service.auth.TwoFactorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TwoFactorControllerTest {

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TwoFactorController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void setup_ReturnsSetupDto() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cpf");
        when(authService.buscarIdPorCpf("cpf")).thenReturn(1L);

        Setup2FADTO dto = new Setup2FADTO();
        when(twoFactorService.configurar2FA(1L)).thenReturn(dto);

        ResponseEntity<Setup2FADTO> res = controller.setup(auth);

        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        assertThat(res.getBody()).isEqualTo(dto);
    }

    @Test
    void enable_ReturnsOkMessage() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cpf");
        when(authService.buscarIdPorCpf("cpf")).thenReturn(1L);

        Enable2FADTO dto = new Enable2FADTO();
        dto.setHabilitar(true);

        ResponseEntity<MessageResponseDTO> res = controller.enable(dto, auth);

        assertThat(res.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void verify_InvalidCode_Returns401() {
        Verify2FADTO dto = new Verify2FADTO();
        dto.setCpf("cpf");
        dto.setCodigo("1234");

        when(authService.buscarIdPorCpf("cpf")).thenReturn(1L);
        when(twoFactorService.validarCodigoLogin(1L, "1234")).thenReturn(false);

        ResponseEntity<?> res = controller.verify(dto);

        assertThat(res.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void verify_ValidCode_ReturnsToken() {
        Verify2FADTO dto = new Verify2FADTO();
        dto.setCpf("cpf");
        dto.setCodigo("1234");

        when(authService.buscarIdPorCpf("cpf")).thenReturn(1L);
        when(twoFactorService.validarCodigoLogin(1L, "1234")).thenReturn(true);

        AuthResponseDTO token = new AuthResponseDTO();
        when(authService.gerarTokenPorCpf("cpf")).thenReturn(token);

        ResponseEntity<?> res = controller.verify(dto);

        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        assertThat(res.getBody()).isEqualTo(token);
    }

    @Test
    void resend_ReturnsOk() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cpf");
        when(authService.buscarIdPorCpf("cpf")).thenReturn(1L);

        ResponseEntity<MessageResponseDTO> res = controller.resend(auth);

        assertThat(res.getStatusCodeValue()).isEqualTo(200);
    }
}
