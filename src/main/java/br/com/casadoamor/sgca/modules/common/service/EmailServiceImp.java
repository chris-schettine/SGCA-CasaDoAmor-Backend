package br.com.casadoamor.sgca.modules.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImp implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

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
        try {
            log.info("Preparando envio de email para: {}", to);
            log.info("From: {}, Subject: {}", fromEmail, subject);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            log.info("Enviando email via JavaMailSender...");
            mailSender.send(message);
            log.info("✅ Email enviado com sucesso para: {}", to);
        } catch (Exception e) {
            log.error("❌ ERRO ao enviar email para: {}. Erro: {}", to, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar email", e);
        }
    }

    @Override
    public void enviarEmailAtivacaoConta(String to, String nome, String token, String senhaTemporaria) {
        String subject = "Ative sua conta - SGCA Casa do Amor";
        String activationLink = frontendUrl + "/activate-account?token=" + token;
        String message = String.format(
            "Olá %s,\n\n" +
            "Sua conta foi criada no sistema SGCA - Casa do Amor!\n\n" +
            "Para ativar sua conta e definir sua senha definitiva, clique no link abaixo:\n" +
            "%s\n\n" +
            "Seus dados temporários:\n" +
            "Email: %s\n" +
            "Senha temporária: %s\n\n" +
            "IMPORTANTE:\n" +
            "- Este link expira em 24 horas\n" +
            "- Você precisará da senha temporária para ativar a conta\n" +
            "- Após a ativação, você definirá sua própria senha\n\n" +
            "Se você não solicitou esta conta, ignore este email.\n\n" +
            "Atenciosamente,\n" +
            "Equipe SGCA - Casa do Amor",
            nome, activationLink, to, senhaTemporaria
        );
        sendSimpleEmail(to, subject, message);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            log.info("📧 Preparando envio de email HTML para: {}", to);
            log.info("From: {}, Subject: {}", fromEmail, subject);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            log.info("Enviando email HTML via JavaMailSender...");
            mailSender.send(message);
            log.info("✅ Email HTML enviado com sucesso para: {}", to);
        } catch (MessagingException e) {
            log.error("❌ ERRO (MessagingException) ao enviar email HTML para: {}. Erro: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("❌ ERRO ao enviar email HTML para: {}. Erro: {}", to, e.getMessage(), e);
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
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
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
