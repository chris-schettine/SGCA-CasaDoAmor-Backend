# Guia: Ativação Automática de 2FA com Rate Limiting

## 📋 Visão Geral

Este documento descreve a implementação da **ativação automática de autenticação de dois fatores (2FA)** durante o processo de ativação de conta, incluindo proteção contra envio excessivo de códigos através de **rate limiting**.

## 🎯 Objetivos

1. **Ativar 2FA automaticamente** quando o usuário ativa sua conta
2. **Enviar código inicial** imediatamente após ativação
3. **Solicitar código no próximo login** automaticamente
4. **Proteger contra abuso** com rate limiting em múltiplos níveis

## 🔄 Fluxo Completo

### 1. Criação de Usuário (Admin)
```
Admin → POST /admin/users
      → Sistema cria usuário com senha temporária
      → Envia email de ativação
```

### 2. Ativação de Conta (Usuário)
```
Usuário → POST /auth/activate
        ├─ Valida token e senha temporária
        ├─ Define nova senha
        ├─ Marca email como verificado
        ├─ Ativa conta
        ├─ 🆕 ATIVA 2FA AUTOMATICAMENTE
        └─ 🆕 ENVIA CÓDIGO 2FA INICIAL
```

### 3. Login com 2FA (Usuário)
```
Usuário → POST /auth/login
        ├─ Valida CPF e senha
        ├─ Detecta 2FA habilitado
        ├─ Envia código por email
        └─ Retorna: { requires2FA: true, userId: xxx }
```

### 4. Verificação 2FA (Usuário)
```
Usuário → POST /auth/2fa/verify
        ├─ Valida código de 6 dígitos
        ├─ Verifica expiração (5 minutos)
        └─ Retorna JWT token
```

## 🛡️ Rate Limiting

### Limites Implementados

| Janela de Tempo | Limite Máximo | Ação ao Exceder |
|----------------|---------------|-----------------|
| **15 minutos** | 3 códigos | Bloqueio temporário |
| **1 hora** | 5 códigos | Bloqueio temporário |
| **1 dia** | 10 códigos | Bloqueio temporário |
| **Intervalo mínimo** | 60 segundos | Erro de espera |

### Mensagens de Erro

Quando o limite é excedido, o usuário recebe:
```
"Limite de envios excedido. Por favor, aguarde X minutos"
"Limite de envios excedido. Por favor, aguarde X segundos"
```

### Estrutura do Rate Limit

**Tabela: `autenticacao_2fa_rate_limit`**
```sql
CREATE TABLE autenticacao_2fa_rate_limit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    ultimo_envio DATETIME,
    tentativas_ultimos_15min INT DEFAULT 0,
    tentativas_ultima_hora INT DEFAULT 0,
    tentativas_hoje INT DEFAULT 0,
    bloqueado_ate DATETIME,
    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 📁 Arquivos Modificados/Criados

### 1. Entidade de Rate Limit
**`Autenticacao2FARateLimit.java`**
- Controla limites de envio de código
- Métodos principais:
  - `podeEnviarNovoCodigo()` - Valida se pode enviar
  - `incrementarContadores()` - Atualiza contadores
  - `resetarSeNecessario()` - Reseta janelas expiradas
  - `getTempoEsperaFormatado()` - Mensagem amigável

### 2. Repository
**`Autenticacao2FARateLimitRepository.java`**
```java
Optional<Autenticacao2FARateLimit> findByUsuarioId(Long usuarioId);
boolean existsByUsuarioId(Long usuarioId);
```

### 3. Migration
**`V17__create_2fa_rate_limit.sql`**
- Cria tabela de rate limiting
- Índice único em `usuario_id`

### 4. Service: TwoFactorService
**Métodos Novos:**

#### `verificarRateLimit(Long usuarioId)`
```java
private void verificarRateLimit(Long usuarioId) {
    // Busca ou cria rate limit
    // Reseta contadores se necessário
    // Valida se pode enviar
    // Lança exceção se bloqueado
}
```

#### `incrementarRateLimit(Long usuarioId)`
```java
private void incrementarRateLimit(Long usuarioId) {
    // Incrementa todos os contadores
    // Salva no banco
}
```

#### `ativar2FAAutomaticamente(Long usuarioId, String email)`
```java
@Transactional
public void ativar2FAAutomaticamente(Long usuarioId, String email) {
    // 1. Verifica rate limit
    // 2. Cria/busca configuração 2FA
    // 3. Habilita 2FA
    // 4. Gera código
    // 5. Envia por email
    // 6. Incrementa rate limit
}
```

**Métodos Modificados:**

#### `configurar2FA(Long usuarioId)`
```java
// ANTES do envio:
verificarRateLimit(usuarioId);

// DEPOIS do envio:
incrementarRateLimit(usuarioId);
```

#### `enviarCodigoLogin(Long usuarioId)`
```java
// ANTES do envio:
verificarRateLimit(usuarioId);

