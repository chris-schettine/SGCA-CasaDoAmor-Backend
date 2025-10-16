package br.com.casadoamor.sgca.service.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import br.com.casadoamor.sgca.service.common.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImp implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String to, String verificationCode) {
        String subject = "Verificação de Email - Casa do Amor";
        String body = buildVerificationEmailBody(verificationCode);
        sendHtmlEmail(to, subject, body);
    }

    @Override
    public void send2FACode(String to, String code) {
        String subject = "Código de Autenticação - Casa do Amor";
        String body = build2FAEmailBody(code);
        sendHtmlEmail(to, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "Redefinição de Senha - Casa do Amor";
        String body = buildPasswordResetEmailBody(resetToken);
        sendHtmlEmail(to, subject, body);
    }

    @Override
    public void sendPasswordRecoveryEmail(String to, String recoveryLink) {
        String subject = "Recuperação de Conta - Casa do Amor";
        String body = buildPasswordRecoveryEmailBody(recoveryLink);
        sendHtmlEmail(to, subject, body);
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
        log.info("Email sent to: {}", to);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);
            log.info("HTML Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildVerificationEmailBody(String code) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Verificação de Email</h2>
                    <p>Seu código de verificação é:</p>
                    <h1 style="color: #4CAF50; letter-spacing: 5px;">%s</h1>
                    <p>Este código expira em 15 minutos.</p>
                    <p>Se você não solicitou esta verificação, ignore este email.</p>
                </body>
                </html>
                """.formatted(code);
    }

    private String build2FAEmailBody(String code) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Código de Autenticação de Dois Fatores</h2>
                    <p>Seu código 2FA é:</p>
                    <h1 style="color: #2196F3; letter-spacing: 5px;">%s</h1>
                    <p>Este código expira em 5 minutos.</p>
                </body>
                </html>
                """.formatted(code);
    }

    private String buildPasswordResetEmailBody(String resetToken) {
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
        return """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Redefinição de Senha</h2>
                    <p>Você solicitou a redefinição de sua senha.</p>
                    <p>Clique no link abaixo para redefinir:</p>
                    <a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Redefinir Senha
                    </a>
                    <p>Este link expira em 1 hora.</p>
                    <p>Se você não solicitou esta redefinição, ignore este email.</p>
                </body>
                </html>
                """
                .formatted(resetLink);
    }

    private String buildPasswordRecoveryEmailBody(String recoveryLink) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Recuperação de Conta</h2>
                    <p>Clique no link abaixo para recuperar sua conta:</p>
                    <a href="%s" style="background-color: #2196F3; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Recuperar Conta
                    </a>
                    <p>Este link expira em 24 horas.</p>
                </body>
                </html>
                """
                .formatted(recoveryLink);
    }
}
