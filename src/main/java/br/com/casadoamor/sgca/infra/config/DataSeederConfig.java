package br.com.casadoamor.sgca.infra.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.casadoamor.sgca.modules.admin.entity.Perfil;
import br.com.casadoamor.sgca.modules.admin.entity.Permissao;
import br.com.casadoamor.sgca.modules.admin.repository.PerfilRepository;
import br.com.casadoamor.sgca.modules.admin.repository.PermissaoRepository;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.TipoUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuração de seed de dados iniciais
 * Apenas executado nos perfis: dev, test
 * 
 * Para desabilitar, use: spring.profiles.active=prod
 * Para habilitar, use: spring.profiles.active=dev ou spring.profiles.active=test
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"}) // Apenas em ambientes de desenvolvimento e teste
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeederConfig {

    private final AuthUsuarioRepository usuarioRepository;
    private final PermissaoRepository permissaoRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDatabase() {
        return args -> {
            log.info("🌱 Iniciando seed de dados...");

            // Criar permissões
            seedPermissoes();

            // Criar perfis
            seedPerfis();

            // Criar usuário admin
            seedAdminUser();

            // Criar usuários de teste (opcional)
            // seedTestUsers();

            log.info("✅ Seed de dados concluído com sucesso!");
        };
    }

    private void seedPermissoes() {
        log.info("📋 Criando permissões...");

        List<Permissao> permissoes = List.of(
            createPermissaoIfNotExists("PACIENTE_READ", "Permissão para visualizar pacientes"),
            createPermissaoIfNotExists("PACIENTE_WRITE", "Permissão para criar e editar pacientes"),
            createPermissaoIfNotExists("PACIENTE_DELETE", "Permissão para excluir pacientes"),
            createPermissaoIfNotExists("PRONTUARIO_READ", "Permissão para visualizar prontuários"),
            createPermissaoIfNotExists("PRONTUARIO_WRITE", "Permissão para criar e editar prontuários"),
            createPermissaoIfNotExists("PRONTUARIO_DELETE", "Permissão para excluir prontuários"),
            createPermissaoIfNotExists("USER_READ", "Permissão para visualizar usuários"),
            createPermissaoIfNotExists("USER_WRITE", "Permissão para criar e editar usuários"),
            createPermissaoIfNotExists("USER_DELETE", "Permissão para excluir usuários")
        );

        log.info("✅ {} permissões criadas/verificadas", permissoes.size());
    }

    private Permissao createPermissaoIfNotExists(String nome, String descricao) {
        return permissaoRepository.findByNome(nome)
            .orElseGet(() -> {
                Permissao permissao = Permissao.builder()
                    .nome(nome)
                    .descricao(descricao)
                    .build();
                Permissao saved = permissaoRepository.save(permissao);
                log.debug("  ➕ Permissão criada: {}", nome);
                return saved;
            });
    }

    private void seedPerfis() {
        log.info("👥 Criando perfis...");

        // MEDICO_GERAL
        createPerfilIfNotExists(
            "MEDICO_GERAL",
            "Perfil para médicos com acesso geral",
            List.of("PACIENTE_READ", "PACIENTE_WRITE", "PRONTUARIO_READ", "PRONTUARIO_WRITE")
        );

        // ENFERMEIRO
        createPerfilIfNotExists(
            "ENFERMEIRO",
            "Perfil para enfermeiros",
            List.of("PACIENTE_READ", "PRONTUARIO_READ")
        );

        // RECEPCIONISTA
        createPerfilIfNotExists(
            "RECEPCIONISTA",
            "Perfil para recepcionistas",
            List.of("PACIENTE_READ", "PACIENTE_WRITE")
        );

        // PSICOLOGO
        createPerfilIfNotExists(
            "PSICOLOGO",
            "Perfil para psicólogos",
            List.of("PACIENTE_READ", "PACIENTE_WRITE", "PRONTUARIO_READ", "PRONTUARIO_WRITE")
        );

        log.info("✅ Perfis criados/verificados");
    }

    private Perfil createPerfilIfNotExists(String nome, String descricao, List<String> permissoesNomes) {
        return perfilRepository.findByNome(nome)
            .orElseGet(() -> {
                Perfil perfil = Perfil.builder()
                    .nome(nome)
                    .descricao(descricao)
                    .build();

                // Adicionar permissões
                for (String permissaoNome : permissoesNomes) {
                    permissaoRepository.findByNome(permissaoNome)
                        .ifPresent(perfil::adicionarPermissao);
                }

                Perfil saved = perfilRepository.save(perfil);
                log.debug("  ➕ Perfil criado: {} com {} permissões", nome, permissoesNomes.size());
                return saved;
            });
    }

    private void seedAdminUser() {
        log.info("👤 Criando usuário admin...");

        String cpf = "00000000000";
        if (usuarioRepository.findByCpf(cpf).isEmpty()) {
            AuthUsuario admin = AuthUsuario.builder()
                .nome("Admin Sistema")
                .email("admin@casadoamor.com")
                .cpf(cpf)
                .senhaHash(passwordEncoder.encode("Admin@123"))
                .telefone("(77) 99999-9999")
                .tipo(TipoUsuario.ADMINISTRADOR)
                .ativo(true)
                .emailVerificado(true)
                .build();

            usuarioRepository.save(admin);
            log.info("✅ Admin criado: {} (CPF: {}, Senha: Admin@123)", admin.getNome(), cpf);
        } else {
            log.info("✅ Admin já existe no banco de dados");
        }
    }

    /**
     * Cria usuários de teste (opcional)
     * Descomente para usar
     */
    @SuppressWarnings("unused")
    private void seedTestUsers() {
        log.info("👥 Criando usuários de teste...");

        // Médico
        createTestUser(
            "Dr. João Silva",
            "joao.silva@casadoamor.com",
            "12345678900",
            "(77) 98888-7777",
            TipoUsuario.MEDICO,
            "MEDICO_GERAL"
        );

        // Enfermeiro
        createTestUser(
            "Enf. Maria Santos",
            "maria.santos@casadoamor.com",
            "98765432100",
            "(77) 98888-6666",
            TipoUsuario.ENFERMEIRO,
            "ENFERMEIRO"
        );

        // Recepcionista
        createTestUser(
            "Ana Oliveira",
            "ana.oliveira@casadoamor.com",
            "11122233344",
            "(77) 98888-5555",
            TipoUsuario.RECEPCIONISTA,
            "RECEPCIONISTA"
        );

        log.info("✅ Usuários de teste criados");
    }

    private void createTestUser(String nome, String email, String cpf, String telefone, 
                                 TipoUsuario tipo, String perfilNome) {
        if (usuarioRepository.findByCpf(cpf).isEmpty()) {
            AuthUsuario usuario = AuthUsuario.builder()
                .nome(nome)
                .email(email)
                .cpf(cpf)
                .senhaHash(passwordEncoder.encode("Teste@123"))
                .telefone(telefone)
                .tipo(tipo)
                .ativo(true)
                .emailVerificado(true)
                .build();

            // Adicionar perfil
            perfilRepository.findByNome(perfilNome)
                .ifPresent(perfil -> usuario.getPerfis().add(perfil));

            usuarioRepository.save(usuario);
            log.debug("  ➕ Usuário criado: {} (CPF: {}, Senha: Teste@123)", nome, cpf);
        }
    }
}
