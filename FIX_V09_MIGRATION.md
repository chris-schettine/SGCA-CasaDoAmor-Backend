# Como Corrigir a Migration V09 na Nuvem

## Problema
A migration V09 falhou na nuvem porque tentava adicionar `profissao AFTER email`, mas:
- A coluna `email` não existe em `dados_pessoais`
- A coluna `profissao` já existe desde a V01

## Solução

### 1. Conectar no banco da nuvem
```bash
mysql -h <HOST> -u <USER> -p<PASSWORD> <DATABASE>
```

### 2. Remover o registro da migration falhada
```sql
DELETE FROM flyway_schema_history WHERE version = '09';
```

### 3. Verificar se a coluna nome_mae já existe
```sql
DESCRIBE dados_pessoais;
```

### 4. Se nome_mae NÃO existir, adicionar manualmente
```sql
ALTER TABLE dados_pessoais 
ADD COLUMN nome_mae VARCHAR(255) NULL AFTER nome;
```

### 5. Marcar a migration como sucesso
```sql
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
VALUES (
  (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history AS fsh),
  '09',
  'add nome mae profissao dados pessoais',
  'SQL',
  'V09__add_nome_mae_profissao_dados_pessoais.sql',
  -1234567890,  -- Será atualizado pelo Flyway
  USER(),
  0,
  1
);
```

### 6. Fazer deploy da versão corrigida
```bash
git add .
git commit -m "fix: correct V09 migration - remove profissao (already exists)"
git push origin dev
```

### 7. Reiniciar aplicação na nuvem
A aplicação agora deve iniciar sem erros.

## Mudanças Feitas no Código
- ✅ Removida tentativa de adicionar coluna `profissao` (já existe)
- ✅ Removida referência à coluna `email` (não existe)
- ✅ Mantida apenas adição de `nome_mae`
