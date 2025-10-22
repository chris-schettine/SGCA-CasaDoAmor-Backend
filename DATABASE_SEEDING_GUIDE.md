# 🌱 Guia de Seed de Dados (Database Seeding)

Este documento explica como configurar o seed automático de dados no sistema SGCA - Casa do Amor.

## 📚 Índice
1. [Visão Geral](#visão-geral)
2. [Opção 1: Flyway Migration (SQL)](#opção-1-flyway-migration-sql)
3. [Opção 2: DataSeederConfig (Java)](#opção-2-dataseederconfig-java)
4. [Comparação das Abordagens](#comparação-das-abordagens)
5. [Configuração por Ambiente](#configuração-por-ambiente)
6. [Como Usar](#como-usar)

---

## Visão Geral

O sistema oferece **duas abordagens** para seed de dados:

1. **Flyway Migration (SQL)** - Dados permanentes, versionados com o schema
2. **DataSeederConfig (Java)** - Dados de desenvolvimento/teste, apenas em ambientes específicos

Ambas as abordagens estão implementadas e prontas para uso!

---

## Opção 1: Flyway Migration (SQL)

### 📁 Localização
```
src/main/resources/db/migration/V06__seed_initial_data.sql
```

### ✅ Vantagens
- **Permanente**: Executado em todos os ambientes (dev, test, prod)
- **Versionado**: Parte do histórico de migrações do banco
- **Rollback**: Pode ser revertido com Flyway
- **Rastreável**: Fica registrado na tabela `flyway_schema_history`
- **Controle**: Ideal para dados essenciais do sistema

### ⚙️ Como Funciona
- Flyway executa automaticamente ao iniciar a aplicação
- Se a migration já foi executada, não executa novamente
- Usa `INSERT IGNORE` para evitar duplicatas

### 📝 Dados Incluídos
```sql
-- ✅ Admin padrão
CPF: 00000000000
Senha: Admin@123
Email: admin@casadoamor.com

-- ✅ 9 Permissões básicas
PACIENTE_READ, PACIENTE_WRITE, PACIENTE_DELETE
PRONTUARIO_READ, PRONTUARIO_WRITE, PRONTUARIO_DELETE
USER_READ, USER_WRITE, USER_DELETE

-- ✅ 4 Perfis básicos
MEDICO_GERAL, ENFERMEIRO, RECEPCIONISTA, PSICOLOGO
(cada um com suas permissões já associadas)
```

### 🔧 Como Modificar
1. Edite o arquivo `V06__seed_initial_data.sql`
2. **IMPORTANTE**: Se já foi executado, crie uma nova migration `V07__update_seed.sql`
3. Rebuild e restart do container

---

## Opção 2: DataSeederConfig (Java)

### 📁 Localização
```
src/main/java/br/com/casadoamor/sgca/config/DataSeederConfig.java
```

### ✅ Vantagens
- **Condicional**: Apenas em ambientes específicos (dev, test)
- **Flexível**: Fácil de modificar sem criar novas migrations
- **Programável**: Usa Java, permite lógica complexa
- **Limpo**: Não "suja" o banco de produção com dados de teste
- **Rápido**: Ideal para desenvolvimento local

### ⚙️ Como Funciona
```java
@Profile({"dev", "test"}) // Apenas dev e test!
public class DataSeederConfig {
    @Bean
    CommandLineRunner seedDatabase() {
        // Executado ao iniciar a aplicação
    }
}
```

### 📝 Dados Incluídos
```
-- ✅ Admin padrão
CPF: 00000000000
Senha: Admin@123

-- ✅ 9 Permissões básicas
(mesmas do SQL)

-- ✅ 4 Perfis básicos
(mesmos do SQL)

-- 🔓 Usuários de teste (opcional)
Dr. João Silva (MEDICO)
Enf. Maria Santos (ENFERMEIRO)
Ana Oliveira (RECEPCIONISTA)
```

### 🔧 Como Modificar
1. Edite `DataSeederConfig.java`
2. Adicione/remova dados conforme necessário
3. Rebuild e restart do container

### 🔓 Habilitar Usuários de Teste
Para criar usuários de teste automaticamente, descomente a linha:

```java
@Bean
CommandLineRunner seedDatabase() {
    return args -> {
        seedPermissoes();
        seedPerfis();
        seedAdminUser();
        
        // Descomente a linha abaixo:
        seedTestUsers(); // ← Cria Dr. João Silva, Enf. Maria, etc.
        
        log.info("✅ Seed concluído!");
    };
}
```

---

## Comparação das Abordagens

| Característica | Flyway Migration (SQL) | DataSeederConfig (Java) |
|----------------|------------------------|-------------------------|
| **Quando executa** | Sempre (todos ambientes) | Apenas dev/test |
| **Permanência** | Permanente | Temporário/Teste |
| **Modificação** | Requer nova migration | Edição direta |
| **Rollback** | Flyway managed | Manual |
| **Dados de teste** | ❌ Não recomendado | ✅ Ideal |
| **Produção** | ✅ Seguro | ❌ Desabilitado |
| **Complexidade** | SQL puro | Java + JPA |

---

## Configuração por Ambiente

### 🟢 Desenvolvimento (application.properties)
```properties
# Habilita seeder Java
spring.profiles.active=dev

# Flyway sempre executa
spring.flyway.enabled=true
```

### 🟡 Teste (application-test.properties)
```properties
# Habilita seeder Java
spring.profiles.active=test

# Flyway sempre executa
spring.flyway.enabled=true
```

### 🔴 Produção (application-docker.properties)
```properties
# Desabilita seeder Java
spring.profiles.active=prod

# Flyway sempre executa (apenas V01-V06)
spring.flyway.enabled=true
```

---

## Como Usar

### 🚀 Primeira Vez (Build Limpo)

```bash
# 1. Limpar volumes antigos
docker-compose down -v

# 2. Rebuild e restart
docker compose up -d --build

# 3. Ver logs do seed
docker logs spring_sgca | grep "🌱"
```

**Resultado Esperado:**
```
🌱 Iniciando seed de dados...
📋 Criando permissões...
✅ 9 permissões criadas/verificadas
👥 Criando perfis...
✅ Perfis criados/verificados
👤 Criando usuário admin...
✅ Admin criado: Admin Sistema (CPF: 00000000000, Senha: Admin@123)
✅ Seed de dados concluído com sucesso!
```

### 🔄 Atualizações

**Para Flyway (SQL)**:
```bash
# Criar nova migration
# src/main/resources/db/migration/V07__update_seed.sql

# Rebuild
docker compose up -d --build
```

**Para DataSeeder (Java)**:
```bash
# Editar DataSeederConfig.java
# Rebuild
docker compose up -d --build
```

### 🧪 Testar Seed

```bash
# Login com admin
curl -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "00000000000",
    "senha": "Admin@123"
  }'

# Listar permissões (requer token)
curl -X GET http://localhost:8090/admin/permissions \
  -H "Authorization: Bearer SEU_TOKEN"

# Listar perfis
curl -X GET http://localhost:8090/admin/roles \
  -H "Authorization: Bearer SEU_TOKEN"
```

---

## 🎯 Recomendações

### Para Desenvolvimento Local
✅ Use **DataSeederConfig (Java)**
- Mais flexível
- Fácil de modificar
- Dados de teste incluídos

### Para Staging/Produção
✅ Use **Flyway Migration (SQL)**
- Dados essenciais apenas
- Versionado e rastreável
- Mais seguro

### Melhor dos Dois Mundos
✅ Use **AMBOS**:
- **Flyway V06**: Admin + Permissões + Perfis (essenciais)
- **DataSeederConfig**: Usuários de teste (apenas dev/test)

---

## 📋 Checklist de Implementação

- [x] Flyway Migration V06 criado
- [x] DataSeederConfig implementado
- [x] Profile-based activation configurado
- [x] Verificação de duplicatas implementada
- [x] Logs informativos adicionados
- [x] Usuários de teste opcionais
- [x] Documentação completa

---

## 🔍 Troubleshooting

### Seed não executou
```bash
# Verificar profile ativo
docker logs spring_sgca | grep "spring.profiles.active"

# Verificar se Flyway executou
docker logs spring_sgca | grep "Flyway"

# Verificar se DataSeeder executou
docker logs spring_sgca | grep "🌱"
```

### Duplicatas no banco
- Flyway: Usa `INSERT IGNORE` - seguro
- DataSeeder: Verifica `findByCpf()` antes de criar

### Resetar tudo
```bash
# Remover volumes e rebuild
docker-compose down -v
docker compose up -d --build
```

---

## 📞 Suporte

Para mais informações, consulte:
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [CommandLineRunner](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/CommandLineRunner.html)

---

**Desenvolvido para: SGCA - Casa do Amor**  
**Última Atualização: Outubro 2025**
