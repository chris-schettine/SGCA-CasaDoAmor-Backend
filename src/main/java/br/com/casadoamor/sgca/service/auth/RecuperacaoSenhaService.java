package br.com.casadoamor.sgca.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.entity.auth.TokenRecuperacao;
import br.com.casadoamor.sgca.enums.TipoToken;
import br.com.casadoamor.sgca.repository.auth.TokenRecuperacaoRepository;
import br.com.casadoamor.sgca.service.common.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para gerenciamento de tokens de recuperação de senha e verificação de email
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private final TokenRecuperacaoRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    private static final int TAMANHO_TOKEN = 32;
    private static final int HORAS_VALIDADE = 24;

    /**
     * Gera token seguro de recuperação de senha
     */
    @Transactional
    public String gerarTokenRecuperacao(AuthUsuario usuario) {
        String token = gerarTokenSeguro();
        String tokenHash = hashToken(token);

        // Invalida tokens anteriores
        invalidarTokensAnteriores(usuario, TipoToken.RECUPERACAO_SENHA);

        // Cria novo token
        TokenRecuperacao tokenRecuperacao = TokenRecuperacao.builder()
                .usuario(usuario)
                .tokenHash(tokenHash)
                .tipo(TipoToken.RECUPERACAO_SENHA)
                .expiracao(LocalDateTime.now().plusHours(HORAS_VALIDADE))
                .usado(false)
                .build();

        tokenRepository.save(tokenRecuperacao);
        log.info("Token de recuperação gerado para usuário: {}", usuario.getEmail());

        return token; // Retorna token original (não o hash)
    }

    /**
     * Gera token seguro de verificação de email
     */
    @Transactional
    public String gerarTokenVerificacao(AuthUsuario usuario) {
        String token = gerarTokenSeguro();
        String tokenHash = hashToken(token);

        // Invalida tokens anteriores
        invalidarTokensAnteriores(usuario, TipoToken.VERIFICACAO_EMAIL);

        // Cria novo token
        TokenRecuperacao tokenVerificacao = TokenRecuperacao.builder()
                .usuario(usuario)
                .tokenHash(tokenHash)
                .tipo(TipoToken.VERIFICACAO_EMAIL)
                .expiracao(LocalDateTime.now().plusHours(HORAS_VALIDADE))
                .usado(false)
                .build();

        tokenRepository.save(tokenVerificacao);
        log.info("Token de verificação gerado para usuário: {}", usuario.getEmail());

        return token;
    }

    /**
     * Valida token de recuperação de senha
     */
    @Transactional
    public Optional<TokenRecuperacao> validarToken(String token, TipoToken tipo) {
        String tokenHash = hashToken(token);
        
        Optional<TokenRecuperacao> tokenOpt = tokenRepository
                .findByTokenHashAndTipo(tokenHash, tipo);

        if (tokenOpt.isEmpty()) {
            log.warn("Token não encontrado ou tipo inválido");
            return Optional.empty();
        }

        TokenRecuperacao tokenRecuperacao = tokenOpt.get();

        if (!tokenRecuperacao.isValido()) {
            log.warn("Token inválido (expirado ou já usado)");
            return Optional.empty();
        }

        return Optional.of(tokenRecuperacao);
    }

    /**
     * Marca token como usado
     */
    @Transactional
    public void marcarTokenComoUsado(TokenRecuperacao token) {
        token.setUsado(true);
        token.setUsadoEm(LocalDateTime.now());
        tokenRepository.save(token);
        log.info("Token marcado como usado para usuário: {}", token.getUsuario().getEmail());
    }

    /**
     * Envia email de recuperação de senha
     */
    public void enviarEmailRecuperacao(AuthUsuario usuario, String token) {
        log.info("🔔 INICIANDO enviarEmailRecuperacao para: {}", usuario.getEmail());
        String linkRecuperacao = String.format("%s/reset-password?token=%s", frontendUrl, token);
        log.info("🔗 Link de recuperação gerado: {}", linkRecuperacao);
        
        String assunto = "Recuperação de Senha - Casa do Amor";
        String mensagem = String.format(
            "Olá %s,\n\n" +
            "Recebemos uma solicitação para redefinir sua senha.\n\n" +
            "Clique no link abaixo para criar uma nova senha:\n%s\n\n" +
            "Este link é válido por %d horas.\n\n" +
            "Se você não solicitou esta alteração, ignore este email.\n\n" +
            "Atenciosamente,\n" +
            "Equipe Casa do Amor",
            usuario.getNome(), linkRecuperacao, HORAS_VALIDADE
        );

        log.info("📧 Chamando emailService.enviarEmail para: {}", usuario.getEmail());
        emailService.enviarEmail(usuario.getEmail(), assunto, mensagem);
        log.info("✅ emailService.enviarEmail executado (verificar logs do EmailServiceImp)");
        log.info("Email de recuperação enviado para: {}", usuario.getEmail());
    }

    /**
     * Envia email de verificação
     */
    public void enviarEmailVerificacao(AuthUsuario usuario, String token) {
        String linkVerificacao = String.format("%s/verify-email?token=%s", frontendUrl, token);
        
        String assunto = "Verificação de Email - Casa do Amor";
        String mensagem = String.format(
            "Olá %s,\n\n" +
            "Bem-vindo à Casa do Amor!\n\n" +
            "Clique no link abaixo para verificar seu email:\n%s\n\n" +
            "Este link é válido por %d horas.\n\n" +
            "Atenciosamente,\n" +
            "Equipe Casa do Amor",
            usuario.getNome(), linkVerificacao, HORAS_VALIDADE
        );

        emailService.enviarEmail(usuario.getEmail(), assunto, mensagem);
        log.info("Email de verificação enviado para: {}", usuario.getEmail());
    }

    /**
     * Invalida todos os tokens anteriores de um tipo específico
     */
    private void invalidarTokensAnteriores(AuthUsuario usuario, TipoToken tipo) {
        tokenRepository.findByUsuarioIdAndUsadoAndExpiracaoAfter(
            usuario.getId(), false, LocalDateTime.now()
        ).forEach(token -> {
            token.setUsado(true);
            tokenRepository.save(token);
        });
    }

    /**
     * Gera token seguro aleatório
     */
    private String gerarTokenSeguro() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TAMANHO_TOKEN];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Gera hash SHA-256 do token
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash do token", e);
        }
    }

    /**
     * Limpa tokens expirados (método para agendamento)
     */
    @Transactional
    public void limparTokensExpirados() {
        tokenRepository.deleteByExpiracaoBefore(LocalDateTime.now());
        log.info("Tokens expirados removidos");
    }
}
