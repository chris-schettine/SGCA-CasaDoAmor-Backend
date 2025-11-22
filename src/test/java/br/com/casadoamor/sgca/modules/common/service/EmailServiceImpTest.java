package br.com.casadoamor.sgca.modules.common.service;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceImpTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImp service;

    @BeforeEach
    void setUp() throws Exception {
        // inject config properties manually
        setField(service, "fromEmail", "noreply@ex.com");
        setField(service, "frontendUrl", "https://app.example.com");
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void sendSimpleEmail_sendsMessage() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        service.sendSimpleEmail("to@ex.com", "subj", "hello");

        // Assert
        verify(mailSender).send(cap.capture());
        SimpleMailMessage msg = cap.getValue();
        assertThat(msg.getFrom()).isEqualTo("noreply@ex.com");
        assertThat(msg.getTo()).contains("to@ex.com");
        assertThat(msg.getSubject()).isEqualTo("subj");
        assertThat(msg.getText()).isEqualTo("hello");
    }

    @Test
    void sendSimpleEmail_onFailure_throwsRuntime() {
        doThrow(new RuntimeException("boom")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> service.sendSimpleEmail("to@ex.com", "s", "t"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao enviar email");
    }

    @Test
    void sendHtmlVariants_createMimeAndSend() throws Exception {
        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);

        service.sendVerificationEmail("a@b","CODE");
        service.send2FACode("a@b","C2");
        service.sendPasswordResetEmail("a@b","TK");
        service.sendPasswordRecoveryEmail("a@b","LINK");

        // verify send called 4 times
        verify(mailSender, times(4)).send(mime);
    }

    @Test
    void enviarEmailAtivacaoConta_buildsAndSends() throws Exception {
        // sendSimpleEmail uses mailSender, we spy on it
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        service.enviarEmailAtivacaoConta("target@ex","Name","tok","tmpPass");

        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(cap.capture());
        SimpleMailMessage msg = cap.getValue();
        assertThat(msg.getTo()).contains("target@ex");
        assertThat(msg.getSubject()).contains("Ative sua conta");
        assertThat(msg.getText()).contains("Senha temporária: tmpPass");
    }
}
