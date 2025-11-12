package br.com.casadoamor.sgca.modules.paciente.dtos;

import java.time.LocalDateTime;
import java.util.List;

import br.com.casadoamor.sgca.modules.common.dto.DadoPessoalDTO;
import br.com.casadoamor.sgca.modules.common.dto.DadoSocialDTO;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PacienteDTO {
    String id;
    DadoPessoalDTO dadoPessoal;
    EnderecoDTO endereco;
    String email;
    String imageUrl;
    LocalDateTime createdAt;
    List<ContatoEmergenciaDTO> contatosDeEmergencia;
    List<DadoClinicoDTO> dadosClinicos;
    DadoSocialDTO dadoSocial;
    InformacaoHospitalarDTO informacaoHospitalar;
}
