package br.com.casadoamor.sgca.modules.funcionario.service;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.funcionario.dto.ConsentimentoLGPDRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ConsentimentoLGPDResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.entity.ConsentimentoLGPDProfissional;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import br.com.casadoamor.sgca.modules.funcionario.repository.ConsentimentoLGPDProfissionalRepository;
import br.com.casadoamor.sgca.modules.funcionario.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de consentimentos LGPD de profissionais
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentimentoLGPDService {

    private final ConsentimentoLGPDProfissionalRepository consentimentoRepository;
    private final ProfissionalRepository profissionalRepository;

    @Transactional
    public ConsentimentoLGPDResponseDTO registrarConsentimento(
            String profissionalUuid,
            ConsentimentoLGPDRequestDTO dto,
            AuthUsuario usuarioLogado) {
        
        log.info("Registrando consentimento LGPD para profissional: {}", profissionalUuid);

        Profissional profissional = profissionalRepository.findByUuid(profissionalUuid)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        ConsentimentoLGPDProfissional consentimento = ConsentimentoLGPDProfissional.builder()
                .profissional(profissional)
                .versaoTermo(dto.getVersaoTermo())
                .escopo(dto.getEscopo())
                .concorda(dto.getConcorda())
                .ipOrigem(dto.getIpOrigem())
                .userAgent(dto.getUserAgent())
                .registradoPor(usuarioLogado)
                .metadata(dto.getMetadata())
                .createdBy(usuarioLogado)
                .build();

        consentimento = consentimentoRepository.save(consentimento);
        log.info("Consentimento LGPD registrado: ID={}", consentimento.getId());

        return toResponseDTO(consentimento);
    }

    @Transactional(readOnly = true)
    public List<ConsentimentoLGPDResponseDTO> listarConsentimentos(String profissionalUuid) {
        Profissional profissional = profissionalRepository.findByUuid(profissionalUuid)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        return consentimentoRepository.findByProfissionalOrderByDataConsentimentoDesc(profissional)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean hasConsentimentoValido(String profissionalUuid) {
        Profissional profissional = profissionalRepository.findByUuid(profissionalUuid)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        return consentimentoRepository.hasConsentimentoValido(profissional);
    }

    private ConsentimentoLGPDResponseDTO toResponseDTO(ConsentimentoLGPDProfissional consentimento) {
        return ConsentimentoLGPDResponseDTO.builder()
                .id(consentimento.getId())
                .versaoTermo(consentimento.getVersaoTermo())
                .escopo(consentimento.getEscopo())
                .concorda(consentimento.getConcorda())
                .dataConsentimento(consentimento.getDataConsentimento())
                .ipOrigem(consentimento.getIpOrigem())
                .registradoPorNome(consentimento.getRegistradoPor() != null ? 
                        consentimento.getRegistradoPor().getNome() : null)
                .metadata(consentimento.getMetadata())
                .build();
    }
}
