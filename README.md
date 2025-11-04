# SGCA - Sistema de Gestão Casa do Amor Backend

Sistema de gerenciamento backend para a Casa do Amor, desenvolvido em Spring Boot para controle de usuários, pacientes e profissionais de saúde com recursos avançados de segurança.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Docker](https://img.shields.io/badge/Docker-Supported-blue)
![Security](https://img.shields.io/badge/Security-JWT%20%2B%202FA-red)

## 📋 Índice

- [🔒 Aviso de Segurança](#-aviso-de-segurança)
- [Visão Geral](#visão-geral)
- [Funcionalidades de Segurança](#funcionalidades-de-segurança)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Configuração](#instalação-e-configuração)
- [Executando o Projeto](#executando-o-projeto)
- [API Documentation](#api-documentation)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Segurança e Autenticação](#segurança-e-autenticação)
- [Docker](#docker)
- [Banco de Dados](#banco-de-dados)
- [Testes](#testes)
- [Melhorias Futuras](#melhorias-futuras)
- [Contribuição](#contribuição)

## 🔒 Aviso de Segurança

⚠️ **IMPORTANTE**: Este projeto contém arquivos de configuração sensíveis.

- ❌ **NUNCA** commite o arquivo `.env` 
- ❌ **NUNCA** commite secrets (JWT_SECRET, senhas, tokens de API)
- ✅ Use `.env.template` ou `.env.example` como referência
- 🔐 Mantenha suas credenciais em segurança
- 🔑 Gere um JWT_SECRET único e forte para produção
- 📧 Use senhas de aplicativo para configuração de email

**Leia:** [SECURITY_NOTICE.md](SECURITY_NOTICE.md) para mais informações sobre práticas de segurança.

## 🎯 Visão Geral

O SGCA Backend é uma API REST desenvolvida para gerenciar operações da Casa do Amor:

- **Autenticação e Autorização**: Sistema completo com JWT e 2FA
- **Gestão de Usuários**: Controle de acesso por perfis (Admin, Recepcionista, etc.)
- **Pacientes**: Cadastro completo com dados pessoais, clínicos e endereços
- **Acompanhantes**: Gestão de acompanhantes dos pacientes com relacionamentos familiares
- **Contatos de Emergência**: Registro de contatos para situações de emergência
- **Profissionais de Saúde**: Gestão de profissionais com documentos e especialidades
- **Upload de Arquivos**: Sistema de gerenciamento de fotos de perfil
- **Auditoria**: Rastreamento de sessões e tentativas de login

### Funcionalidades Principais

- ✅ **Autenticação JWT** com refresh tokens
- ✅ **2FA (Two-Factor Authentication)** via email
- ✅ **Recuperação de senha** com tokens seguros
- ✅ **Gerenciamento de sessões** ativas
- ✅ **Rate limiting** para proteção contra ataques
- ✅ **Upload de arquivos** com validação de tipo e tamanho
- ✅ **Migração de banco de dados** com Flyway
- ✅ **API RESTful** com versionamento
- ✅ **Documentação Swagger/OpenAPI** interativa
- ✅ **Containerização Docker** com multi-stage build
- ✅ **Health checks** e monitoring com Spring Actuator
- ✅ **CORS configurado** para integração frontend

## 🛡️ Funcionalidades de Segurança

### Autenticação e Autorização
- **JWT (JSON Web Tokens)** para autenticação stateless
- **BCrypt** com força 12 para hashing de senhas
- **Autenticação 2FA** opcional via código por email
- **Controle de acesso baseado em roles** (RBAC)
- **Sessões rastreadas** com possibilidade de logout remoto

### Proteções Implementadas
- **Rate Limiting** com Bucket4j para prevenir brute force
- **Bloqueio de conta** após múltiplas tentativas falhas
- **Histórico de senhas** para prevenir reutilização
- **Tokens de recuperação** com expiração e uso único
- **Validação de força de senha** com requisitos mínimos
- **Ativação de conta** via email antes do primeiro login
- **Senhas temporárias** para novos usuários criados por admin

### Boas Práticas
- Container Docker executa com **usuário não-root**
- **Secrets gerenciados** via variáveis de ambiente
- **CORS restrito** a origens específicas
- **Validação de entrada** em todos os endpoints
- **Tratamento centralizado de exceções**

## 🚀 Tecnologias

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.3.5** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Spring Web** - API REST
- **Spring Mail** - Envio de emails
- **Hibernate** - ORM
- **Maven** - Gerenciamento de dependências
- **Lombok** - Redução de boilerplate

### Segurança
- **JWT (jjwt 0.12.6)** - Tokens de autenticação
- **BCrypt** - Hashing de senhas
- **Bucket4j** - Rate limiting
- **Spring Security** - Framework de segurança

### Banco de Dados
- **MySQL 8.0** - Banco de dados relacional
- **Flyway** - Migração e versionamento de schema
- **HikariCP** - Pool de conexões de alta performance

### Documentação
- **SpringDoc OpenAPI 3** - Documentação automática da API
- **Swagger UI** - Interface interativa para testes

### DevOps
- **Docker & Docker Compose** - Containerização
- **Multi-stage build** - Otimização de imagens
- **Health checks** - Monitoramento de serviços

## 📋 Pré-requisitos

### Para execução local:
- Java 21 ou superior
- Maven 3.8+
- MySQL 8.0

### Para execução com Docker:
- Docker 20.10+
- Docker Compose 2.0+

## ⚙️ Instalação e Configuração

### 1. Clone o repositório
```bash
git clone https://github.com/chris-schettine/SGCA-CasaDoAmor-Backend.git
cd SGCA-CasaDoAmor-Backend
```

### 2. Configuração do Banco de Dados (Local)

Crie o banco de dados MySQL:
```sql
CREATE DATABASE sgca CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sgca_user'@'localhost' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON sgca.* TO 'sgca_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configuração das Variáveis de Ambiente

⚠️ **IMPORTANTE**: Não use as credenciais padrão em produção!

Copie o arquivo de exemplo e configure suas credenciais:
```bash
cp .env.example .env
```

Edite o arquivo `.env` com suas credenciais:
```bash
# Database
SGCA_DB_PASSWORD=sua_senha_segura_aqui
MYSQL_ROOT_PASSWORD=sua_senha_root_aqui
MYSQL_DATABASE=sgca
MYSQL_USER=sgca_user

# JWT - GERE UM SECRET FORTE E ÚNICO!
# Use: echo -n "sua-frase-secreta-muito-longa" | base64
JWT_SECRET=sua_chave_base64_aqui
JWT_EXPIRATION=3600000

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu_email@gmail.com
SGCA_EMAIL_PASSWORD=sua_senha_de_app_aqui
```

### 4. Configuração do application.properties

Certifique-se de que o `application.properties` usa variáveis de ambiente:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/sgca
spring.datasource.username=sgca_user
spring.datasource.password=${SGCA_DB_PASSWORD}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:3600000}

# Email
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${SGCA_EMAIL_PASSWORD}
```

## 🏃‍♂️ Executando o Projeto

### Opção 1: Execução Local

```bash
# Compilar o projeto
mvn clean compile

# Executar testes
mvn test

# Executar aplicação
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### Opção 2: Docker (Recomendado)

```bash
# Construir e executar com Docker Compose
docker compose up --build -d

# Verificar status dos containers
docker compose ps

# Ver logs
docker compose logs -f sgca-backend
```

A aplicação estará disponível em: `http://localhost:8090`

### Opção 3: Apenas Docker do Backend

```bash
# Build da imagem
docker build -t sgca-backend .

# Executar container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/sgca \
  -e SPRING_DATASOURCE_USERNAME=sgca_user \
  -e SPRING_DATASOURCE_PASSWORD=admin \
  sgca-backend
```

## 📚 API Documentation

### Swagger UI (Recomendado)
Acesse a documentação interativa:
- Local: `http://localhost:8080/docs`
- Docker: `http://localhost:8090/docs`

### Base URL
- Local: `http://localhost:8080`
- Docker: `http://localhost:8090`

### Health Check
```http
GET /actuator/health
```

### Autenticação

Todos os endpoints (exceto públicos) requerem autenticação via JWT.

#### Header de Autorização
```http
Authorization: Bearer {seu_token_jwt}
```

### Endpoints Públicos (Sem Autenticação)

#### Autenticação
```http
POST   /auth/register              # Registrar novo usuário
POST   /auth/login                 # Login (retorna JWT)
POST   /auth/forgot-password       # Solicitar recuperação de senha
POST   /auth/reset-password        # Redefinir senha com token
POST   /auth/verify-email          # Verificar email
POST   /auth/activate-account      # Ativar conta
POST   /auth/resend-activation     # Reenviar email de ativação
GET    /api/files/**               # Acessar arquivos públicos (fotos)
```

### Endpoints Autenticados

#### Autenticação e Perfil
```http
POST   /auth/logout                # Logout da sessão atual
POST   /auth/logout-all            # Logout de todas as sessões
GET    /auth/sessions              # Listar sessões ativas
DELETE /auth/sessions/{token}      # Encerrar sessão específica
POST   /auth/change-password       # Alterar senha
```

#### Two-Factor Authentication (2FA)
```http
POST   /auth/2fa/setup             # Configurar 2FA
POST   /auth/2fa/enable            # Habilitar/Desabilitar 2FA
POST   /auth/2fa/verify            # Verificar código 2FA no login
POST   /auth/2fa/disable           # Desabilitar 2FA
```

#### Pacientes (Requer autenticação)
```http
POST   /pacientes                  # Criar paciente
GET    /pacientes                  # Listar pacientes
GET    /pacientes/{id}             # Buscar por ID
PUT    /pacientes/{id}             # Atualizar paciente
DELETE /pacientes/{id}             # Deletar paciente
```

#### Acompanhantes (Requer autenticação)
```http
POST   /acompanhantes/             # Registrar acompanhante
PATCH  /acompanhantes/{id}         # Editar acompanhante
GET    /acompanhantes/             # Listar acompanhantes (paginação)
```

#### Contatos de Emergência (Requer autenticação)
```http
POST   /contatos-emergencia        # Criar contato de emergência
GET    /contatos-emergencia        # Listar contatos
GET    /contatos-emergencia/{id}   # Buscar por ID
PUT    /contatos-emergencia/{id}   # Atualizar contato
DELETE /contatos-emergencia/{id}   # Deletar contato
```

#### Upload de Arquivos
```http
POST   /api/files/upload           # Upload de foto (multipart/form-data)
GET    /api/files/{filename}       # Acessar arquivo
```

#### Admin (Requer role ADMINISTRADOR)
```http
GET    /admin/usuarios             # Listar todos usuários
POST   /admin/usuarios             # Criar usuário (com senha temporária)
PUT    /admin/usuarios/{id}        # Atualizar usuário
DELETE /admin/usuarios/{id}        # Deletar usuário
POST   /admin/usuarios/{id}/reset-password  # Resetar senha
GET    /admin/perfis               # Listar perfis
POST   /admin/perfis               # Criar perfil
POST   /admin/usuarios/{id}/perfis # Atribuir perfil a usuário
```

### Exemplo de Requisição: Login

```bash
# 1. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "12345678900",
    "senha": "SuaSenha123!"
  }'

# Resposta:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "email": "usuario@example.com",
  "nome": "Nome do Usuário",
  "tipoUsuario": "RECEPCIONISTA",
  "expiresIn": 3600000,
  "requires2FA": false
}

# 2. Usar o token em requisições
curl -X GET http://localhost:8080/pacientes \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Exemplo de Requisição: Criar Acompanhante

```bash
curl -X POST http://localhost:8080/acompanhantes/ \
  -H "Authorization: Bearer {seu_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": "uuid-do-paciente",
    "podeAjudarNaCozinha": true,
    "parentesco": "FILHO",
    "dadoPessoal": {
      "nome": "João Silva Filho",
      "cpf": "12345678901",
      "dataNascimento": "2000-01-01",
      "telefone": "(11) 99999-9999",
      "email": "joao.filho@email.com"
    },
    "endereco": {
      "logradouro": "Rua das Flores",
      "numero": 123,
      "bairro": "Centro",
      "cidade": "São Paulo",
      "estado": "SP",
      "cep": "01234567"
    }
  }'
```

## 📁 Estrutura do Projeto

```
src/main/java/br/com/casadoamor/sgca/
├── SgcaBackendApplication.java     # Classe principal
├── annotation/                     # Anotações customizadas
│   └── RateLimit.java             # Anotação para rate limiting
├── aspect/                         # Aspectos AOP
│   └── RateLimitAspect.java       # Implementação de rate limiting
├── config/                         # Configurações
│   ├── SecurityConfig.java        # Configuração Spring Security
│   ├── SwaggerConfig.java         # Configuração OpenAPI/Swagger
│   └── exception/                 # Tratamento global de exceções
│       ├── GlobalExceptionHandler.java
│       ├── CustomError.java
│       └── RateLimitExceededException.java
├── controller/                     # Controllers REST
│   ├── admin/                     # Endpoints administrativos
│   │   └── AdminController.java
│   ├── auth/                      # Autenticação
│   │   ├── AuthController.java
│   │   └── TwoFactorController.java
│   ├── acompanhante/              # Gestão de acompanhantes
│   │   └── AcompanhanteController.java
│   ├── file/                      # Upload de arquivos
│   │   └── FileController.java
│   └── paciente/                  # Gestão de pacientes
│       └── PacienteController.java
├── dto/                           # Data Transfer Objects
│   ├── admin/                     # DTOs administrativos
│   ├── acompanhante/              # DTOs de acompanhantes
│   ├── auth/                      # DTOs de autenticação
│   ├── common/                    # DTOs comuns
│   ├── paciente/                  # DTOs de pacientes
│   └── twofactor/                 # DTOs de 2FA
├── entity/                        # Entidades JPA
│   ├── admin/                     # Entidades administrativas
│   │   ├── AuthUsuario.java
│   │   ├── Perfil.java
│   │   ├── SessaoUsuario.java
│   │   └── HistoricoSenha.java
│   ├── acompanhante/              # Entidades de acompanhantes
│   │   └── Acompanhante.java
│   ├── auth/                      # Entidades de autenticação
│   │   ├── TentativaLogin.java
│   │   ├── TokenRecuperacao.java
│   │   └── Autenticacao2FA.java
│   ├── common/                    # Entidades comuns
│   │   ├── BaseEntity.java
│   │   ├── DadoPessoal.java
│   │   └── Endereco.java
│   └── paciente/                  # Entidades de pacientes
│       ├── Paciente.java
│       └── DadoClinico.java
├── enums/                         # Enumerações
│   ├── TipoUsuarioEnum.java
│   ├── TipoToken.java
│   └── EstadoEnum.java
├── exception/                     # Exceções customizadas
│   └── ResourceNotFoundException.java
├── mapper/                        # Mapeadores (Entity <-> DTO)
├── repository/                    # Repositórios JPA
│   ├── acompanhante/
│   │   └── AcompanhanteRepository.java
│   ├── admin/
│   ├── auth/
│   └── paciente/
├── security/                      # Componentes de segurança
│   ├── JwtUtil.java              # Utilitário JWT
│   ├── JwtAuthenticationFilter.java  # Filtro JWT
│   └── UserDetailsServiceImpl.java   # Carregamento de usuários
├── service/                       # Serviços de negócio
│   ├── acompanhante/              # Serviços de acompanhantes
│   │   └── AcompanhanteService.java
│   ├── admin/                     # Serviços administrativos
│   │   ├── UserManagementService.java
│   │   ├── PerfilService.java
│   │   └── SessaoService.java
│   ├── auth/                      # Serviços de autenticação
│   │   ├── AuthService.java
│   │   ├── RecuperacaoSenhaService.java
│   │   └── TwoFactorService.java
│   ├── common/                    # Serviços comuns
│   │   ├── EmailService.java
│   │   └── FileStorageService.java
│   └── paciente/
│       └── PacienteService.java
└── util/                          # Utilitários
    └── PasswordValidator.java     # Validação de senhas

src/main/resources/
├── application.properties          # Configuração principal
├── application-docker.properties   # Configuração para Docker
├── application-test.properties     # Configuração para testes
└── db/migration/                   # Migrações Flyway
    ├── V01__create_tables.sql
    ├── V02__create_tables.sql
    ├── V03__create_autenticacao_2fa.sql
    ├── V04__add_senha_temporaria.sql
    └── V05__add_foto_columns.sql
```

## � Segurança e Autenticação

### Fluxo de Autenticação

1. **Registro** → Usuário se registra com CPF e senha
2. **Ativação** → Email de ativação é enviado
3. **Login** → Usuário faz login com CPF e senha
4. **2FA (Opcional)** → Se habilitado, código é enviado por email
5. **Token JWT** → Token é retornado para uso em requisições

### Configuração de Segurança

#### Requisitos de Senha
- Mínimo 8 caracteres
- Pelo menos 1 letra maiúscula
- Pelo menos 1 letra minúscula
- Pelo menos 1 número
- Pelo menos 1 caractere especial
- Não pode ser reutilizada (últimas 5 senhas)

#### BCrypt Hashing
```java
// Força 12 (recomendado para dados de saúde)
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
```

#### JWT Configuration
```properties
jwt.secret=${JWT_SECRET}          # Base64 encoded secret
jwt.expiration=3600000             # 1 hora em milissegundos
```

#### Rate Limiting
```java
@RateLimit(requests = 5, window = 60)  // 5 requisições por minuto
public ResponseEntity<?> login(...) { ... }
```

### Roles e Permissões

| Role | Descrição | Permissões |
|------|-----------|------------|
| **ADMINISTRADOR** | Acesso total ao sistema | Gerenciar usuários, perfis, todas as operações |
| **RECEPCIONISTA** | Operações do dia-a-dia | Criar/editar pacientes, visualizar dados |
| **PROFISSIONAL_SAUDE** | Profissional de saúde | Acessar dados clínicos, atualizar tratamentos |

### Proteções Implementadas

#### Bloqueio de Conta
- 5 tentativas falhas de login → Conta bloqueada
- Desbloqueio via email ou por administrador

#### Sessões
- Rastreamento de todas as sessões ativas
- Logout remoto de sessões específicas
- Logout de todas as sessões simultaneamente

#### Tokens de Recuperação
- Validade de 1 hora
- Uso único (invalidado após uso)
- Todos os tokens anteriores são invalidados ao gerar novo

## 🐳 Docker

### Arquivo docker-compose.yml

O projeto inclui configuração completa com:
- **MySQL 8.0** na porta 3316 (externa) → 3306 (interna)
- **Backend Spring Boot** na porta 8090 (externa) → 8080 (interna)
- **Rede dedicada** (sgca-network)
- **Volumes persistentes** para dados do MySQL e uploads
- **Health checks** configurados
- **Usuário não-root** no container do backend

### Comandos Docker Úteis

```bash
# Iniciar todos os serviços
docker compose up -d

# Iniciar com rebuild (após mudanças no código)
docker compose up --build -d

# Parar todos os serviços
docker compose down

# Ver logs em tempo real
docker compose logs -f

# Ver logs específicos
docker compose logs mysql
docker compose logs sgca

# Verificar status dos containers
docker compose ps

# Acessar container MySQL
docker exec -it mysql_sgca mysql -u sgca_user -p sgca

# Acessar shell do container backend
docker exec -it spring_sgca sh

# Rebuild completo (remove containers e imagens)
docker compose down
docker compose build --no-cache
docker compose up -d

# Limpar volumes (⚠️ CUIDADO: remove dados!)
docker compose down -v
```

### Portas Utilizadas

| Serviço | Porta Interna | Porta Externa | Descrição |
|---------|---------------|---------------|-----------|
| MySQL | 3306 | 3316 | Banco de dados |
| Backend | 8080 | 8090 | API REST |

### Variáveis de Ambiente Docker

Configuradas no arquivo `.env`:
```env
# MySQL
MYSQL_DATABASE=sgca
MYSQL_USER=sgca_user
SGCA_DB_PASSWORD=sua_senha_aqui
MYSQL_ROOT_PASSWORD=sua_senha_root_aqui

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu_email@gmail.com
SGCA_EMAIL_PASSWORD=sua_senha_de_app_aqui
```

## 🗄️ Banco de Dados

### Gerenciamento de Schema

O projeto usa **Flyway** para versionamento e migração do banco de dados.

#### Arquivos de Migração
```
src/main/resources/db/migration/
├── V01__create_tables.sql              # Tabelas principais
├── V02__create_tables.sql              # Tabelas adicionais
├── V03__create_autenticacao_2fa.sql    # Suporte a 2FA
├── V04__add_senha_temporaria.sql       # Senhas temporárias
├── V05__add_foto_columns.sql           # Colunas para fotos
├── V06__seed_initial_data.sql          # Dados iniciais
├── V07__seed_dados_pessoais.sql        # Dados pessoais seed
├── V08__seed_enderecos_pacientes.sql   # Endereços de pacientes
├── V09__add_nome_mae_profissao.sql     # Nome da mãe e profissão
├── V10__update_dados_clinicos.sql      # Atualização dados clínicos
├── V11__seed_recepcionista_test.sql    # Usuário recepcionista teste
├── V12__add_granular_permissions.sql   # Permissões granulares
├── V13__add_auth_usuarios.sql          # Usuários de autenticação
├── V14__remove_rg_orgao_emissor.sql    # Remoção RG e órgão emissor
├── V15__clean_cpf_formatting.sql       # Limpeza formatação CPF
├── V16__create_registros_profissionais.sql # Registros profissionais
├── V17__create_2fa_rate_limit.sql      # Rate limit para 2FA
├── V18__enable_2fa_existing_users.sql  # Habilitar 2FA usuários existentes
├── V19__fix_admin_password.sql         # Correção senha admin
├── V20__fix_admin_credentials.sql      # Correção credenciais admin
├── V21__fix_admin_password_hash.sql    # Hash senha admin
├── V22__fix_admin_final.sql            # Correção final admin
├── V23__fix_cors_and_admin.sql         # Correção CORS e admin
├── V24__update_table_acompanhente.sql  # Tabela acompanhantes
├── V25__create_historico_paciente.sql  # Histórico do paciente
├── V26__create_roles_acompanhantes.sql # Roles para acompanhantes
├── V27__alter_paciente_table.sql       # Alteração tabela pacientes
└── V28__create_contato_emergencia.sql  # Contatos de emergência
```

### Principais Tabelas

#### auth_usuario
Armazena usuários do sistema com autenticação
```sql
CREATE TABLE auth_usuario (
  id CHAR(36) PRIMARY KEY,
  cpf VARCHAR(11) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  senha_hash VARCHAR(255) NOT NULL,
  tipo VARCHAR(50) NOT NULL,  -- ADMINISTRADOR, RECEPCIONISTA, etc.
  ativo BOOLEAN DEFAULT TRUE,
  conta_bloqueada BOOLEAN DEFAULT FALSE,
  senha_temporaria BOOLEAN DEFAULT FALSE,
  requer_2fa BOOLEAN DEFAULT FALSE,
  email_verificado BOOLEAN DEFAULT FALSE,
  ...
);
```

#### dados_pessoais
Dados pessoais de indivíduos
```sql
CREATE TABLE dados_pessoais (
  id CHAR(36) PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  cpf VARCHAR(11) UNIQUE NOT NULL,
  data_nascimento DATE,
  rg VARCHAR(10),
  telefone VARCHAR(255),
  foto_url VARCHAR(500),
  ...
);
```

#### pacientes
Informações de pacientes
```sql
CREATE TABLE pacientes (
  id CHAR(36) PRIMARY KEY,
  dado_pessoal_id CHAR(36),
  endereco_id CHAR(36),
  FOREIGN KEY (dado_pessoal_id) REFERENCES dados_pessoais(id),
  FOREIGN KEY (endereco_id) REFERENCES enderecos(id)
);
```

#### dados_clinicos
Dados clínicos dos pacientes
```sql
CREATE TABLE dados_clinicos (
  id CHAR(36) PRIMARY KEY,
  diagnostico VARCHAR(255),
  tratamento VARCHAR(255),
  usa_sonda BOOLEAN NOT NULL,
  tipo_sonda VARCHAR(255),
  usa_curativo BOOLEAN NOT NULL,
  paciente_id CHAR(36) NOT NULL,
  FOREIGN KEY (paciente_id) REFERENCES pacientes(id)
);
```

#### sessao_usuario
Rastreamento de sessões ativas
```sql
CREATE TABLE sessao_usuario (
  id CHAR(36) PRIMARY KEY,
  usuario_id CHAR(36) NOT NULL,
  token_jwt TEXT NOT NULL,
  ip_address VARCHAR(45),
  user_agent VARCHAR(500),
  data_login TIMESTAMP NOT NULL,
  data_expiracao TIMESTAMP NOT NULL,
  ativa BOOLEAN DEFAULT TRUE,
  ...
);
```

#### tentativa_login
Registro de tentativas de login (para bloqueio)
```sql
CREATE TABLE tentativa_login (
  id CHAR(36) PRIMARY KEY,
  cpf VARCHAR(11) NOT NULL,
  sucesso BOOLEAN NOT NULL,
  ip_address VARCHAR(45),
  data_tentativa TIMESTAMP NOT NULL,
  ...
);
```

### Configuração Flyway

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
```

### Backup e Restore

#### Backup
```bash
# Via Docker
docker exec mysql_sgca mysqldump -u sgca_user -p sgca > backup_sgca_$(date +%Y%m%d).sql

# Local
mysqldump -u sgca_user -p sgca > backup_sgca_$(date +%Y%m%d).sql
```

#### Restore
```bash
# Via Docker
docker exec -i mysql_sgca mysql -u sgca_user -p sgca < backup_sgca_20251017.sql

# Local
mysql -u sgca_user -p sgca < backup_sgca_20251017.sql
```

## 🧪 Testes

### Executar Testes

```bash
# Executar todos os testes
mvn test

# Executar com relatório de cobertura
mvn test jacoco:report

# Executar testes específicos
mvn test -Dtest=JwtUtilTest
mvn test -Dtest=AuthControllerTest

# Pular testes durante build
mvn package -DskipTests

# Executar testes com perfil específico
mvn test -Dspring.profiles.active=test
```

### Cobertura de Testes

O projeto inclui testes para:

#### Segurança
- ✅ `JwtUtilTest` - Geração e validação de tokens JWT
- ✅ `UserDetailsServiceImplTest` - Carregamento de usuários

#### Controllers
- ✅ `AuthControllerTest` - Endpoints de autenticação
- ✅ `TwoFactorControllerTest` - Endpoints de 2FA
- ✅ `FileControllerTest` - Upload e download de arquivos

#### Aspectos
- ✅ `RateLimitAspectTest` - Validação de rate limiting

#### Utilitários
- ✅ `PasswordValidatorTest` - Validação de senhas

#### Exception Handling
- ✅ `GlobalExceptionHandlerTest` - Tratamento de exceções

### Teste Manual da API

Use os scripts incluídos para testar endpoints:

```bash
# Linux/Mac
chmod +x test-api.sh test-email.sh
./test-api.sh
./test-email.sh

# Windows PowerShell
.\test-api.sh
.\test-email.sh
```

### Postman/Insomnia Collection

Importe a coleção de exemplos (se disponível) ou use o Swagger UI para testes interativos:
```
http://localhost:8080/docs
```

## 🔧 Melhorias Futuras

### Segurança
- [ ] Implementar refresh tokens para renovação automática
- [ ] Adicionar autenticação via OAuth2/SSO (Google, Microsoft)
- [ ] Implementar CAPTCHA para prevenir bots
- [ ] Adicionar auditoria completa (quem alterou o quê e quando)
- [ ] Implementar criptografia de dados sensíveis no banco
- [ ] Adicionar detecção de dispositivos suspeitos

### Performance
- [ ] Adicionar cache com Redis para sessões
- [ ] Implementar paginação em todos os endpoints de listagem
- [ ] Otimizar queries com índices adicionais
- [ ] Implementar lazy loading para relacionamentos JPA
- [ ] Adicionar compressão de respostas HTTP

### Funcionalidades
- [ ] Sistema de notificações push
- [ ] Exportação de relatórios em PDF/Excel
- [ ] Dashboard com estatísticas e gráficos
- [ ] Agendamento de consultas
- [ ] Histórico médico completo
- [ ] Integração com sistemas de prontuário eletrônico

### DevOps
- [ ] CI/CD com GitHub Actions
- [ ] Análise de código com SonarQube
- [ ] Monitoramento com Prometheus + Grafana
- [ ] Logging centralizado com ELK Stack
- [ ] Testes de carga com JMeter
- [ ] Deploy em Kubernetes

### Testes
- [ ] Aumentar cobertura de testes para >80%
- [ ] Adicionar testes de integração end-to-end
- [ ] Implementar testes de performance
- [ ] Adicionar testes de segurança (OWASP)
- [ ] Testes de regressão automatizados

### Documentação
- [ ] Adicionar diagramas de arquitetura
- [ ] Documentar fluxos de processo
- [ ] Criar guia de contribuição detalhado
- [ ] Adicionar exemplos de código
- [ ] Vídeos tutoriais para setup

## 🔧 Configurações de Ambiente

### Profiles Disponíveis

- **default**: Desenvolvimento local com MySQL
- **docker**: Execução em container Docker
- **test**: Testes com H2 em memória (se configurado)

### Variáveis de Ambiente Completas

```env
# Database
SGCA_DB_PASSWORD=sua_senha_segura
MYSQL_ROOT_PASSWORD=senha_root_segura
MYSQL_DATABASE=sgca
MYSQL_USER=sgca_user

# JWT
JWT_SECRET=sua_chave_base64_muito_longa_e_segura
JWT_EXPIRATION=3600000

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu_email@gmail.com
SGCA_EMAIL_PASSWORD=senha_de_aplicativo_do_gmail

# File Upload
FILE_UPLOAD_DIR=uploads
FILE_UPLOAD_BASE_URL=http://localhost:8080/api/files
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=5MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=5MB

# Password History
PASSWORD_HISTORY_CHECK_LAST=5

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=default
```

### Como Gerar JWT_SECRET Seguro

```bash
# Linux/Mac
echo -n "sua-frase-secreta-muito-longa-e-unica-aqui" | base64

# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("sua-frase-secreta-muito-longa-e-unica-aqui"))

# Online (não recomendado para produção)
# https://www.base64encode.org/
```

## 🤝 Contribuição

Contribuições são bem-vindas! Por favor, siga estas diretrizes:

### Como Contribuir

1. **Fork** o projeto
2. Crie uma **branch** para sua feature (`git checkout -b feature/MinhaFeature`)
3. **Commit** suas mudanças (`git commit -m 'feat: Adiciona MinhaFeature'`)
4. **Push** para a branch (`git push origin feature/MinhaFeature`)
5. Abra um **Pull Request**

### Padrões de Código

#### Convenções Java
- Seguir convenções Java/Spring Boot
- Usar Lombok para reduzir boilerplate
- Documentar métodos públicos com JavaDoc
- Nomear variáveis de forma descritiva

#### Commits Semânticos
```
feat: Nova funcionalidade
fix: Correção de bug
docs: Alteração em documentação
style: Formatação, ponto e vírgula faltando, etc
refactor: Refatoração de código
test: Adição ou correção de testes
chore: Atualização de dependências, configurações, etc
```

Exemplos:
```bash
git commit -m "feat: Adiciona endpoint de relatórios"
git commit -m "fix: Corrige validação de CPF"
git commit -m "docs: Atualiza README com exemplos de API"
```

#### Testes
- Escrever testes para novas funcionalidades
- Manter cobertura de testes > 70%
- Executar testes antes de fazer commit
- Incluir testes unitários e de integração

#### Code Review
- Revisar código antes de submeter PR
- Responder a comentários do review
- Garantir que o CI passa antes de merge

### Reportar Bugs

Ao reportar bugs, inclua:
- Descrição clara do problema
- Passos para reproduzir
- Comportamento esperado vs. atual
- Screenshots (se aplicável)
- Versão do Java, Spring Boot, etc.

### Sugerir Melhorias

- Descreva a melhoria proposta
- Explique o benefício
- Forneça exemplos de uso

## 📝 Changelog

### [0.0.2-SNAPSHOT] - 2025-11-04

#### ✨ Added
- **Módulo Acompanhantes**: Sistema completo para gestão de acompanhantes dos pacientes
  - CRUD de acompanhantes com paginação e filtros
  - Relacionamento com pacientes via parentesco
  - Permissões granulares (criar, editar, visualizar, excluir)
- **Contatos de Emergência**: Cadastro de contatos para situações de emergência
  - Vinculação com pacientes
  - Informações de contato (nome, telefone, email)
- **Histórico do Paciente**: Rastreamento de alterações nos dados dos pacientes
- **Dados Adicionais**: Nome da mãe e profissão nos dados pessoais
- **Migrações de Banco**: 23 novas migrações (V06-V28) com dados iniciais e estrutura aprimorada
- **Correções de Segurança**: Ajustes em CORS e configurações administrativas

#### 🔒 Security
- ✅ Permissões granulares para acompanhantes
- ✅ Rate limiting aprimorado para 2FA
- ✅ Correções em configurações de CORS
- ✅ Validação de dados pessoais atualizada

#### 🗄️ Database
- ✅ Tabela `acompanhantes` com relacionamentos
- ✅ Tabela `contatos_emergencia` 
- ✅ Tabela `historico_paciente`
- ✅ Índices otimizados para performance
- ✅ Dados iniciais populados

### [0.0.1-SNAPSHOT] - 2025-10-17

#### ✨ Added
- Sistema completo de autenticação com JWT
- Autenticação de dois fatores (2FA) via email
- Recuperação de senha com tokens seguros
- Gerenciamento de sessões ativas
- Upload de arquivos (fotos de perfil)
- Rate limiting para proteção contra ataques
- Validação de força de senha
- Histórico de senhas para prevenir reutilização
- Bloqueio de conta após tentativas falhas
- Ativação de conta via email
- Senhas temporárias para novos usuários
- CRUD completo para pacientes
- Gestão de profissionais de saúde
- Migração de banco com Flyway
- Documentação interativa com Swagger
- Containerização com Docker
- Health checks e monitoring
- Configuração CORS
- Suporte a múltiplos perfis de usuário

#### 🔒 Security
- ✅ BCrypt password hashing (força 12)
- ✅ JWT com chave configurável
- ✅ Spring Security configurado
- ✅ Rate limiting implementado
- ✅ Validação de entrada
- ✅ Container não-root
- ✅ Proteção CSRF desabilitada (API stateless)
- ✅ Sessões stateless

#### 🐛 Known Issues
- Deprecated methods em alguns testes (getStatusCodeValue)
- Alguns imports não utilizados
- Docker base image tem 1 vulnerabilidade alta (requer atualização)

### [Planejado] - Próxima Versão

#### 🚀 Planned Features
- Refresh tokens
- OAuth2/SSO
- Cache com Redis
- Exportação de relatórios
- Dashboard administrativo
- Notificações push
- Testes de integração E2E

## 📞 Suporte

Para dúvidas, problemas ou sugestões:

### 🐛 Issues
- Reporte bugs via [GitHub Issues](https://github.com/chris-schettine/SGCA-CasaDoAmor-Backend/issues)
- Use labels apropriadas: `bug`, `enhancement`, `question`, `documentation`

### 💬 Discussões
- Participe das [GitHub Discussions](https://github.com/chris-schettine/SGCA-CasaDoAmor-Backend/discussions)

### 📧 Contato
- Email: contato@casadoamor.com.br

## 📄 Licença

Este projeto está sob licença MIT. Veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 🚀 Quick Start

```bash
# 1. Clone o repositório
git clone https://github.com/chris-schettine/SGCA-CasaDoAmor-Backend.git
cd SGCA-CasaDoAmor-Backend

# 2. Configure as variáveis de ambiente
cp .env.example .env
# Edite .env com suas credenciais

# 3. Execute com Docker (recomendado)
docker compose up -d

# 4. Verifique se está rodando
curl http://localhost:8090/actuator/health

# 5. Acesse a documentação interativa
# http://localhost:8090/docs
```

**🎉 Aplicação rodando em http://localhost:8090**

---

## 📊 Status do Projeto

![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow)
![Versão](https://img.shields.io/badge/Vers%C3%A3o-0.0.2--SNAPSHOT-blue)
![Cobertura](https://img.shields.io/badge/Cobertura%20de%20Testes-~70%25-green)
![Licença](https://img.shields.io/badge/Licen%C3%A7a-MIT-blue)

### Próximos Passos
- [ ] Implementar refresh tokens
- [ ] Adicionar testes de integração E2E
- [ ] Configurar CI/CD com GitHub Actions
- [ ] Implementar módulo de profissionais de saúde
- [ ] Adicionar dashboard administrativo
- [ ] Sistema de notificações
- [ ] Exportação de relatórios em PDF/Excel
- [ ] Histórico médico completo
- [ ] Agendamento de consultas
- [ ] Deploy em ambiente de produção
- [ ] Documentação de arquitetura detalhada

---

**Desenvolvido com ❤️ para Casa do Amor**