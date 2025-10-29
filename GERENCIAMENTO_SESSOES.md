# 🔐 Gerenciamento de Sessões JWT - SGCA Backend

## 📋 Índice
- [Visão Geral](#visão-geral)
- [Endpoints Disponíveis](#endpoints-disponíveis)
- [Permissões e Controle de Acesso](#permissões-e-controle-de-acesso)
- [Casos de Uso](#casos-de-uso)
- [Como Funciona o Logout](#como-funciona-o-logout)
- [Segurança e Auditoria](#segurança-e-auditoria)

---

## 🎯 Visão Geral

O sistema implementa **gerenciamento completo de sessões JWT** com as seguintes capacidades:

✅ **Rastreamento de sessões** - Cada login cria uma sessão registrada no banco  
✅ **Multi-dispositivo** - Usuários podem ter múltiplas sessões ativas simultaneamente  
✅ **Revogação granular** - Deslogar de dispositivos específicos ou todos de uma vez  
✅ **Controle administrativo** - Admins podem revogar sessões de qualquer usuário  
✅ **Auditoria completa** - Histórico de todas as sessões (ativas e revogadas)  

---

## 📡 Endpoints Disponíveis

### 👤 Endpoints de Usuário (Qualquer Role)

#### 1. Listar Minhas Sessões
```http
GET /auth/sessions
Authorization: Bearer {token}
```

**Resposta:**
```json
[
  {
    "id": 1,
    "ipOrigem": "192.168.1.100",
    "userAgent": "Mozilla/5.0 (Windows NT 10.0...",
    "criadoEm": "2025-10-29T10:00:00",
    "expiraEm": "2025-10-29T11:00:00",
    "ativo": true,
    "atual": true,
    "usuario": {
      "id": 5,
      "nome": "João Silva",
      "email": "joao@email.com",
      "cpf": "12345678900",
      "tipo": "RECEPCIONISTA"
    }
  },
  {
    "id": 2,
    "ipOrigem": "192.168.1.200",
    "userAgent": "Mozilla/5.0 (Android...",
    "criadoEm": "2025-10-29T09:30:00",
    "expiraEm": "2025-10-29T10:30:00",
    "ativo": true,
    "atual": false,
    "usuario": { ... }
  }
]
```

**Campo `atual`:**
- `true` = Sessão que você está usando agora (token atual)
- `false` = Outras sessões suas (outros dispositivos)

---

#### 2. Revogar Minha Sessão Específica
```http
DELETE /auth/sessions/{id}
Authorization: Bearer {token}
```

**Comportamento:**
- ✅ Se `{id}` = sessão de outro dispositivo → **Desloga APENAS aquele dispositivo**
- ⚠️ Se `{id}` = sessão atual → **Você será deslogado imediatamente**

**Exemplo:**
```bash
# Ver minhas sessões
curl -X GET "http://localhost:8080/auth/sessions" \
  -H "Authorization: Bearer $TOKEN" | jq .

# Deslogar do celular (sessão ID 2)
curl -X DELETE "http://localhost:8080/auth/sessions/2" \
  -H "Authorization: Bearer $TOKEN"

# ✅ Continuo logado no computador (sessão ID 1)
```

**Resposta de sucesso:**
```json
{
  "success": true,
  "message": "Sessão revogada com sucesso"
}
```

**Erros possíveis:**
- **404** - Sessão não encontrada
- **403** - Sessão não pertence a você
- **401** - Token inválido/expirado

---

### 🛡️ Endpoints Administrativos (ADMINISTRADOR apenas)

#### 3. Ver Todas as Sessões do Sistema
```http
GET /admin/audit/sessions
Authorization: Bearer {admin_token}
```

**Permissão:** ADMINISTRADOR ou AUDITOR

**Resposta:**
```json
{
  "totalSessoes": 15,
  "sessoes": [
    {
      "id": 1,
      "ipOrigem": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "criadoEm": "2025-10-29T10:00:00",
      "expiraEm": "2025-10-29T11:00:00",
      "ativo": true,
      "atual": false,
      "usuario": {
        "id": 5,
        "nome": "João Silva",
        "email": "joao@email.com",
        "cpf": "12345678900",
        "tipo": "RECEPCIONISTA"
      }
    }
    // ... mais 14 sessões
  ]
}
```

---

#### 4. Revogar Sessão Específica de Qualquer Usuário
```http
DELETE /admin/audit/sessions/{id}
Authorization: Bearer {admin_token}
```

**Permissão:** ADMINISTRADOR apenas

**Comportamento:**
- Admin pode revogar **qualquer sessão** do sistema
- Usuário será deslogado **imediatamente**
- Não há restrição de "só pode revogar próprias sessões"

**Exemplo:**
```bash
# Admin vê sessões ativas
curl -X GET "http://localhost:8080/admin/audit/sessions" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Admin identifica sessão suspeita (ID 42)
# IP estranho: 203.0.113.50 (Rússia)
# Usuário: Maria Silva (deveria estar no Brasil)

# Admin revoga a sessão suspeita
curl -X DELETE "http://localhost:8080/admin/audit/sessions/42" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Resposta:**
```json
{
  "success": true,
  "message": "Sessão revogada com sucesso. Usuário será deslogado na próxima requisição."
}
```

---

#### 5. Revogar TODAS as Sessões de um Usuário (Force Logout)
```http
DELETE /admin/audit/users/{userId}/sessions
Authorization: Bearer {admin_token}
```

**Permissão:** ADMINISTRADOR apenas

**Comportamento:**
- Revoga **todas as sessões** do usuário especificado
- Desloga de **todos os dispositivos** (computador, celular, tablet, etc.)
- Útil para **contas comprometidas** ou **bloqueio de segurança**

**Exemplo:**
```bash
# Admin força logout total do usuário ID 10
curl -X DELETE "http://localhost:8080/admin/audit/users/10/sessions" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Resposta:**
```json
{
  "success": true,
  "message": "Todas as sessões do usuário foram revogadas. Force logout realizado."
}
```

---

## 🔐 Permissões e Controle de Acesso

| Ação | Usuário Normal | Admin | Auditor |
|------|---------------|-------|---------|
| Ver minhas sessões | ✅ | ✅ | ✅ |
| Revogar minha sessão | ✅ | ✅ | ✅ |
| Ver sessões de outros | ❌ | ✅ | ✅ (somente leitura) |
| Revogar sessão de outros | ❌ | ✅ | ❌ |
| Force logout de usuário | ❌ | ✅ | ❌ |

**Importante:**
- **AUDITOR** pode apenas **visualizar** sessões (não pode revogar)
- **ADMINISTRADOR** tem controle total sobre todas as sessões
- **Usuários normais** só controlam suas próprias sessões

---

## 🎯 Casos de Uso

### 1️⃣ Usuário Normal: "Esqueci meu celular logado"

**Problema:** Você logou no celular de um amigo e esqueceu de fazer logout.

**Solução:**
```bash
# 1. Fazer login no computador
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"cpf":"12345678900","senha":"SuaSenha@123"}' | jq -r '.token')

# 2. Ver todas as suas sessões
curl -X GET "http://localhost:8080/auth/sessions" \
  -H "Authorization: Bearer $TOKEN" | jq .

# Resposta mostra:
# [
#   { "id": 1, "ipOrigem": "192.168.1.100", "atual": true },   ← Seu PC
#   { "id": 2, "ipOrigem": "192.168.1.200", "atual": false }   ← Celular do amigo
# ]

# 3. Revogar a sessão do celular
curl -X DELETE "http://localhost:8080/auth/sessions/2" \
  -H "Authorization: Bearer $TOKEN"

# ✅ Celular foi deslogado, você continua logado no PC
```

---

### 2️⃣ Admin: "Detectei atividade suspeita"

**Problema:** Sistema de monitoramento alertou login de IP estrangeiro para usuário que deveria estar no Brasil.

**Solução:**
```bash
# 1. Admin vê todas as sessões ativas
curl -X GET "http://localhost:8080/admin/audit/sessions" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# 2. Identificar sessão suspeita
# {
#   "id": 42,
#   "ipOrigem": "203.0.113.50",  ← IP da Rússia
#   "usuario": {
#     "nome": "Maria Silva",
#     "email": "maria@empresa.com.br"
#   }
# }

# 3. Revogar sessão suspeita imediatamente
curl -X DELETE "http://localhost:8080/admin/audit/sessions/42" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 4. Opcionalmente, bloquear TODAS as sessões da Maria
curl -X DELETE "http://localhost:8080/admin/audit/users/10/sessions" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 5. Entrar em contato com Maria para verificar
```

---

### 3️⃣ Admin: "Conta foi comprometida"

**Problema:** Usuário reportou que sua conta foi hackeada.

**Solução:**
```bash
# 1. Force logout total (deslogar de TODOS os dispositivos)
curl -X DELETE "http://localhost:8080/admin/audit/users/10/sessions" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 2. Resetar senha do usuário
curl -X POST "http://localhost:8080/admin/users/10/reset-password" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 3. Bloquear conta temporariamente (se necessário)
curl -X PATCH "http://localhost:8080/admin/users/10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ativo": false}'

# ✅ Hacker deslogado
# ✅ Senha antiga não funciona mais
# ✅ Conta bloqueada até usuário confirmar identidade
```

---

### 4️⃣ Admin: "Trocar senha de funcionário desligado"

**Problema:** Funcionário foi desligado mas ainda tem acesso ao sistema.

**Solução:**
```bash
# 1. Deslogar de todos os dispositivos
curl -X DELETE "http://localhost:8080/admin/audit/users/15/sessions" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 2. Desativar conta
curl -X PATCH "http://localhost:8080/admin/users/15" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ativo": false}'

# ✅ Ex-funcionário imediatamente deslogado
# ✅ Não consegue mais fazer login
```

---

## 🔄 Como Funciona o Logout

### Logout Normal vs Force Logout

| Tipo | Método | Escopo | Quando Usar |
|------|--------|--------|-------------|
| **Logout Normal** | `DELETE /auth/sessions/{id}` | Sessão específica | Deslogar de um dispositivo |
| **Force Logout** | `DELETE /admin/audit/users/{userId}/sessions` | Todas sessões | Emergência/segurança |

### O que acontece quando uma sessão é revogada?

1. **Marcação no banco:**
   ```sql
   UPDATE auth_sessoes_usuarios 
   SET ativo = false 
   WHERE id = ?
   ```

2. **Token JWT:**
   - Token **não é deletado** (JWT é stateless)
   - Token **continua válido** até expirar naturalmente
   - Mas sistema **valida no banco** antes de aceitar

3. **Próxima requisição:**
   ```
   Cliente → Request com token
   Servidor → Verifica JWT (válido ✅)
   Servidor → Consulta banco (sessão ativa? ❌)
   Servidor → Retorna 401 Unauthorized
   ```

4. **Cliente é forçado a fazer login novamente**

---

## 🔍 Segurança e Auditoria

### Logs Automáticos

Toda revogação de sessão gera logs:

```log
[INFO] Sessão 42 revogada para usuário ID: 10
[INFO] FORCE LOGOUT: Todas as sessões revogadas para usuário ID: 10
```

### Rastreamento no Banco

Tabela `auth_sessoes_usuarios` mantém **histórico completo**:

```sql
SELECT 
    id,
    usuario_id,
    ip_origem,
    criado_em,
    expira_em,
    ativo
FROM auth_sessoes_usuarios
WHERE usuario_id = 10
ORDER BY criado_em DESC;
```

Exemplo:
```
id | usuario_id | ip_origem      | criado_em           | expira_em           | ativo
---|------------|----------------|---------------------|---------------------|-------
42 | 10         | 203.0.113.50   | 2025-10-29 10:00:00 | 2025-10-29 11:00:00 | false ← Revogada
41 | 10         | 192.168.1.100  | 2025-10-29 09:30:00 | 2025-10-29 10:30:00 | false ← Revogada
40 | 10         | 192.168.1.100  | 2025-10-29 08:00:00 | 2025-10-29 09:00:00 | false ← Expirada
```

### Análise de Padrões Suspeitos

Admin pode usar endpoint de auditoria para detectar:

```bash
# Ver tentativas de login falhadas
curl -X GET "http://localhost:8080/admin/audit/logins?sucesso=false" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Ver sessões de IPs suspeitos
curl -X GET "http://localhost:8080/admin/audit/sessions" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.sessoes[] | select(.ipOrigem | startswith("203."))'
```

---

## 🚨 Alertas e Boas Práticas

### ⚠️ Cuidados ao Revogar Sessões

1. **Não revogue sua própria sessão admin por engano**
   - Se revogar `DELETE /auth/sessions/{sua_sessao_atual}`, você será deslogado
   - Sempre verifique o campo `atual: true` antes

2. **Force logout é irreversível**
   - `DELETE /admin/audit/users/{userId}/sessions` desloga **todos os dispositivos**
   - Usuário precisará fazer login novamente em **cada dispositivo**

3. **JWT continua válido tecnicamente**
   - Sessão revogada ≠ JWT invalidado
   - Sistema valida no banco antes de aceitar requisição
   - Segurança depende da validação correta no backend

### ✅ Recomendações

1. **Sempre confirme antes de force logout:**
   ```bash
   # Ver sessões do usuário antes de revogar
   curl -X GET "http://localhost:8080/admin/audit/sessions" \
     -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.sessoes[] | select(.usuario.id == 10)'
   ```

2. **Documente motivo da revogação:**
   - Mantenha log manual ou ticket de suporte
   - Útil para auditoria posterior

3. **Comunique o usuário:**
   - Se revogar sessões por segurança, avise o usuário
   - Explique por que ele foi deslogado

4. **Monitore logins após revogação:**
   - Verifique se usuário consegue fazer login novamente
   - Detecte se hacker tenta relogar

---

## 📞 Suporte

Para dúvidas sobre gerenciamento de sessões:
- 📧 Email: suporte@casadoamor.org.br
- 📖 Documentação: `/docs` (Swagger UI)
- 🔧 Logs: `/var/log/sgca-backend/`

---

**Data da Documentação:** 29/10/2025  
**Versão do Sistema:** 1.0.0  
**Status:** ✅ Implementado e Testado
