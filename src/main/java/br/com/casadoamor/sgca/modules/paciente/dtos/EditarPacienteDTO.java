package br.com.casadoamor.sgca.modules.paciente.dtos;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarPacienteDTO {
  @Valid
  private EditarDadoPessoalInputDTO dadoPessoal;

  @Valid
  private EditarEnderecoInputDTO endereco;

  @Valid
  private EditarDadoSocialInputDTO dadoSocial;

  @Valid
  private EditarInformacaoHospitalarInputDTO informacaoHospitalar;

  private String email;
}
