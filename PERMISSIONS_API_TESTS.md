# Testes dos Novos Endpoints de Permissões

Este arquivo contém exemplos de chamadas HTTP para testar os novos endpoints de gestão de permissões.

## Pré-requisitos

1. Obter token de autenticação (usuário admin):
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "00000000001",
    "senha": "admin123"
  }' | jq -r '.token')

echo "Token: $TOKEN"
```

## 1. Listar Todas as Permissões

```bash
curl -X GET "http://localhost:8080/admin/permissions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

**Resposta esperada:**
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
  }
]
```

## 2. Listar Todos os Perfis (com permissões)

```bash
curl -X GET "http://localhost:8080/admin/roles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

## 3. Buscar Perfil Específico

```bash
# Buscar perfil RECEPCIONISTA (assumindo ID 2)
curl -X GET "http://localhost:8080/admin/roles/2" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

## 4. Criar Novo Perfil com Permissões

```bash
curl -X POST "http://localhost:8080/admin/roles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "ROLE_ENFERMEIRO_TESTE",
    "descricao": "Perfil de teste para enfermeiro",
    "permissoesIds": [2, 5, 6]
  }' | jq
```

**Salvar ID do perfil criado:**
```bash
PERFIL_ID=$(curl -s -X POST "http://localhost:8080/admin/roles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "ROLE_TEST_PERMISSIONS",
    "descricao": "Perfil de teste",
    "permissoesIds": [2]
  }' | jq -r '.id')

echo "Perfil criado com ID: $PERFIL_ID"
```

## 5. Adicionar Permissões a um Perfil (incremental)

```bash
# Adicionar permissões 3 e 4 ao perfil criado
curl -X POST "http://localhost:8080/admin/roles/$PERFIL_ID/permissions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissoesIds": [3, 4]
  }' | jq
```

**Resultado esperado:** Perfil agora tem permissões [2, 3, 4]

## 6. Remover Permissões de um Perfil

```bash
# Remover permissão 4 do perfil
curl -X DELETE "http://localhost:8080/admin/roles/$PERFIL_ID/permissions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissoesIds": [4]
  }' | jq
```

**Resultado esperado:** Perfil agora tem permissões [2, 3]

## 7. Atualizar Perfil (substituir todas as permissões)

```bash
# Substituir todas as permissões do perfil por [5, 6, 7]
curl -X PUT "http://localhost:8080/admin/roles/$PERFIL_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "ROLE_TEST_PERMISSIONS",
    "descricao": "Perfil de teste atualizado",
    "permissoesIds": [5, 6, 7]
  }' | jq
```

**Resultado esperado:** Perfil agora tem permissões [5, 6, 7]

## 8. Listar Usuários

```bash
curl -X GET "http://localhost:8080/admin/users?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

**Para buscar usuário específico (southhenrique@hotmail.com):**
```bash
USER_ID=$(curl -s -X GET "http://localhost:8080/admin/users?searchText=southhenrique" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq -r '.content[0].id')

echo "User ID: $USER_ID"
```

## 9. Atribuir Perfis a Usuário

```bash
# Atribuir perfil RECEPCIONISTA (ID 2) e perfil de teste ao usuário
curl -X POST "http://localhost:8080/admin/users/$USER_ID/roles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "perfisIds": [2, '$PERFIL_ID']
  }' | jq
```

## 10. Obter Permissões Efetivas do Usuário

```bash
curl -X GET "http://localhost:8080/admin/users/$USER_ID/effective-permissions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

**Resposta esperada:** Lista consolidada de todas as permissões do usuário (agregadas de seus perfis)

## 11. Verificar Usuário com Perfis e Permissões

```bash
curl -X GET "http://localhost:8080/admin/users/$USER_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

## Teste Completo (Script)

Execute este script completo para testar todo o fluxo:

