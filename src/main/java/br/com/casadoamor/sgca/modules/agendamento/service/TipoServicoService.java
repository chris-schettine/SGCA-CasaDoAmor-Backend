package br.com.casadoamor.sgca.modules.agendamento.service;

import br.com.casadoamor.sgca.modules.agendamento.entity.TipoServico;
import br.com.casadoamor.sgca.modules.agendamento.dto.TipoServicoResponseDTO;
import br.com.casadoamor.sgca.modules.agendamento.repository.TipoServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TipoServicoService {

    private final TipoServicoRepository tipoServicoRepository;

    @Transactional(readOnly = true)
    public TipoServicoResponseDTO buscarPorId(Long id) {
        TipoServico tipoServico = tipoServicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de serviço não encontrado"));
        return toResponseDTO(tipoServico);
    }

    @Transactional(readOnly = true)
    public List<TipoServicoResponseDTO> listarAtivos() {
        return tipoServicoRepository.findByAtivoTrue().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private TipoServicoResponseDTO toResponseDTO(TipoServico tipo) {
        return TipoServicoResponseDTO.builder()
                .id(tipo.getId())
                .codigo(tipo.getCodigo())
                .nome(tipo.getNome())
                .descricao(tipo.getDescricao())
                .categoria(tipo.getCategoria())
                .duracaoMinutos(tipo.getDuracaoMinutos())
                .requerProfissional(tipo.getRequerProfissional())
                .permiteAcompanhante(tipo.getPermiteAcompanhante())
                .ativo(tipo.getAtivo())
                .observacoes(tipo.getObservacoes())
                .build();
    }
}
