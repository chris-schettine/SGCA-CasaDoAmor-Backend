package br.com.casadoamor.sgca.service.common;

public interface EmailService {
    void sendVerificationEmail(String to, String verificationCode);
    void send2FACode(String to, String code);
    void sendPasswordResetEmail(String to, String resetToken);
    void sendPasswordRecoveryEmail(String to, String recoveryLink);
    void sendSimpleEmail(String to, String subject, String text);
    
    // Método genérico para envio de emails (compatibilidade)
    default void enviarEmail(String to, String subject, String message) {
        sendSimpleEmail(to, subject, message);
    }
    
    // Método para enviar senha temporária
    default void enviarSenhaTemporaria(String to, String nome, String senhaTemporaria) {
        String subject = "Sua conta foi criada - SGCA Casa do Amor";
        String message = String.format(
            "Olá %s,\n\n" +
            "Sua conta foi criada no sistema SGCA - Casa do Amor.\n\n" +
            "Seus dados de acesso:\n" +
            "Email: %s\n" +
            "Senha temporária: %s\n\n" +
            "IMPORTANTE: Por questões de segurança, altere sua senha no primeiro acesso.\n\n" +
            "Atenciosamente,\n" +
            "Equipe SGCA - Casa do Amor",
            nome, to, senhaTemporaria
        );
        sendSimpleEmail(to, subject, message);
    }

    // Método para enviar email de ativação de conta
    default void enviarEmailAtivacaoConta(String to, String nome, String token, String senhaTemporaria) {
        String subject = "Ative sua conta - SGCA Casa do Amor";
        String activationLink = "http://localhost:3000/activate-account?token=" + token;
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
}
