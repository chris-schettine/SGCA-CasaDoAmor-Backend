package br.com.casadoamor.sgca.service.auth;

import br.com.casadoamor.sgca.dto.auth.request.ActivateAccountRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.FirstLoginPasswordChangeDTO;
import br.com.casadoamor.sgca.dto.common.MessageResponseDTO;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.entity.auth.TokenRecuperacao;
import br.com.casadoamor.sgca.enums.TipoToken;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import br.com.casadoamor.sgca.repository.auth.TokenRecuperacaoRepository;
import br.com.casadoamor.sgca.service.common.EmailService;
import br.com.casadoamor.sgca.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service para ativação de contas criadas por administradores
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountActivationService {

    private final AuthUsuarioRepository usuarioRepository;
    private final TokenRecuperacaoRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final HistoricoSenhaService historicoSenhaService;

    private static final int TOKEN_EXPIRATION_HOURS = 24;

    /**
     * Gera token de ativação e envia email com instruções
     */
    @Transactional
    public void enviarEmailAtivacao(AuthUsuario usuario, String senhaTemporaria) {
        log.info("Gerando token de ativação para usuário: {}", usuario.getEmail());

        // Gera token seguro
        String token = gerarTokenSeguro();

        // Cria registro de token
        TokenRecuperacao tokenAtivacao = new TokenRecuperacao();
        tokenAtivacao.setUsuario(usuario);
        tokenAtivacao.setTokenHash(token);
        tokenAtivacao.setTipo(TipoToken.VERIFICACAO_EMAIL);
        tokenAtivacao.setExpiracao(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));
        tokenAtivacao.setUsado(false);

        tokenRepository.save(tokenAtivacao);

        // Envia email com link de ativação + senha temporária
        try {
            log.info("📨 Iniciando envio de email de ativação para: {}", usuario.getEmail());
            emailService.enviarEmailAtivacaoConta(
                    usuario.getEmail(),
                    usuario.getNome(),
                    token,
                    senhaTemporaria);
            log.info("✅ Email de ativação enviado com sucesso para: {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("❌ ERRO ao enviar email de ativação para: {}. Erro: {}", usuario.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar email de ativação");
        }
    }

    /**
     * Ativa conta e define senha definitiva
     */
    @Transactional
    public MessageResponseDTO ativarConta(ActivateAccountRequestDTO dto) {
        log.info("Ativando conta para email: {}", dto.getEmail());

        // Busca usuário
        AuthUsuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verifica se já está ativado
        if (usuario.getEmailVerificado()) {
            throw new RuntimeException("Conta já foi ativada");
        }

        // Busca token
        TokenRecuperacao token = tokenRepository
                .findByTokenHashAndTipo(dto.getToken(), TipoToken.VERIFICACAO_EMAIL)
                .orElseThrow(() -> new RuntimeException("Token de ativação inválido"));

        // Valida token
        if (!token.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Token não pertence a este usuário");
        }

        if (token.getUsado()) {
            throw new RuntimeException("Token já foi utilizado");
        }

        if (token.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token de ativação expirado. Solicite um novo ao administrador.");
        }

        // Valida senha temporária
        if (!passwordEncoder.matches(dto.getSenhaTemporaria(), usuario.getSenhaHash())) {
            throw new RuntimeException("Senha temporária inválida");
        }

        // Valida nova senha
        if (!dto.getNovaSenha().equals(dto.getConfirmarSenha())) {
            throw new RuntimeException("Senhas não conferem");
        }

        // Valida política de senha
        PasswordValidator.validarOuLancarExcecao(dto.getNovaSenha());

        // Verifica se a senha já foi usada recentemente
        if (historicoSenhaService.senhaJaUsada(usuario.getId(), dto.getNovaSenha())) {
            throw new IllegalArgumentException(
                "Esta senha já foi utilizada recentemente. Por favor, escolha uma senha diferente."
            );
        }

        // Atualiza usuário
        usuario.setEmailVerificado(true);
        usuario.setAtivo(true); // Ativa a conta após verificação de email
        usuario.setSenhaTemporaria(false);
        String novaSenhaHash = passwordEncoder.encode(dto.getNovaSenha());
        usuario.setSenhaHash(novaSenhaHash);
        usuario.setUltimaAlteracaoSenhaEm(LocalDateTime.now());

        usuarioRepository.save(usuario);
        
        // Salva no histórico
        historicoSenhaService.salvarHistorico(usuario.getId(), novaSenhaHash);

        // Marca token como usado
        token.setUsado(true);
        tokenRepository.save(token);

        log.info("Conta ativada com sucesso para usuário: {}", usuario.getEmail());

        return MessageResponseDTO.success("Conta ativada com sucesso! Você já pode fazer login.");
    }

    /**
     * Reenvia email de ativação (caso tenha expirado)
     */
    @Transactional
    public MessageResponseDTO reenviarEmailAtivacao(String email) {
        log.info("Reenviando email de ativação para: {}", email);

        AuthUsuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getEmailVerificado()) {
            throw new RuntimeException("Conta já está ativada");
        }

        if (!usuario.getSenhaTemporaria()) {
            throw new RuntimeException("Esta conta não possui senha temporária");
        }

        // Invalida tokens anteriores
        tokenRepository.findByUsuarioAndTipo(usuario, TipoToken.VERIFICACAO_EMAIL)
                .forEach(token -> {
                    token.setUsado(true);
                    tokenRepository.save(token);
                });

        // Gera nova senha temporária
        String novaSenhaTemporaria = gerarSenhaAleatoria();
        usuario.setSenhaHash(passwordEncoder.encode(novaSenhaTemporaria));
        usuarioRepository.save(usuario);

        // Envia novo email
        enviarEmailAtivacao(usuario, novaSenhaTemporaria);

        return MessageResponseDTO.success(
                "Novo email de ativação enviado. Verifique sua caixa de entrada.");
    }

    /**
     * Gera token seguro de 64 caracteres
     */
    private String gerarTokenSeguro() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Gera senha aleatória de 12 caracteres
     */
    private String gerarSenhaAleatoria() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder senha = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            senha.append(chars.charAt(random.nextInt(chars.length())));
        }

        return senha.toString();
    }

    public void trocarSenhaTemporaria(String cpf, FirstLoginPasswordChangeDTO request) {
        AuthUsuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!usuario.getSenhaTemporaria()) {
            throw new RuntimeException("A conta não está marcada para troca de senha temporária");
        }

        if (!request.getNovaSenha().equals(request.getConfirmarSenha())) {
            throw new RuntimeException("As senhas não conferem");
        }

        // Valida política de senha
        PasswordValidator.validarOuLancarExcecao(request.getNovaSenha());

        // Verifica se a senha já foi usada recentemente
        if (historicoSenhaService.senhaJaUsada(usuario.getId(), request.getNovaSenha())) {
            throw new IllegalArgumentException(
                "Esta senha já foi utilizada recentemente. Por favor, escolha uma senha diferente."
            );
        }

        String novaSenhaHash = passwordEncoder.encode(request.getNovaSenha());
        usuario.setSenhaHash(novaSenhaHash);
        usuario.setSenhaTemporaria(false);
        usuario.setUltimaAlteracaoSenhaEm(LocalDateTime.now());

        usuarioRepository.save(usuario);
        
        // Salva no histórico
        historicoSenhaService.salvarHistorico(usuario.getId(), novaSenhaHash);

        log.info("Usuário {} trocou a senha temporária com sucesso", usuario.getEmail());
    }
}
