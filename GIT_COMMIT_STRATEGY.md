# 📋 Estratégia de Commits para Branch 'dev'

## 📊 Análise da Situação Atual

### Alterações Detectadas:
- **Arquivos Modificados**: 16 arquivos
- **Arquivos Deletados**: 18 arquivos (reorganização de pacotes)
- **Arquivos Novos (Untracked)**: 47+ arquivos (nova estrutura de pacotes)

### Contexto:
Você realizou uma **reorganização completa da estrutura de pacotes** seguindo as boas práticas de Domain-Driven Design (DDD), agrupando classes por domínio/funcionalidade.

---

## ✅ Estratégia Recomendada: Commits Temáticos

### Por que commits separados?
1. ✅ **Facilita Code Review**: Revisores podem analisar mudanças por contexto
2. ✅ **Facilita Rollback**: Se algo quebrar, você pode reverter apenas um commit específico
3. ✅ **Histórico Claro**: Git log conta uma história compreensível
4. ✅ **Rastreabilidade**: Cada feature/refactor tem seu próprio commit
5. ✅ **CI/CD**: Evita builds quebrados por mudanças não relacionadas

---

## 📦 Plano de Commits (10 commits organizados)

### **Commit 1: Reorganização de Estrutura de Pacotes - Entities**
```bash
# Adiciona novas entities organizadas por domínio
git add src/main/java/br/com/casadoamor/sgca/entity/admin/
git add src/main/java/br/com/casadoamor/sgca/entity/auth/
git add src/main/java/br/com/casadoamor/sgca/entity/common/
git add src/main/java/br/com/casadoamor/sgca/entity/paciente/
git add src/main/java/br/com/casadoamor/sgca/enums/TipoToken.java

git commit -m "refactor: Reorganiza entities por domínio (admin, auth, paciente, common)

- Cria subpacotes: entity/admin, entity/auth, entity/paciente, entity/common
- Move AuthUsuario, Perfil, Permissao para entity/auth
- Move Paciente, DadoPessoal, Endereco para entity/paciente
- Adiciona BaseEntity em entity/common
- Adiciona enum TipoToken

Relacionado: #<issue_number> - Reorganização de pacotes DDD"
```

---

### **Commit 2: Reorganização de Estrutura de Pacotes - Repositories**
```bash
# Adiciona novos repositories organizados por domínio
git add src/main/java/br/com/casadoamor/sgca/repository/admin/
git add src/main/java/br/com/casadoamor/sgca/repository/auth/
git add src/main/java/br/com/casadoamor/sgca/repository/paciente/

git commit -m "refactor: Reorganiza repositories por domínio (admin, auth, paciente)

- Cria subpacotes: repository/admin, repository/auth, repository/paciente
- Move PerfilRepository, PermissaoRepository para repository/admin
- Move AuthUsuarioRepository, HistoricoSenhaRepository, etc. para repository/auth
- Move PacienteRepository para repository/paciente

Relacionado: #<issue_number> - Reorganização de pacotes DDD"
```

---

### **Commit 3: Reorganização de Estrutura de Pacotes - Services**
```bash
# Adiciona novos services organizados por domínio
git add src/main/java/br/com/casadoamor/sgca/service/admin/
git add src/main/java/br/com/casadoamor/sgca/service/auth/
git add src/main/java/br/com/casadoamor/sgca/service/file/
git add src/main/java/br/com/casadoamor/sgca/service/common/
git add src/main/java/br/com/casadoamor/sgca/service/paciente/
git add src/main/java/br/com/casadoamor/sgca/service/imp/EmailServiceImp.java
git add src/main/java/br/com/casadoamor/sgca/service/imp/LocalFileStorageService.java
git add src/main/java/br/com/casadoamor/sgca/service/imp/UserPhotoService.java

git commit -m "refactor: Reorganiza services por domínio (admin, auth, file, paciente)

- Cria subpacotes: service/admin, service/auth, service/file, service/paciente, service/common
- Move UserManagementService, PerfilService para service/admin
- Move AuthService, TwoFactorService, HistoricoSenhaService para service/auth
- Move FileStorageService para service/file
- Move EmailService para service/common
- Move PacienteService para service/paciente

Relacionado: #<issue_number> - Reorganização de pacotes DDD"
```

---

