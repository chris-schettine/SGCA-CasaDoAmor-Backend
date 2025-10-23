# Guia de API: Gestão de Permissões para Frontend

## Visão Geral

Este documento descreve como o frontend deve consumir os endpoints de gestão de permissões para permitir que administradores atribuam permissões a perfis e usuários.

## Arquitetura de Permissões

O sistema usa um modelo hierárquico:
- **Permissões** → Ações específicas (ex: `USUARIOS_CRIAR`, `DADOS_CLINICOS_EDITAR`)
- **Perfis (Roles)** → Agrupamentos de permissões (ex: `ROLE_MEDICO`, `ROLE_RECEPCIONISTA`)
- **Usuários** → Possuem um ou mais perfis, herdando suas permissões

```
Usuário
  └─ Perfis (1 ou mais)
       └─ Permissões (0 ou mais)
```

## Endpoints Disponíveis

### 1. Listar Todas as Permissões

**GET** `/admin/permissions`

Retorna todas as permissões ativas cadastradas no sistema.

**Headers:**
```http
Authorization: Bearer <token>
```

**Resposta (200 OK):**
```json
[
  {
    "id": 1,
    "nome": "USUARIOS_CRIAR",
    "descricao": "Permite criar novos usuários"
  },
  {
    "id": 2,
    "nome": "USUARIOS_VER",
    "descricao": "Permite visualizar usuários"
  },
  {
    "id": 3,
    "nome": "USUARIOS_EDITAR",
    "descricao": "Permite editar usuários existentes"
  }
]
```

**Uso:** Use este endpoint para popular um checklist/multi-select de permissões disponíveis.

---

### 2. Listar Todos os Perfis

**GET** `/admin/roles`

Retorna todos os perfis ativos com suas permissões.

**Headers:**
```http
Authorization: Bearer <token>
```

**Resposta (200 OK):**
```json
[
  {
    "id": 1,
    "nome": "ROLE_ADMINISTRADOR",
    "descricao": "Administrador do sistema",
    "totalPermissoes": 25,
    "permissoes": [
      {
        "id": 1,
        "nome": "USUARIOS_CRIAR",
        "descricao": "Permite criar novos usuários"
      },
      {
        "id": 2,
        "nome": "USUARIOS_VER",
        "descricao": "Permite visualizar usuários"
      }
    ]
  },
  {
    "id": 2,
    "nome": "ROLE_RECEPCIONISTA",
    "descricao": "Recepcionista",
    "totalPermissoes": 5,
    "permissoes": [
      {
        "id": 2,
        "nome": "USUARIOS_VER",
        "descricao": "Permite visualizar usuários"
      }
    ]
  }
]
```

**Uso:** Exibir lista de perfis e suas permissões atuais.

---

### 3. Buscar Perfil por ID

**GET** `/admin/roles/{id}`

Retorna detalhes de um perfil específico.

**Headers:**
```http
Authorization: Bearer <token>
```

**Resposta (200 OK):**
```json
{
  "id": 2,
  "nome": "ROLE_RECEPCIONISTA",
  "descricao": "Recepcionista",
  "totalPermissoes": 5,
  "permissoes": [
    {
      "id": 2,
      "nome": "USUARIOS_VER",
      "descricao": "Permite visualizar usuários"
    }
  ]
}
```

---

### 4. Criar Perfil com Permissões

**POST** `/admin/roles`

Cria um novo perfil e atribui permissões iniciais.

**Headers:**
```http
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "nome": "ROLE_ENFERMEIRO",
  "descricao": "Enfermeiro da Casa do Amor",
  "permissoesIds": [2, 5, 6, 10]
}
```

**Resposta (201 Created):**
```json
{
  "id": 5,
  "nome": "ROLE_ENFERMEIRO",
  "descricao": "Enfermeiro da Casa do Amor",
  "totalPermissoes": 4,
  "permissoes": [
    {
      "id": 2,
      "nome": "USUARIOS_VER",
      "descricao": "Permite visualizar usuários"
    },
    {
      "id": 5,
      "nome": "DADOS_CLINICOS_CRIAR",
      "descricao": "Permite criar dados clínicos"
    }
  ]
}
```

