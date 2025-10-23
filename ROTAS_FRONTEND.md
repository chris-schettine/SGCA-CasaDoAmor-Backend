# 🔗 Rotas do Frontend - Integração com Backend

Este documento lista todas as rotas que o frontend deve implementar para funcionar corretamente com os links enviados por email pelo backend.

## 📧 Links Enviados por Email

### 1. Ativação de Conta
**Email enviado quando:** Admin cria um novo usuário

**Link gerado pelo backend:**
```
http://localhost:5173/activate-account?token={token}
```

**Dados no email:**
- Nome do usuário
- Email
- Senha temporária
- Link de ativação (expira em 24 horas)

**Rota do frontend deve:**
- Capturar o `token` da query string
- Exibir formulário solicitando:
  - Senha temporária (fornecida no email)
  - Nova senha
  - Confirmação da nova senha
- Fazer POST para `/auth/activate-account` com:
  ```json
  {
    "token": "...",
    "senhaTemporaria": "...",
    "novaSenha": "..."
  }
  ```

**Endpoint do backend:**
```
POST /auth/activate-account
```

---

### 2. Redefinição de Senha (Esqueci minha senha)
**Email enviado quando:** Usuário clica em "Esqueci minha senha"

**Link gerado pelo backend:**
```
http://localhost:5173/reset-password?token={resetToken}
```

**Dados no email:**
- Link para redefinir senha (expira em 1 hora)

**Rota do frontend deve:**
- Capturar o `token` da query string
- Exibir formulário solicitando:
  - Nova senha
  - Confirmação da nova senha
- Fazer POST para `/auth/reset-password` com:
  ```json
  {
    "token": "...",
    "novaSenha": "..."
  }
  ```

**Endpoint do backend:**
```
POST /auth/reset-password
```

---

### 3. Verificação de Email
**Email enviado quando:** Usuário se registra ou solicita verificação

**Não usa link, apenas código:**
- Email contém código de 6 dígitos
- Código expira em 15 minutos

**Rota do frontend deve:**
- Exibir campo para inserir código
- Fazer POST para `/auth/verify-email` com:
  ```json
  {
    "email": "usuario@email.com",
    "codigo": "123456"
  }
  ```

**Endpoint do backend:**
```
POST /auth/verify-email
```

---

### 4. Autenticação 2FA
**Email enviado quando:** Usuário com 2FA habilitado faz login

**Não usa link, apenas código:**
- Email contém código de 6 dígitos
- Código expira em 5 minutos

**Rota do frontend deve:**
- Após login com CPF e senha, se `requires2FA: true`:
- Redirecionar para página de 2FA
- Exibir campo para inserir código
- Fazer POST para `/auth/2fa/verify` com:
  ```json
  {
    "cpf": "12345678900",
    "codigo": "123456"
  }
  ```

**Endpoint do backend:**
```
POST /auth/2fa/verify
```

---

## 🛣️ Rotas Recomendadas do Frontend

### Páginas Públicas (Não Autenticadas)
```
/login                    # Login com CPF e senha
/register                 # Registro de novo usuário
/forgot-password          # Solicitar recuperação de senha
/reset-password           # Redefinir senha (com token na URL)
/activate-account         # Ativar conta (com token na URL)
/verify-email             # Verificar email (inserir código)
/2fa-verify               # Verificar código 2FA
```

### Páginas Autenticadas
```
/dashboard                # Dashboard principal
/profile                  # Perfil do usuário
/change-password          # Alterar senha (usuário logado)
/2fa-setup                # Configurar 2FA
/sessions                 # Gerenciar sessões ativas
/pacientes                # Listar pacientes
/pacientes/novo           # Cadastrar paciente
/pacientes/:id            # Detalhes do paciente
/admin                    # Painel administrativo (apenas ADMIN)
/admin/usuarios           # Gerenciar usuários (apenas ADMIN)
```

---

## 📝 Exemplos de Implementação

### React Router (exemplo)
```tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Rotas públicas */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/activate-account" element={<ActivateAccountPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/2fa-verify" element={<TwoFactorVerifyPage />} />
        
        {/* Rotas autenticadas */}
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
        {/* ... outras rotas */}
      </Routes>
    </BrowserRouter>
  );
}
```

### Exemplo: Página de Ativação de Conta
```tsx
// ActivateAccountPage.tsx
import { useSearchParams } from 'react-router-dom';
import { useState } from 'react';

export function ActivateAccountPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  
  const [senhaTemporaria, setSenhaTemporaria] = useState('');
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');

  const handleActivate = async () => {
    const response = await fetch('http://localhost:8080/auth/activate-account', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        token,
        senhaTemporaria,
        novaSenha
      })
    });
    
    if (response.ok) {
      // Redirecionar para login
      window.location.href = '/login';
    }
  };

  return (
    <div>
      <h1>Ativar Conta</h1>
      <input 
        type="password" 
        placeholder="Senha temporária (do email)"
        value={senhaTemporaria}
        onChange={(e) => setSenhaTemporaria(e.target.value)}
      />
      <input 
        type="password" 
        placeholder="Nova senha"
        value={novaSenha}
        onChange={(e) => setNovaSenha(e.target.value)}
      />
      <input 
        type="password" 
        placeholder="Confirmar senha"
        value={confirmarSenha}
        onChange={(e) => setConfirmarSenha(e.target.value)}
      />
      <button onClick={handleActivate}>Ativar Conta</button>
    </div>
  );
}
```

---

## ✅ Checklist de Integração

- [ ] Rota `/activate-account` implementada
- [ ] Rota `/reset-password` implementada
- [ ] Rota `/verify-email` implementada
- [ ] Rota `/2fa-verify` implementada
- [ ] Validação de senhas no frontend (mínimo 8 caracteres, maiúscula, minúscula, número, especial)
- [ ] Tratamento de erros (token expirado, inválido, etc.)
- [ ] Feedback visual para usuário (loading, sucesso, erro)
- [ ] Redirecionamentos após ações bem-sucedidas
- [ ] Teste de integração completo (email → link → ativação/reset)

---

## 🔧 Variável de Ambiente no Frontend

Configure a URL base da API no seu frontend:

```env
# .env (frontend)
VITE_API_URL=http://localhost:8080
# ou para produção:
# VITE_API_URL=https://api.sgca.casadoamor.com.br
```

E use no código:
```tsx
const API_URL = import.meta.env.VITE_API_URL;

fetch(`${API_URL}/auth/activate-account`, { ... });
```

---

**Atualizado em:** 23/10/2025
