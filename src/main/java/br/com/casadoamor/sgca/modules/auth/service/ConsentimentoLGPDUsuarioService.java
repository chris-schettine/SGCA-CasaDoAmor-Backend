package br.com.casadoamor.sgca.modules.auth.service;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.ConsentimentoLGPDUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.auth.repository.ConsentimentoLGPDUsuarioRepository;
import br.com.casadoamor.sgca.modules.funcionario.dto.ConsentimentoLGPDRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ConsentimentoLGPDResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de consentimentos LGPD de usuários do sistema
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentimentoLGPDUsuarioService {

    private final ConsentimentoLGPDUsuarioRepository consentimentoRepository;
    private final AuthUsuarioRepository authUsuarioRepository;

    /**
     * Registra um novo consentimento LGPD para um usuário
     */
    @Transactional
    public ConsentimentoLGPDResponseDTO registrarConsentimento(
            String usuarioCpf,
            ConsentimentoLGPDRequestDTO dto,
            AuthUsuario usuarioLogado) {

        log.info("Registrando consentimento LGPD para usuário CPF: {}", usuarioCpf);

        // Buscar usuário
        AuthUsuario usuario = authUsuarioRepository.findByCpf(usuarioCpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com CPF: " + usuarioCpf));

        // Capturar informações de rastreamento
        HttpServletRequest request = getCurrentRequest();
        String ipOrigem = request != null ? getClientIp(request) : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        // Criar consentimento
        ConsentimentoLGPDUsuario consentimento = ConsentimentoLGPDUsuario.builder()
                .usuario(usuario)
                .versaoTermo(dto.getVersaoTermo())
                .escopo(dto.getEscopo())
                .concorda(dto.getConcorda())
                .dataConsentimento(LocalDateTime.now())
                .ipOrigem(ipOrigem)
                .userAgent(userAgent)
                .registradoPor(usuarioLogado)
                .metadata(dto.getMetadata())
                .createdBy(usuarioLogado)
                .build();

        ConsentimentoLGPDUsuario saved = consentimentoRepository.save(consentimento);
        log.info("Consentimento LGPD registrado com sucesso. ID: {}, Usuário: {}, Concorda: {}",
                saved.getId(), usuarioCpf, saved.getConcorda());

        return toResponseDTO(saved);
    }

    /**
     * Lista histórico completo de consentimentos de um usuário
     */
    @Transactional(readOnly = true)
    public List<ConsentimentoLGPDResponseDTO> listarConsentimentos(String usuarioCpf) {
        log.info("Listando consentimentos LGPD para usuário CPF: {}", usuarioCpf);

        AuthUsuario usuario = authUsuarioRepository.findByCpf(usuarioCpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com CPF: " + usuarioCpf));

        List<ConsentimentoLGPDUsuario> consentimentos = consentimentoRepository
                .findByUsuarioOrderByDataConsentimentoDesc(usuario);

        return consentimentos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se o usuário possui consentimento válido (última entrada com concorda=true)
     */
    @Transactional(readOnly = true)
    public boolean hasConsentimentoValido(String usuarioCpf) {
        log.debug("Verificando consentimento válido para usuário CPF: {}", usuarioCpf);

        AuthUsuario usuario = authUsuarioRepository.findByCpf(usuarioCpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com CPF: " + usuarioCpf));

        return consentimentoRepository.findFirstByUsuarioOrderByDataConsentimentoDesc(usuario)
                .map(ConsentimentoLGPDUsuario::getConcorda)
                .orElse(false);
    }

    /**
     * Obtém o consentimento mais recente de um usuário
     */
    @Transactional(readOnly = true)
    public ConsentimentoLGPDResponseDTO obterConsentimentoAtual(String usuarioCpf) {
        log.debug("Obtendo consentimento atual para usuário CPF: {}", usuarioCpf);

        AuthUsuario usuario = authUsuarioRepository.findByCpf(usuarioCpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com CPF: " + usuarioCpf));

        return consentimentoRepository.findFirstByUsuarioOrderByDataConsentimentoDesc(usuario)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    /**
     * Converte entidade para DTO de resposta
     */
    private ConsentimentoLGPDResponseDTO toResponseDTO(ConsentimentoLGPDUsuario consentimento) {
        return ConsentimentoLGPDResponseDTO.builder()
                .id(consentimento.getId())
                .versaoTermo(consentimento.getVersaoTermo())
                .escopo(consentimento.getEscopo())
                .concorda(consentimento.getConcorda())
                .dataConsentimento(consentimento.getDataConsentimento())
                .ipOrigem(consentimento.getIpOrigem())
                .userAgent(consentimento.getUserAgent())
                .registradoPorNome(consentimento.getRegistradoPor() != null ?
                        consentimento.getRegistradoPor().getNome() : null)
                .metadata(consentimento.getMetadata())
                .createdAt(consentimento.getCreatedAt())
                .build();
    }

    /**
     * Obtém a requisição HTTP atual
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * Extrai o IP do cliente da requisição (considerando proxies)
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headerCandidates = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerCandidates) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For pode conter múltiplos IPs, pegar o primeiro
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
