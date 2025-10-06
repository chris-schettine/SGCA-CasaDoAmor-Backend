# SGCA - Sistema de Gestão Casa do Amor Backend

Sistema de gerenciamento backend para a Casa do Amor, desenvolvido em Spring Boot para controle de usuários, pessoas físicas e profissionais de saúde.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Docker](https://img.shields.io/badge/Docker-Supported-blue)

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Configuração](#instalação-e-configuração)
- [Executando o Projeto](#executando-o-projeto)
- [API Documentation](#api-documentation)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Docker](#docker)
- [Banco de Dados](#banco-de-dados)
- [Testes](#testes)
- [Contribuição](#contribuição)

## 🎯 Visão Geral

O SGCA Backend é uma API REST desenvolvida para gerenciar:

- **Usuários**: Sistema de autenticação e controle de acesso
- **Pessoas Físicas**: Cadastro completo de indivíduos com endereços
- **Profissionais de Saúde**: Gestão de profissionais com documentos e especialidades
- **CID**: Classificação Internacional de Doenças

### Funcionalidades Principais

- ✅ CRUD completo para todas as entidades
- ✅ API RESTful com padrões HTTP
- ✅ Persistência em MySQL
- ✅ Containerização com Docker
- ✅ Configuração multi-ambiente
- ✅ Health checks e monitoring
- ✅ CORS configurado para integração frontend

## 🚀 Tecnologias

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.0** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Web** - API REST
- **Hibernate** - ORM
- **Maven** - Gerenciamento de dependências
- **Lombok** - Redução de boilerplate

### Banco de Dados
- **MySQL 8.0** - Banco de dados relacional
- **HikariCP** - Pool de conexões

### DevOps
- **Docker & Docker Compose** - Containerização
- **Multi-stage build** - Otimização de imagens

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

### 3. Configuração das Propriedades

Configure `src/main/resources/application.properties` conforme necessário:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/sgca
spring.datasource.username=sgca_user
spring.datasource.password=admin

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
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

### Base URL
- Local: `http://localhost:8080`
- Docker: `http://localhost:8090`

### Health Check
```http
GET /actuator/health
```

### Endpoints Principais

#### Usuários (`/api/1.0/usuarios`)
```http
POST   /api/1.0/usuarios           # Criar usuário
GET    /api/1.0/usuarios           # Listar usuários
GET    /api/1.0/usuarios/{id}      # Buscar por ID
GET    /api/1.0/usuarios/{cpf}/cpf # Buscar por CPF
PUT    /api/1.0/usuarios/{id}      # Atualizar usuário
DELETE /api/1.0/usuarios/{id}      # Deletar usuário
```

#### Pessoas Físicas (`/api/1.0/pessoa-fisica`)
```http
POST   /api/1.0/pessoa-fisica              # Criar pessoa física
GET    /api/1.0/pessoa-fisica              # Listar todas
GET    /api/1.0/pessoa-fisica/{id}         # Buscar por ID
GET    /api/1.0/pessoa-fisica/{nome}/nome  # Buscar por nome
GET    /api/1.0/pessoa-fisica/{cpf}/cpf    # Buscar por CPF
PUT    /api/1.0/pessoa-fisica/{id}         # Atualizar
PATCH  /api/1.0/pessoa-fisica/{id}         # Atualização parcial
DELETE /api/1.0/pessoa-fisica/{id}         # Deletar
```

#### Profissionais de Saúde (`/api/1.0/profissionais-saude`)
```http
POST   /api/1.0/profissionais-saude     # Criar profissional
GET    /api/1.0/profissionais-saude     # Listar todos
GET    /api/1.0/profissionais-saude/{id} # Buscar por ID
PUT    /api/1.0/profissionais-saude/{id} # Atualizar
DELETE /api/1.0/profissionais-saude/{id} # Deletar
```

#### CID (`/api/1.0/cid`)
```http
POST   /api/1.0/cid        # Criar CID
GET    /api/1.0/cid        # Listar CIDs
GET    /api/1.0/cid/{id}   # Buscar por ID
PUT    /api/1.0/cid/{id}   # Atualizar CID
DELETE /api/1.0/cid/{id}   # Deletar CID
```

### Exemplo de Requisição

#### Criar Pessoa Física
```json
POST /api/1.0/pessoa-fisica
Content-Type: application/json

{
  "nome": "João Silva",
  "cpf": "123.456.789-00",
  "telefone": "(11) 99999-9999",
  "email": "joao@email.com",
  "endereco": {
    "cep": "01234-567",
    "logradouro": "Rua das Flores",
    "numero": "123",
    "bairro": "Centro",
    "cidade": "São Paulo",
    "estado": "SP"
  }
}
```

## 📁 Estrutura do Projeto

```
src/main/java/br/com/casadoamor/sgca/
├── SgcaBackendApplication.java     # Classe principal
├── config/                         # Configurações
│   └── CorsConfig.java            # Configuração CORS
├── controller/                     # Controllers REST
│   ├── CidController.java
│   ├── PessoaFisicaController.java
│   ├── ProfissionalSaudeController.java
│   └── UsuarioController.java
├── dto/                           # Data Transfer Objects
│   ├── CidDto.java
│   ├── EnderecoDto.java
│   ├── PessoaFisicaDto.java
│   ├── ProfissionalSaudeDto.java
│   ├── UsuarioDto.java
│   ├── UsuarioRequestJson.java
│   └── ProfissionalSaudeRequestJson.java
├── entity/                        # Entidades JPA
│   ├── Cid.java
│   ├── Endereco.java
│   ├── PessoaFisica.java
│   ├── ProfissionalSaude.java
│   ├── Usuario.java
│   └── TipoDocumentoProfissionalSaudeEnum.java
├── exception/                     # Exceções customizadas
│   └── ResourceNotFoundException.java
├── mapper/                        # Mappers (Entity <-> DTO)
│   ├── CidMapper.java
│   ├── PessoaFisicaMapper.java
│   ├── ProfissionalSaudeMapper.java
│   └── UsuarioMapper.java
├── repository/                    # Repositórios JPA
│   ├── CidRepository.java
│   ├── PessoaFisicaRepository.java
│   ├── ProfissionalSaudeRepository.java
│   └── UsuarioRepository.java
└── service/                       # Serviços de negócio
    ├── CidService.java / CidServiceImpl.java
    ├── PessoaFisicaService.java / PessoaFisicaServiceImpl.java
    ├── ProfissionalSaudeService.java / ProfissionalSaudeServiceImpl.java
    └── UsuarioService.java / UsuarioServiceImpl.java
```

## 🐳 Docker

### Arquivo docker-compose.yml

O projeto inclui configuração completa com:
- **MySQL 8.0** na porta 3307
- **Backend Spring Boot** na porta 8090
- **Rede dedicada** (sgca-network)
- **Volumes persistentes** para dados
- **Health checks** configurados

### Comandos Docker Úteis

```bash
# Parar todos os serviços
docker compose down

# Rebuild completo
docker compose up --build --force-recreate

# Ver logs específicos
docker compose logs mysql
docker compose logs sgca-backend

# Acessar container MySQL
docker exec -it mysql_sgca mysql -u sgca_user -padmin sgca

# Limpar volumes (CUIDADO: remove dados!)
docker compose down -v
```

## 🗄️ Banco de Dados

### Schema Principal

#### Tabela: usuario
```sql
CREATE TABLE usuario (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ativo BIT(1) NOT NULL,
  cpf VARCHAR(255) NOT NULL UNIQUE,
  senha VARCHAR(255) NOT NULL,
  pessoa_fisica_id BIGINT UNIQUE,
  FOREIGN KEY (pessoa_fisica_id) REFERENCES pessoa_fisica(id)
);
```

#### Tabela: pessoa_fisica
```sql
CREATE TABLE pessoa_fisica (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  cpf VARCHAR(255) UNIQUE,
  data_nascimento DATE,
  rg VARCHAR(255),
  naturalidade VARCHAR(255),
  profissao VARCHAR(255),
  telefone VARCHAR(255),
  email VARCHAR(255),
  -- campos de endereco embutidos
  cep VARCHAR(255),
  endereco VARCHAR(255),
  numero INT,
  bairro VARCHAR(255),
  cidade VARCHAR(255),
  estado VARCHAR(255),
  complemento VARCHAR(255)
);
```

### Inicialização

O banco é inicializado automaticamente pelo Hibernate com `ddl-auto=update`.
Scripts de inicialização estão em `mysql-init/01-init.sql`.

## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Executar testes com relatório de cobertura
mvn test jacoco:report

# Executar apenas testes de integração
mvn test -Dtest="*IntegrationTest"

# Pular testes durante build
mvn package -DskipTests
```

### Teste Manual da API

Use o script incluído para testar endpoints:
```bash
chmod +x test-api.sh
./test-api.sh
```

## 🔧 Configurações de Ambiente

### Profiles Disponíveis

- **default**: Desenvolvimento local
- **docker**: Execução em container
- **test**: Testes com H2 em memória

### Variáveis de Ambiente (Docker)

```env
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/sgca
SPRING_DATASOURCE_USERNAME=sgca_user
SPRING_DATASOURCE_PASSWORD=admin
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8080
```

## 🔒 Segurança

⚠️ **Nota Importante**: Este projeto atualmente armazena senhas em texto plano. Para produção, implemente:

1. **Hashing de senhas** com BCrypt
2. **Autenticação JWT**
3. **Spring Security**
4. **Validação de entrada**
5. **Rate limiting**

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Padrões de Código

- Seguir convenções Java/Spring Boot
- Usar Lombok para reduzir boilerplate
- Documentar métodos públicos
- Escrever testes para novas funcionalidades
- Manter cobertura de testes > 80%

## 📝 Changelog

### [0.0.1-SNAPSHOT] - 2025-09-29

#### Added
- Estrutura inicial do projeto Spring Boot
- CRUD completo para Usuários, Pessoas Físicas, Profissionais de Saúde e CID
- Configuração Docker com MySQL
- API REST com versionamento
- Health checks e monitoring básico
- Configuração CORS para frontend
- Suporte a múltiplos ambientes

#### Security Notes
- Senhas armazenadas em texto plano (necessária implementação de hash)
- Sem autenticação/autorização (necessário Spring Security)

## 📞 Suporte

Para dúvidas e suporte:
- Abra uma issue no GitHub
- Email: [contato@casadoamor.com.br]

## 📄 Licença

Este projeto está sob licença MIT. Veja o arquivo `LICENSE` para detalhes.

---

## 🚀 Quick Start

```bash
# Clone e execute rapidamente
git clone https://github.com/chris-schettine/SGCA-CasaDoAmor-Backend.git
cd SGCA-CasaDoAmor-Backend
docker compose up --build -d

# Teste a aplicação
curl http://localhost:8090/actuator/health
curl http://localhost:8090/api/1.0/pessoa-fisica
```

**🎉 Aplicação rodando em http://localhost:8090**