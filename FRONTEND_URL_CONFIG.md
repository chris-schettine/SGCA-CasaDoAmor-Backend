# Configuração de URL Dinâmica para Links de Email

## Visão Geral

O sistema agora suporta configuração dinâmica da URL do frontend através da variável de ambiente `FRONTEND_URL`. Isso permite que os links enviados por email (ativação de conta, redefinição de senha, etc.) sejam ajustados automaticamente para diferentes ambientes.

## Variável de Ambiente

### `FRONTEND_URL`

**Descrição:** URL base do frontend onde os usuários acessarão o sistema.

**Valor Padrão:** `http://localhost:3000`

**Exemplos de Uso:**

- **Desenvolvimento Local:** `http://localhost:3000`
- **Ambiente de Staging:** `https://staging.sgca.casadoamor.com.br`
- **Produção:** `https://sgca.casadoamor.com.br`

## Configuração

### 1. Arquivo `.env`

Adicione ou atualize a variável no seu arquivo `.env`:

```bash
# Frontend URL Configuration
FRONTEND_URL=https://sgca.casadoamor.com.br
```

### 2. Docker Compose

A variável já está configurada no `docker-compose.yml`:

```yaml
environment:
  FRONTEND_URL: ${FRONTEND_URL:-http://localhost:3000}
```

### 3. Variáveis do Sistema (Produção)

Para ambientes de produção (AWS, Azure, etc.), defina a variável de ambiente:

```bash
export FRONTEND_URL=https://sgca.casadoamor.com.br
```

Ou configure diretamente no painel de variáveis de ambiente do seu provedor cloud.

## Links Afetados

Os seguintes links em emails serão gerados dinamicamente com base na variável `FRONTEND_URL`:

1. **Ativação de Conta**
   - Template: `{FRONTEND_URL}/activate-account?token={token}`
   - Exemplo: `https://sgca.casadoamor.com.br/activate-account?token=abc123...`

2. **Redefinição de Senha**
   - Template: `{FRONTEND_URL}/reset-password?token={token}`
   - Exemplo: `https://sgca.casadoamor.com.br/reset-password?token=xyz789...`

3. **Recuperação de Conta**
   - Template: `{FRONTEND_URL}/recover-account?link={link}`
   - Exemplo: `https://sgca.casadoamor.com.br/recover-account?link=def456...`

## Verificação

Para verificar se a configuração está correta:

1. **Criar um usuário via endpoint de admin:**
   ```bash
   curl -X POST "http://localhost:8090/admin/users" \
     -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{
       "nome": "Teste",
       "email": "teste@exemplo.com",
       "cpf": "12345678900",
       "telefone": "(77) 99999-9999",
       "tipo": "RECEPCIONISTA"
     }'
   ```

2. **Verificar os logs do container:**
   ```bash
   docker logs spring_sgca 2>&1 | grep "activate-account"
   ```

3. **Confirmar que o link usa a URL configurada:**
   - Deve aparecer algo como: `https://sgca.casadoamor.com.br/activate-account?token=...`
   - **NÃO** deve aparecer: `http://localhost:3000/activate-account?token=...`

## Exemplo Completo de Deploy

### Ambiente de Produção

**Arquivo `.env` (ou variáveis de ambiente):**
```bash
# Database
SGCA_DB_PASSWORD=prod_secure_password
MYSQL_ROOT_PASSWORD=root_secure_password
MYSQL_DATABASE=sgca_prod
MYSQL_USER=sgca_user

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=casadoamoremconquista@gmail.com
SGCA_EMAIL_PASSWORD=your-16-char-app-password

# JWT
JWT_SECRET=your_production_jwt_secret_here
JWT_EXPIRATION=3600000

# Frontend URL - IMPORTANTE CONFIGURAR PARA PRODUÇÃO!
FRONTEND_URL=https://sgca.casadoamor.com.br
```

**Comando Docker:**
```bash
docker compose up -d
```

## Troubleshooting

### Problema: Links ainda apontam para localhost

**Solução:** Verifique se:
1. A variável `FRONTEND_URL` está definida no `.env`
2. O container foi recriado após adicionar a variável: `docker compose up -d --force-recreate`
3. A variável não tem espaços ou caracteres especiais

### Problema: Links com http ao invés de https

**Solução:** Certifique-se de que a variável `FRONTEND_URL` inclui o protocolo completo:
```bash
# Correto
FRONTEND_URL=https://sgca.casadoamor.com.br

# Incorreto
FRONTEND_URL=sgca.casadoamor.com.br
```

## Implementação Técnica

### Arquivos Modificados

1. **`application.properties`**
   ```properties
   app.frontend.url=${FRONTEND_URL:http://localhost:3000}
   ```

2. **`EmailServiceImp.java`**
   ```java
   @Value("${app.frontend.url}")
   private String frontendUrl;
   ```

3. **`docker-compose.yml`**
   ```yaml
   environment:
     FRONTEND_URL: ${FRONTEND_URL:-http://localhost:3000}
   ```

4. **`.env.example`**
   ```bash
   FRONTEND_URL=http://localhost:3000
   ```

---

**Última Atualização:** 23/10/2025
**Versão:** 1.0
