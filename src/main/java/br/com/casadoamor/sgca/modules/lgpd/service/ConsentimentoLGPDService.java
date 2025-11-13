package br.com.casadoamor.sgca.modules.lgpd.service;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDRequestDTO;
import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDResponseDTO;
import br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD;
import br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD.TipoEntidadeLGPD;
import br.com.casadoamor.sgca.modules.lgpd.repository.ConsentimentoLGPDRepository;
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
 * Service unificado para gerenciar consentimentos LGPD de todas as entidades
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentimentoLGPDService {

    private final ConsentimentoLGPDRepository consentimentoRepository;

    /**
     * Registra um novo consentimento LGPD
     * Captura automaticamente IP e User-Agent da requisição
     */
    @Transactional
    public ConsentimentoLGPDResponseDTO registrarConsentimento(
            TipoEntidadeLGPD tipoEntidade,
            Long entidadeId,
            ConsentimentoLGPDRequestDTO request,
            AuthUsuario registradoPor
    ) {
        log.info("Registrando consentimento LGPD: tipo={}, entidadeId={}, versao={}, concorda={}", 
                tipoEntidade, entidadeId, request.getVersaoTermo(), request.getConcorda());

        // Captura contexto da requisição
        HttpServletRequest httpRequest = getCurrentRequest();
        String ipOrigem = httpRequest != null ? getClientIp(httpRequest) : null;
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;

        // Cria o consentimento
        ConsentimentoLGPD consentimento = ConsentimentoLGPD.builder()
                .tipoEntidade(tipoEntidade)
                .entidadeId(entidadeId)
                .versaoTermo(request.getVersaoTermo())
                .escopo(request.getEscopo())
                .concorda(request.getConcorda())
                .dataConsentimento(LocalDateTime.now())
                .ipOrigem(ipOrigem)
                .userAgent(userAgent)
                .registradoPor(registradoPor)
                .createdBy(registradoPor)
                .metadata(request.getMetadata())
                .build();

        ConsentimentoLGPD saved = consentimentoRepository.save(consentimento);
        
        log.info("Consentimento registrado com sucesso: id={}, uuid={}, ip={}", 
                saved.getId(), saved.getUuid(), ipOrigem);

        return toResponseDTO(saved);
    }

    /**
     * Lista todos os consentimentos de uma entidade
     */
    @Transactional(readOnly = true)
    public List<ConsentimentoLGPDResponseDTO> listarConsentimentos(
            TipoEntidadeLGPD tipoEntidade,
            Long entidadeId
    ) {
        log.info("Listando consentimentos: tipo={}, entidadeId={}", tipoEntidade, entidadeId);

        return consentimentoRepository
                .findByTipoEntidadeAndEntidadeIdOrderByDataConsentimentoDesc(tipoEntidade, entidadeId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se uma entidade possui consentimento válido
     */
    @Transactional(readOnly = true)
    public boolean hasConsentimentoValido(TipoEntidadeLGPD tipoEntidade, Long entidadeId) {
        boolean valido = consentimentoRepository.hasConsentimentoValido(tipoEntidade, entidadeId);
        log.debug("Verificação de consentimento: tipo={}, entidadeId={}, valido={}", 
                tipoEntidade, entidadeId, valido);
        return valido;
    }

    /**
     * Obtém o consentimento mais recente de uma entidade
     */
    @Transactional(readOnly = true)
    public ConsentimentoLGPDResponseDTO obterConsentimentoAtual(
            TipoEntidadeLGPD tipoEntidade,
            Long entidadeId
    ) {
        log.info("Buscando consentimento atual: tipo={}, entidadeId={}", tipoEntidade, entidadeId);

        return consentimentoRepository
                .findFirstByTipoEntidadeAndEntidadeIdOrderByDataConsentimentoDesc(tipoEntidade, entidadeId)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    /**
     * Lista consentimentos por versão do termo
     */
    @Transactional(readOnly = true)
    public List<ConsentimentoLGPDResponseDTO> listarPorVersao(String versaoTermo) {
        log.info("Listando consentimentos por versão: {}", versaoTermo);

        return consentimentoRepository.findByVersaoTermoOrderByDataConsentimentoDesc(versaoTermo)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista consentimentos por tipo de entidade
     */
    @Transactional(readOnly = true)
    public List<ConsentimentoLGPDResponseDTO> listarPorTipo(TipoEntidadeLGPD tipoEntidade) {
        log.info("Listando consentimentos por tipo: {}", tipoEntidade);

        return consentimentoRepository.findByTipoEntidadeOrderByDataConsentimentoDesc(tipoEntidade)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista consentimentos em um período
     */
    @Transactional(readOnly = true)
    public List<ConsentimentoLGPDResponseDTO> listarPorPeriodo(
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        log.info("Listando consentimentos por período: {} a {}", dataInicio, dataFim);

        return consentimentoRepository.findByDataConsentimentoBetween(dataInicio, dataFim)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca consentimentos desatualizados que precisam ser renovados
     */
    @Transactional(readOnly = true)
    public List<ConsentimentoLGPDResponseDTO> listarConsentimentosDesatualizados(
            TipoEntidadeLGPD tipoEntidade,
            String versaoAtual
    ) {
        log.info("Buscando consentimentos desatualizados: tipo={}, versaoAtual={}", 
                tipoEntidade, versaoAtual);

        return consentimentoRepository.findConsentimentosDesatualizados(tipoEntidade, versaoAtual)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Conta consentimentos por tipo e concordância
     */
    @Transactional(readOnly = true)
    public Long contarConsentimentos(TipoEntidadeLGPD tipoEntidade, Boolean concorda) {
        return consentimentoRepository.countByTipoEntidadeAndConcorda(tipoEntidade, concorda);
    }

    // ==================== Métodos auxiliares ====================

    /**
     * Obtém a requisição HTTP atual
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("Não foi possível obter a requisição atual: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrai o IP real do cliente, considerando proxies
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
                // Pega o primeiro IP se houver múltiplos (cadeia de proxies)
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Converte entidade para DTO de resposta
     */
    private ConsentimentoLGPDResponseDTO toResponseDTO(ConsentimentoLGPD consentimento) {
        return ConsentimentoLGPDResponseDTO.builder()
                .id(consentimento.getId())
                .uuid(consentimento.getUuid())
                .tipoEntidade(consentimento.getTipoEntidade().name())
                .entidadeId(consentimento.getEntidadeId())
                .versaoTermo(consentimento.getVersaoTermo())
                .escopo(consentimento.getEscopo())
                .concorda(consentimento.getConcorda())
                .dataConsentimento(consentimento.getDataConsentimento())
                .ipOrigem(consentimento.getIpOrigem())
                .userAgent(consentimento.getUserAgent())
                .registradoPorNome(consentimento.getRegistradoPor() != null 
                        ? consentimento.getRegistradoPor().getNome() 
                        : null)
                .metadata(consentimento.getMetadata())
                .createdAt(consentimento.getCreatedAt())
                .build();
    }
}
