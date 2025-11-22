package br.com.casadoamor.sgca.modules.acompanhante.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.casadoamor.sgca.infra.config.exception.CustomError;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.RegistrarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.acompanhante.mapper.AcompanhanteMapper;
import br.com.casadoamor.sgca.modules.acompanhante.repository.AcompanhanteRepository;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.repository.DadoPessoalRepository;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.mapper.HistoricoPacienteMapper;
import br.com.casadoamor.sgca.modules.paciente.repository.HistoricoPacienteRepository;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
class AcompanhanteServiceImpTest {

    @Mock
    AcompanhanteRepository acompanhanteRepository;

    @Mock
    PacienteRepository pacienteRepository;

    @Mock
    HistoricoPacienteRepository historicoRepository;

    @Mock
    AcompanhanteMapper acompanhanteMapper;

    @Mock
    HistoricoPacienteMapper historicoPacienteMapper;

    @Mock
    DadoPessoalRepository dadoPessoalRepository;

    @InjectMocks
    AcompanhanteServiceImp service;

    @Test
    void registrarAcompanhante_success() {
        RegistrarAcompanhanteDTO dto = new RegistrarAcompanhanteDTO();
        dto.setPacienteId("p1");
        dto.setDadoPessoal(br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalInputDTO.builder()
            .nome("N").cpf("12345678900").rg("rg").telefone("+5511999999999").dataNascimento(java.time.LocalDate.now().minusYears(30)).sexo(br.com.casadoamor.sgca.modules.common.enums.SexoEnum.MASCULINO)
            .build());
        dto.setEndereco(br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoInputDTO.builder().logradouro("L").numero(1).bairro("B").cidade("C").estado(br.com.casadoamor.sgca.modules.common.enums.EstadoEnum.SAO_PAULO).cep("12345-678").build());
        dto.setParentesco(br.com.casadoamor.sgca.modules.common.enums.Parentesco.PAI);
        dto.setPodeAjudarNaCozinha(Boolean.TRUE);

        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(new Paciente()));
        when(dadoPessoalRepository.findByCpf(any())).thenReturn(Optional.empty());
        when(dadoPessoalRepository.findByRg(any())).thenReturn(Optional.empty());

        Acompanhante ent = new Acompanhante(); ent.setDadoPessoal(new DadoPessoal()); ent.getDadoPessoal().setNome("N");
        when(acompanhanteMapper.toEntity(any(), any())).thenReturn(ent);
        when(acompanhanteMapper.mapToDTO(ent)).thenReturn(br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO.builder().id("a1").build());

        var out = service.registrarAcompanhante(dto);

        assertThat(out).isNotNull();
        verify(acompanhanteRepository).save(any());
        verify(historicoRepository).save(any());
    }

    @Test
    void registrarAcompanhante_throws_whenPacienteMissing() {
        RegistrarAcompanhanteDTO dto = new RegistrarAcompanhanteDTO(); dto.setPacienteId("notfound");
        when(pacienteRepository.findById("notfound")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarAcompanhante(dto)).isInstanceOf(CustomError.class).hasMessageContaining("Paciente não encontrado");
    }

    @Test
    void registrarAcompanhante_throws_whenCpfDuplicate() {
        RegistrarAcompanhanteDTO dto = new RegistrarAcompanhanteDTO(); dto.setPacienteId("p1");
        dto.setDadoPessoal(br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalInputDTO.builder().cpf("12345678900").build());

        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(new Paciente()));
        when(dadoPessoalRepository.findByCpf(any())).thenReturn(Optional.of(new DadoPessoal()));

        assertThatThrownBy(() -> service.registrarAcompanhante(dto)).isInstanceOf(CustomError.class).hasMessageContaining("CPF já cadastrado no sistema");
    }
}