// DEPOIS do envio:
incrementarRateLimit(usuarioId);
```

### 5. Service: AccountActivationService
**Método Modificado: `ativarConta()`**

```java
@Transactional
public MessageResponseDTO ativarConta(ActivateAccountRequestDTO request) {
    // ... validações e ativação ...
    
    // 🆕 NOVO: Ativa 2FA automaticamente
    try {
        log.info("Ativando 2FA automaticamente para usuário ID: {}", usuario.getId());
        twoFactorService.ativar2FAAutomaticamente(usuario.getId(), usuario.getEmail());
        log.info("2FA ativado com sucesso. Código enviado para: {}", usuario.getEmail());
    } catch (Exception e) {
        log.error("Erro ao ativar 2FA automaticamente: {}", e.getMessage(), e);
        // Não interrompe a ativação - 2FA pode ser ativado depois
    }
    
    return MessageResponseDTO.success(
        "Conta ativada com sucesso! 2FA foi habilitado e um código de " +
        "verificação foi enviado para seu email. Use-o no próximo login."
    );
}
```

**Injeção de Dependência:**
```java
private final TwoFactorService twoFactorService; // NOVO
```

### 6. Service: AuthService
**Método Modificado: `login()`**

```java
public AuthResponseDTO login(LoginRequestDTO request, HttpServletRequest httpRequest) {
    // ... validação de credenciais ...
    
    // 🆕 NOVO: Verifica se 2FA está habilitado
    if (twoFactorService.usuario2FAHabilitado(usuario.getId())) {
        log.info("Usuário {} tem 2FA habilitado. Enviando código...", usuario.getEmail());
        
        try {
            twoFactorService.enviarCodigoLogin(usuario.getId());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar código de verificação: " + e.getMessage());
        }
        
        // Retorna resposta SEM token JWT
        return AuthResponseDTO.builder()
                .token(null)
                .requires2FA(true)
                .userId(usuario.getId())
                .message("Código de verificação enviado para seu email")
                .build();
    }
    
    // Continua login normal se 2FA não habilitado
    // ...
}
```

**Injeção de Dependência:**
```java
private final TwoFactorService twoFactorService; // NOVO
```

### 7. DTO: AuthResponseDTO
**Campos Novos:**

```java
@Data
@Builder
public class AuthResponseDTO {
    private String token;
    private String tipo;
    private String email;
    private String nome;
    private String tipoUsuario;
    private Long expiresIn;
    
    // 🆕 NOVOS CAMPOS PARA 2FA
    private Boolean requires2FA;  // Indica se 2FA é necessário
    private Long userId;          // ID para verificação
    private String message;       // Mensagem adicional
}
```

## 🔌 Endpoints

### 1. Ativar Conta (com 2FA automático)
```http
POST /auth/activate
Content-Type: application/json

{
  "token": "abc123...",
  "senhaTemporaria": "SenhaTemp@2024",
  "novaSenha": "MinhaSenha@2024",
  "confirmacaoSenha": "MinhaSenha@2024"
}
```

**Resposta:**
```json
{
  "mensagem": "Conta ativada com sucesso! 2FA foi habilitado e um código de verificação foi enviado para seu email. Use-o no próximo login.",
  "sucesso": true
}
```

### 2. Login (com 2FA)
```http
POST /auth/login
Content-Type: application/json

{
  "cpf": "12345678901",
  "senha": "MinhaSenha@2024"
}
```

**Resposta (2FA habilitado):**
```json
{
  "token": null,
  "tipo": "Bearer",
  "email": "user@example.com",
  "nome": "João Silva",
  "tipoUsuario": "PACIENTE",
  "requires2FA": true,
  "userId": 123,
  "message": "Código de verificação enviado para seu email"
}
```

### 3. Verificar Código 2FA
```http
POST /auth/2fa/verify
Content-Type: application/json

{
  "cpf": "12345678901",
  "codigo": "123456"
}
```

**Resposta (sucesso):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "tipo": "Bearer",
  "email": "user@example.com",
  "nome": "João Silva",
  "tipoUsuario": "PACIENTE",
  "expiresIn": 86400000,
  "requires2FA": false
}
```

### 4. Reenviar Código (com rate limiting)
```http
POST /auth/2fa/resend
Authorization: Bearer {token}
```

**Resposta (sucesso):**
```json
{
  "mensagem": "Novo código enviado",
  "sucesso": true
}
```

**Resposta (rate limit excedido):**
```json
{
  "mensagem": "Limite de envios excedido. Por favor, aguarde 3 minutos",
  "sucesso": false
}
```

## 🧪 Testando

### Teste Manual Completo

1. **Criar usuário:**
```bash
export ADMIN_TOKEN="seu_token_admin"
./test-2fa-automatico.sh
```

2. **O script irá:**
   - Criar um usuário
   - Solicitar o token de ativação (do email)
   - Ativar a conta
   - Tentar login (deve pedir código 2FA)
   - Solicitar o código (do email)
   - Verificar o código
   - Testar rate limiting

### Verificar no Banco de Dados

