# Guia de Teste de Email - SGCA Backend

Este guia mostra como testar se o envio de emails está funcionando corretamente.

## 🎯 Métodos de Teste

### Método 1: Usando o Script Automatizado (Recomendado)

```bash
./test-email.sh
```

O script oferece um menu interativo com as seguintes opções:
1. **Testar envio de email** - Envia email de recuperação de senha
2. **Verificar variáveis de ambiente** - Confirma configurações
3. **Verificar logs de email** - Mostra logs relacionados a email
4. **Verificar se aplicação está rodando** - Status da aplicação
5. **Abrir Swagger UI** - Interface visual da API

### Método 2: Usando curl Diretamente

```bash
# Teste de recuperação de senha
curl -X POST http://localhost:8090/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "seu@email.com"}'
```

**Resposta esperada (sucesso):**
```json
{
  "message": "Email de recuperação enviado com sucesso"
}
```

### Método 3: Usando Swagger UI (Interface Visual)

1. Acesse: http://localhost:8090/swagger-ui/index.html
2. Navegue até a seção **"auth-controller"**
3. Encontre o endpoint **POST /api/auth/forgot-password**
4. Clique em **"Try it out"**
5. Preencha o JSON:
   ```json
   {
     "email": "seu@email.com"
   }
   ```
6. Clique em **"Execute"**
7. Verifique a resposta e seu email

## 📋 Endpoints Disponíveis para Teste de Email

### 1. Recuperação de Senha (Forgot Password)
- **Endpoint:** `POST /api/auth/forgot-password`
- **Descrição:** Envia email com link de recuperação de senha
- **Body:**
  ```json
  {
    "email": "usuario@example.com"
  }
  ```

### 2. Código 2FA (Two-Factor Authentication)
- **Endpoint:** `POST /api/auth/2fa/resend`
- **Descrição:** Reenvia código de autenticação de dois fatores
- **Requer:** Autenticação JWT
- **Body:**
  ```json
  {
    "email": "usuario@example.com"
  }
  ```

### 3. Ativação de Conta (se implementado)
- **Endpoint:** `POST /api/auth/resend-activation`
- **Descrição:** Reenvia email de ativação de conta

## 🔍 Como Verificar se o Email Foi Enviado

### 1. Verificar Logs da Aplicação

```bash
# Ver todos os logs relacionados a email
docker logs spring_sgca 2>&1 | grep -i mail

# Ver logs em tempo real
docker logs -f spring_sgca
```

**Logs de sucesso esperados:**
```
DEBUG o.s.mail.javamail.JavaMailSenderImpl : Added recipient: seu@email.com
DEBUG o.s.mail.javamail.JavaMailSenderImpl : Sent message
```

### 2. Verificar Caixa de Email

Verifique:
- ✅ Caixa de entrada
- ✅ Pasta de spam/lixo eletrônico
- ✅ Pasta de promoções (Gmail)

### 3. Verificar Configurações no Container

```bash
# Verificar variáveis de ambiente
docker exec spring_sgca env | grep -E "(MAIL|SGCA_EMAIL)"

# Resultado esperado:
# MAIL_HOST=smtp.gmail.com
# MAIL_PORT=587
# MAIL_USERNAME=casadoamoremconquista@gmail.com
# SGCA_EMAIL_PASSWORD=****************
```

## ⚠️ Solução de Problemas

### Erro: "Failed to send email"

**Possíveis causas:**
1. **Senha de App incorreta**
   - Verifique se a senha no `.env` é a Senha de App do Google
   - Formato correto: sem espaços (exemplo: `ouswvapticrolnva`)

2. **Configurações SMTP incorretas**
   - Host: `smtp.gmail.com`
   - Port: `587`
   - STARTTLS: `enabled`

3. **Conta Gmail bloqueada**
   - Verifique: https://myaccount.google.com/security
   - Certifique-se que "Verificação em duas etapas" está ativa

**Solução:**
```bash
# 1. Verificar variáveis de ambiente
docker exec spring_sgca env | grep MAIL

# 2. Recriar senha de app
# Acesse: https://myaccount.google.com/apppasswords

# 3. Atualizar .env com nova senha

# 4. Reiniciar aplicação
docker compose down && docker compose up -d
```

### Erro: "User not found" ou "Invalid email"

**Causa:** O email não existe no banco de dados

**Solução:** Primeiro registre um usuário ou use um email existente

```bash
# Registrar novo usuário via Swagger UI
# Endpoint: POST /api/auth/register
```

### Email não chega

**Verificações:**

1. **Logs mostram envio?**
   ```bash
   docker logs spring_sgca 2>&1 | grep -i "sent message"
   ```

2. **Email está na pasta Spam?**
   - Marque como "não é spam"

3. **Aguarde alguns minutos**
   - Pode haver atraso no envio

4. **Verifique conta do Gmail**
   ```bash
   # Acesse: https://mail.google.com/mail/u/0/#sent
   # Verifique se o email aparece nos "Enviados"
   ```

## 📊 Exemplo de Teste Completo

```bash
# 1. Verificar se aplicação está rodando
curl http://localhost:8090/actuator/health

# 2. Verificar configurações de email
docker exec spring_sgca env | grep -E "(MAIL|SGCA_EMAIL)"

# 3. Enviar email de teste
curl -X POST http://localhost:8090/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "casadoamoremconquista@gmail.com"}'

# 4. Verificar logs
docker logs spring_sgca 2>&1 | grep -i mail | tail -n 20

# 5. Verificar email
echo "Verifique a caixa de entrada de: casadoamoremconquista@gmail.com"
```

## 🎓 Dicas Úteis

### Teste com Seu Próprio Email

Para testar rapidamente, use seu próprio email:

```bash
curl -X POST http://localhost:8090/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "seu.email@gmail.com"}'
```

### Habilitar Debug de Email

Para ver mais detalhes nos logs, o `EmailConfig.java` já está configurado com:
```java
props.put("mail.debug", "true");
```

Isso mostra toda a comunicação SMTP nos logs.

### Monitorar Logs em Tempo Real

```bash
docker logs -f spring_sgca | grep -i mail
```

## 📚 Referências

- **Swagger UI:** http://localhost:8090/swagger-ui/index.html
- **API Docs:** http://localhost:8090/v3/api-docs
- **Health Check:** http://localhost:8090/actuator/health
- **Senhas de App Google:** https://myaccount.google.com/apppasswords

---

**Última atualização:** 16 de outubro de 2025
