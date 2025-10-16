# Guia de Seed do Banco de Dados - SGCA Backend

## 📋 Sobre o Seed

O arquivo `mysql-init/02-seed.sql` contém dados de exemplo para popular o banco de dados e facilitar os testes da aplicação.

## 🎯 Dados Incluídos no Seed

### 👥 Usuários e Credenciais

Todos os usuários têm a senha padrão: **`senha123`**

| Email | Tipo | Senha | Descrição |
|-------|------|-------|-----------|
| `admin@casadoamor.com` | ADMIN | senha123 | Administrador do sistema |
| `enfermeira@casadoamor.com` | ENFERMEIRO | senha123 | Profissional Enfermeira |
| `medico@casadoamor.com` | MEDICO | senha123 | Profissional Médico |
| `psicologa@casadoamor.com` | PSICOLOGO | senha123 | Profissional Psicóloga |
| `casadoamoremconquista@gmail.com` | ADMIN | senha123 | Admin com email do Gmail configurado |

### 🏥 Profissionais de Saúde

- **Enfermeira**: Maria Santos - COREN-123456/RJ
- **Médico**: Carlos Oliveira - CRM-654321/MG
- **Psicóloga**: Ana Paula Costa - CRP-789012/BA

### 👨‍⚕️ Pacientes

1. **José da Silva** - Diabetes Mellitus Tipo 2
   - Usa sonda nasoenteral
   - Tratamento: Insulinoterapia
   - Acompanhante: Pedro Silva

2. **Francisca Pereira** - Hipertensão Arterial
   - Controle medicamentoso
   - Acompanhante: Mariana Pereira

## 🚀 Como Aplicar o Seed

### Opção 1: Inicialização Automática (Recomendado)

O seed será aplicado automaticamente quando você iniciar os containers pela primeira vez:

```bash
# Remover volumes existentes (ATENÇÃO: isso apaga todos os dados!)
docker compose down -v

# Iniciar containers (o seed será aplicado automaticamente)
docker compose up -d

# Aguardar inicialização
docker logs mysql_sgca -f
```

**Observe nos logs:**
```
MySQL initialization completed for SGCA Backend
Seed executado com sucesso!
```

### Opção 2: Aplicação Manual

Se você já tem os containers rodando e quer reaplicar o seed:

```bash
# 1. Parar a aplicação
docker compose down

# 2. Remover volume do MySQL (apaga dados existentes)
docker volume rm sgca-casadoamor-backend_mysql_data

# 3. Reiniciar containers
docker compose up -d
```

### Opção 3: Executar SQL Manualmente

```bash
# Com containers rodando
docker exec -i mysql_sgca mysql -usgca_user -padmin sgca < mysql-init/02-seed.sql
```

## 🔍 Verificar se o Seed Foi Aplicado

### Verificar Usuários Criados

```bash
docker exec -it mysql_sgca mysql -usgca_user -padmin sgca -e "SELECT email, tipo_usuario, ativo FROM usuarios;"
```

**Saída esperada:**
```
+-------------------------------------+--------------+-------+
| email                               | tipo_usuario | ativo |
+-------------------------------------+--------------+-------+
| admin@casadoamor.com                | ADMIN        |     1 |
| enfermeira@casadoamor.com           | ENFERMEIRO   |     1 |
| medico@casadoamor.com               | MEDICO       |     1 |
| psicologa@casadoamor.com            | PSICOLOGO    |     1 |
| casadoamoremconquista@gmail.com     | ADMIN        |     1 |
+-------------------------------------+--------------+-------+
```

### Verificar Pacientes

```bash
docker exec -it mysql_sgca mysql -usgca_user -padmin sgca -e "
SELECT p.id, dp.nome, dp.cpf 
FROM pacientes p 
JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id;"
```

### Verificar Todos os Dados

```bash
docker exec -it mysql_sgca mysql -usgca_user -padmin sgca -e "
SELECT 
    (SELECT COUNT(*) FROM usuarios) as usuarios,
    (SELECT COUNT(*) FROM profissional_saude) as profissionais,
    (SELECT COUNT(*) FROM pacientes) as pacientes,
    (SELECT COUNT(*) FROM acompanhantes) as acompanhantes,
    (SELECT COUNT(*) FROM dados_pessoais) as dados_pessoais,
    (SELECT COUNT(*) FROM enderecos) as enderecos;"
```

## 🧪 Testar Login com Credenciais do Seed

### Via curl

```bash
# Login como Admin
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@casadoamor.com",
    "password": "senha123"
  }'
```

### Via Swagger UI

1. Acesse: http://localhost:8090/swagger-ui/index.html
2. Vá para **auth-controller**
3. Use **POST /api/auth/login**
4. Teste com qualquer credencial da tabela acima

## 📧 Testar Email com Usuário do Seed

Agora você pode testar o envio de emails usando o usuário do Gmail:

```bash
# Recuperação de senha
curl -X POST http://localhost:8090/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "casadoamoremconquista@gmail.com"}'
```

O email será enviado para: `casadoamoremconquista@gmail.com`

## 🔄 Resetar Banco de Dados

Se você quiser começar do zero novamente:

```bash
# Parar e remover tudo
docker compose down -v

# Iniciar novamente (seed será reaplicado)
docker compose up -d

# Verificar logs
docker logs mysql_sgca -f
```

## ⚠️ Notas Importantes

### Senhas Criptografadas

As senhas no seed estão criptografadas com BCrypt (strength 12):
- **Senha original**: `senha123`
- **Hash BCrypt**: `$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe`

### IDs Fixos

Os IDs são fixos (UUIDs pré-definidos) para facilitar testes e referências:
- Usuários: `u1111111...`, `u2222222...`, etc.
- Pacientes: `pac55555...`, `pac66666...`
- Profissionais: `p2222222...`, `p3333333...`

### Ordem de Execução

Os arquivos em `mysql-init/` são executados em ordem alfabética:
1. `01-init.sql` - Cria database e usuário
2. `02-seed.sql` - Popula com dados de exemplo

## 📚 Estrutura dos Dados

```
dados_pessoais (8 registros)
    ├── usuarios (5 registros)
    │   ├── Admin
    │   ├── Enfermeira
    │   ├── Médico
    │   ├── Psicóloga
    │   └── Admin Gmail
    │
    ├── profissional_saude (3 registros)
    │   ├── Enfermeira
    │   ├── Médico
    │   └── Psicóloga
    │
    ├── pacientes (2 registros)
    │   ├── José da Silva
    │   └── Francisca Pereira
    │
    └── acompanhantes (2 registros)
        ├── Pedro Silva
        └── Mariana Pereira

enderecos (6 registros)
    └── Um endereço para cada pessoa

dados_clinicos (2 registros)
    └── Um para cada paciente
```

## 🎓 Casos de Uso para Testes

### 1. Teste de Login
```bash
# Login como diferentes tipos de usuário
- Admin: admin@casadoamor.com
- Enfermeira: enfermeira@casadoamor.com
- Médico: medico@casadoamor.com
```

### 2. Teste de Recuperação de Senha
```bash
# Usar email do Gmail configurado
casadoamoremconquista@gmail.com
```

### 3. Teste de CRUD de Pacientes
```bash
# Listar pacientes existentes
# Editar: José da Silva
# Criar novo paciente
```

### 4. Teste de 2FA
```bash
# Login com qualquer usuário
# Ativar 2FA
# Testar código enviado por email
```

---

**Última atualização:** 16 de outubro de 2025