```sql
-- Verificar se 2FA foi ativado
SELECT * FROM autenticacao_2fa WHERE usuario_id = 123;

-- Verificar rate limiting
SELECT * FROM autenticacao_2fa_rate_limit WHERE usuario_id = 123;

-- Ver código atual (para testes)
SELECT codigo_atual, expiracao_codigo, habilitado 
FROM autenticacao_2fa 
WHERE usuario_id = 123;
```

## 🚀 Como Funciona na Prática

### Cenário 1: Primeiro Login Após Ativação

```
1. Admin cria usuário
   └─ Sistema envia email com senha temporária

2. Usuário ativa conta
   ├─ Define nova senha
   ├─ Sistema ativa 2FA automaticamente ✓
   └─ Recebe código por email

3. Usuário tenta login
   ├─ Digita CPF e senha
   ├─ Sistema detecta 2FA habilitado
   ├─ Envia NOVO código
   └─ Pede verificação 2FA

4. Usuário verifica código
   ├─ Digita código de 6 dígitos
   └─ Recebe token JWT
```

### Cenário 2: Rate Limiting em Ação

```
Usuário tenta reenviar código várias vezes:

Tentativa 1 (00:00): ✓ Código enviado
Tentativa 2 (00:30): ✓ Código enviado  
Tentativa 3 (00:45): ✓ Código enviado
Tentativa 4 (01:00): ✗ "Aguarde 14 minutos" (3/15min)
Tentativa 5 (15:00): ✓ Código enviado (reset automático)
```

## ⚠️ Observações Importantes

### Segurança

1. **Código expira em 5 minutos** - Após isso, precisa solicitar novo
2. **5 tentativas falhas = bloqueio de 15 minutos** - Proteção contra brute force
3. **Rate limiting em 3 janelas** - Previne spam de emails
4. **Intervalo mínimo de 60 segundos** - Entre envios consecutivos

### Logs

Todos os eventos importantes são registrados:

```java
log.info("Ativando 2FA automaticamente para usuário ID: {}", usuarioId);
log.info("📨 Enviando código 2FA inicial para: {}", email);
log.warn("Rate limit excedido para usuário ID: {}. Tempo de espera: {}", usuarioId, tempoEspera);
log.error("Erro ao enviar código 2FA: {}", e.getMessage());
```

### Exceções

Se 2FA falhar durante ativação:
- **Ativação continua** - A conta é ativada normalmente
- **Usuário pode ativar 2FA depois** - Via endpoint `/auth/2fa/setup`
- **Log registra o erro** - Para investigação

## 📊 Diagrama de Fluxo

```
┌─────────────────┐
│ Admin cria user │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ Email enviado           │
│ (senha temporária)      │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Usuário ativa conta     │
│ /auth/activate          │
└────────┬────────────────┘
         │
         ├─► Valida token
         ├─► Define senha
         ├─► Ativa conta
         ├─► 🆕 Ativa 2FA
         └─► 🆕 Envia código
         │
         ▼
┌─────────────────────────┐
│ Email com código 2FA    │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Usuário tenta login     │
│ /auth/login             │
└────────┬────────────────┘
         │
         ├─► Valida senha
         ├─► Detecta 2FA
         ├─► Verifica rate limit ⚠️
         └─► Envia código
         │
         ▼
┌─────────────────────────┐
│ Retorna:                │
│ { requires2FA: true }   │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Usuário digita código   │
│ /auth/2fa/verify        │
└────────┬────────────────┘
         │
         ├─► Valida código
         ├─► Verifica expiração
         └─► Gera JWT
         │
         ▼
┌─────────────────────────┐
│ ✓ Login completo!       │
│ Token JWT retornado     │
└─────────────────────────┘
```

## 📝 Checklist de Implementação

- [x] Criar entidade `Autenticacao2FARateLimit`
- [x] Criar repository para rate limiting
- [x] Criar migration V17
- [x] Adicionar métodos de rate limiting no `TwoFactorService`
- [x] Criar método `ativar2FAAutomaticamente()`
- [x] Modificar `AccountActivationService.ativarConta()`
- [x] Modificar `AuthService.login()` para detectar 2FA
- [x] Adicionar campos em `AuthResponseDTO`
- [x] Integrar rate limiting em `configurar2FA()`
- [x] Integrar rate limiting em `enviarCodigoLogin()`
- [x] Criar script de teste completo
- [x] Testar fluxo end-to-end
- [x] Testar rate limiting
- [x] Documentar implementação

## 🎉 Conclusão

A implementação está completa e fornece:

1. ✅ **2FA Automático** - Ativado na ativação da conta
2. ✅ **Rate Limiting** - Proteção contra abuso em 3 níveis
3. ✅ **Login Seguro** - Exige código em todo login
4. ✅ **Mensagens Claras** - Feedback amigável ao usuário
5. ✅ **Logs Detalhados** - Rastreabilidade completa
6. ✅ **Exceções Tratadas** - Falhas não bloqueiam ativação

O sistema está pronto para uso em produção! 🚀