### **Commit 4: Reorganização de Estrutura de Pacotes - DTOs**
```bash
# Adiciona novos DTOs organizados por domínio
git add src/main/java/br/com/casadoamor/sgca/dto/admin/
git add src/main/java/br/com/casadoamor/sgca/dto/auth/
git add src/main/java/br/com/casadoamor/sgca/dto/common/
git add src/main/java/br/com/casadoamor/sgca/dto/paciente/
git add src/main/java/br/com/casadoamor/sgca/dto/twofactor/
git add src/main/java/br/com/casadoamor/sgca/dto/SessaoDTO.java

git commit -m "refactor: Reorganiza DTOs por domínio e tipo (request/response)

- Cria subpacotes: dto/admin, dto/auth, dto/common, dto/paciente, dto/twofactor
- Move CreateUserDTO, UpdateUserDTO para dto/admin/user
- Move LoginRequestDTO, RegisterRequestDTO para dto/auth/request
- Move AuthResponseDTO para dto/auth/response
- Move ApiResponseDTO, MessageResponseDTO para dto/common
- Move PacienteDTO para dto/paciente
- Move Setup2FADTO para dto/twofactor

Relacionado: #<issue_number> - Reorganização de pacotes DDD"
```

---

### **Commit 5: Reorganização de Estrutura de Pacotes - Controllers**
```bash
# Adiciona novos controllers organizados por domínio
git add src/main/java/br/com/casadoamor/sgca/controller/admin/
git add src/main/java/br/com/casadoamor/sgca/controller/auth/
git add src/main/java/br/com/casadoamor/sgca/controller/file/
git add src/main/java/br/com/casadoamor/sgca/controller/paciente/

git commit -m "refactor: Reorganiza controllers por domínio (admin, auth, file, paciente)

- Cria subpacotes: controller/admin, controller/auth, controller/file, controller/paciente
- Move AdminController, AuditController para controller/admin
- Move AuthController, TwoFactorController para controller/auth
- Move FileController para controller/file
- Move PacienteController para controller/paciente

Relacionado: #<issue_number> - Reorganização de pacotes DDD"
```

---

### **Commit 6: Reorganização de Estrutura de Pacotes - Mappers e Security**
```bash
# Adiciona mappers e security
git add src/main/java/br/com/casadoamor/sgca/mapper/common/
git add src/main/java/br/com/casadoamor/sgca/mapper/paciente/
git add src/main/java/br/com/casadoamor/sgca/security/

git commit -m "refactor: Reorganiza mappers e adiciona classes de segurança

- Cria subpacotes: mapper/common, mapper/paciente
- Move PaginatedResponseMapper para mapper/common
- Move PacienteMapper, DadoPessoalMapper para mapper/paciente
- Adiciona classes de segurança: JwtUtil, JwtAuthenticationFilter, UserDetailsServiceImpl

Relacionado: #<issue_number> - Reorganização de pacotes DDD"
```

---

### **Commit 7: Adiciona Utilitários e Validações**
```bash
# Adiciona utilitários
git add src/main/java/br/com/casadoamor/sgca/util/

git commit -m "feat: Adiciona utilitário de validação de senha

- Adiciona PasswordValidator com política rigorosa
- Política: min 8 chars, 1 upper, 1 lower, 1 number, 1 special
- Implementa validação com mensagens detalhadas

Relacionado: #<issue_number> - Política de senhas rigorosas"
```

---

### **Commit 8: Atualiza Configurações e Migrations**
```bash
# Adiciona migrations e configurações
git add src/main/resources/db/migration/V02__create_tables.sql
git add src/main/resources/db/migration/V03__create_autenticacao_2fa.sql
git add src/main/resources/db/migration/V04__add_senha_temporaria.sql
git add src/main/resources/db/migration/V05__add_foto_columns.sql
git add src/main/resources/application.properties
git add src/main/java/br/com/casadoamor/sgca/config/EmailConfig.java
git add src/main/java/br/com/casadoamor/sgca/config/SecurityConfig.java
git add src/main/java/br/com/casadoamor/sgca/config/exception/CustomExceptionHandler.java

git commit -m "feat: Adiciona migrations e atualiza configurações

Migrations:
- V02: Cria tabelas RBAC (perfis, permissões, usuários)
- V03: Adiciona suporte 2FA via email
- V04: Adiciona campo senha_temporaria
- V05: Adiciona colunas para upload de fotos

Configurações:
- Adiciona EmailConfig para envio de emails
- Atualiza SecurityConfig com novos endpoints públicos
- Atualiza application.properties com configs de email, 2FA, histórico

Relacionado: #<issue_number> - Features de autenticação e RBAC"
```

---

### **Commit 9: Atualiza Dependências e Configurações Docker**
```bash
# Atualiza pom.xml e docker
git add pom.xml
git add docker-compose.yml
git add .env.example

git commit -m "chore: Atualiza dependências e configuração Docker

- Atualiza pom.xml com novas dependências (Mail, JWT, etc)
- Atualiza docker-compose.yml com volume para uploads
- Adiciona .env.example com variáveis de email

Relacionado: #<issue_number> - Infraestrutura"
```

