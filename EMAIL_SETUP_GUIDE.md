# Guia de Configuração de Email - Gmail

## ⚠️ IMPORTANTE: Senha de App é Obrigatória

O Gmail não permite mais o uso de senhas normais para aplicações externas. Você **DEVE** criar uma **Senha de App** (App Password).

## Passo 1: Verificar Autenticação em Duas Etapas

1. Acesse: https://myaccount.google.com/security
2. Certifique-se de que a **Verificação em duas etapas** está **ATIVADA**
3. Se não estiver ativada, clique em "Verificação em duas etapas" e siga as instruções

## Passo 2: Criar uma Senha de App

1. Acesse: https://myaccount.google.com/apppasswords
   - Ou vá em: Conta Google → Segurança → Verificação em duas etapas → Senhas de app
2. Faça login se solicitado
3. Em "Selecionar app", escolha **"Correio"** ou **"Outro (nome personalizado)"**
4. Digite um nome descritivo como: **"SGCA Backend"**
5. Clique em **"Gerar"**
6. O Google mostrará uma senha de 16 caracteres (exemplo: `abcd efgh ijkl mnop`)
7. **COPIE** essa senha (sem espaços)

## Passo 3: Atualizar o Arquivo `.env`

Edite o arquivo `.env` e substitua a senha atual pela **Senha de App**:

```env
# Configurações de Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=casadoamoremconquista@gmail.com
MAIL_PASSWORD=abcdefghijklmnop          # ← Senha de App (sem espaços)
SGCA_EMAIL_PASSWORD=abcdefghijklmnop    # ← Mesma senha de app
```

## Passo 4: Reiniciar a Aplicação

```bash
# Parar os containers
docker compose down

# Iniciar novamente
docker compose up -d --build
```

## Passo 5: Testar o Envio de Email

Você pode testar o envio de email através da API de registro ou recuperação de senha:

### Teste via Swagger UI
1. Acesse: http://localhost:8090/swagger-ui/index.html
2. Encontre o endpoint de registro ou recuperação de senha
3. Execute uma requisição de teste

### Teste via curl
```bash
# Exemplo de teste de recuperação de senha
curl -X POST http://localhost:8090/api/auth/esqueci-senha \
  -H "Content-Type: application/json" \
  -d '{"email": "teste@example.com"}'
```

## Solução de Problemas

### Erro: "535-5.7.8 Username and Password not accepted"
- **Causa**: Senha incorreta ou senha de app não configurada
- **Solução**: Verifique se você está usando a Senha de App, não a senha normal

### Erro: "530 5.7.0 Must issue a STARTTLS command first"
- **Causa**: Configuração de porta ou STARTTLS incorreta
- **Solução**: Verifique se `MAIL_PORT=587` e se `mail.smtp.starttls.enable=true`

### Erro: "Authentication failed"
- **Causa**: Verificação em duas etapas não está ativada
- **Solução**: Ative a verificação em duas etapas primeiro

## Configurações Atuais

As seguintes configurações estão definidas no `application.properties`:

```properties
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:casadoamoremconquista@gmail.com}
spring.mail.password=${SGCA_EMAIL_PASSWORD:dummy_password}
```

E no `EmailConfig.java`:

```java
props.put("mail.transport.protocol", "smtp");
props.put("mail.smtp.auth", "true");
props.put("mail.smtp.starttls.enable", "true");
props.put("mail.debug", "true");
```

## Recursos Adicionais

- [Senhas de app do Google](https://support.google.com/accounts/answer/185833)
- [Verificação em duas etapas](https://support.google.com/accounts/answer/185839)
- [Permitir apps menos seguros (Descontinuado)](https://support.google.com/accounts/answer/6010255)

---

**Nota de Segurança**: Nunca compartilhe sua senha de app publicamente ou em repositórios Git. Mantenha o arquivo `.env` no `.gitignore`.
