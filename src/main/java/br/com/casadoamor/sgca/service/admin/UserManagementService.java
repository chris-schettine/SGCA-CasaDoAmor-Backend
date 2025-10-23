package br.com.casadoamor.sgca.service.admin;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.dto.admin.perfil.PerfilDTO;
import br.com.casadoamor.sgca.dto.admin.permissao.PermissaoDTO;
import br.com.casadoamor.sgca.dto.admin.user.AtribuirRolesDTO;
import br.com.casadoamor.sgca.dto.admin.user.CreateUserDTO;
import br.com.casadoamor.sgca.dto.admin.user.UpdateUserDTO;
import br.com.casadoamor.sgca.dto.admin.user.UserResponseDTO;
import br.com.casadoamor.sgca.dto.auth.AuthUsuarioDadosPessoaisDTO;
import br.com.casadoamor.sgca.dto.auth.AuthUsuarioEnderecoDTO;
import br.com.casadoamor.sgca.entity.admin.Perfil;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.entity.auth.AuthUsuarioDadosPessoais;
import br.com.casadoamor.sgca.entity.auth.AuthUsuarioEndereco;
import br.com.casadoamor.sgca.repository.admin.PerfilRepository;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioDadosPessoaisRepository;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioEnderecoRepository;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import br.com.casadoamor.sgca.service.auth.AccountActivationService;
import lombok.RequiredArgsConstructor;

