package br.com.casadoamor.sgca.service.auth;

import br.com.casadoamor.sgca.dto.twofactor.Enable2FADTO;
import br.com.casadoamor.sgca.dto.twofactor.Setup2FADTO;
import br.com.casadoamor.sgca.entity.auth.Autenticacao2FA;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.auth.Autenticacao2FARepository;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import br.com.casadoamor.sgca.service.common.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Service para autenticação de dois fatores (2FA) via email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorService {

    private final Autenticacao2FARepository autenticacao2FARepository;
    private final AuthUsuarioRepository authUsuarioRepository;
    private final EmailService emailService;

    private static final int CODIGO_EXPIRACAO_MINUTOS = 5;
    private static final int CODIGO_LENGTH = 6;

    /**
     * Configura 2FA para o usuário (envia código inicial)
     */
    @Transactional
    public Setup2FADTO configurar2FA(Long usuarioId) {
        log.info("Configurando 2FA para usuário ID: {}", usuarioId);

        AuthUsuario usuario = authUsuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Busca ou cria configuração 2FA
        Autenticacao2FA config = autenticacao2FARepository.findByUsuarioId(usuarioId)
                .orElse(new Autenticacao2FA());

        config.setUsuarioId(usuarioId);

        // Gera código de 6 dígitos
        String codigo = gerarCodigoAleatorio();
        config.setCodigoAtual(codigo);
        config.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(CODIGO_EXPIRACAO_MINUTOS));
        config.setTentativasFalhas(0);
        config.setBloqueadoAte(null);

        autenticacao2FARepository.save(config);

        // Envia código por email
        try {
            log.info("📨 Iniciando envio de código 2FA para: {}", usuario.getEmail());
            emailService.send2FACode(usuario.getEmail(), codigo);
            log.info("✅ Código 2FA enviado com sucesso para: {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("❌ ERRO ao enviar email 2FA para: {}. Erro: {}", usuario.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar código 2FA. Tente novamente.");
        }

        return new Setup2FADTO(
                "Código de verificação enviado para " + maskEmail(usuario.getEmail()),
                config.getHabilitado(),
                maskEmail(usuario.getEmail())
        );
    }

    /**
     * Habilita ou desabilita 2FA após validar código
     */
    @Transactional
    public void alterarStatus2FA(Long usuarioId, Enable2FADTO dto) {
        log.info("Alterando status 2FA para usuário ID: {} - Habilitar: {}", usuarioId, dto.getHabilitar());

        Autenticacao2FA config = autenticacao2FARepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Configure o 2FA primeiro"));

        // Verifica bloqueio
        if (config.isBloqueado()) {
            throw new RuntimeException("Muitas tentativas falhas. Tente novamente em alguns minutos.");
        }

        // Verifica expiração
        if (config.isCodigoExpirado()) {
            throw new RuntimeException("Código expirado. Solicite um novo código.");
        }

        // Valida código
        if (!config.getCodigoAtual().equals(dto.getCodigo())) {
            config.incrementarTentativasFalhas();
            autenticacao2FARepository.save(config);
            log.warn("Código 2FA inválido para usuário ID: {}", usuarioId);
            throw new RuntimeException("Código inválido");
        }

        // Atualiza status
        config.setHabilitado(dto.getHabilitar());
        config.resetarTentativasFalhas();
        config.setCodigoAtual(null);
        config.setExpiracaoCodigo(null);

        if (dto.getHabilitar()) {
            config.setDataHabilitacao(LocalDateTime.now());
            config.setDataDesabilitacao(null);
            log.info("2FA habilitado para usuário ID: {}", usuarioId);
        } else {
            config.setDataDesabilitacao(LocalDateTime.now());
            log.info("2FA desabilitado para usuário ID: {}", usuarioId);
        }

        autenticacao2FARepository.save(config);
    }

    /**
     * Verifica se usuário tem 2FA habilitado
     */
    public boolean usuario2FAHabilitado(Long usuarioId) {
        return autenticacao2FARepository.existsByUsuarioIdAndHabilitado(usuarioId);
    }

    /**
     * Gera e envia código 2FA para login
     */
    @Transactional
    public void enviarCodigoLogin(Long usuarioId) {
        log.info("Enviando código 2FA de login para usuário ID: {}", usuarioId);

        AuthUsuario usuario = authUsuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Autenticacao2FA config = autenticacao2FARepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("2FA não configurado"));

        if (!config.getHabilitado()) {
            throw new RuntimeException("2FA não está habilitado");
        }

        // Verifica bloqueio
        if (config.isBloqueado()) {
            throw new RuntimeException("Muitas tentativas falhas. Tente novamente em alguns minutos.");
        }

        // Gera novo código
        String codigo = gerarCodigoAleatorio();
        config.setCodigoAtual(codigo);
        config.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(CODIGO_EXPIRACAO_MINUTOS));
        autenticacao2FARepository.save(config);

        // Envia por email
        try {
            log.info("📨 Iniciando envio de código 2FA de login para: {}", usuario.getEmail());
            emailService.send2FACode(usuario.getEmail(), codigo);
            log.info("✅ Código 2FA de login enviado com sucesso para: {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("❌ ERRO ao enviar email 2FA de login para: {}. Erro: {}", usuario.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar código 2FA");
        }
    }

    /**
     * Valida código 2FA durante o login
     */
    @Transactional
    public boolean validarCodigoLogin(Long usuarioId, String codigo) {
        log.info("Validando código 2FA para usuário ID: {}", usuarioId);

        Autenticacao2FA config = autenticacao2FARepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("2FA não configurado"));

        // Verifica bloqueio
        if (config.isBloqueado()) {
            throw new RuntimeException("Muitas tentativas falhas. Tente novamente em alguns minutos.");
        }

        // Verifica expiração
        if (config.isCodigoExpirado()) {
            throw new RuntimeException("Código expirado. Solicite um novo código.");
        }

        // Valida código
        if (!config.getCodigoAtual().equals(codigo)) {
            config.incrementarTentativasFalhas();
            autenticacao2FARepository.save(config);
            log.warn("Código 2FA de login inválido para usuário ID: {}", usuarioId);
            return false;
        }

        // Sucesso - reseta tentativas e limpa código
        config.resetarTentativasFalhas();
        config.setCodigoAtual(null);
        config.setExpiracaoCodigo(null);
        autenticacao2FARepository.save(config);

        log.info("Código 2FA validado com sucesso para usuário ID: {}", usuarioId);
        return true;
    }

    /**
     * Gera código aleatório de 6 dígitos
     */
    private String gerarCodigoAleatorio() {
        SecureRandom random = new SecureRandom();
        int max = (int) Math.pow(10, CODIGO_LENGTH) - 1; // 999999
        int min = (int) Math.pow(10, CODIGO_LENGTH - 1);
        int codigo = random.nextInt(max - min + 1) + min; // Garante 6 dígitos
        return String.valueOf(codigo);
    }

    /**
     * Mascara email para exibição
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 3) {
            return username.charAt(0) + "***@" + domain;
        }
        return username.substring(0, 3) + "***@" + domain;
    }
}
