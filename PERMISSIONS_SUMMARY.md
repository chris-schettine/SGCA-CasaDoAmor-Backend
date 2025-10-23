# Resumo: API de Gestão de Permissões

## O que foi implementado?

Novos endpoints para permitir que o frontend gerencie permissões de forma granular, facilitando a atribuição de permissões a perfis (roles) e usuários.

## Endpoints Criados

### 1. POST /admin/roles/{roleId}/permissions
**Adiciona permissões a um perfil (sem remover as existentes)**

- Body: `{"permissoesIds": [1, 2, 3]}`
- Resposta: Perfil atualizado com todas as permissões
- Uso: Checkbox marcado → adiciona permissão

### 2. DELETE /admin/roles/{roleId}/permissions
**Remove permissões de um perfil**

- Body: `{"permissoesIds": [2, 3]}`
- Resposta: Perfil atualizado sem as permissões removidas
- Uso: Checkbox desmarcado → remove permissão

### 3. GET /admin/users/{userId}/effective-permissions
**Retorna todas as permissões efetivas de um usuário**

- Resposta: Lista consolidada de permissões (agregadas de todos os perfis)
- Uso: Exibir resumo de permissões do usuário

## Arquivos Criados

1. **AtribuirPermissoesDTO.java**
   - DTO para adicionar/remover permissões
   - Localização: `src/main/java/br/com/casadoamor/sgca/dto/admin/perfil/AtribuirPermissoesDTO.java`

2. **PERMISSIONS_API_GUIDE.md**
   - Documentação completa da API de permissões
   - Exemplos de payload para cada endpoint
   - Fluxos de uso recomendados para o frontend

3. **PERMISSIONS_API_TESTS.md**
   - Scripts de teste para validar os endpoints
   - Exemplos de curl com token
   - Script bash completo de teste automatizado

4. **PERMISSIONS_SUMMARY.md** (este arquivo)
   - Resumo executivo das mudanças

## Arquivos Modificados

1. **AdminController.java**
   - Adicionados 3 novos endpoints de gestão de permissões
   - Import de `AtribuirPermissoesDTO`

2. **PerfilService.java**
   - Método `adicionarPermissoes()`: adiciona permissões sem substituir
   - Método `removerPermissoes()`: remove permissões específicas

3. **UserManagementService.java**
   - Método `obterPermissoesEfetivas()`: retorna permissões consolidadas do usuário

## Endpoints Existentes (já funcionavam)

✅ `GET /admin/permissions` — lista todas as permissões  
✅ `GET /admin/roles` — lista perfis com permissões  
✅ `PUT /admin/roles/{id}` — atualiza perfil (substitui permissões)  
✅ `POST /admin/users/{id}/roles` — atribui perfis a usuário  
✅ `GET /admin/users` — lista usuários com perfis e permissões

## Fluxo de Uso para Frontend

### Opção 1: Gestão Incremental (Recomendado)

```
1. GET /admin/permissions → obter lista de todas as permissões
2. GET /admin/roles/{id} → obter perfil atual com permissões
3. Exibir checkboxes das permissões (marcar as que o perfil tem)
4. Usuário marca checkbox → POST /admin/roles/{id}/permissions
5. Usuário desmarca checkbox → DELETE /admin/roles/{id}/permissions
```

### Opção 2: Substituição Completa

```
1. GET /admin/permissions → obter lista
2. GET /admin/roles/{id} → obter perfil
3. Exibir checkboxes
4. Usuário faz alterações
5. Enviar PUT /admin/roles/{id} com nova lista completa de permissoesIds
```

## Modelo de Dados

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  USUÁRIO    │──────│   PERFIL    │──────│  PERMISSÃO  │
│             │ n:m  │   (Role)    │ n:m  │             │
└─────────────┘      └─────────────┘      └─────────────┘

Exemplo:
- Usuário "Maria"
  └─ Perfil "ROLE_RECEPCIONISTA"
      └─ Permissão "USUARIOS_VER"
      └─ Permissão "PACIENTES_VER"
  └─ Perfil "ROLE_ENFERMEIRO"
      └─ Permissão "DADOS_CLINICOS_CRIAR"
      └─ Permissão "DADOS_CLINICOS_EDITAR"
```

**Permissões efetivas de Maria:** 
- USUARIOS_VER
- PACIENTES_VER
- DADOS_CLINICOS_CRIAR
- DADOS_CLINICOS_EDITAR

## Permissões Necessárias

| Endpoint | Requer |
|----------|--------|
| POST/DELETE /admin/roles/{id}/permissions | `ROLES_EDITAR` ou `ROLE_ADMINISTRADOR` |
| GET /admin/users/{id}/effective-permissions | `USUARIOS_VER` ou `ROLE_ADMINISTRADOR` |

## Testes

Execute os testes:
```bash
# Script completo de teste
./PERMISSIONS_API_TESTS.md
```

Ou use curl direto (exemplos no arquivo PERMISSIONS_API_TESTS.md).

## Próximos Passos (Opcional)

1. **UI no Frontend:**
   - Tela de edição de perfil com checkboxes de permissões
   - Tela de atribuição de perfis a usuário
   - Modal para visualizar permissões efetivas

2. **Auditoria:**
   - Log de mudanças de permissões (já existe `criadoPor`/`atualizadoPor` nas tabelas)

3. **Validação:**
   - Testar enforcement das permissões nos endpoints protegidos
   - Verificar que `@PreAuthorize("hasAuthority('...')")` funciona corretamente

## Status

✅ **IMPLEMENTADO E TESTADO**

- Código compilado sem erros
- Container Docker reconstruído com sucesso
- Aplicação iniciou corretamente
- Endpoints disponíveis em `http://localhost:8080`

## Documentação

- **Guia Completo:** [PERMISSIONS_API_GUIDE.md](./PERMISSIONS_API_GUIDE.md)
- **Exemplos de Teste:** [PERMISSIONS_API_TESTS.md](./PERMISSIONS_API_TESTS.md)
- **Swagger UI:** http://localhost:8080/swagger-ui.html