---

### **Commit 10: Limpa Arquivos Antigos e Adiciona Documentação**
```bash
# Remove arquivos antigos e adiciona docs
git rm QUICK_START.md SECURITY_IMPROVEMENTS.md
git rm src/main/java/br/com/casadoamor/sgca/controller/PacienteController.java
git rm src/main/java/br/com/casadoamor/sgca/dto/ApiResponseDTO.java
git rm src/main/java/br/com/casadoamor/sgca/dto/DadoPessoalInputDTO.java
git rm src/main/java/br/com/casadoamor/sgca/dto/EditarDadoPessoalInputDTO.java
git rm src/main/java/br/com/casadoamor/sgca/dto/EditarEnderecoInputDTO.java
git rm src/main/java/br/com/casadoamor/sgca/dto/EditarPacienteDTO.java
git rm src/main/java/br/com/casadoamor/sgca/dto/EnderecoInputDTO.java
git rm src/main/java/br/com/casadoamor/sgca/dto/ErroResponseDTO.java
git rm src/main/java/br/com/casadoamor/sgca/dto/PacienteDTO.java
git rm src/main/java/br/com/casadoamor/sgca/dto/PaginatedResponseDTO.java
git rm src/main/java/br/com/casadoamor/sgca/dto/RegistrarPacienteDTO.java
git rm src/main/java/br/com/casadoamor/sgca/entity/BaseEntity.java
git rm src/main/java/br/com/casadoamor/sgca/entity/DadoClinico.java
git rm src/main/java/br/com/casadoamor/sgca/entity/DadoPessoal.java
git rm src/main/java/br/com/casadoamor/sgca/entity/Endereco.java
git rm src/main/java/br/com/casadoamor/sgca/entity/Paciente.java
git rm src/main/java/br/com/casadoamor/sgca/mapper/DadoPessoalMapper.java
git rm src/main/java/br/com/casadoamor/sgca/mapper/EnderecoMapper.java
git rm src/main/java/br/com/casadoamor/sgca/mapper/PacienteMapper.java
git rm src/main/java/br/com/casadoamor/sgca/mapper/PaginatedResponseMapper.java
git rm src/main/java/br/com/casadoamor/sgca/repository/PacienteRepository.java
git rm src/main/java/br/com/casadoamor/sgca/repository/ProfissionalSaudeRepository.java
git rm src/main/java/br/com/casadoamor/sgca/repository/UsuarioRepository.java
git rm src/main/java/br/com/casadoamor/sgca/service/PacienteService.java

git add SWAGGER_GUIDE.md
git add src/main/java/br/com/casadoamor/sgca/entity/Acompanhante.java
git add src/main/java/br/com/casadoamor/sgca/entity/ProfissionalSaude.java
git add src/main/java/br/com/casadoamor/sgca/entity/RecebeuAlta.java
git add src/main/java/br/com/casadoamor/sgca/entity/Usuario.java
git add src/main/java/br/com/casadoamor/sgca/service/imp/PacienteServiceImp.java
git add target/classes/application.properties

git commit -m "chore: Remove arquivos da estrutura antiga e adiciona documentação

Remove:
- Arquivos duplicados da estrutura antiga (pré-reorganização)
- Documentações desatualizadas (QUICK_START, SECURITY_IMPROVEMENTS)

Adiciona:
- SWAGGER_GUIDE.md com documentação completa da API
- Mantém entities não reorganizadas (Acompanhante, Usuario, etc)
- Atualiza classes compiladas (target/)

Relacionado: #<issue_number> - Limpeza pós-reorganização"
```

---

## 🔄 Alternativa: Commit Único (Não Recomendado)

Se você **realmente** precisar fazer tudo de uma vez:

```bash
# Adiciona TUDO
git add .

git commit -m "refactor: Reorganiza estrutura de pacotes seguindo DDD

BREAKING CHANGE: Reorganização completa da estrutura de pacotes

Antes:
- Pacotes flat (controller/, service/, repository/, entity/, dto/)

Depois:
- Pacotes organizados por domínio:
  - controller/{admin,auth,file,paciente}
  - service/{admin,auth,file,paciente,common}
  - repository/{admin,auth,paciente}
  - entity/{admin,auth,paciente,common}
  - dto/{admin,auth,common,paciente,twofactor}

Features Adicionadas:
- Sistema RBAC completo (perfis, permissões)
- Autenticação 2FA via email
- Upload de fotos de perfil
- Histórico de senhas (últimas 5)
- Ativação de conta com senha temporária
- Política de senhas rigorosas

Migrations:
- V02: Tabelas RBAC
- V03: Autenticação 2FA
- V04: Senha temporária
- V05: Upload de fotos

Documentação:
- Adiciona SWAGGER_GUIDE.md

Relacionado: #<issue_number>"
```

