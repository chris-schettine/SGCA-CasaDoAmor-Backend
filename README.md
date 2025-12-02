# SGCA - Sistema de Gestão Casa do Amor Backend

Sistema de gerenciamento backend para a Casa do Amor, desenvolvido em Spring Boot para controle de usuários, pacientes e profissionais de saúde com recursos avançados de segurança.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Docker](https://img.shields.io/badge/Docker-Supported-blue)
![Security](https://img.shields.io/badge/Security-JWT%20%2B%202FA-red)

<!-- CONTRIBUTORS_STATS_START -->
### Contribuidores (commits)

_Geração automática: este bloco é atualizado por Actions com a contagem de commits por contribuidor._

> Executando atualização automática do gráfico de commits...

<!-- CONTRIBUTORS_STATS_END -->

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
- **Profissionais de Saúde**: Gestão completa de profissionais com categorias e tipos de vínculo
- **Tipos de Serviço**: Cadastro de serviços oferecidos pelos profissionais
- **Agendamentos**: Sistema completo de gestão de consultas e procedimentos para pacientes e acompanhantes
- **Dashboard Estatístico**: 40+ métricas em tempo real sobre agendamentos e performance da unidade
- **Consentimentos LGPD**: Sistema unificado de gerenciamento de termos e consentimentos para todas as entidades (usuários, profissionais, pacientes, visitantes, fornecedores)
- **Hospedagens**: Sistema de controle de estadias dos pacientes com histórico completo
- **Quartos/Leitos**: Gerenciamento de acomodações com controle de ocupação e separação por ala
- **Upload de Arquivos**: Sistema de gerenciamento de fotos de perfil para usuários e profissionais
- **Auditoria**: Rastreamento de sessões e tentativas de login

### Funcionalidades Principais

- ✅ **Autenticação JWT** com refresh tokens
- ✅ **2FA (Two-Factor Authentication)** via email
- ✅ **Recuperação de senha** com tokens seguros
- ✅ **Gerenciamento de sessões** ativas
- ✅ **Sistema de Agendamentos** completo (pacientes e acompanhantes)
- ✅ **Dashboard com 40+ métricas** em tempo real
- ✅ **Estatísticas de Performance** (taxa de comparecimento, cancelamentos, etc.)
- ✅ **Top 10 Rankings** (profissionais e serviços mais solicitados)
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

#### Profissionais (ADMIN=total, RECEPCIONISTA+AUDITOR=leitura)
```http
POST   /api/profissionais                    # Criar profissional (ADMIN)
PUT    /api/profissionais/{uuid}             # Atualizar profissional (ADMIN)
GET    /api/profissionais/{uuid}             # Buscar por UUID (inclui foto)
GET    /api/profissionais                    # Listar profissionais (inclui foto)
GET    /api/profissionais/categoria/{cat}    # Filtrar por categoria
GET    /api/profissionais/tipos-vinculo      # Listar tipos de vínculo (dropdown)
GET    /api/profissionais/categorias         # Listar categorias profissionais (dropdown)
PATCH  /api/profissionais/{uuid}/inativar    # Inativar profissional (ADMIN)
DELETE /api/profissionais/{uuid}             # Deletar profissional (ADMIN)
```

#### Tipos de Serviço (ADMIN=total, RECEPCIONISTA+AUDITOR=leitura)
```http
GET    /api/tipos-servico                    # Listar tipos de serviço
GET    /api/tipos-servico/{id}               # Buscar por ID
```

#### Consentimentos LGPD (Sistema Unificado - autenticação requerida)
```http
# Endpoints para Usuários
POST   /api/usuarios/{cpf}/consentimentos-lgpd        # Registrar consentimento
GET    /api/usuarios/{cpf}/consentimentos-lgpd        # Listar histórico
GET    /api/usuarios/{cpf}/consentimentos-lgpd/valido # Verificar se tem consentimento válido
GET    /api/usuarios/{cpf}/consentimentos-lgpd/atual  # Obter consentimento atual

# Endpoints para Profissionais
POST   /api/profissionais/{uuid}/consentimentos       # Registrar consentimento
GET    /api/profissionais/{uuid}/consentimentos       # Listar histórico
GET    /api/profissionais/{uuid}/consentimentos/valido # Verificar se tem consentimento válido
GET    /api/profissionais/{uuid}/consentimentos/atual  # Obter consentimento atual

# Endpoints Administrativos (ADMIN ou AUDITOR)
GET    /api/consentimentos-lgpd/tipo/{tipo}          # Listar por tipo de entidade
GET    /api/consentimentos-lgpd/versao/{versao}      # Listar por versão do termo
GET    /api/consentimentos-lgpd/estatisticas         # Estatísticas de consentimentos
```

#### Quartos/Leitos (ADMIN=total, RECEPCIONISTA+AUDITOR=leitura)
```http
POST   /api/quartos                          # Cadastrar quarto (ADMIN)
PUT    /api/quartos/{uuid}                   # Atualizar quarto (ADMIN)
GET    /api/quartos/{uuid}                   # Buscar por UUID
GET    /api/quartos                          # Listar todos
GET    /api/quartos/ativos                   # Listar ativos
GET    /api/quartos/disponiveis              # Quartos com vagas
GET    /api/quartos/disponiveis/ala/{ala}    # Vagas por ala
GET    /api/quartos/ala/{ala}                # Filtrar por ala (FEMININA/MASCULINA/MISTA)
GET    /api/quartos/estatisticas             # Estatísticas de ocupação
GET    /api/quartos/paginated                # Lista paginada
PATCH  /api/quartos/{uuid}/inativar          # Inativar quarto (ADMIN)
DELETE /api/quartos/{uuid}                   # Deletar quarto (ADMIN)
```

#### Hospedagens (ADMIN=total, RECEPCIONISTA+AUDITOR=leitura)
```http
POST   /api/hospedagens                      # Registrar entrada (ADMIN)
PUT    /api/hospedagens/{uuid}/saida         # Registrar saída (ADMIN)
PUT    /api/hospedagens/{uuid}/transferir    # Transferir quarto (ADMIN)
GET    /api/hospedagens/{uuid}               # Buscar por UUID
GET    /api/hospedagens/ativas               # Listar ativas
GET    /api/hospedagens/paciente/{id}        # Histórico do paciente
GET    /api/hospedagens/quarto/{uuid}        # Histórico do quarto
GET    /api/hospedagens/periodo              # Filtrar por período
GET    /api/hospedagens/previsao-vencida     # Saídas atrasadas
GET    /api/hospedagens/paciente/{id}/ativa  # Verificar se tem hospedagem ativa
GET    /api/hospedagens/paginated            # Lista paginada
DELETE /api/hospedagens/{uuid}               # Deletar hospedagem (ADMIN)
```

#### Agendamentos de Pacientes (Requer autenticação)
```http
# Listar e Criar
GET    /api/agendamentos/pacientes                      # Listar todos (paginação)
POST   /api/agendamentos/pacientes                      # Criar agendamento

# Buscar e Filtrar
GET    /api/agendamentos/pacientes/{uuid}               # Buscar por UUID
GET    /api/agendamentos/pacientes/paciente/{id}        # Por paciente
GET    /api/agendamentos/pacientes/profissional/{id}    # Por profissional (com período)

# Elegibilidade e Disponibilidade
GET    /api/agendamentos/pacientes/pacientes-elegiveis  # Pacientes com hospedagem ativa
GET    /api/agendamentos/pacientes/profissionais-elegiveis # Profissionais disponíveis

# Gestão
POST   /api/agendamentos/pacientes/verificar-conflito   # Verificar conflito de horário
PUT    /api/agendamentos/pacientes/{uuid}/confirmar     # Confirmar agendamento
DELETE /api/agendamentos/pacientes/{uuid}               # Cancelar agendamento
```

#### Agendamentos de Acompanhantes (Requer autenticação)
```http
# Listar e Criar
GET    /api/agendamentos/acompanhantes                         # Listar todos (paginação)
POST   /api/agendamentos/acompanhantes                         # Criar agendamento

# Buscar e Filtrar
GET    /api/agendamentos/acompanhantes/{uuid}                  # Buscar por UUID
GET    /api/agendamentos/acompanhantes/acompanhante/{id}       # Por acompanhante
GET    /api/agendamentos/acompanhantes/profissional/{id}       # Por profissional (com período)

# Elegibilidade e Disponibilidade
GET    /api/agendamentos/acompanhantes/acompanhantes-elegiveis # Acompanhantes elegíveis
GET    /api/agendamentos/acompanhantes/profissionais-elegiveis # Profissionais disponíveis

# Gestão
POST   /api/agendamentos/acompanhantes/verificar-conflito      # Verificar conflito de horário
PUT    /api/agendamentos/acompanhantes/{uuid}/confirmar        # Confirmar agendamento
DELETE /api/agendamentos/acompanhantes/{uuid}                  # Cancelar agendamento
```

#### Estatísticas de Agendamentos (Dashboard)
```http
# Métricas Gerais
GET    /api/agendamentos/estatisticas/resumo                   # Resumo geral (total, pendentes, concluídos)
GET    /api/agendamentos/estatisticas/hoje                     # Agendamentos do dia
GET    /api/agendamentos/estatisticas/semana                   # Agendamentos da semana

# Performance
GET    /api/agendamentos/estatisticas/taxa-comparecimento      # Taxa de comparecimento
GET    /api/agendamentos/estatisticas/taxa-cancelamento        # Taxa de cancelamento
GET    /api/agendamentos/estatisticas/tempo-medio-espera       # Tempo médio de espera

# Rankings Top 10
GET    /api/agendamentos/estatisticas/profissionais-mais-agendados  # Top 10 profissionais
GET    /api/agendamentos/estatisticas/servicos-mais-solicitados     # Top 10 serviços

# Filtros por Período
GET    /api/agendamentos/estatisticas/periodo?inicio={data}&fim={data}  # Estatísticas por período
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

### Exemplo de Requisição: Criar Agendamento

```bash
# 1. Listar pacientes elegíveis (com hospedagem ativa)
curl -X GET http://localhost:8080/api/agendamentos/pacientes/pacientes-elegiveis \
  -H "Authorization: Bearer {seu_token}"

# 2. Listar profissionais disponíveis
curl -X GET http://localhost:8080/api/agendamentos/pacientes/profissionais-elegiveis \
  -H "Authorization: Bearer {seu_token}"

# 3. Criar agendamento
curl -X POST http://localhost:8080/api/agendamentos/pacientes \
  -H "Authorization: Bearer {seu_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": "9e7b315a-89d4-4f29-b28d-78492e5ad4d3",
    "profissionalId": 2,
    "tipoServicoId": 1,
    "dataHoraInicio": "2025-12-10T09:00:00",
    "dataHoraFim": "2025-12-10T10:00:00",
    "duracaoMinutos": 60,
    "observacoes": "Consulta de rotina",
    "hospedagemId": "uuid-da-hospedagem-ativa"
  }'

# 4. Listar agendamentos (com paginação)
curl -X GET "http://localhost:8080/api/agendamentos/pacientes?page=0&size=20" \
  -H "Authorization: Bearer {seu_token}"
```


## � Sistema LGPD Unificado

### Visão Geral

O sistema implementa um **design polimórfico** para gerenciamento de consentimentos LGPD, permitindo que múltiplas entidades (usuários, profissionais, pacientes, visitantes, fornecedores) compartilhem a mesma infraestrutura de consentimentos.

### Arquitetura

#### Tabela Unificada
- **Uma única tabela** `consentimentos_lgpd` com discriminador `tipo_entidade`
- Suporta 5 tipos de entidade via enum: `USUARIO`, `PROFISSIONAL`, `PACIENTE`, `VISITANTE`, `FORNECEDOR`
- Relacionamento genérico: `tipo_entidade` + `entidade_id` identifica univocamente a entidade

#### Captura Automática de Contexto
- **IP do cliente**: Captura com suporte a proxies (X-Forwarded-For, Proxy-Client-IP, etc.)
- **User-Agent**: Navegador/dispositivo utilizado
- **Metadata JSON**: Informações adicionais em formato flexível
- **Auditoria**: Rastreamento de quem criou e quem registrou o consentimento

### Como Usar

#### Registrar Consentimento para Usuário
```bash
POST /api/usuarios/{cpf}/consentimentos-lgpd
Authorization: Bearer {token}
Content-Type: application/json

{
  "versaoTermo": "1.0",
  "escopo": "dados_pessoais,dados_clinicos",
  "concorda": true,
  "metadata": "{\"origem\": \"portal_web\"}"
}
```

#### Registrar Consentimento para Profissional
```bash
POST /api/profissionais/{uuid}/consentimentos
Authorization: Bearer {token}
Content-Type: application/json

{
  "versaoTermo": "1.0",
  "escopo": "uso_imagem,divulgacao",
  "concorda": true
}
```

#### Verificar Consentimento Válido
```bash
GET /api/usuarios/{cpf}/consentimentos-lgpd/valido
Authorization: Bearer {token}

# Resposta:
{
  "valido": true
}
```

#### Obter Consentimento Atual
```bash
GET /api/profissionais/{uuid}/consentimentos/atual
Authorization: Bearer {token}

# Resposta:
{
  "id": 1,
  "uuid": "abc123...",
  "tipoEntidade": "PROFISSIONAL",
  "versaoTermo": "1.0",
  "concorda": true,
  "dataConsentimento": "2025-11-16T10:30:00",
  "ipOrigem": "192.168.1.100",
  ...
}
```

### Queries Disponíveis

O `ConsentimentoLGPDRepository` fornece 12+ queries customizadas:

- `findByTipoEntidadeAndEntidadeId()` - Buscar por tipo e ID
- `hasConsentimentoValido()` - Verificar se tem consentimento válido
- `findConsentimentosDesatualizados()` - Consentimentos com versão antiga
- `findByVersaoTermo()` - Buscar por versão específica
- `countByTipoEntidade()` - Contar por tipo de entidade
- E mais...

### Adicionando Novo Tipo de Entidade

Para adicionar suporte a uma nova entidade (ex: `FORNECEDOR`):

1. **Adicionar no enum** `TipoEntidadeLGPD.java`:
```java
public enum TipoEntidadeLGPD {
    USUARIO, PROFISSIONAL, PACIENTE, VISITANTE, FORNECEDOR  // Adicione aqui
}
```

2. **Criar endpoints no controller**:
```java
@PostMapping("/api/fornecedores/{id}/consentimentos")
public ResponseEntity<?> registrarConsentimentoFornecedor(
    @PathVariable Long id,
    @RequestBody ConsentimentoLGPDRequestDTO dto
) {
    consentimentoService.registrarConsentimento(
        TipoEntidadeLGPD.FORNECEDOR, id, dto, getUsuarioAutenticado()
    );
    return ResponseEntity.ok().build();
}
```

3. **Usar os mesmos serviços e repositories** - sem necessidade de criar novos!

### Migração de Dados

A migração V53 consolidou dados de tabelas antigas:
```sql
-- Migrou dados de consentimentos_lgpd_profissionais (tipo='PROFISSIONAL')
-- Migrou dados de consentimentos_lgpd_usuarios (tipo='USUARIO')
-- Preservou tabelas antigas com prefixo _old_ para auditoria
```

## �📁 Estrutura do Projeto

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
├── V05__add_foto_columns.sql           # Colunas para fotos (auth_usuarios)
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
├── V28__create_contato_emergencia.sql  # Contatos de emergência
├── ...                                 # Migrações V29-V52
├── V53__unify_consentimentos_lgpd.sql  # Unificação LGPD (tabela polimórfica)
├── V54__add_foto_columns_profissionais.sql # Campos foto em profissionais
├── V55__create_tipos_vinculo.sql       # Tabela tipos_vinculo
└── V56__alter_profissionais_tipo_vinculo.sql # Refatoração profissionais (FK tipos_vinculo)
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

#### profissionais
Informações de profissionais de saúde (funcionários e voluntários)
```sql
CREATE TABLE profissionais (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  uuid VARCHAR(36) UNIQUE NOT NULL,
  nome VARCHAR(255) NOT NULL,
  cpf VARCHAR(11) UNIQUE NOT NULL,           -- CPF não criptografado
  cpf_criptografado VARBINARY(512),          -- CPF criptografado
  categoria VARCHAR(50) NOT NULL,            -- MEDICO, ENFERMAGEM, etc.
  tipo_vinculo_id BIGINT,                    -- FK para tipos_vinculo
  tipo_vinculo_backup VARCHAR(255),          -- Backup do tipo_vinculo enum antigo
  foto_url VARCHAR(500),                     -- URL completa da foto
  foto_path VARCHAR(255),                    -- Caminho relativo no storage
  foto_atualizada_em TIMESTAMP,              -- Data/hora última atualização da foto
  ativo BOOLEAN DEFAULT TRUE,
  ...,
  CONSTRAINT fk_profissionais_tipo_vinculo FOREIGN KEY (tipo_vinculo_id) REFERENCES tipos_vinculo(id)
);
```

#### tipos_vinculo
Tipos de vínculo profissional (tabela de domínio)
```sql
CREATE TABLE tipos_vinculo (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(10) UNIQUE NOT NULL,        -- PADRAO, CLT, PJ, AUT, VOL
  nome VARCHAR(100) NOT NULL,                -- Padrão, CLT, Pessoa Jurídica, etc.
  ativo BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);
-- Registros: (1,PADRAO,Padrão), (2,CLT,CLT), (3,PJ,Pessoa Jurídica), (4,AUT,Autônomo), (5,VOL,Voluntário)
```

#### consentimentos_lgpd
Sistema unificado de consentimentos LGPD (todas as entidades)
```sql
CREATE TABLE consentimentos_lgpd (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  uuid VARCHAR(36) UNIQUE NOT NULL,
  tipo_entidade VARCHAR(50) NOT NULL,  -- USUARIO, PROFISSIONAL, PACIENTE, etc.
  entidade_id BIGINT NOT NULL,         -- ID da entidade na tabela correspondente
  versao_termo VARCHAR(50) NOT NULL,
  escopo VARCHAR(5000),
  concorda BOOLEAN NOT NULL,
  data_consentimento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  ip_origem VARCHAR(45),               -- IPv4 ou IPv6
  user_agent VARCHAR(500),
  metadata JSON,                        -- Dados adicionais em JSON
  registrado_por_id BIGINT,            -- Quem registrou o consentimento
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by_id BIGINT,
  INDEX idx_tipo_entidade_id (tipo_entidade, entidade_id),
  INDEX idx_versao (versao_termo)
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

### [0.0.4-SNAPSHOT] - 2025-11-16

#### ✨ Added
- **Sistema LGPD Unificado**: Refatoração completa do módulo de consentimentos
  - Tabela unificada `consentimentos_lgpd` com discriminador `tipo_entidade`
  - Suporte para múltiplos tipos: USUARIO, PROFISSIONAL, PACIENTE, VISITANTE, FORNECEDOR
  - API genérica para registrar/consultar consentimentos de qualquer entidade
  - Captura automática de IP e User-Agent (com suporte a proxies)
  - Endpoints específicos para usuários e profissionais
  - Endpoints administrativos para estatísticas e consultas por tipo/versão
  - Migração V53: Consolidação de dados de tabelas antigas
- **Campos de Foto em Profissionais**: Paridade com auth_usuarios
  - Adicionados `foto_url`, `foto_path`, `foto_atualizada_em` na tabela profissionais
  - DTOs atualizados: ProfissionalResponseDTO e ProfissionalResumoDTO
  - Índice otimizado: `idx_profissionais_foto` em foto_path
  - Migração V54: ALTER TABLE com novos campos
- **Tabela Tipos de Vínculo**: Sistema de tipos de vínculo empregatício
  - Tabela `tipos_vinculo` com 5 tipos: PADRAO, CLT, PJ, AUT, VOL
  - Endpoint dropdown: GET /api/profissionais/tipos-vinculo
  - Migração V55: Criação e população da tabela
- **Refatoração Profissionais**: Integração com tipos_vinculo
  - Campo `tipo_vinculo_id` como FK para tipos_vinculo (substitui enum)
  - Campo `cpf` VARCHAR(11) adicionado (além do cpf_criptografado)
  - Campo `tipo_vinculo_backup` para preservar dados antigos
  - DTOs atualizados: tipoVinculoId no request, tipoVinculo objeto no response
  - Migração V56: Refatoração da estrutura de profissionais

#### 🗄️ Database
- ✅ Migração V53: Unificação de consentimentos LGPD
  - Criação da tabela `consentimentos_lgpd` com tipo_entidade
  - Migração de dados de `consentimentos_lgpd_profissionais` e `consentimentos_lgpd_usuarios`
  - Preservação de tabelas antigas com prefixo `_old_`
- ✅ Migração V54: Adição de campos foto em profissionais
  - foto_url VARCHAR(500) - URL completa para acessar a foto
  - foto_path VARCHAR(255) - Caminho relativo no storage
  - foto_atualizada_em TIMESTAMP - Data/hora da última atualização
  - Índice idx_profissionais_foto para performance

#### 🔧 Refactoring
- ✅ Sistema LGPD: De múltiplas tabelas para design polimórfico
  - ConsentimentoLGPD: Entity unificada com enum TipoEntidadeLGPD
  - ConsentimentoLGPDService: Métodos genéricos aceitando tipo + entidadeId
  - ConsentimentoLGPDController: Endpoints para usuarios, profissionais e admin
  - 12+ queries customizadas no repository para consultas específicas
- ✅ Correção de autenticação em ConsentimentoLGPDController
  - Helper method `getUsuarioAutenticado()` para evitar cast errors
  - Ajuste de @PreAuthorize: `authentication.principal.cpf` → `authentication.principal.username`

#### 📚 Documentation
- ✅ README atualizado com informações sobre LGPD unificado
- ✅ Documentação de campos de foto em profissionais
- ✅ Exemplos de uso dos novos endpoints LGPD

### [0.0.3-SNAPSHOT] - 2025-11-10

#### ✨ Added
- **Módulo Profissionais**: Sistema completo para gestão de profissionais de saúde
  - CRUD de profissionais com categorias (MEDICO, ENFERMAGEM, ODONTOLOGIA, etc.)
  - Tipos de vínculo (FUNCIONARIO, VOLUNTARIO)
  - Validação de documentos profissionais (CRM, COREN, CRO, etc.)
  - Gestão de status ativo/inativo
- **Módulo Tipos de Serviço**: Cadastro de serviços oferecidos
  - Listagem de tipos de serviço disponíveis
- **Módulo Hospedagens**: Sistema completo de controle de estadias
  - Registro de entrada e saída de pacientes
  - Transferência entre quartos
  - Histórico completo de hospedagens por paciente
  - Status de hospedagem (ATIVA, ENCERRADA, TRANSFERENCIA)
  - Validação de hospedagens ativas (um paciente por vez)
  - Alertas para previsões de saída vencidas
- **Módulo Quartos/Leitos**: Gerenciamento de acomodações
  - CRUD de quartos com controle de capacidade
  - Separação por ala (FEMININA, MASCULINA, MISTA)
  - Tipos de quarto (INDIVIDUAL, COMPARTILHADO)
  - Controle automático de ocupação (incremento/decremento)
  - Estatísticas de ocupação (geral e por ala)
  - Status de manutenção
  - Validação de capacidade (impede redução abaixo da ocupação atual)
- **Migrações de Banco**: 18 novas migrações (V29-V46)
  - V43: Adição de tipo_vinculo em profissionais
  - V44: Correção de enum CategoriaProfissional
  - V45: Tabela de quartos com FKs corretas
  - V46: Tabela de hospedagens com relacionamentos

#### 🔒 Security
- ✅ Controle de acesso baseado em roles para todos os módulos
  - ADMINISTRADOR: Acesso total (criar, editar, deletar)
  - RECEPCIONISTA e AUDITOR: Apenas leitura (GET endpoints)
- ✅ Validações de entrada em todos os DTOs
- ✅ Soft delete implementado em todas as entidades

#### 🗄️ Database
- ✅ Tabela `profissionais` com categoria e tipo_vinculo
- ✅ Tabela `tipos_servico` para serviços oferecidos
- ✅ Tabela `consentimentos_lgpd` para termos de privacidade
- ✅ Tabela `quartos` com controle de ocupação e alas
- ✅ Tabela `hospedagens` com histórico completo
- ✅ 25+ queries customizadas no QuartoRepository
- ✅ 20+ queries customizadas no HospedagemRepository
- ✅ Índices otimizados para performance
- ✅ Foreign Keys corrigidas (auth_usuarios)

#### 🔧 Configuration
- ✅ `spring.jpa.hibernate.ddl-auto` alterado de `update` para `none`
  - Flyway agora é o gerenciador exclusivo do schema
  - Resolve conflitos entre Hibernate DDL e Flyway

#### 📚 Documentation
- ✅ Swagger/OpenAPI completo para todos os novos endpoints
- ✅ Documentação de negócio em JavaDoc
- ✅ README atualizado com exemplos de uso

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
![Versão](https://img.shields.io/badge/Vers%C3%A3o-0.0.4--SNAPSHOT-blue)
![Cobertura](https://img.shields.io/badge/Cobertura%20de%20Testes-~70%25-green)
![Licença](https://img.shields.io/badge/Licen%C3%A7a-MIT-blue)

### Próximos Passos
- [x] Sistema LGPD unificado com design polimórfico ✅
- [x] Campos de foto em profissionais ✅
- [ ] Implementar upload de fotos para profissionais
- [ ] Remover TestAuthController (risco de segurança)
- [ ] Implementar refresh tokens
- [ ] Adicionar testes de integração E2E
- [ ] Configurar CI/CD com GitHub Actions
- [ ] Adicionar dashboard administrativo
- [ ] Sistema de notificações
- [ ] Exportação de relatórios em PDF/Excel
- [ ] Histórico médico completo
- [ ] Agendamento de consultas
- [ ] Deploy em ambiente de produção
- [ ] Documentação de arquitetura detalhada

---

**Desenvolvido com ❤️ para Casa do Amor**