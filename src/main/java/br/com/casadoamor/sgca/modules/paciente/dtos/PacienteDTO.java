package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public class PacienteDTO {
    private String id;
    private DadoPessoalDTO dadoPessoal;
    private EnderecoDTO endereco;
    private String email;
    private String imageUrl;
    private LocalDateTime createdAt;
    private List<ContatoEmergenciaDTO> contatosDeEmergencia;
    private List<DadoClinicoDTO> dadosClinicos;
}