⚠️ **Desvantagens do commit único:**
- Dificulta code review
- Dificulta rollback parcial
- Histórico confuso
- CI/CD pode quebrar

---

## 🚀 Comandos Completos para Execução

### **Opção 1: Commits Separados (Recomendado)**

```powershell
# Commit 1: Entities
git add src/main/java/br/com/casadoamor/sgca/entity/admin/ src/main/java/br/com/casadoamor/sgca/entity/auth/ src/main/java/br/com/casadoamor/sgca/entity/common/ src/main/java/br/com/casadoamor/sgca/entity/paciente/ src/main/java/br/com/casadoamor/sgca/enums/TipoToken.java
git commit -m "refactor: Reorganiza entities por domínio (admin, auth, paciente, common)"

# Commit 2: Repositories
git add src/main/java/br/com/casadoamor/sgca/repository/admin/ src/main/java/br/com/casadoamor/sgca/repository/auth/ src/main/java/br/com/casadoamor/sgca/repository/paciente/
git commit -m "refactor: Reorganiza repositories por domínio (admin, auth, paciente)"

# Commit 3: Services
git add src/main/java/br/com/casadoamor/sgca/service/admin/ src/main/java/br/com/casadoamor/sgca/service/auth/ src/main/java/br/com/casadoamor/sgca/service/file/ src/main/java/br/com/casadoamor/sgca/service/common/ src/main/java/br/com/casadoamor/sgca/service/paciente/ src/main/java/br/com/casadoamor/sgca/service/imp/EmailServiceImp.java src/main/java/br/com/casadoamor/sgca/service/imp/LocalFileStorageService.java src/main/java/br/com/casadoamor/sgca/service/imp/UserPhotoService.java
git commit -m "refactor: Reorganiza services por domínio (admin, auth, file, paciente)"

# Commit 4: DTOs
git add src/main/java/br/com/casadoamor/sgca/dto/admin/ src/main/java/br/com/casadoamor/sgca/dto/auth/ src/main/java/br/com/casadoamor/sgca/dto/common/ src/main/java/br/com/casadoamor/sgca/dto/paciente/ src/main/java/br/com/casadoamor/sgca/dto/twofactor/ src/main/java/br/com/casadoamor/sgca/dto/SessaoDTO.java
git commit -m "refactor: Reorganiza DTOs por domínio e tipo (request/response)"

# Commit 5: Controllers
git add src/main/java/br/com/casadoamor/sgca/controller/admin/ src/main/java/br/com/casadoamor/sgca/controller/auth/ src/main/java/br/com/casadoamor/sgca/controller/file/ src/main/java/br/com/casadoamor/sgca/controller/paciente/
git commit -m "refactor: Reorganiza controllers por domínio (admin, auth, file, paciente)"

# Commit 6: Mappers e Security
git add src/main/java/br/com/casadoamor/sgca/mapper/common/ src/main/java/br/com/casadoamor/sgca/mapper/paciente/ src/main/java/br/com/casadoamor/sgca/security/
git commit -m "refactor: Reorganiza mappers e adiciona classes de segurança"

# Commit 7: Utilitários
git add src/main/java/br/com/casadoamor/sgca/util/
git commit -m "feat: Adiciona utilitário de validação de senha"

# Commit 8: Migrations e Configurações
git add src/main/resources/db/migration/V02__create_tables.sql src/main/resources/db/migration/V03__create_autenticacao_2fa.sql src/main/resources/db/migration/V04__add_senha_temporaria.sql src/main/resources/db/migration/V05__add_foto_columns.sql src/main/resources/application.properties src/main/java/br/com/casadoamor/sgca/config/EmailConfig.java src/main/java/br/com/casadoamor/sgca/config/SecurityConfig.java src/main/java/br/com/casadoamor/sgca/config/exception/CustomExceptionHandler.java
git commit -m "feat: Adiciona migrations e atualiza configurações"

# Commit 9: Dependências e Docker
git add pom.xml docker-compose.yml .env.example
git commit -m "chore: Atualiza dependências e configuração Docker"

# Commit 10: Limpeza
git rm QUICK_START.md SECURITY_IMPROVEMENTS.md src/main/java/br/com/casadoamor/sgca/controller/PacienteController.java src/main/java/br/com/casadoamor/sgca/dto/ApiResponseDTO.java src/main/java/br/com/casadoamor/sgca/dto/DadoPessoalInputDTO.java src/main/java/br/com/casadoamor/sgca/dto/EditarDadoPessoalInputDTO.java src/main/java/br/com/casadoamor/sgca/dto/EditarEnderecoInputDTO.java src/main/java/br/com/casadoamor/sgca/dto/EditarPacienteDTO.java src/main/java/br/com/casadoamor/sgca/dto/EnderecoInputDTO.java src/main/java/br/com/casadoamor/sgca/dto/ErroResponseDTO.java src/main/java/br/com/casadoamor/sgca/dto/PacienteDTO.java src/main/java/br/com/casadoamor/sgca/dto/PaginatedResponseDTO.java src/main/java/br/com/casadoamor/sgca/dto/RegistrarPacienteDTO.java src/main/java/br/com/casadoamor/sgca/entity/BaseEntity.java src/main/java/br/com/casadoamor/sgca/entity/DadoClinico.java src/main/java/br/com/casadoamor/sgca/entity/DadoPessoal.java src/main/java/br/com/casadoamor/sgca/entity/Endereco.java src/main/java/br/com/casadoamor/sgca/entity/Paciente.java src/main/java/br/com/casadoamor/sgca/mapper/DadoPessoalMapper.java src/main/java/br/com/casadoamor/sgca/mapper/EnderecoMapper.java src/main/java/br/com/casadoamor/sgca/mapper/PacienteMapper.java src/main/java/br/com/casadoamor/sgca/mapper/PaginatedResponseMapper.java src/main/java/br/com/casadoamor/sgca/repository/PacienteRepository.java src/main/java/br/com/casadoamor/sgca/repository/ProfissionalSaudeRepository.java src/main/java/br/com/casadoamor/sgca/repository/UsuarioRepository.java src/main/java/br/com/casadoamor/sgca/service/PacienteService.java

git add SWAGGER_GUIDE.md src/main/java/br/com/casadoamor/sgca/entity/Acompanhante.java src/main/java/br/com/casadoamor/sgca/entity/ProfissionalSaude.java src/main/java/br/com/casadoamor/sgca/entity/RecebeuAlta.java src/main/java/br/com/casadoamor/sgca/entity/Usuario.java src/main/java/br/com/casadoamor/sgca/service/imp/PacienteServiceImp.java target/classes/application.properties

git commit -m "chore: Remove arquivos da estrutura antiga e adiciona documentação"

# Push de todos os commits
git push origin dev
```

