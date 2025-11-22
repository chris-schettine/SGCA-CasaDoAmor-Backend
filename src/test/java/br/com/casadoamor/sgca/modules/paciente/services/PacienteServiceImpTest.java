package br.com.casadoamor.sgca.modules.paciente.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.casadoamor.sgca.infra.config.exception.CustomError;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.DadoSocial;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.dadoClinico.entity.DadoClinico;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarObitoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

@ExtendWith(MockitoExtension.class)
class PacienteServiceImpTest {

  @Mock
  private br.com.casadoamor.sgca.modules.common.repository.DadoPessoalRepository dadoPessoalRepository;

  @Mock
  private br.com.casadoamor.sgca.modules.common.mapper.EnderecoMapper enderecoMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.common.mapper.DadoPessoalMapper dadoPessoalMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.paciente.mapper.PacienteMapper pacienteMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.common.mapper.PaginatedResponseMapper paginatedMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository pacienteRepository;

  @Mock
  private br.com.casadoamor.sgca.modules.paciente.mapper.HistoricoPacienteMapper historicoPacienteMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.paciente.repository.HistoricoPacienteRepository historicoRepository;

  @Mock
  private br.com.casadoamor.sgca.modules.paciente.mapper.ContatoEmergenciaMapper contatoEmergenciaMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.common.mapper.DadoSocialMapper dadoSocialMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.paciente.mapper.PoliticaPrivacidadeMapper politicaPrivacidadeMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.dadoClinico.mapper.DadoClinicoMapper dadoClinicoMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.paciente.mapper.InformacaoHospitalarMapper informacaoHospitalarMapper;

  @Mock
  private br.com.casadoamor.sgca.modules.paciente.mapper.ObituarioMapper obituarioMapper;

  @InjectMocks
  private PacienteServiceImp service;

  @BeforeEach
  void setup() {
    // ensure security context is clear by default
    SecurityContextHolder.clearContext();
  }

  @Test
  void registrarPaciente_happyPath_savesAndReturnsDTO() {
    RegistrarPacienteDTO dto = new RegistrarPacienteDTO();
    DadoPessoalInputDTO dado = DadoPessoalInputDTO.builder()
      .cpf("12345678900")
      .rg("rg")
      .dataNascimento(LocalDate.now().minusYears(30))
      .nome("J")
      .telefone("+5511999999999")
      .sexo(br.com.casadoamor.sgca.modules.common.enums.SexoEnum.MASCULINO)
      .build();

    dto.setDadoPessoal(dado);
    dto.setEmail("TEST@EXAMPLE.COM");
    dto.setEndereco(EnderecoInputDTO.builder().logradouro("L").numero(1).bairro("B").cidade("C").estado(br.com.casadoamor.sgca.modules.common.enums.EstadoEnum.SAO_PAULO).cep("12345-678").build());
    dto.setDadoClinico(new DadoClinicoInputDTO());

    when(dadoPessoalRepository.findByCpf(any())).thenReturn(Optional.empty());
    when(dadoPessoalRepository.findByRg(any())).thenReturn(Optional.empty());
    when(pacienteRepository.findByEmail(any())).thenReturn(Optional.empty());

    when(dadoPessoalMapper.toEntity(any())).thenReturn(new DadoPessoal());
    when(enderecoMapper.toEntity(any())).thenReturn(new Endereco());
    when(pacienteMapper.toEntityFromEntities(any(), any())).thenReturn(new Paciente());
    when(dadoClinicoMapper.toEntity(any())).thenReturn(new DadoClinico());
    when(politicaPrivacidadeMapper.aceitouPolitica()).thenReturn(new br.com.casadoamor.sgca.modules.paciente.entity.PoliticaPrivacidade());
    when(dadoSocialMapper.toEntity(any())).thenReturn(new DadoSocial());
    when(contatoEmergenciaMapper.toEntityList(any(), any())).thenReturn(List.of());
    when(informacaoHospitalarMapper.toEntity(any())).thenReturn(null);
    when(historicoPacienteMapper.toEntity(any(), any(), any())).thenReturn(new HistoricoPaciente());
    when(pacienteMapper.toDTO(any())).thenReturn(br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO.builder().id("id").email("test@example.com").build());

    var result = service.registrarPaciente(dto);

    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("test@example.com");
    verify(pacienteRepository, times(2)).save(any());
    verify(historicoRepository).save(any());
  }

