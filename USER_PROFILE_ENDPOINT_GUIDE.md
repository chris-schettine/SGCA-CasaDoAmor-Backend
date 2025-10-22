# 👤 User Profile Endpoint - Quick Reference

## 📍 Endpoint Created

### GET `/auth/me` - Get Current User Profile

Returns the complete profile data of the authenticated user based on the JWT token.

---

## 🔐 Authentication Required

**Yes** - This endpoint requires a valid JWT Bearer token.

---

## 📝 How to Use

### 1. **Login First**
```bash
curl -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "00000000000",
    "senha": "Admin@123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tipo": "Bearer",
  "email": "admin@casadoamor.com",
  "nome": "Admin Sistema",
  "tipoUsuario": "ADMINISTRADOR",
  "expiresIn": 3600000
}
```

### 2. **Get User Profile**
```bash
curl -X GET http://localhost:8090/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": 1,
  "uuid": "872831f7-af78-11f0-b26d-6ac9e63d98cb",
  "nome": "Admin Sistema",
  "email": "admin@casadoamor.com",
  "cpf": "00000000000",
  "telefone": "(77) 99999-9999",
  "tipo": "ADMINISTRADOR",
  "ativo": true,
  "emailVerificado": true,
  "ultimoLoginEm": "2025-10-22T21:37:22",
  "criadoEm": "2025-10-22T18:54:24",
  "atualizadoEm": "2025-10-22T21:37:22",
  "perfis": []
}
```

---

## 📊 Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | User's database ID |
| `uuid` | String | User's unique identifier (UUID) |
| `nome` | String | User's full name |
| `email` | String | User's email address |
| `cpf` | String | User's CPF |
| `telefone` | String | User's phone number |
| `tipo` | String | User type (ADMINISTRADOR, MEDICO, etc.) |
| `ativo` | Boolean | Whether user is active |
| `emailVerificado` | Boolean | Whether email is verified |
| `ultimoLoginEm` | DateTime | Last login timestamp |
| `criadoEm` | DateTime | Account creation timestamp |
| `atualizadoEm` | DateTime | Last update timestamp |
| `perfis` | Array | User's roles/profiles with permissions |

---

## 🎯 Use Cases

### Frontend Display
Use this endpoint to display user information in:
- **Navigation bar** (name, avatar, role)
- **User profile page** (all user details)
- **Dashboard header** (welcome message)
- **Settings page** (editable profile fields)

### Example Frontend Integration (React/Vue/Angular)

```javascript
// After login, store token
localStorage.setItem('token', response.token);

// Fetch user profile
async function getUserProfile() {
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8090/auth/me', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const user = await response.json();
  
  // Display in UI
  document.getElementById('user-name').textContent = user.nome;
  document.getElementById('user-email').textContent = user.email;
  document.getElementById('user-type').textContent = user.tipo;
}
```

---

## 🔄 Complete Flow

```
1. User enters CPF + Password
   ↓
2. POST /auth/login
   ↓
3. Receive JWT Token
   ↓
4. Store token (localStorage, sessionStorage, cookies)
   ↓
5. GET /auth/me with Authorization header
   ↓
6. Receive user profile data
   ↓
7. Display user info in system area
```

---

## ⚡ Using with the Token Script

```bash
# Generate token
./get-token.sh

# Token is saved to /tmp/sgca_token.txt
TOKEN=$(cat /tmp/sgca_token.txt)

# Get user profile
curl -X GET "http://localhost:8090/auth/me" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

## 🛡️ Security

- **Token Required:** Yes - endpoint is protected
- **Auto-extraction:** User CPF is extracted from JWT token automatically
- **No parameters needed:** User is identified by their token
- **Permissions:** Any authenticated user can access their own profile

---

## 📖 API Documentation (Swagger)

This endpoint is documented in Swagger UI:
```
http://localhost:8090/swagger-ui/index.html
```

Look for:
- **Tag:** Autenticação
- **Operation:** GET /auth/me
- **Summary:** "Obter perfil do usuário"

---

## 🔍 Error Responses

### 401 Unauthorized
```json
{
  "message": "Token inválido ou não fornecido",
  "timestamp": 1761169042000
}
```
**Cause:** No token provided or invalid token

### 404 Not Found
```json
{
  "message": "Usuário não encontrado",
  "timestamp": 1761169042000
}
```
**Cause:** User doesn't exist in database (rare - token would be invalid first)

---

## 💡 Pro Tips

### 1. Cache User Data
After fetching user profile, cache it in your frontend state management (Redux, Vuex, etc.)

### 2. Refresh on Token Change
Refetch user profile after:
- Login
- Profile update
- Token refresh

### 3. Display User Avatar
If you add photo URL support later:
```javascript
<img src={user.fotoUrl || '/default-avatar.png'} alt={user.nome} />
```

### 4. Role-Based UI
```javascript
if (user.tipo === 'ADMINISTRADOR') {
  // Show admin menu items
} else if (user.tipo === 'MEDICO') {
  // Show doctor menu items
}
```

---

## 📋 Quick Test Commands

### Test with curl
```bash
# 1-liner: Login and get profile
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login -H "Content-Type: application/json" -d '{"cpf":"00000000000","senha":"Admin@123"}' | jq -r '.token') && curl -s -X GET "http://localhost:8090/auth/me" -H "Authorization: Bearer $TOKEN" | jq .
```

### Test with Swagger
1. Open: http://localhost:8090/swagger-ui/index.html
2. Click "Authorize" 🔒
3. Paste your JWT token
4. Click "Authorize"
5. Find GET /auth/me
6. Click "Try it out"
7. Click "Execute"

---

## ✅ Summary

**Endpoint:** `GET /auth/me`  
**Purpose:** Get authenticated user's profile data  
**Auth:** Bearer JWT Token (required)  
**Use Case:** Display user info in system area after login  
**Response:** Complete user profile with roles and permissions  

---

**Last Updated:** October 22, 2025  
**Status:** ✅ Implemented and Tested
