package br.com.casadoamor.sgca.service.auth;

import br.com.casadoamor.sgca.entity.auth.HistoricoSenha;
import br.com.casadoamor.sgca.repository.auth.HistoricoSenhaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsável por gerenciar o histórico de senhas dos usuários.
 * Previne a reutilização de senhas antigas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricoSenhaService {

    private final HistoricoSenhaRepository historicoSenhaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Quantidade de senhas antigas a verificar.
     * Padrão: 5 últimas senhas.
     */
    @Value("${password.history.check-last:5}")
    private int quantidadeSenhasVerificar;

    /**
     * Salva uma nova senha no histórico do usuário.
     *
     * @param usuarioId ID do usuário
     * @param senhaHash Hash da senha (já deve estar criptografada)
     */
    @Transactional
    public void salvarHistorico(Long usuarioId, String senhaHash) {
        log.debug("Salvando senha no histórico do usuário ID: {}", usuarioId);
        
        HistoricoSenha historico = HistoricoSenha.builder()
                .usuarioId(usuarioId)
                .senhaHash(senhaHash)
                .criadoEm(LocalDateTime.now())
                .build();
        
        historicoSenhaRepository.save(historico);
        log.info("Senha salva no histórico do usuário ID: {}", usuarioId);
    }

    /**
     * Verifica se uma senha já foi utilizada recentemente pelo usuário.
     * Compara com as últimas N senhas (configurável via password.history.check-last).
     *
     * @param usuarioId ID do usuário
     * @param novaSenha Senha em texto plano a ser verificada
     * @return true se a senha já foi usada, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean senhaJaUsada(Long usuarioId, String novaSenha) {
        log.debug("Verificando se senha já foi usada pelo usuário ID: {}", usuarioId);
        
        // Busca as últimas N senhas do usuário
        List<HistoricoSenha> historicoSenhas = historicoSenhaRepository
                .findTopNByUsuarioIdOrderByCriadoEmDesc(
                        usuarioId, 
                        PageRequest.of(0, quantidadeSenhasVerificar)
                );

        if (historicoSenhas.isEmpty()) {
            log.debug("Usuário ID: {} não possui histórico de senhas", usuarioId);
            return false;
        }

        // Verifica se a nova senha corresponde a alguma senha antiga
        for (HistoricoSenha historico : historicoSenhas) {
            if (passwordEncoder.matches(novaSenha, historico.getSenhaHash())) {
                log.warn("Usuário ID: {} tentou reutilizar uma senha antiga", usuarioId);
                return true;
            }
        }

        log.debug("Senha não foi encontrada no histórico do usuário ID: {}", usuarioId);
        return false;
    }

    /**
     * Retorna a quantidade de senhas que são verificadas no histórico.
     *
     * @return Quantidade de senhas verificadas
     */
    public int getQuantidadeSenhasVerificar() {
        return quantidadeSenhasVerificar;
    }

    /**
     * Busca o histórico de senhas de um usuário.
     *
     * @param usuarioId ID do usuário
     * @param limite    Quantidade máxima de registros
     * @return Lista do histórico de senhas
     */
    @Transactional(readOnly = true)
    public List<HistoricoSenha> buscarHistorico(Long usuarioId, int limite) {
        return historicoSenhaRepository.findTopNByUsuarioIdOrderByCriadoEmDesc(
                usuarioId, 
                PageRequest.of(0, limite)
        );
    }

    /**
     * Conta quantas senhas um usuário possui no histórico.
     *
     * @param usuarioId ID do usuário
     * @return Quantidade de senhas
     */
    @Transactional(readOnly = true)
    public long contarHistorico(Long usuarioId) {
        return historicoSenhaRepository.countByUsuarioId(usuarioId);
    }
}