  @Test
  void validarPacienteExistente_whenCpfAlreadyPresent_throwsBadRequest() {
    RegistrarPacienteDTO dto = new RegistrarPacienteDTO();
    DadoPessoalInputDTO dado = DadoPessoalInputDTO.builder().cpf("12345678900").rg("rg").dataNascimento(LocalDate.now().minusYears(20)).nome("N").telefone("+5511999999999").sexo(br.com.casadoamor.sgca.modules.common.enums.SexoEnum.FEMININO).build();
    dto.setDadoPessoal(dado);
    dto.setEmail("a@b.com");
    dto.setEndereco(EnderecoInputDTO.builder().logradouro("L").numero(1).bairro("B").cidade("C").estado(br.com.casadoamor.sgca.modules.common.enums.EstadoEnum.SAO_PAULO).cep("12345-678").build());
    dto.setDadoClinico(new DadoClinicoInputDTO());

    when(dadoPessoalRepository.findByCpf(any())).thenReturn(Optional.of(new DadoPessoal()));

    assertThatThrownBy(() -> service.registrarPaciente(dto))
      .isInstanceOf(CustomError.class)
      .hasMessageContaining("CPF já cadastrado no sistema");
  }

  @Test
  void deletarPaciente_marksAsDeleted_andSaves() {
    String id = "pid";
    Paciente paciente = new Paciente();
    paciente.setStatus(br.com.casadoamor.sgca.modules.common.enums.PacienteStatus.ATIVO);

    // set authentication
    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(new TestingAuthenticationToken("user-x", null));
    SecurityContextHolder.setContext(ctx);

    when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));

    service.deletarPaciente(id);

    assertThat(paciente.isDeleted()).isTrue();
    assertThat(paciente.getStatus()).isEqualTo(br.com.casadoamor.sgca.modules.common.enums.PacienteStatus.INATIVO);
    assertThat(paciente.getDeletedBy()).isEqualTo("user-x");
    verify(pacienteRepository).save(any());
  }

  @Test
  void deletarPaciente_whenAlreadyDeleted_throwsBadRequest() {
    String id = "pid";
    Paciente paciente = new Paciente();
    paciente.setDeletedAt(LocalDateTime.now());

    // set authentication so deletarPaciente does not NPE
    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(new TestingAuthenticationToken("user-b", null));
    SecurityContextHolder.setContext(ctx);

    when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));

    assertThatThrownBy(() -> service.deletarPaciente(id))
      .isInstanceOf(CustomError.class)
      .hasMessageContaining("Paciente já foi removido anteriormente");
  }

  @Test
  void registrarObito_setsStatusAndSaves() {
    String id = "pid";
    Paciente paciente = new Paciente();
    paciente.setStatus(br.com.casadoamor.sgca.modules.common.enums.PacienteStatus.ATIVO);

    RegistrarObitoDTO dto = new RegistrarObitoDTO();

    when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));
    when(obituarioMapper.toEntity(dto)).thenReturn(new br.com.casadoamor.sgca.modules.paciente.entity.Obituario());
    when(historicoPacienteMapper.toEntity(any(), any(), any())).thenReturn(new HistoricoPaciente());
    when(pacienteMapper.toDTO(paciente)).thenReturn(br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO.builder().id(id).email("x@x").build());

    var result = service.registrarObito(id, dto);

    assertThat(result).isNotNull();
    assertThat(paciente.getStatus()).isEqualTo(br.com.casadoamor.sgca.modules.common.enums.PacienteStatus.FALECIDO);
    verify(historicoRepository).save(any());
  }

  @Test
  void registrarObito_whenAlreadyFaleceido_throwsBadRequest() {
    String id = "pid";
    Paciente paciente = new Paciente();
    paciente.setStatus(br.com.casadoamor.sgca.modules.common.enums.PacienteStatus.FALECIDO);

    when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));

    assertThatThrownBy(() -> service.registrarObito(id, new RegistrarObitoDTO()))
      .isInstanceOf(CustomError.class)
      .hasMessageContaining("Óbito já registrado para este paciente.");
  }
}