```bash
#!/bin/bash

# 1. Login e obter token
echo "=== 1. Obtendo token de admin ==="
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "00000000001",
    "senha": "admin123"
  }' | jq -r '.token')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ Erro ao obter token"
    exit 1
fi
echo "✅ Token obtido"

# 2. Listar permissões
echo -e "\n=== 2. Listando permissões disponíveis ==="
curl -s -X GET "http://localhost:8080/admin/permissions" \
  -H "Authorization: Bearer $TOKEN" | jq '.[] | {id, nome}'

# 3. Criar perfil de teste
echo -e "\n=== 3. Criando perfil de teste ==="
PERFIL_ID=$(curl -s -X POST "http://localhost:8080/admin/roles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "ROLE_TEST_AUTOMATION",
    "descricao": "Perfil criado automaticamente para teste",
    "permissoesIds": [2, 15]
  }' | jq -r '.id')

if [ "$PERFIL_ID" == "null" ] || [ -z "$PERFIL_ID" ]; then
    echo "❌ Erro ao criar perfil"
    exit 1
fi
echo "✅ Perfil criado com ID: $PERFIL_ID"

# 4. Adicionar permissões
echo -e "\n=== 4. Adicionando permissões ao perfil ==="
curl -s -X POST "http://localhost:8080/admin/roles/$PERFIL_ID/permissions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissoesIds": [5, 6]
  }' | jq '{id, nome, totalPermissoes}'

# 5. Buscar usuário de teste
echo -e "\n=== 5. Buscando usuário de teste ==="
USER_ID=$(curl -s -X GET "http://localhost:8080/admin/users?searchText=southhenrique" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.content[0].id')

if [ "$USER_ID" == "null" ] || [ -z "$USER_ID" ]; then
    echo "⚠️  Usuário de teste não encontrado (southhenrique@hotmail.com)"
else
    echo "✅ Usuário encontrado: $USER_ID"
    
    # 6. Atribuir perfil ao usuário
    echo -e "\n=== 6. Atribuindo perfil ao usuário ==="
    curl -s -X POST "http://localhost:8080/admin/users/$USER_ID/roles" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"perfisIds\": [2, $PERFIL_ID]
      }" | jq '{id, nome, perfis: .perfis | length}'
    
    # 7. Obter permissões efetivas
    echo -e "\n=== 7. Permissões efetivas do usuário ==="
    curl -s -X GET "http://localhost:8080/admin/users/$USER_ID/effective-permissions" \
      -H "Authorization: Bearer $TOKEN" | jq '.[] | .nome'
fi

# 8. Remover permissões do perfil
echo -e "\n=== 8. Removendo permissão do perfil ==="
curl -s -X DELETE "http://localhost:8080/admin/roles/$PERFIL_ID/permissions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissoesIds": [6]
  }' | jq '{id, nome, totalPermissoes}'

# 9. Deletar perfil de teste
echo -e "\n=== 9. Deletando perfil de teste ==="
curl -s -X DELETE "http://localhost:8080/admin/roles/$PERFIL_ID" \
  -H "Authorization: Bearer $TOKEN" | jq

echo -e "\n✅ Teste completo finalizado"
```

Salve este script como `test-permissions-api.sh` e execute:
```bash
chmod +x test-permissions-api.sh
./test-permissions-api.sh
```

## Códigos HTTP Esperados

| Operação | Endpoint | Sucesso | Erro Comum |
|----------|----------|---------|------------|
| Listar permissões | GET /admin/permissions | 200 | 403 (sem permissão) |
| Criar perfil | POST /admin/roles | 201 | 409 (nome duplicado) |
| Adicionar permissões | POST /admin/roles/{id}/permissions | 200 | 404 (perfil não encontrado) |
| Remover permissões | DELETE /admin/roles/{id}/permissions | 200 | 404 (perfil não encontrado) |
| Atribuir perfis | POST /admin/users/{id}/roles | 200 | 404 (usuário não encontrado) |
| Permissões efetivas | GET /admin/users/{id}/effective-permissions | 200 | 404 (usuário não encontrado) |

## Troubleshooting

### 401 Unauthorized
- Verificar se o token foi obtido corretamente
- Token pode ter expirado (gerar novo)

### 403 Forbidden
- Usuário não tem a permissão necessária
- Verificar se o usuário tem role ADMINISTRADOR ou a permissão específica

### 404 Not Found
- ID do recurso (perfil/usuário/permissão) não existe
- Verificar se o recurso não foi soft-deleted

### 409 Conflict
- Nome de perfil/permissão já existe
- Usar nome diferente ou atualizar o existente