---

### 5. Atualizar Perfil (substituir permissões)

**PUT** `/admin/roles/{id}`

Atualiza um perfil. Se `permissoesIds` for fornecido, substitui **todas** as permissões antigas.

**Headers:**
```http
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "nome": "ROLE_RECEPCIONISTA",
  "descricao": "Recepcionista atualizado",
  "permissoesIds": [2, 3, 15]
}
```

**Resposta (200 OK):**
```json
{
  "id": 2,
  "nome": "ROLE_RECEPCIONISTA",
  "descricao": "Recepcionista atualizado",
  "totalPermissoes": 3,
  "permissoes": [
    {
      "id": 2,
      "nome": "USUARIOS_VER",
      "descricao": "Permite visualizar usuários"
    },
    {
      "id": 3,
      "nome": "USUARIOS_EDITAR",
      "descricao": "Permite editar usuários existentes"
    },
    {
      "id": 15,
      "nome": "PACIENTES_VER",
      "descricao": "Permite visualizar pacientes"
    }
  ]
}
```

⚠️ **Importante:** Este endpoint **substitui** todas as permissões. Use os endpoints abaixo para adicionar/remover permissões individuais.

---

### 6. Adicionar Permissões a um Perfil (incremental)

**POST** `/admin/roles/{roleId}/permissions`

Adiciona permissões a um perfil **sem remover as existentes**.

**Headers:**
```http
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "permissoesIds": [7, 8, 9]
}
```

**Resposta (200 OK):**
```json
{
  "id": 2,
  "nome": "ROLE_RECEPCIONISTA",
  "descricao": "Recepcionista",
  "totalPermissoes": 8,
  "permissoes": [
    {
      "id": 2,
      "nome": "USUARIOS_VER",
      "descricao": "Permite visualizar usuários"
    },
    {
      "id": 7,
      "nome": "AGENDAMENTOS_CRIAR",
      "descricao": "Permite criar agendamentos"
    }
  ]
}
```

**Uso:** Ideal para checkboxes individuais — quando admin marca uma permissão, chama este endpoint com o ID da permissão.

---

### 7. Remover Permissões de um Perfil

**DELETE** `/admin/roles/{roleId}/permissions`

Remove permissões de um perfil.

**Headers:**
```http
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "permissoesIds": [7, 8]
}
```

**Resposta (200 OK):**
```json
{
  "id": 2,
  "nome": "ROLE_RECEPCIONISTA",
  "descricao": "Recepcionista",
  "totalPermissoes": 6,
  "permissoes": [
    {
      "id": 2,
      "nome": "USUARIOS_VER",
      "descricao": "Permite visualizar usuários"
    }
  ]
}
```

**Uso:** Ideal para checkboxes individuais — quando admin desmarca uma permissão, chama este endpoint com o ID da permissão.

---

### 8. Listar Usuários (com perfis e permissões)

**GET** `/admin/users?page=0&size=10&searchText=maria`

Retorna usuários com seus perfis e permissões.

**Headers:**
```http
Authorization: Bearer <token>
```

