package br.com.casadoamor.sgca.service.auth;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.dto.twofactor.Enable2FADTO;
import br.com.casadoamor.sgca.dto.twofactor.Setup2FADTO;
import br.com.casadoamor.sgca.entity.auth.Autenticacao2FA;
import br.com.casadoamor.sgca.entity.auth.Autenticacao2FARateLimit;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.auth.Autenticacao2FARateLimitRepository;
import br.com.casadoamor.sgca.repository.auth.Autenticacao2FARepository;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import br.com.casadoamor.sgca.service.common.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para autenticação de dois fatores (2FA) via email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorService {

    private final Autenticacao2FARepository autenticacao2FARepository;
    private final Autenticacao2FARateLimitRepository rateLimitRepository;
    private final AuthUsuarioRepository authUsuarioRepository;
    private final EmailService emailService;

    private static final int CODIGO_EXPIRACAO_MINUTOS = 5;
    private static final int CODIGO_LENGTH = 6;

    /**
     * Configura 2FA para o usuário gerando e enviando código
     */
    @Transactional
    public Setup2FADTO configurar2FA(Long usuarioId) {
        log.info("Configurando 2FA para usuário ID: {}", usuarioId);

        // Verifica rate limit antes de enviar código
        verificarRateLimit(usuarioId);

        AuthUsuario usuario = authUsuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Busca ou cria configuração
        Autenticacao2FA config = autenticacao2FARepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Autenticacao2FA novaConfig = new Autenticacao2FA();
                    novaConfig.setUsuarioId(usuarioId);
                    novaConfig.setHabilitado(false);
                    return novaConfig;
                });

        // Gera código
        String codigo = gerarCodigoAleatorio();
        config.setCodigoAtual(codigo);
        config.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(CODIGO_EXPIRACAO_MINUTOS));
        autenticacao2FARepository.save(config);

        // Envia por email
        try {
            log.info("📨 Iniciando envio de código 2FA de configuração para: {}", usuario.getEmail());
            emailService.send2FACode(usuario.getEmail(), codigo);
            log.info("✅ Código 2FA de configuração enviado com sucesso para: {}", usuario.getEmail());
            
            // Incrementa contadores de rate limit após envio bem-sucedido
            incrementarRateLimit(usuarioId);
        } catch (Exception e) {
            log.error("❌ ERRO ao enviar email 2FA de configuração para: {}. Erro: {}", usuario.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar código 2FA");
        }

        return new Setup2FADTO(
                "Código de verificação enviado para " + maskEmail(usuario.getEmail()) + 
                ". O código expira em " + CODIGO_EXPIRACAO_MINUTOS + " minutos.",
                config.getHabilitado(),
                maskEmail(usuario.getEmail())
        );
    }    /**
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
        // Temporarily disable 2FA system-wide for emergency debugging/maintenance.
        // To re-enable, restore the original line below.
        // return autenticacao2FARepository.existsByUsuarioIdAndHabilitado(usuarioId);
        return false;
    }

    /**
     * Gera e envia código 2FA para login
     */
    @Transactional
    public void enviarCodigoLogin(Long usuarioId) {
        log.info("Enviando código 2FA de login para usuário ID: {}", usuarioId);

        // Verifica rate limit antes de enviar código
        verificarRateLimit(usuarioId);

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
            
            // Incrementa contadores de rate limit após envio bem-sucedido
            incrementarRateLimit(usuarioId);
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

    /**
     * Verifica e valida o rate limit antes de enviar código 2FA
     * Lança exceção se o usuário excedeu os limites de envio
     */
    private void verificarRateLimit(Long usuarioId) {
        log.debug("Verificando rate limit para usuário ID: {}", usuarioId);
        
        // Busca ou cria registro de rate limit
        Autenticacao2FARateLimit rateLimit = rateLimitRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Autenticacao2FARateLimit novoRateLimit = new Autenticacao2FARateLimit();
                    novoRateLimit.setUsuarioId(usuarioId);
                    return novoRateLimit;
                });

        // Reseta contadores se necessário
        rateLimit.resetarSeNecessario();

        // Verifica se pode enviar novo código
        if (!rateLimit.podeEnviarNovoCodigo()) {
            String tempoEspera = rateLimit.getTempoEsperaFormatado();
            log.warn("Rate limit excedido para usuário ID: {}. Tempo de espera: {}", usuarioId, tempoEspera);
            throw new RuntimeException("Limite de envios excedido. Por favor, aguarde " + tempoEspera);
        }
        
        log.debug("Rate limit OK para usuário ID: {}", usuarioId);
    }

    /**
     * Incrementa os contadores de rate limit após envio bem-sucedido
     */
    private void incrementarRateLimit(Long usuarioId) {
        log.debug("Incrementando contadores de rate limit para usuário ID: {}", usuarioId);
        
        Autenticacao2FARateLimit rateLimit = rateLimitRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Autenticacao2FARateLimit novoRateLimit = new Autenticacao2FARateLimit();
                    novoRateLimit.setUsuarioId(usuarioId);
                    return novoRateLimit;
                });

        rateLimit.incrementarContadores();
        rateLimitRepository.save(rateLimit);
        
        log.debug("Contadores de rate limit atualizados para usuário ID: {}", usuarioId);
    }

    /**
     * Ativa 2FA automaticamente durante ativação de conta
     * Cria configuração, habilita 2FA e envia primeiro código
     */
    @Transactional
    public void ativar2FAAutomaticamente(Long usuarioId, String email) {
        log.info("Ativando 2FA automaticamente para usuário ID: {}", usuarioId);

        // Verifica rate limit antes de prosseguir
        verificarRateLimit(usuarioId);

        // Busca ou cria configuração 2FA
        Autenticacao2FA config = autenticacao2FARepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Autenticacao2FA novaConfig = new Autenticacao2FA();
                    novaConfig.setUsuarioId(usuarioId);
                    novaConfig.setHabilitado(false);
                    return novaConfig;
                });

        // Habilita 2FA
        config.setHabilitado(true);
        config.setDataHabilitacao(LocalDateTime.now());
        config.setDataDesabilitacao(null);
        config.resetarTentativasFalhas();

        // Gera código inicial
        String codigo = gerarCodigoAleatorio();
        config.setCodigoAtual(codigo);
        config.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(CODIGO_EXPIRACAO_MINUTOS));
        
        autenticacao2FARepository.save(config);

        // Envia código por email
        try {
            log.info("📨 Enviando código 2FA inicial para: {}", email);
            emailService.send2FACode(email, codigo);
            log.info("✅ 2FA ativado automaticamente e código enviado para usuário ID: {}", usuarioId);
            
            // Incrementa contadores de rate limit após envio bem-sucedido
            incrementarRateLimit(usuarioId);
        } catch (Exception e) {
            log.error("❌ ERRO ao enviar código 2FA inicial para: {}. Erro: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar código 2FA de ativação");
        }
    }
}
