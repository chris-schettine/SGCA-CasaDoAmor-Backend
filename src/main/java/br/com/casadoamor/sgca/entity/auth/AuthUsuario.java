package br.com.casadoamor.sgca.entity.auth;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import br.com.casadoamor.sgca.entity.admin.Perfil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;
    private String nome;
    private String email;
    private String senhaHash;
    private String telefone;
    private String cpf;
    private LocalDateTime ultimoLoginEm;

    @Builder.Default
    private Boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TipoUsuario tipo = TipoUsuario.RECEPCIONISTA;

    @Builder.Default
    private Integer tentativasFalhasDeLogin = 0;
    private LocalDateTime lockedUntil;

    @Builder.Default
    private Boolean emailVerificado = false;
    
    @Builder.Default
    private Boolean senhaTemporaria = false; // Flag para forçar troca de senha
    
    private LocalDateTime ultimaAlteracaoSenhaEm;
    
    // Campos para armazenamento de foto de perfil
    private String fotoUrl;        // URL completa para acessar a foto
    private String fotoPath;       // Caminho relativo do arquivo no storage
    private LocalDateTime fotoAtualizadaEm; // Data/hora da última atualização da foto

    @Column(updatable = false)
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "criado_por")
    private AuthUsuario criadoPor;

    private LocalDateTime atualizadoEm;

    @ManyToOne
    @JoinColumn(name = "atualizado_por")
    private AuthUsuario atualizadoPor;

    private LocalDateTime deletadoEm;

    @Column(columnDefinition = "JSON")
    private String metadados;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "auth_usuarios_perfis",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    @Builder.Default
    private Set<Perfil> perfis = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id")
    private AuthUsuarioEndereco endereco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dados_pessoais_id")
    private AuthUsuarioDadosPessoais dadosPessoais;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        if (uuid == null) {
            uuid = java.util.UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public enum TipoUsuario {
        ADMINISTRADOR, DENTISTA, ENFERMEIRO, FISIOTERAPEUTA,
        MEDICO, NUTRICIONISTA, RECEPCIONISTA, AUDITOR
    }
}