**Resposta (200 OK):**
```json
{
  "content": [
    {
      "id": 5,
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "nome": "Maria Silva",
      "email": "maria@example.com",
      "cpf": "12345678901",
      "telefone": "(11) 98765-4321",
      "tipo": "PACIENTE",
      "ativo": true,
      "emailVerificado": true,
      "ultimoLoginEm": "2025-10-20T14:30:00",
      "criadoEm": "2025-10-01T10:00:00",
      "atualizadoEm": "2025-10-20T14:30:00",
      "perfis": [
        {
          "id": 2,
          "nome": "ROLE_RECEPCIONISTA",
          "descricao": "Recepcionista",
          "totalPermissoes": 6,
          "permissoes": [
            {
              "id": 2,
              "nome": "USUARIOS_VER",
              "descricao": "Permite visualizar usuários"
            },
            {
              "id": 15,
              "nome": "PACIENTES_VER",
              "descricao": "Permite visualizar pacientes"
            }
          ]
        }
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

**Uso:** Exibir lista de usuários mostrando seus perfis e permissões herdadas.

---

### 9. Atribuir Perfis a um Usuário

**POST** `/admin/users/{userId}/roles`

Atribui perfis a um usuário. **Substitui** todos os perfis antigos pelos novos.

**Headers:**
```http
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "perfisIds": [2, 3]
}
```

**Resposta (200 OK):**
```json
{
  "id": 5,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "nome": "Maria Silva",
  "email": "maria@example.com",
  "cpf": "12345678901",
  "telefone": "(11) 98765-4321",
  "tipo": "PACIENTE",
  "ativo": true,
  "emailVerificado": true,
  "perfis": [
    {
      "id": 2,
      "nome": "ROLE_RECEPCIONISTA",
      "descricao": "Recepcionista",
      "totalPermissoes": 6,
      "permissoes": [...]
    },
    {
      "id": 3,
      "nome": "ROLE_ENFERMEIRO",
      "descricao": "Enfermeiro",
      "totalPermissoes": 8,
      "permissoes": [...]
    }
  ]
}
```

**Uso:** Quando admin atribui/remove perfis de um usuário, enviar lista completa de IDs dos perfis desejados.

---

### 10. Obter Permissões Efetivas de um Usuário

**GET** `/admin/users/{userId}/effective-permissions`

Retorna todas as permissões efetivas de um usuário (agregadas de todos os seus perfis, sem duplicatas).

**Headers:**
```http
Authorization: Bearer <token>
```

**Resposta (200 OK):**
```json
[
  {
    "id": 2,
    "nome": "USUARIOS_VER",
    "descricao": "Permite visualizar usuários"
  },
  {
    "id": 3,
    "nome": "USUARIOS_EDITAR",
    "descricao": "Permite editar usuários existentes"
  },
  {
    "id": 5,
    "nome": "DADOS_CLINICOS_CRIAR",
    "descricao": "Permite criar dados clínicos"
  },
  {
    "id": 15,
    "nome": "PACIENTES_VER",
    "descricao": "Permite visualizar pacientes"
  }
]
```

**Uso:** Exibir um resumo consolidado de todas as permissões que o usuário possui (útil em telas de auditoria/visualização).

---

## Fluxo de Uso Recomendado para Frontend

### Cenário 1: Criar Novo Perfil com Permissões

1. Carregar lista de permissões: `GET /admin/permissions`
2. Exibir checklist/multi-select das permissões
3. Admin seleciona permissões desejadas
4. Criar perfil: `POST /admin/roles` com `permissoesIds`

### Cenário 2: Editar Permissões de um Perfil Existente

**Opção A: Substituir todas as permissões**
1. Buscar perfil: `GET /admin/roles/{id}`
2. Exibir checklist com permissões atuais marcadas
3. Admin faz alterações
4. Atualizar perfil: `PUT /admin/roles/{id}` com nova lista completa de `permissoesIds`

**Opção B: Adicionar/Remover individualmente**
1. Buscar perfil: `GET /admin/roles/{id}`
2. Exibir checklist com permissões atuais marcadas
3. Admin marca uma permissão: `POST /admin/roles/{id}/permissions` com `permissoesIds: [idDaPermissao]`
4. Admin desmarca uma permissão: `DELETE /admin/roles/{id}/permissions` com `permissoesIds: [idDaPermissao]`

### Cenário 3: Atribuir Perfis a Usuário

1. Buscar lista de perfis: `GET /admin/roles`
2. Buscar usuário: `GET /admin/users/{id}`
3. Exibir multi-select de perfis (marcar os que o usuário já tem)
4. Admin faz alterações
5. Atribuir perfis: `POST /admin/users/{id}/roles` com lista completa de `perfisIds`

### Cenário 4: Ver Permissões Efetivas de Usuário

1. Buscar usuário: `GET /admin/users/{id}` → retorna perfis e permissões
2. **OU** buscar permissões consolidadas: `GET /admin/users/{id}/effective-permissions`

---

## Códigos de Resposta HTTP

| Código | Descrição |
|--------|-----------|
| `200 OK` | Operação bem-sucedida |
| `201 Created` | Recurso criado com sucesso |
| `400 Bad Request` | Dados inválidos ou erro de validação |
| `403 Forbidden` | Usuário não tem permissão para a operação |
| `404 Not Found` | Recurso não encontrado |
| `409 Conflict` | Conflito (ex: nome de perfil já existe) |

---

## Permissões Necessárias

| Endpoint | Permissões Aceitas |
|----------|-------------------|
| `GET /admin/permissions` | `PERMISSOES_VER` ou `ROLE_ADMINISTRADOR` |
| `POST /admin/permissions` | `ROLE_ADMINISTRADOR` |
| `PUT /admin/permissions/{id}` | `PERMISSOES_EDITAR` ou `ROLE_ADMINISTRADOR` |
| `GET /admin/roles` | `ROLES_VER` ou `ROLE_ADMINISTRADOR` |
| `POST /admin/roles` | `ROLES_CRIAR` ou `ROLE_ADMINISTRADOR` |
| `PUT /admin/roles/{id}` | `ROLES_EDITAR` ou `ROLE_ADMINISTRADOR` |
| `POST /admin/roles/{id}/permissions` | `ROLES_EDITAR` ou `ROLE_ADMINISTRADOR` |
| `DELETE /admin/roles/{id}/permissions` | `ROLES_EDITAR` ou `ROLE_ADMINISTRADOR` |
| `GET /admin/users` | `USUARIOS_VER` ou `ROLE_ADMINISTRADOR` |
| `POST /admin/users/{id}/roles` | `ROLE_ADMINISTRADOR` |
| `GET /admin/users/{id}/effective-permissions` | `USUARIOS_VER` ou `ROLE_ADMINISTRADOR` |

---

## Notas de Implementação

1. **Permissões sempre via Perfis:** No modelo atual, permissões são sempre atribuídas a **perfis**, e usuários herdam permissões dos seus perfis. Não há atribuição direta de permissões a usuários.

2. **Permissões duplicadas:** O sistema previne duplicatas automaticamente — adicionar uma permissão já existente em um perfil não causa erro.

3. **Soft delete:** Perfis e permissões deletados não são retornados nas listagens e não podem ser atribuídos.

4. **Cache de autenticação:** Após alterar perfis/permissões de um usuário, pode ser necessário que ele faça logout/login novamente para as mudanças serem refletidas (depende da implementação de JWT no backend).

---

## Exemplos de UI Recomendados

### Tela de Edição de Perfil
```
┌────────────────────────────────────────┐
│ Editar Perfil: ROLE_RECEPCIONISTA     │
├────────────────────────────────────────┤
│ Nome: ROLE_RECEPCIONISTA               │
│ Descrição: [___________________________]│
│                                        │
│ Permissões (6 selecionadas):           │
│ ☑ USUARIOS_VER                         │
│ ☐ USUARIOS_CRIAR                       │
│ ☐ USUARIOS_EDITAR                      │
│ ☑ PACIENTES_VER                        │
│ ☑ PACIENTES_CRIAR                      │
│ ...                                    │
│                                        │
│ [Salvar] [Cancelar]                    │
└────────────────────────────────────────┘
```

### Tela de Usuário com Perfis
```
┌────────────────────────────────────────┐
│ Usuário: Maria Silva                   │
├────────────────────────────────────────┤
│ Email: maria@example.com               │
│ CPF: 123.456.789-01                    │
│ Ativo: Sim                             │
│                                        │
│ Perfis:                                │
│ • ROLE_RECEPCIONISTA (6 permissões)    │
│ • ROLE_ENFERMEIRO (8 permissões)       │
│                                        │
│ [Ver Permissões Efetivas]              │
│ [Editar Perfis]                        │
└────────────────────────────────────────┘
```

---

## Suporte

Para dúvidas ou problemas com a API, consulte:
- Swagger/OpenAPI: `http://localhost:8080/swagger-ui.html`
- Logs do backend para detalhes de erros
- Este documento para referência de payloads
