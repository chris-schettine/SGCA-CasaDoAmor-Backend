package br.com.casadoamor.sgca.dto.admin.user;

import java.time.LocalDateTime;
import java.util.List;

import br.com.casadoamor.sgca.dto.admin.perfil.PerfilDTO;
import br.com.casadoamor.sgca.dto.auth.AuthUsuarioDadosPessoaisDTO;
import br.com.casadoamor.sgca.dto.auth.AuthUsuarioEnderecoDTO;
import br.com.casadoamor.sgca.dto.auth.RegistroProfissionalResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta com dados do usuário
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String uuid;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String tipo;
    private Boolean ativo;
    private Boolean emailVerificado;
    private LocalDateTime ultimoLoginEm;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private List<PerfilDTO> perfis;
    private AuthUsuarioDadosPessoaisDTO dadosPessoais;
    private AuthUsuarioEnderecoDTO endereco;
    private RegistroProfissionalResponseDTO registroProfissional;
}
