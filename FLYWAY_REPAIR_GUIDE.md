# Guia de Correção Flyway - Migration V08

## Problema
A migration `V08__seed_enderecos_pacientes.sql` pode ter sido aplicada com um checksum diferente no banco de dados, causando erro de validação do Flyway.

## Sintomas
```
Caused by: org.flywaydb.core.api.exception.FlywayValidateException: Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 08
-> Applied to database : -189628197
-> Resolved locally    : 1661311494
```

## Soluções

### Opção 1: Flyway Repair (Recomendado para Desenvolvimento/Homologação)

Execute o comando Flyway repair para recalcular o checksum:

```bash
# Via Maven
./mvnw flyway:repair

# Via Docker (se estiver rodando em container)
docker exec -it <container_name> ./mvnw flyway:repair
```

### Opção 2: Atualizar Checksum Manualmente no Banco (Produção)

**⚠️ ATENÇÃO: Execute com cuidado em produção!**

Conecte-se ao banco de dados e execute:

```sql
-- Verificar o checksum atual
SELECT version, checksum, description 
FROM flyway_schema_history 
WHERE version = '08';

-- Atualizar para o checksum correto
UPDATE flyway_schema_history 
SET checksum = 1661311494 
WHERE version = '08';

-- Verificar novamente
SELECT version, checksum, description 
FROM flyway_schema_history 
WHERE version = '08';
```

### Opção 3: Remover e Reaplicar (Apenas Desenvolvimento)

**⚠️ NUNCA FAÇA ISSO EM PRODUÇÃO!**

```sql
-- Deletar dados das tabelas afetadas
DELETE FROM pacientes;
DELETE FROM enderecos WHERE cep LIKE '45000-%';

-- Remover a entrada da migration
DELETE FROM flyway_schema_history WHERE version = '08';

-- Reiniciar a aplicação para reaplicar a migration
```

## Prevenção em Produção

### Antes do Deploy:

1. **Verificar migrations pendentes:**
   ```bash
   ./mvnw flyway:info
   ```

2. **Se houver checksum mismatch:**
   - Execute o repair ANTES de iniciar a aplicação
   - Ou execute o UPDATE manual do checksum

3. **Após correção, validar:**
   ```bash
   ./mvnw flyway:validate
   ```

### Durante o Deploy:

```bash
# 1. Parar a aplicação
systemctl stop sgca-backend  # ou docker compose stop sgca

# 2. Executar repair (se necessário)
./mvnw flyway:repair

# 3. Validar migrations
./mvnw flyway:validate

# 4. Iniciar a aplicação
systemctl start sgca-backend  # ou docker compose up -d sgca
```

## Notas Importantes

- ✅ **NUNCA modifique migrations já aplicadas em produção**
- ✅ Sempre crie novas migrations (V09, V10, etc.) para mudanças
- ✅ Teste migrations em ambiente de desenvolvimento antes de produção
- ✅ Mantenha backups do banco antes de aplicar correções
- ⚠️ O checksum `1661311494` é o correto para a versão atual da V08

## Verificação de Sucesso

Após aplicar a correção, a aplicação deve iniciar sem erros e você deve ver nos logs:

```
Flyway Community Edition ... by Redgate
Database: jdbc:mysql://...
Successfully validated X migrations (execution time XXXXms)
```
