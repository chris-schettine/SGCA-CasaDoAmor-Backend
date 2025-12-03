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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
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
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarObitoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

import org.springframework.data.jpa.domain.Specification;

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
    dto.setEndereco(EnderecoInputDTO.builder().logradouro("L").numero(1).bairro("B").cidade("C")
        .estado(br.com.casadoamor.sgca.modules.common.enums.EstadoEnum.SAO_PAULO).cep("12345-678").build());
    dto.setDadoClinico(new DadoClinicoInputDTO());

    when(dadoPessoalRepository.findByCpf(any())).thenReturn(Optional.empty());
    when(dadoPessoalRepository.findByRg(any())).thenReturn(Optional.empty());
    when(pacienteRepository.findByEmail(any())).thenReturn(Optional.empty());

    when(dadoPessoalMapper.toEntity(any())).thenReturn(new DadoPessoal());
    when(enderecoMapper.toEntity(any())).thenReturn(new Endereco());
    when(pacienteMapper.toEntityFromEntities(any(), any())).thenReturn(new Paciente());
    when(dadoClinicoMapper.toEntity(any())).thenReturn(new DadoClinico());
    when(politicaPrivacidadeMapper.aceitouPolitica())
        .thenReturn(new br.com.casadoamor.sgca.modules.paciente.entity.PoliticaPrivacidade());
    when(dadoSocialMapper.toEntity(any())).thenReturn(new DadoSocial());
    when(contatoEmergenciaMapper.toEntityList(any(), any())).thenReturn(List.of());
    when(informacaoHospitalarMapper.toEntity(any())).thenReturn(null);
    when(historicoPacienteMapper.toEntity(any(), any(), any())).thenReturn(new HistoricoPaciente());
    when(pacienteMapper.toDTO(any())).thenReturn(
        br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO.builder().id("id").email("test@example.com").build());

    var result = service.registrarPaciente(dto);

    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("test@example.com");
    verify(pacienteRepository, times(2)).save(any());
    verify(historicoRepository).save(any());
  }

  @Test
  void validarPacienteExistente_whenCpfAlreadyPresent_throwsBadRequest() {
    RegistrarPacienteDTO dto = new RegistrarPacienteDTO();
    DadoPessoalInputDTO dado = DadoPessoalInputDTO.builder().cpf("12345678900").rg("rg")
        .dataNascimento(LocalDate.now().minusYears(20)).nome("N").telefone("+5511999999999")
        .sexo(br.com.casadoamor.sgca.modules.common.enums.SexoEnum.FEMININO).build();
    dto.setDadoPessoal(dado);
    dto.setEmail("a@b.com");
    dto.setEndereco(EnderecoInputDTO.builder().logradouro("L").numero(1).bairro("B").cidade("C")
        .estado(br.com.casadoamor.sgca.modules.common.enums.EstadoEnum.SAO_PAULO).cep("12345-678").build());
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
    when(pacienteMapper.toDTO(paciente))
        .thenReturn(br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO.builder().id(id).email("x@x").build());

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

  @Test
  void validarPacienteExistente_whenRgAlreadyPresent_throwsBadRequest() {
    RegistrarPacienteDTO dto = new RegistrarPacienteDTO();
    // Use RG with only digits so limparRg returns a non-empty value
    DadoPessoalInputDTO dado = DadoPessoalInputDTO.builder().cpf("12345678900").rg("123456789")
        .dataNascimento(LocalDate.now().minusYears(20)).nome("N").telefone("+5511999999999")
        .sexo(br.com.casadoamor.sgca.modules.common.enums.SexoEnum.FEMININO).build();
    dto.setDadoPessoal(dado);
    dto.setEmail("a@b.com");

    when(dadoPessoalRepository.findByCpf(any())).thenReturn(Optional.empty());
    // limparRg("123456789") returns "123456789" (only digits)
    when(dadoPessoalRepository.findByRg("123456789")).thenReturn(Optional.of(new DadoPessoal()));

    assertThatThrownBy(() -> service.registrarPaciente(dto))
        .isInstanceOf(CustomError.class)
        .hasMessageContaining("RG já cadastrado no sistema");
  }

  @Test
  void validarPacienteExistente_whenEmailAlreadyPresent_throwsBadRequest() {
    RegistrarPacienteDTO dto = new RegistrarPacienteDTO();
    DadoPessoalInputDTO dado = DadoPessoalInputDTO.builder().cpf("12345678900").rg("rg")
        .dataNascimento(LocalDate.now().minusYears(20)).nome("N").telefone("+5511999999999")
        .sexo(br.com.casadoamor.sgca.modules.common.enums.SexoEnum.FEMININO).build();
    dto.setDadoPessoal(dado);
    dto.setEmail("existente@b.com");

    when(dadoPessoalRepository.findByCpf(any())).thenReturn(Optional.empty());
    when(dadoPessoalRepository.findByRg(any())).thenReturn(Optional.empty());
    when(pacienteRepository.findByEmail("existente@b.com")).thenReturn(Optional.of(new Paciente()));

    assertThatThrownBy(() -> service.registrarPaciente(dto))
        .isInstanceOf(CustomError.class)
        .hasMessageContaining("Email já cadastrado no sistema");
  }

  @Test
  void validarPacienteExistente_whenDataNascimentoInFuture_throwsBadRequest() {
    RegistrarPacienteDTO dto = new RegistrarPacienteDTO();
    DadoPessoalInputDTO dado = DadoPessoalInputDTO.builder().cpf("12345678900").rg("rg")
        .dataNascimento(LocalDate.now().plusDays(1)).nome("N").telefone("+5511999999999")
        .sexo(br.com.casadoamor.sgca.modules.common.enums.SexoEnum.FEMININO).build();
    dto.setDadoPessoal(dado);
    dto.setEmail("a@b.com");

    when(dadoPessoalRepository.findByCpf(any())).thenReturn(Optional.empty());
    when(dadoPessoalRepository.findByRg(any())).thenReturn(Optional.empty());
    when(pacienteRepository.findByEmail(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.registrarPaciente(dto))
        .isInstanceOf(CustomError.class)
        .hasMessageContaining("Data de nascimento não pode ser no futuro");
  }

  @Test
  void editarPaciente_happyPath_updatesAllFields() {
    String id = "pid";
    br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO dto = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO();

    // Setup DTO
    br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO dado = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO();
    dado.setCpf("11111111111");
    dado.setRg("rg-new");
    dto.setDadoPessoal(dado);

    dto.setEmail("new@example.com");

    br.com.casadoamor.sgca.modules.paciente.dtos.EditarEnderecoInputDTO endereco = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarEnderecoInputDTO();
    endereco.setLogradouro("New St");
    dto.setEndereco(endereco);

    dto.setDadoSocial(new br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoSocialInputDTO());

    br.com.casadoamor.sgca.modules.paciente.dtos.EditarInformacaoHospitalarInputDTO info = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarInformacaoHospitalarInputDTO();
    info.setNomeHospitalReferencia("Hosp");
    dto.setInformacaoHospitalar(info);

    // Setup existing Paciente
    Paciente paciente = new Paciente();
    paciente.setDadoPessoal(new DadoPessoal());
    paciente.getDadoPessoal().setCpf("00000000000");
    paciente.getDadoPessoal().setRg("rg-old");
    paciente.setEndereco(new Endereco());
    paciente.setDadoSocial(new DadoSocial());
    paciente.setInformacaoHospitalar(new br.com.casadoamor.sgca.modules.paciente.entity.InformacaoHospitalar());
    paciente.setEmail("old@example.com");

    when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));

    // Mocks for updates
    when(dadoPessoalMapper.updateEntity(any(), any())).thenReturn(new DadoPessoal());
    when(enderecoMapper.updateEntity(any(), any())).thenReturn(new Endereco());
    when(dadoSocialMapper.toEntityFromEditarDadoPessoalInputDTO(any(), any())).thenReturn(new DadoSocial());
    when(pacienteMapper.toDTO(any()))
        .thenReturn(br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO.builder().build());
    when(historicoPacienteMapper.toEntity(any(), any(), any())).thenReturn(new HistoricoPaciente());

    var result = service.editarPaciente(id, dto);

    assertThat(result).isNotNull();
    verify(pacienteRepository).save(paciente);
    verify(historicoRepository).save(any());
  }

  @Test
  void editarPaciente_whenNotFound_throwsNotFound() {
    when(pacienteRepository.findById("any")).thenReturn(Optional.empty());
    br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO dto = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO();

    assertThatThrownBy(() -> service.editarPaciente("any", dto))
        .isInstanceOf(CustomError.class)
        .hasMessageContaining("Paciente não encontrado");
  }

  @Test
  void editarPaciente_whenCpfConflict_throwsBadRequest() {
    String id = "pid";
    br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO dto = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO();
    br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO dado = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO();
    dado.setCpf("11111111111");
    dto.setDadoPessoal(dado);

    Paciente paciente = new Paciente();
    paciente.setDadoPessoal(new DadoPessoal());
    paciente.getDadoPessoal().setCpf("22222222222");

    when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));
    when(pacienteRepository.existsByCpf("11111111111")).thenReturn(true);

    assertThatThrownBy(() -> service.editarPaciente(id, dto))
        .isInstanceOf(CustomError.class)
        .hasMessageContaining("CPF já cadastrado");
  }

  @Test
  void editarPaciente_whenRgConflict_throwsBadRequest() {
    String id = "pid";
    br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO dto = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO();
    br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO dado = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO();
    // Use digits-only RG value since limparRg removes non-digits
    dado.setRg("987654321");
    dto.setDadoPessoal(dado);

    Paciente paciente = new Paciente();
    paciente.setDadoPessoal(new DadoPessoal());
    // Use different digits-only RG for existing value
    paciente.getDadoPessoal().setRg("123456789");

    when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));
    // limparRg("987654321") returns "987654321"
    when(pacienteRepository.existsByRg("987654321")).thenReturn(true);

    assertThatThrownBy(() -> service.editarPaciente(id, dto))
        .isInstanceOf(CustomError.class)
        .hasMessageContaining("RG já cadastrado");
  }

  @Test
  void editarPaciente_whenEmailConflict_throwsBadRequest() {
    String id = "pid";
    br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO dto = new br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO();
    dto.setEmail("conflict@example.com");

    Paciente paciente = new Paciente();
    paciente.setEmail("old@example.com");

    when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));
    when(pacienteRepository.existsByEmail("conflict@example.com")).thenReturn(true);

    assertThatThrownBy(() -> service.editarPaciente(id, dto))
        .isInstanceOf(CustomError.class)
        .hasMessageContaining("Email já cadastrado");
  }

  // ==================== TESTES DE PAGINAÇÃO ====================

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withSearchText_returnsFilteredPaginatedResult() {
    // Given
    Paciente paciente = new Paciente();
    paciente.setId("p1");
    List<Paciente> allPacientes = List.of(paciente);

    PacienteDTO pacienteDTO = PacienteDTO.builder().id("p1").email("test@test.com").build();

    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(allPacientes);
    when(pacienteMapper.toDTO(paciente)).thenReturn(pacienteDTO);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(pacienteDTO))
            .totalCount(1)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.pacientesPaginados(
        "João", 10, 0,
        null, null, null,
        null, null,
        null, null, null,
        null, null
    );

    // Then
    assertThat(result).isNotNull();
    assertThat(result.totalCount()).isEqualTo(1);
    verify(pacienteRepository).findAll(any(Specification.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withStatusFilter_returnsFilteredResult() {
    // Given
    Paciente paciente = new Paciente();
    paciente.setId("p1");
    paciente.setStatus(br.com.casadoamor.sgca.modules.common.enums.PacienteStatus.ATIVO);
    List<Paciente> allPacientes = List.of(paciente);

    PacienteDTO pacienteDTO = PacienteDTO.builder().id("p1").email("test@test.com").build();

    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(allPacientes);
    when(pacienteMapper.toDTO(paciente)).thenReturn(pacienteDTO);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(pacienteDTO))
            .totalCount(1)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.pacientesPaginados(
        null, 10, 0,
        br.com.casadoamor.sgca.modules.common.enums.PacienteStatus.ATIVO, null, null,
        null, null,
        null, null, null,
        null, null
    );

    // Then
    assertThat(result).isNotNull();
    verify(pacienteRepository).findAll(any(Specification.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withPaginationOffset_returnsPaginatedWithPreviousPage() {
    // Given
    Paciente p1 = new Paciente();
    p1.setId("p1");
    Paciente p2 = new Paciente();
    p2.setId("p2");
    Paciente p3 = new Paciente();
    p3.setId("p3");
    List<Paciente> allPacientes = List.of(p1, p2, p3);

    PacienteDTO dto2 = PacienteDTO.builder().id("p2").email("b@b.com").build();

    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(allPacientes);
    // O service usa offset=1, limit=1, então pega apenas p2 (subList(1, 2))
    when(pacienteMapper.toDTO(p2)).thenReturn(dto2);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(dto2))
            .totalCount(3)
            .hasPreviousPage(true)
            .hasNextPage(true)
            .build());

    // When - offset=1, limit=1, so skip first and get second
    var result = service.pacientesPaginados(
        null, 1, 1,
        null, null, null,
        null, null,
        null, null, null,
        null, null
    );

    // Then
    assertThat(result).isNotNull();
    assertThat(result.hasPreviousPage()).isTrue();
    assertThat(result.hasNextPage()).isTrue();
    assertThat(result.totalCount()).isEqualTo(3);
  }

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withNoResults_returnsEmptyPaginatedResult() {
    // Given
    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(List.of());
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of())
            .totalCount(0)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.pacientesPaginados(
        "nonexistent", 10, 0,
        null, null, null,
        null, null,
        null, null, null,
        null, null
    );

    // Then
    assertThat(result).isNotNull();
    assertThat(result.totalCount()).isEqualTo(0);
    assertThat(result.nodes()).isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withDiagnosticoFilter_appliesFilter() {
    // Given
    Paciente paciente = new Paciente();
    paciente.setId("p1");
    List<Paciente> allPacientes = List.of(paciente);

    PacienteDTO pacienteDTO = PacienteDTO.builder().id("p1").email("test@test.com").build();

    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(allPacientes);
    when(pacienteMapper.toDTO(paciente)).thenReturn(pacienteDTO);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(pacienteDTO))
            .totalCount(1)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.pacientesPaginados(
        null, 10, 0,
        null, "Câncer", null,
        null, null,
        null, null, null,
        null, null
    );

    // Then
    assertThat(result).isNotNull();
    verify(pacienteRepository).findAll(any(Specification.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withHospitalReferenciaFilter_appliesFilter() {
    // Given
    Paciente paciente = new Paciente();
    paciente.setId("p1");
    List<Paciente> allPacientes = List.of(paciente);

    PacienteDTO pacienteDTO = PacienteDTO.builder().id("p1").email("test@test.com").build();

    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(allPacientes);
    when(pacienteMapper.toDTO(paciente)).thenReturn(pacienteDTO);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(pacienteDTO))
            .totalCount(1)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.pacientesPaginados(
        null, 10, 0,
        null, null, "Hospital das Clínicas",
        null, null,
        null, null, null,
        null, null
    );

    // Then
    assertThat(result).isNotNull();
    verify(pacienteRepository).findAll(any(Specification.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withDateRangeFilter_appliesFilter() {
    // Given
    Paciente paciente = new Paciente();
    paciente.setId("p1");
    List<Paciente> allPacientes = List.of(paciente);

    PacienteDTO pacienteDTO = PacienteDTO.builder().id("p1").email("test@test.com").build();

    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(allPacientes);
    when(pacienteMapper.toDTO(paciente)).thenReturn(pacienteDTO);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(pacienteDTO))
            .totalCount(1)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.pacientesPaginados(
        null, 10, 0,
        null, null, null,
        "2024-01-01", "2024-12-31",
        null, null, null,
        null, null
    );

    // Then
    assertThat(result).isNotNull();
    verify(pacienteRepository).findAll(any(Specification.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withAgeRangeAndGenderFilter_appliesFilters() {
    // Given
    Paciente paciente = new Paciente();
    paciente.setId("p1");
    List<Paciente> allPacientes = List.of(paciente);

    PacienteDTO pacienteDTO = PacienteDTO.builder().id("p1").email("test@test.com").build();

    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(allPacientes);
    when(pacienteMapper.toDTO(paciente)).thenReturn(pacienteDTO);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(pacienteDTO))
            .totalCount(1)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.pacientesPaginados(
        null, 10, 0,
        null, null, null,
        null, null,
        18, 65, br.com.casadoamor.sgca.modules.common.enums.SexoEnum.MASCULINO,
        null, null
    );

    // Then
    assertThat(result).isNotNull();
    verify(pacienteRepository).findAll(any(Specification.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withCidadeAndNecessidadeFilter_appliesFilters() {
    // Given
    Paciente paciente = new Paciente();
    paciente.setId("p1");
    List<Paciente> allPacientes = List.of(paciente);

    PacienteDTO pacienteDTO = PacienteDTO.builder().id("p1").email("test@test.com").build();

    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(allPacientes);
    when(pacienteMapper.toDTO(paciente)).thenReturn(pacienteDTO);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(pacienteDTO))
            .totalCount(1)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.pacientesPaginados(
        null, 10, 0,
        null, null, null,
        null, null,
        null, null, null,
        "São Paulo", "Cadeira de Rodas"
    );

    // Then
    assertThat(result).isNotNull();
    verify(pacienteRepository).findAll(any(Specification.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void pacientesPaginados_withAllFilters_appliesAllFilters() {
    // Given
    Paciente paciente = new Paciente();
    paciente.setId("p1");
    List<Paciente> allPacientes = List.of(paciente);

    PacienteDTO pacienteDTO = PacienteDTO.builder().id("p1").email("test@test.com").build();

    when(pacienteRepository.findAll(any(Specification.class))).thenReturn(allPacientes);
    when(pacienteMapper.toDTO(paciente)).thenReturn(pacienteDTO);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(pacienteDTO))
            .totalCount(1)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.pacientesPaginados(
        "João", 10, 0,
        br.com.casadoamor.sgca.modules.common.enums.PacienteStatus.ATIVO, "Câncer", "Hospital Samaritano",
        "2024-01-01", "2024-12-31",
        30, 50, br.com.casadoamor.sgca.modules.common.enums.SexoEnum.FEMININO,
        "Campinas", "Diabetes"
    );

    // Then
    assertThat(result).isNotNull();
    verify(pacienteRepository).findAll(any(Specification.class));
  }

  // ==================== TESTES DE HISTÓRICO PAGINADO ====================

  @Test
  void historicoPacientePaginado_happyPath_returnsPaginatedHistorico() {
    // Given
    String pacienteId = "pid";
    Paciente paciente = new Paciente();
    paciente.setId(pacienteId);

    HistoricoPaciente h1 = new HistoricoPaciente();
    h1.setId("h1");
    h1.setDescricao("Registro inicial");
    HistoricoPaciente h2 = new HistoricoPaciente();
    h2.setId("h2");
    h2.setDescricao("Atualização de dados");
    List<HistoricoPaciente> historicos = List.of(h1, h2);

    HistoricoPacienteDTO dto1 = new HistoricoPacienteDTO();
    dto1.setId("h1");
    HistoricoPacienteDTO dto2 = new HistoricoPacienteDTO();
    dto2.setId("h2");

    when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
    when(historicoRepository.findByPacienteOrderByDataRegistroDesc(paciente)).thenReturn(historicos);
    when(historicoPacienteMapper.toHistoricoPacienteDTO(h1)).thenReturn(dto1);
    when(historicoPacienteMapper.toHistoricoPacienteDTO(h2)).thenReturn(dto2);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(dto1, dto2))
            .totalCount(2)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.historicoPacientePaginado(pacienteId, 10, 0);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.totalCount()).isEqualTo(2);
    assertThat(result.hasPreviousPage()).isFalse();
    assertThat(result.hasNextPage()).isFalse();
  }

  @Test
  void historicoPacientePaginado_withOffset_returnsPaginatedWithPreviousPage() {
    // Given
    String pacienteId = "pid";
    Paciente paciente = new Paciente();
    paciente.setId(pacienteId);

    HistoricoPaciente h1 = new HistoricoPaciente();
    h1.setId("h1");
    HistoricoPaciente h2 = new HistoricoPaciente();
    h2.setId("h2");
    HistoricoPaciente h3 = new HistoricoPaciente();
    h3.setId("h3");
    List<HistoricoPaciente> historicos = List.of(h1, h2, h3);

    HistoricoPacienteDTO dto2 = new HistoricoPacienteDTO();
    dto2.setId("h2");

    when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
    when(historicoRepository.findByPacienteOrderByDataRegistroDesc(paciente)).thenReturn(historicos);
    when(historicoPacienteMapper.toHistoricoPacienteDTO(h2)).thenReturn(dto2);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(dto2))
            .totalCount(3)
            .hasPreviousPage(true)
            .hasNextPage(true)
            .build());

    // When - offset=1, limit=1
    var result = service.historicoPacientePaginado(pacienteId, 1, 1);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.hasPreviousPage()).isTrue();
    assertThat(result.hasNextPage()).isTrue();
    assertThat(result.totalCount()).isEqualTo(3);
  }

  @Test
  void historicoPacientePaginado_whenPacienteNotFound_throwsNotFound() {
    // Given
    when(pacienteRepository.findById("nonexistent")).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> service.historicoPacientePaginado("nonexistent", 10, 0))
        .isInstanceOf(CustomError.class)
        .hasMessageContaining("Paciente não encontrado");
  }

  @Test
  void historicoPacientePaginado_withEmptyHistorico_returnsEmptyResult() {
    // Given
    String pacienteId = "pid";
    Paciente paciente = new Paciente();
    paciente.setId(pacienteId);

    when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
    when(historicoRepository.findByPacienteOrderByDataRegistroDesc(paciente)).thenReturn(List.of());
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of())
            .totalCount(0)
            .hasPreviousPage(false)
            .hasNextPage(false)
            .build());

    // When
    var result = service.historicoPacientePaginado(pacienteId, 10, 0);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.totalCount()).isEqualTo(0);
    assertThat(result.nodes()).isEmpty();
  }

  @Test
  void historicoPacientePaginado_atLastPage_hasNoNextPage() {
    // Given
    String pacienteId = "pid";
    Paciente paciente = new Paciente();
    paciente.setId(pacienteId);

    HistoricoPaciente h1 = new HistoricoPaciente();
    h1.setId("h1");
    HistoricoPaciente h2 = new HistoricoPaciente();
    h2.setId("h2");
    List<HistoricoPaciente> historicos = List.of(h1, h2);

    HistoricoPacienteDTO dto2 = new HistoricoPacienteDTO();
    dto2.setId("h2");

    when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
    when(historicoRepository.findByPacienteOrderByDataRegistroDesc(paciente)).thenReturn(historicos);
    when(historicoPacienteMapper.toHistoricoPacienteDTO(h2)).thenReturn(dto2);
    when(paginatedMapper.toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean()))
        .thenReturn(br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO.builder()
            .nodes(List.of(dto2))
            .totalCount(2)
            .hasPreviousPage(true)
            .hasNextPage(false)
            .build());

    // When - offset=1, limit=1, at last page
    var result = service.historicoPacientePaginado(pacienteId, 1, 1);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.hasPreviousPage()).isTrue();
    assertThat(result.hasNextPage()).isFalse();
  }
}
