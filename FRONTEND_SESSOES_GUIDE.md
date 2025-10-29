# 🎨 Guia de Integração Frontend - Gerenciamento de Sessões

## 📋 Índice
- [Como o Backend Valida Sessões](#como-o-backend-valida-sessões)
- [Detecção Automática de Sessão Revogada](#detecção-automática-de-sessão-revogada)
- [Implementação no Frontend](#implementação-no-frontend)
- [Interceptors HTTP](#interceptors-http)
- [Tratamento de Erros](#tratamento-de-erros)
- [UX/UI Recomendado](#uxui-recomendado)
- [Exemplos Práticos](#exemplos-práticos)

---

## 🔒 Como o Backend Valida Sessões

### Fluxo de Validação

Toda requisição autenticada passa por 3 validações:

```
1. JWT válido? (assinatura correta + não expirado)
   ↓
2. Sessão ativa no banco? (ativo = true)
   ↓
3. Sessão não expirada? (expiraEm > agora)
   ↓
✅ Requisição autorizada
```

### O que acontece quando uma sessão é revogada?

```javascript
// Admin revoga a sessão
DELETE /admin/audit/sessions/42

// Backend marca no banco
UPDATE auth_sessoes_usuarios 
SET ativo = false 
WHERE id = 42

// Próxima requisição do usuário
GET /api/qualquer-endpoint
Authorization: Bearer eyJhbGciOiJIUzI1...

// Backend valida:
✅ JWT válido (assinatura OK, não expirado)
❌ Sessão inativa (ativo = false)

// Resposta:
HTTP 401 Unauthorized
{
  "error": "Sessão inválida ou expirada"
}
```

---

## 🚨 Detecção Automática de Sessão Revogada

### Erro HTTP 401

Quando o backend detecta sessão revogada, retorna:

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "error": "Unauthorized",
  "message": "Sessão inválida ou expirada",
  "timestamp": "2025-10-29T10:30:00"
}
```

### O frontend deve:

1. **Detectar 401** em qualquer requisição
2. **Limpar localStorage/sessionStorage** (token, user, etc.)
3. **Redirecionar para login** com mensagem apropriada
4. **Cancelar requisições pendentes** (se houver)

---

## 💻 Implementação no Frontend

### React + Axios

#### 1. Configurar Interceptor Global

```typescript
// src/services/api.ts
import axios from 'axios';
import { logout } from './auth';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para adicionar token em todas as requisições
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para detectar sessão revogada (401)
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Sessão revogada ou token inválido
      logout(); // Limpa storage e redireciona
      
      // Opcional: mostrar toast/notificação
      showNotification({
        type: 'warning',
        title: 'Sessão Encerrada',
        message: 'Sua sessão foi encerrada. Por favor, faça login novamente.',
      });
    }
    return Promise.reject(error);
  }
);

export default api;
```

#### 2. Função de Logout

```typescript
// src/services/auth.ts
export const logout = () => {
  // 1. Limpar armazenamento local
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  localStorage.removeItem('refreshToken'); // se houver
  
  // 2. Limpar estado global (Redux, Context, Zustand, etc.)
  // store.dispatch(clearAuth());
  
  // 3. Redirecionar para login
  window.location.href = '/login?reason=session_expired';
};
```

#### 3. Página de Login com Mensagens Contextuais

```typescript
// src/pages/Login.tsx
import { useSearchParams } from 'react-router-dom';

export const Login = () => {
  const [searchParams] = useSearchParams();
  const reason = searchParams.get('reason');
  
  const getMessage = () => {
    switch (reason) {
      case 'session_expired':
        return {
          type: 'warning',
          text: 'Sua sessão expirou. Por favor, faça login novamente.',
        };
      case 'session_revoked':
        return {
          type: 'error',
          text: 'Sua sessão foi encerrada por um administrador. Entre em contato com o suporte.',
        };
      case 'logout':
        return {
          type: 'info',
          text: 'Você saiu com sucesso.',
        };
      default:
        return null;
    }
  };
  
  const message = getMessage();
  
  return (
    <div>
      {message && (
        <Alert type={message.type}>
          {message.text}
        </Alert>
      )}
      {/* Formulário de login */}
    </div>
  );
};
```

---

### Vue 3 + Axios

```typescript
// src/plugins/axios.ts
import axios from 'axios';
import router from '@/router';
import { useAuthStore } from '@/stores/auth';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      authStore.logout();
      
      router.push({
        name: 'login',
        query: { reason: 'session_expired' }
      });
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

### Angular

```typescript
// src/app/interceptors/auth.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // Adicionar token
    const token = this.authService.getToken();
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Sessão revogada
          this.authService.logout();
          this.router.navigate(['/login'], {
            queryParams: { reason: 'session_expired' }
          });
        }
        return throwError(() => error);
      })
    );
  }
}
```

---

## 🎯 Tratamento de Erros Específicos

### Diferenciar tipos de 401

```typescript
// src/utils/errorHandler.ts
export const handleAuthError = (error: AxiosError) => {
  if (error.response?.status === 401) {
    const message = error.response?.data?.message || '';
    
    if (message.includes('inválida') || message.includes('revogada')) {
      // Sessão revogada pelo admin
      logout('session_revoked');
      showNotification({
        type: 'error',
        title: 'Acesso Negado',
        message: 'Sua sessão foi encerrada. Entre em contato com o administrador.',
        duration: 10000, // 10 segundos
      });
    } else if (message.includes('expirada')) {
      // Token expirado naturalmente
      logout('session_expired');
      showNotification({
        type: 'info',
        title: 'Sessão Expirada',
        message: 'Sua sessão expirou. Faça login novamente.',
      });
    } else {
      // Erro genérico de autenticação
      logout('auth_error');
      showNotification({
        type: 'warning',
        title: 'Erro de Autenticação',
        message: 'Ocorreu um erro. Por favor, faça login novamente.',
      });
    }
  }
};
```

---

## 🎨 UX/UI Recomendado

### 1. Notificação Toast ao Detectar 401

```typescript
// Biblioteca: react-hot-toast, sonner, etc.
import toast from 'react-hot-toast';

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      toast.error('Sua sessão foi encerrada', {
        duration: 5000,
        icon: '🔒',
        position: 'top-center',
      });
      
      // Aguardar 1 segundo antes de redirecionar
      setTimeout(() => {
        logout();
      }, 1000);
    }
    return Promise.reject(error);
  }
);
```

### 2. Modal de Alerta (UX mais invasiva)

```typescript
// Para casos onde o usuário estava preenchendo um formulário
if (error.response?.status === 401) {
  showModal({
    title: '⚠️ Sessão Encerrada',
    message: 'Sua sessão foi encerrada. Faça login novamente para continuar.',
    confirmText: 'Ir para Login',
    cancelable: false,
    onConfirm: () => {
      logout();
    }
  });
}
```

### 3. Página de Transição

```tsx
// src/pages/SessionExpired.tsx
export const SessionExpired = () => {
  const navigate = useNavigate();
  const [countdown, setCountdown] = useState(5);
  
  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev === 1) {
          navigate('/login');
        }
        return prev - 1;
      });
    }, 1000);
    
    return () => clearInterval(timer);
  }, [navigate]);
  
  return (
    <div className="session-expired-page">
      <LockIcon size={64} />
      <h1>Sessão Encerrada</h1>
      <p>
        Sua sessão foi encerrada por motivos de segurança.
        <br />
        Você será redirecionado para o login em {countdown} segundos...
      </p>
      <Button onClick={() => navigate('/login')}>
        Fazer Login Agora
      </Button>
    </div>
  );
};
```

---

## 🔄 Verificação Proativa de Sessão

### Polling Periódico (Opcional)

Em vez de esperar um 401, você pode verificar periodicamente se a sessão ainda é válida:

```typescript
// src/hooks/useSessionCheck.ts
import { useEffect } from 'react';
import api from '@/services/api';

export const useSessionCheck = () => {
  useEffect(() => {
    // Verificar a cada 5 minutos
    const interval = setInterval(async () => {
      try {
        // Endpoint leve que só verifica autenticação
        await api.get('/auth/me');
      } catch (error) {
        // Se falhar, sessão foi revogada
        if (error.response?.status === 401) {
          logout('session_revoked');
        }
      }
    }, 5 * 60 * 1000); // 5 minutos
    
    return () => clearInterval(interval);
  }, []);
};

// Usar no componente raiz (App.tsx)
function App() {
  useSessionCheck();
  
  return (
    <Router>
      {/* ... */}
    </Router>
  );
}
```

### WebSocket para Notificação em Tempo Real (Avançado)

```typescript
// Backend envia evento quando sessão é revogada
// Frontend escuta e desloga imediatamente

// src/services/websocket.ts
const ws = new WebSocket('ws://localhost:8080/ws');

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  
  if (data.type === 'SESSION_REVOKED') {
    showNotification({
      type: 'error',
      title: 'Sessão Encerrada',
      message: 'Sua sessão foi encerrada por um administrador.',
    });
    
    logout('session_revoked');
  }
};
```

---

## 🧪 Exemplos Práticos

### Exemplo Completo: React + TypeScript + Axios

```typescript
// src/services/api.ts
import axios, { AxiosError } from 'axios';
import { toast } from 'sonner';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

// Adicionar token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Detectar sessão revogada
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<{ message: string }>) => {
    if (error.response?.status === 401) {
      // Limpar storage
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // Notificar usuário
      const message = error.response.data?.message;
      
      if (message?.includes('revogada')) {
        toast.error('Sua sessão foi encerrada por um administrador', {
          duration: 8000,
        });
      } else {
        toast.warning('Sessão expirada. Faça login novamente', {
          duration: 5000,
        });
      }
      
      // Redirecionar
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

export default api;
```

### Exemplo: Componente de Gerenciamento de Sessões

```tsx
// src/components/SessionManager.tsx
import { useEffect, useState } from 'react';
import api from '@/services/api';

interface Session {
  id: number;
  ipOrigem: string;
  userAgent: string;
  criadoEm: string;
  atual: boolean;
}

export const SessionManager = () => {
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(false);
  
  const loadSessions = async () => {
    try {
      const { data } = await api.get('/auth/sessions');
      setSessions(data);
    } catch (error) {
      // Erro já tratado pelo interceptor
    }
  };
  
  const revokeSession = async (sessionId: number) => {
    if (!confirm('Deseja realmente encerrar esta sessão?')) {
      return;
    }
    
    setLoading(true);
    try {
      await api.delete(`/auth/sessions/${sessionId}`);
      
      toast.success('Sessão encerrada com sucesso');
      
      // Recarregar lista
      loadSessions();
    } catch (error) {
      toast.error('Erro ao encerrar sessão');
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    loadSessions();
  }, []);
  
  return (
    <div className="sessions-container">
      <h2>Minhas Sessões Ativas</h2>
      
      {sessions.map((session) => (
        <div key={session.id} className="session-card">
          <div className="session-info">
            <strong>{session.atual ? '🟢 Sessão Atual' : '🔵 Outra Sessão'}</strong>
            <p>IP: {session.ipOrigem}</p>
            <p>Dispositivo: {parseUserAgent(session.userAgent)}</p>
            <p>Iniciada em: {formatDate(session.criadoEm)}</p>
          </div>
          
          {!session.atual && (
            <button
              onClick={() => revokeSession(session.id)}
              disabled={loading}
              className="btn-danger"
            >
              Encerrar Sessão
            </button>
          )}
        </div>
      ))}
    </div>
  );
};
```

---

## 📱 Casos de Uso Mobile (React Native)

```typescript
// src/services/api.ts (React Native)
import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { NavigationRef } from '@react-navigation/native';
import { Alert } from 'react-native';

const api = axios.create({
  baseURL: 'http://localhost:8080',
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Limpar storage
      await AsyncStorage.multiRemove(['token', 'user']);
      
      // Mostrar alerta nativo
      Alert.alert(
        'Sessão Encerrada',
        'Sua sessão foi encerrada. Faça login novamente.',
        [
          {
            text: 'OK',
            onPress: () => {
              // Navegar para login
              NavigationRef.navigate('Login');
            }
          }
        ]
      );
    }
    
    return Promise.reject(error);
  }
);
```

---

## ✅ Checklist de Implementação

### Backend (✅ Já implementado)
- [x] Validação de sessão no filtro JWT
- [x] Endpoint para revogar sessões
- [x] Retorno 401 quando sessão inválida

### Frontend (A implementar)
- [ ] Interceptor HTTP para detectar 401
- [ ] Função de logout que limpa storage
- [ ] Redirecionamento automático para login
- [ ] Notificação visual ao usuário
- [ ] Mensagem contextual na página de login
- [ ] Componente de gerenciamento de sessões
- [ ] (Opcional) Verificação periódica de sessão
- [ ] (Opcional) WebSocket para notificação em tempo real

---

## 🎯 Resumo

### O que acontece no fluxo completo:

1. **Admin revoga sessão** (`DELETE /admin/audit/sessions/42`)
2. **Backend marca sessão como inativa** no banco
3. **Usuário faz próxima requisição** (ex: carregar dashboard)
4. **Backend valida** → JWT OK ✅ + Sessão INATIVA ❌
5. **Backend retorna 401** com mensagem
6. **Interceptor do frontend detecta 401**
7. **Frontend limpa storage** (token, user)
8. **Frontend mostra notificação** ("Sessão encerrada")
9. **Frontend redireciona para login**
10. **Usuário vê mensagem** explicando o que aconteceu

### Código mínimo necessário:

```typescript
// Adicionar em api.ts
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

✅ **Com isso, o frontend automaticamente detecta e trata sessões revogadas!**

---

**Data:** 29/10/2025  
**Versão:** 1.0.0  
**Status:** ✅ Implementado e Documentado