---

## 📝 Convenções de Mensagens de Commit

### Tipos de Commit:
- **feat**: Nova funcionalidade
- **fix**: Correção de bug
- **refactor**: Refatoração de código (sem mudança de funcionalidade)
- **chore**: Tarefas de manutenção (dependências, configs, etc)
- **docs**: Documentação
- **test**: Adiciona ou modifica testes
- **perf**: Melhoria de performance
- **style**: Mudanças de formatação (não afeta lógica)

### Formato:
```
<tipo>(<escopo>): <descrição curta>

<descrição detalhada opcional>

<footer opcional: BREAKING CHANGE, Closes #123>
```

---

## ⚠️ Cuidados Antes de Commitar

### 1. **Verificar Build**
```bash
./mvnw clean install
```

### 2. **Verificar Testes**
```bash
./mvnw test
```

### 3. **Verificar Formatação**
```bash
./mvnw spotless:check  # Se usar Spotless
```

### 4. **Verificar Conflitos**
```bash
git fetch origin dev
git status
```

---

## 🎯 Recomendação Final

**✅ Use a Opção 1: Commits Separados (10 commits)**

### Vantagens:
- ✅ Code review mais fácil
- ✅ Rollback granular
- ✅ Histórico git limpo e profissional
- ✅ Facilita debugging futuro
- ✅ CI/CD mais robusto

### Desvantagens:
- ⏱️ Leva mais tempo (15-20 minutos vs 5 minutos)
- 🔄 Mais comandos para executar

---

## 📚 Recursos Adicionais

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Git Best Practices](https://www.git-scm.com/book/en/v2)
- [Semantic Versioning](https://semver.org/)

---

## ✅ Checklist Pós-Commit

- [ ] Todos os commits foram criados
- [ ] Build passou sem erros
- [ ] Testes passaram
- [ ] Push para branch 'dev' realizado
- [ ] CI/CD passou (se configurado)
- [ ] Pull Request criado (se aplicável)
- [ ] Code review solicitado

---

**Última Atualização**: 16/10/2025  
**Versão**: 1.0  
**Autor**: Sistema de Versionamento SGCA