/**
 * Service para gerenciamento de usuários por administradores
 */
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final AuthUsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessaoService sessaoService;
    private final AccountActivationService accountActivationService;
    private final AuthUsuarioEnderecoRepository enderecoRepository;
    private final AuthUsuarioDadosPessoaisRepository dadosPessoaisRepository;

    private static final String SAFE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Cria um novo usuário (admin)
     */
    @Transactional
    public UserResponseDTO criarUsuario(CreateUserDTO dto, Long adminId) {
        // Validações
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }
        if (usuarioRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // Gera senha temporária
        String senhaTemporaria = gerarSenhaAleatoria();

        // Cria o usuário
        AuthUsuario usuario = AuthUsuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .cpf(dto.getCpf())
                .telefone(dto.getTelefone())
                .senhaHash(passwordEncoder.encode(senhaTemporaria))
                .ativo(true)
                .emailVerificado(false)
                .senhaTemporaria(true) // Marca como senha temporária
                .tentativasFalhasDeLogin(0)
                .build();

        // Define tipo
        try {
            usuario.setTipo(AuthUsuario.TipoUsuario.valueOf(dto.getTipo().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de usuário inválido: " + dto.getTipo());
        }

        // Busca admin que está criando
        AuthUsuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado"));
        usuario.setCriadoPor(admin);

        // Cria dados pessoais se fornecidos
        if (dto.getDadosPessoais() != null) {
            AuthUsuarioDadosPessoais dadosPessoais = criarDadosPessoais(dto.getDadosPessoais(), admin);
            usuario.setDadosPessoais(dadosPessoais);
        }

        // Cria endereço se fornecido
        if (dto.getEndereco() != null) {
            AuthUsuarioEndereco endereco = criarEndereco(dto.getEndereco(), admin);
            usuario.setEndereco(endereco);
        }

        // Salva usuário
        AuthUsuario salvo = usuarioRepository.save(usuario);

        // Atribui perfis se fornecidos
        if (dto.getPerfisIds() != null && !dto.getPerfisIds().isEmpty()) {
            atribuirPerfisInterno(salvo, dto.getPerfisIds(), adminId);
        }

        // Envia email de ativação com link + senha temporária
        try {
            accountActivationService.enviarEmailAtivacao(salvo, senhaTemporaria);
        } catch (Exception e) {
            // Log do erro mas não falha a criação
            System.err.println("Erro ao enviar email de ativação: " + e.getMessage());
        }

        return toDTO(salvo);
    }

    /**
     * Lista usuários com paginação
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listarUsuarios(Pageable pageable) {
        return listarUsuarios(null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listarUsuarios(String searchText, Pageable pageable) {
        if (searchText == null || searchText.isBlank()) {
            return usuarioRepository.findAll(pageable).map(this::toDTO);
        }

        String search = "%" + searchText.toLowerCase() + "%";
    String searchPlain = searchText.toLowerCase();

        org.springframework.data.jpa.domain.Specification<br.com.casadoamor.sgca.entity.auth.AuthUsuario> spec = (root, query, cb) -> {
            var predicate = cb.disjunction();

            jakarta.persistence.criteria.Expression<String> nome = root.get("nome").as(String.class);
            jakarta.persistence.criteria.Expression<String> email = root.get("email").as(String.class);
            jakarta.persistence.criteria.Expression<String> telefone = root.get("telefone").as(String.class);

            // contains predicates
            var nomeContains = cb.like(cb.lower(nome), search);
            var emailContains = cb.like(cb.lower(email), search);
            var telefoneContains = cb.like(cb.lower(telefone), search);

            // tipo (enum) - compare name() as lower-case string
            jakarta.persistence.criteria.Expression<String> tipoExpr = root.get("tipo").as(String.class);
            var tipoContains = cb.like(cb.lower(tipoExpr), search);

            jakarta.persistence.criteria.Predicate containsPredicate = cb.or(nomeContains, emailContains, telefoneContains);

            // starts-with
            var nomeStarts = cb.like(cb.lower(nome), searchPlain + "%");
            var emailStarts = cb.like(cb.lower(email), searchPlain + "%");
            var telefoneStarts = cb.like(cb.lower(telefone), searchPlain + "%");
            var tipoStarts = cb.like(cb.lower(tipoExpr), searchPlain + "%");

            // exact
            var exactNome = cb.equal(cb.lower(nome), searchPlain);
            var exactEmail = cb.equal(cb.lower(email), searchPlain);
            var exactTelefone = cb.equal(cb.lower(telefone), searchPlain);
            var exactTipo = cb.equal(cb.lower(tipoExpr), searchPlain);

            var caseExpr = cb.selectCase()
                .when(cb.or(exactNome, exactEmail, exactTelefone, exactTipo), 0)
                .when(cb.or(nomeStarts, emailStarts, telefoneStarts, tipoStarts), 1)
                .when(cb.or(nomeContains, emailContains, telefoneContains, tipoContains), 2)
                .otherwise(3);

            query.orderBy(cb.asc(caseExpr), cb.asc(root.get("id")));

            predicate = containsPredicate;

            return predicate;
        };

        return usuarioRepository.findAll(spec, pageable).map(this::toDTO);
    }

    /**
     * Busca usuário por ID
     */
    @Transactional(readOnly = true)
    public UserResponseDTO buscarPorId(Long id) {
        AuthUsuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toDTO(usuario);
    }

    /**
     * Atualiza dados do usuário
     */
    @Transactional
    public UserResponseDTO atualizarUsuario(Long id, UpdateUserDTO dto, Long adminId) {
        AuthUsuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza campos se fornecidos
        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }
        if (dto.getTelefone() != null) {
            usuario.setTelefone(dto.getTelefone());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(usuario.getEmail())) {
            // Valida se novo email já existe
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Email já cadastrado");
            }
            usuario.setEmail(dto.getEmail());
            usuario.setEmailVerificado(false); // Precisa reverificar
        }
        if (dto.getAtivo() != null) {
            // Impede admin desativar a si mesmo
            if (id.equals(adminId) && !dto.getAtivo()) {
                throw new RuntimeException("Você não pode desativar sua própria conta");
            }
            usuario.setAtivo(dto.getAtivo());
        }
        if (dto.getTipo() != null) {
            try {
                usuario.setTipo(AuthUsuario.TipoUsuario.valueOf(dto.getTipo().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Tipo de usuário inválido: " + dto.getTipo());
            }
        }

        // Busca admin que está atualizando
        AuthUsuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado"));
        usuario.setAtualizadoPor(admin);

        // Atualiza dados pessoais se fornecidos
        if (dto.getDadosPessoais() != null) {
            if (usuario.getDadosPessoais() != null) {
                // Atualiza dados existentes
                atualizarDadosPessoais(usuario.getDadosPessoais(), dto.getDadosPessoais(), admin);
            } else {
                // Cria novos dados
                AuthUsuarioDadosPessoais dadosPessoais = criarDadosPessoais(dto.getDadosPessoais(), admin);
                usuario.setDadosPessoais(dadosPessoais);
            }
        }

        // Atualiza endereço se fornecido
        if (dto.getEndereco() != null) {
            if (usuario.getEndereco() != null) {
                // Atualiza endereço existente
                atualizarEndereco(usuario.getEndereco(), dto.getEndereco(), admin);
            } else {
                // Cria novo endereço
                AuthUsuarioEndereco endereco = criarEndereco(dto.getEndereco(), admin);
                usuario.setEndereco(endereco);
            }
        }

        AuthUsuario atualizado = usuarioRepository.save(usuario);
        return toDTO(atualizado);
    }

    /**
     * Deleta usuário (soft delete)
     */
    @Transactional
    public void deletarUsuario(Long id, Long adminId) {
        // Impede admin deletar a si mesmo
        if (id.equals(adminId)) {
            throw new RuntimeException("Você não pode deletar sua própria conta");
        }

        AuthUsuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getDeletadoEm() != null) {
            throw new RuntimeException("Usuário já foi deletado");
        }

        usuario.setDeletadoEm(LocalDateTime.now());
        usuario.setAtivo(false);
        
        // Revoga todas sessões
        sessaoService.revogarTodasSessoes(id);
        
        usuarioRepository.save(usuario);
    }

    /**
     * Atribui perfis a um usuário
     */
    @Transactional
    public UserResponseDTO atribuirPerfis(Long usuarioId, AtribuirRolesDTO dto, Long adminId) {
        AuthUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        atribuirPerfisInterno(usuario, dto.getPerfisIds(), adminId);
        
        return toDTO(usuario);
    }

    /**
     * Force logout - revoga todas sessões do usuário
     */
    @Transactional
    public void forceLogout(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        
        sessaoService.revogarTodasSessoes(usuarioId);
    }

    /**
     * Obtém permissões efetivas de um usuário (agregadas de todos os perfis)
     */
    @Transactional(readOnly = true)
    public List<PermissaoDTO> obterPermissoesEfetivas(Long usuarioId) {
        AuthUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Coleta todas as permissões de todos os perfis (sem duplicatas)
        return usuario.getPerfis().stream()
                .filter(perfil -> !perfil.isDeletado())
                .flatMap(perfil -> perfil.getPermissoes().stream())
                .filter(permissao -> !permissao.isDeletado())
                .distinct()
                .map(permissao -> PermissaoDTO.builder()
                        .id(permissao.getId())
                        .nome(permissao.getNome())
                        .descricao(permissao.getDescricao())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Método interno para atribuir perfis
     */
    private void atribuirPerfisInterno(AuthUsuario usuario, List<Long> perfisIds, Long adminId) {
        // Remove perfis antigos
        usuario.getPerfis().clear();

        // Busca e adiciona novos perfis
        List<Perfil> perfis = perfilRepository.findByIdIn(perfisIds);
        if (perfis.size() != perfisIds.size()) {
            throw new RuntimeException("Alguns perfis não foram encontrados");
        }

        perfis.forEach(perfil -> {
            if (perfil.isDeletado()) {
                throw new RuntimeException("Perfil " + perfil.getNome() + " foi deletado");
            }
            usuario.getPerfis().add(perfil);
        });

        usuarioRepository.save(usuario);
    }

    /**
     * Gera senha aleatória segura
     */
    private String gerarSenhaAleatoria() {
        StringBuilder senha = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            senha.append(SAFE_CHARS.charAt(RANDOM.nextInt(SAFE_CHARS.length())));
        }
        return senha.toString();
    }

    /**
     * Cria dados pessoais a partir do DTO
     */
    private AuthUsuarioDadosPessoais criarDadosPessoais(AuthUsuarioDadosPessoaisDTO dto, AuthUsuario admin) {
        AuthUsuarioDadosPessoais dadosPessoais = AuthUsuarioDadosPessoais.builder()
                .dataNascimento(dto.getDataNascimento())
                .sexo(dto.getSexo() != null ? AuthUsuarioDadosPessoais.Sexo.valueOf(dto.getSexo().toUpperCase()) : null)
                .genero(dto.getGenero() != null ? AuthUsuarioDadosPessoais.Genero.valueOf(dto.getGenero().toUpperCase()) : AuthUsuarioDadosPessoais.Genero.PREFIRO_NAO_INFORMAR)
                .naturalidade(dto.getNaturalidade())
                .estadoCivil(dto.getEstadoCivil() != null ? AuthUsuarioDadosPessoais.EstadoCivil.valueOf(dto.getEstadoCivil().toUpperCase()) : null)
                .nomeMae(dto.getNomeMae())
                .nomePai(dto.getNomePai())
                .profissao(dto.getProfissao())
                .criadoPor(admin)
                .build();

        return dadosPessoaisRepository.save(dadosPessoais);
    }

    /**
     * Cria endereço a partir do DTO
     */
    private AuthUsuarioEndereco criarEndereco(AuthUsuarioEnderecoDTO dto, AuthUsuario admin) {
        AuthUsuarioEndereco endereco = AuthUsuarioEndereco.builder()
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .complemento(dto.getComplemento())
                .bairro(dto.getBairro())
                .cidade(dto.getCidade())
                .uf(dto.getUf())
                .cep(dto.getCep())
                .criadoPor(admin)
                .build();

        return enderecoRepository.save(endereco);
    }

    /**
     * Atualiza dados pessoais a partir do DTO
     */
    private void atualizarDadosPessoais(AuthUsuarioDadosPessoais dadosPessoais, AuthUsuarioDadosPessoaisDTO dto, AuthUsuario admin) {
        if (dto.getDataNascimento() != null) {
            dadosPessoais.setDataNascimento(dto.getDataNascimento());
        }
        if (dto.getSexo() != null) {
            dadosPessoais.setSexo(AuthUsuarioDadosPessoais.Sexo.valueOf(dto.getSexo().toUpperCase()));
        }
        if (dto.getGenero() != null) {
            dadosPessoais.setGenero(AuthUsuarioDadosPessoais.Genero.valueOf(dto.getGenero().toUpperCase()));
        }
        if (dto.getNaturalidade() != null) {
            dadosPessoais.setNaturalidade(dto.getNaturalidade());
        }
        if (dto.getEstadoCivil() != null) {
            dadosPessoais.setEstadoCivil(AuthUsuarioDadosPessoais.EstadoCivil.valueOf(dto.getEstadoCivil().toUpperCase()));
        }
        if (dto.getNomeMae() != null) {
            dadosPessoais.setNomeMae(dto.getNomeMae());
        }
        if (dto.getNomePai() != null) {
            dadosPessoais.setNomePai(dto.getNomePai());
        }
        if (dto.getProfissao() != null) {
            dadosPessoais.setProfissao(dto.getProfissao());
        }

        dadosPessoais.setAtualizadoEm(LocalDateTime.now());
        dadosPessoais.setAtualizadoPor(admin);
        dadosPessoaisRepository.save(dadosPessoais);
    }

    /**
     * Atualiza endereço a partir do DTO
     */
    private void atualizarEndereco(AuthUsuarioEndereco endereco, AuthUsuarioEnderecoDTO dto, AuthUsuario admin) {
        if (dto.getLogradouro() != null) {
            endereco.setLogradouro(dto.getLogradouro());
        }
        if (dto.getNumero() != null) {
            endereco.setNumero(dto.getNumero());
        }
        if (dto.getComplemento() != null) {
            endereco.setComplemento(dto.getComplemento());
        }
        if (dto.getBairro() != null) {
            endereco.setBairro(dto.getBairro());
        }
        if (dto.getCidade() != null) {
            endereco.setCidade(dto.getCidade());
        }
        if (dto.getUf() != null) {
            endereco.setUf(dto.getUf());
        }
        if (dto.getCep() != null) {
            endereco.setCep(dto.getCep());
        }

        endereco.setAtualizadoEm(LocalDateTime.now());
        endereco.setAtualizadoPor(admin);
        enderecoRepository.save(endereco);
    }

    /**
     * Converte dados pessoais para DTO
     */
    private AuthUsuarioDadosPessoaisDTO dadosPessoaisToDTO(AuthUsuarioDadosPessoais dadosPessoais) {
        if (dadosPessoais == null) {
            return null;
        }

        return AuthUsuarioDadosPessoaisDTO.builder()
                .id(dadosPessoais.getId())
                .dataNascimento(dadosPessoais.getDataNascimento())
                .sexo(dadosPessoais.getSexo() != null ? dadosPessoais.getSexo().name() : null)
                .genero(dadosPessoais.getGenero() != null ? dadosPessoais.getGenero().name() : null)
                .naturalidade(dadosPessoais.getNaturalidade())
                .estadoCivil(dadosPessoais.getEstadoCivil() != null ? dadosPessoais.getEstadoCivil().name() : null)
                .nomeMae(dadosPessoais.getNomeMae())
                .nomePai(dadosPessoais.getNomePai())
                .profissao(dadosPessoais.getProfissao())
                .build();
    }

    /**
     * Converte endereço para DTO
     */
    private AuthUsuarioEnderecoDTO enderecoToDTO(AuthUsuarioEndereco endereco) {
        if (endereco == null) {
            return null;
        }

        return AuthUsuarioEnderecoDTO.builder()
                .id(endereco.getId())
                .logradouro(endereco.getLogradouro())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .bairro(endereco.getBairro())
                .cidade(endereco.getCidade())
                .uf(endereco.getUf())
                .cep(endereco.getCep())
                .build();
    }

    /**
     * Converte entidade para DTO
     */
    private UserResponseDTO toDTO(AuthUsuario usuario) {
        List<PerfilDTO> perfisDTO = usuario.getPerfis().stream()
                .filter(p -> !p.isDeletado())
                .map(p -> PerfilDTO.builder()
                        .id(p.getId())
                        .nome(p.getNome())
                        .descricao(p.getDescricao())
                        .totalPermissoes(p.getPermissoes().size())
                        .permissoes(p.getPermissoes().stream()
                                .map(perm -> PermissaoDTO.builder()
                                        .id(perm.getId())
                                        .nome(perm.getNome())
                                        .descricao(perm.getDescricao())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return UserResponseDTO.builder()
                .id(usuario.getId())
                .uuid(usuario.getUuid())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .telefone(usuario.getTelefone())
                .tipo(usuario.getTipo().name())
                .ativo(usuario.getAtivo())
                .emailVerificado(usuario.getEmailVerificado())
                .ultimoLoginEm(usuario.getUltimoLoginEm())
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .perfis(perfisDTO)
                .dadosPessoais(dadosPessoaisToDTO(usuario.getDadosPessoais()))
                .endereco(enderecoToDTO(usuario.getEndereco()))
                .build();
    }
}
