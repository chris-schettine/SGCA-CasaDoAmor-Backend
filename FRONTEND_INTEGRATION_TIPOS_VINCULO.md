# Guia de Integração Frontend: Tipos de Vínculo

## 🎯 Quick Start para Desenvolvedores Frontend

Este documento é um guia rápido para integrar os novos endpoints de tipos de vínculo no frontend.

---

## 📡 Endpoints Disponíveis

### 1. Listar Tipos de Vínculo (Dropdown)

**Endpoint**: `GET /api/profissionais/tipos-vinculo`  
**Autenticação**: Requerida (JWT Bearer Token)  
**Autorização**: `ADMINISTRADOR`, `RECEPCIONISTA`, `AUDITOR`

**Response**:
```json
[
  {
    "id": 4,
    "valor": "AUT",
    "descricao": "Autônomo"
  },
  {
    "id": 2,
    "valor": "CLT",
    "descricao": "CLT"
  },
  {
    "id": 1,
    "valor": "PADRAO",
    "descricao": "Padrão"
  },
  {
    "id": 3,
    "valor": "PJ",
    "descricao": "Pessoa Jurídica"
  },
  {
    "id": 5,
    "valor": "VOL",
    "descricao": "Voluntário"
  }
]
```

**Características**:
- ✅ Ordenado alfabeticamente por `descricao`
- ✅ Retorna apenas tipos ativos (`ativo = TRUE`)
- ✅ Dinâmico (reflete dados reais do banco)

---

### 2. Listar Categorias Profissionais (Dropdown)

**Endpoint**: `GET /api/profissionais/categorias`  
**Autenticação**: Requerida (JWT Bearer Token)  
**Autorização**: `ADMINISTRADOR`, `RECEPCIONISTA`, `AUDITOR`

**Response**:
```json
[
  {
    "valor": "MEDICO",
    "descricao": "Médico"
  },
  {
    "valor": "ENFERMAGEM",
    "descricao": "Enfermagem"
  },
  {
    "valor": "PSICOLOGIA",
    "descricao": "Psicologia"
  },
  {
    "valor": "FISIOTERAPIA",
    "descricao": "Fisioterapia"
  },
  {
    "valor": "NUTRICAO",
    "descricao": "Nutrição"
  },
  {
    "valor": "SERVICO_SOCIAL",
    "descricao": "Serviço Social"
  },
  {
    "valor": "FARMACIA",
    "descricao": "Farmácia"
  },
  {
    "valor": "TERAPIA_OCUPACIONAL",
    "descricao": "Terapia Ocupacional"
  },
  {
    "valor": "FONOAUDIOLOGIA",
    "descricao": "Fonoaudiologia"
  },
  {
    "valor": "ADMINISTRATIVO",
    "descricao": "Administrativo"
  },
  {
    "valor": "OUTROS",
    "descricao": "Outros"
  }
]
```

---

## 🚨 IMPORTANTE: Mudança de API

### ❌ **ANTES** (versão antiga - NÃO USAR MAIS)
```javascript
// Request
const body = {
  nome: "Dr. João Silva",
  cpf: "12345678901",
  tipoVinculo: "FUNCIONARIO",  // ❌ String enum
  categoria: "MEDICO"
};

// Response
{
  uuid: "abc-123",
  nome: "Dr. João Silva",
  tipoVinculo: "FUNCIONARIO",  // ❌ String enum
  categoria: "MEDICO"
}
```

### ✅ **AGORA** (versão nova - v0.0.4-SNAPSHOT)
```javascript
// Request
const body = {
  nome: "Dr. João Silva",
  cpf: "12345678901",
  tipoVinculoId: 2,  // ✅ ID numérico (2 = CLT)
  categoria: "MEDICO"
};

// Response
{
  uuid: "abc-123",
  nome: "Dr. João Silva",
  cpf: "12345678901",  // ✅ CPF agora retornado
  tipoVinculo: {       // ✅ Objeto completo (inclui id)
    id: 2,
    valor: "CLT",
    descricao: "CLT"
  },
  categoria: "MEDICO"
}
```

---

## 💻 Exemplo de Implementação

### 1. Fetch Tipos de Vínculo para Dropdown

```javascript
async function carregarTiposVinculo() {
  const token = localStorage.getItem('token');
  
  const response = await fetch('/api/profissionais/tipos-vinculo', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (!response.ok) {
    throw new Error('Erro ao carregar tipos de vínculo');
  }
  
  const tipos = await response.json();
  return tipos;
}
```

---

### 2. Renderizar Dropdown

#### HTML
```html
<label for="tipoVinculo">Tipo de Vínculo:</label>
<select id="tipoVinculo" name="tipoVinculo" required>
  <option value="">-- Selecione --</option>
  <!-- Preenchido dinamicamente via JavaScript -->
</select>
```

#### JavaScript (Vanilla)
```javascript
async function preencherDropdownTiposVinculo() {
  const tipos = await carregarTiposVinculo();
  const select = document.getElementById('tipoVinculo');
  
  tipos.forEach(tipo => {
    const option = document.createElement('option');
    option.value = tipo.id;      // use numeric id for form value
    option.textContent = tipo.descricao;  // "CLT"
    select.appendChild(option);
  });
}

// Chamar ao carregar a página
document.addEventListener('DOMContentLoaded', preencherDropdownTiposVinculo);
```

#### React
```jsx
import { useState, useEffect } from 'react';

function TipoVinculoSelect({ value, onChange }) {
  const [tipos, setTipos] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchTipos() {
      try {
        const tipos = await carregarTiposVinculo();
        setTipos(tipos);
      } catch (error) {
        console.error('Erro ao carregar tipos:', error);
      } finally {
        setLoading(false);
      }
    }
    fetchTipos();
  }, []);

  if (loading) return <p>Carregando...</p>;

  return (
    <select value={value} onChange={onChange} required>
      <option value="">-- Selecione --</option>
      {tipos.map(tipo => (
          <option key={tipo.id} value={tipo.id}>
            {tipo.descricao}
          </option>
        ))}
    </select>
  );
}
```

---

### 3. Criar Profissional (POST)

⚠️ **ATENÇÃO**: Você precisa mapear o `codigo` (valor) selecionado para o `ID` correspondente.

#### Mapeamento de Códigos → IDs:
| Código  | ID | Nome              |
|---------|----|--------------------|
| PADRAO  | 1  | Padrão             |
| CLT     | 2  | CLT                |
| PJ      | 3  | Pessoa Jurídica    |
| AUT     | 4  | Autônomo           |
| VOL     | 5  | Voluntário         |

#### Solução Temporária: Lookup Manual
```javascript
async function criarProfissional(formData) {
  const token = localStorage.getItem('token');
  
  // Carregar tipos para fazer lookup
  const tipos = await carregarTiposVinculo();
  
  // Encontrar o tipo selecionado
  const codigoSelecionado = formData.tipoVinculo; // "CLT"
  const tipo = tipos.find(t => t.valor === codigoSelecionado);
  
  if (!tipo) {
    throw new Error('Tipo de vínculo inválido');
  }
  
  // Lookup ID by codigo (hardcoded mapping)
  const mapeamento = {
    'PADRAO': 1,
    'CLT': 2,
    'PJ': 3,
    'AUT': 4,
    'VOL': 5
  };
  
  const tipoVinculoId = mapeamento[tipo.valor];
  
  // Construir body do request
  const body = {
    nome: formData.nome,
    cpf: formData.cpf,
    telefone: formData.telefone,
    email: formData.email,
    tipoVinculoId: tipoVinculoId,  // ID numérico
    categoria: formData.categoria,
    dataAdmissao: formData.dataAdmissao,
    ativo: true
  };
  
  const response = await fetch('/api/profissionais', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Erro ao criar profissional');
  }
  
  const profissional = await response.json();
  return profissional;
}
```

#### ✅ Nota sobre o campo `id` (implemented)

O endpoint `/api/profissionais/tipos-vinculo` agora retorna também o campo `id` em cada item. Exemplo de objeto retornado:

```json
{
  "id": 2,
  "valor": "CLT",
  "descricao": "CLT"
}
```

Com isso, o frontend deve usar `tipo.id` como value do select e enviar `tipoVinculoId` no POST/PUT do profissional.

---

### 4. Exibir Profissional (GET Response)

```javascript
async function buscarProfissional(uuid) {
  const token = localStorage.getItem('token');
  
  const response = await fetch(`/api/profissionais/${uuid}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const profissional = await response.json();
  
  // Response:
  // {
  //   uuid: "abc-123",
  //   nome: "Dr. João Silva",
  //   cpf: "12345678901",
  //   tipoVinculo: {
  //     valor: "CLT",
  //     descricao: "CLT"
  //   },
  //   categoria: "MEDICO"
  // }
  
  console.log(`Nome: ${profissional.nome}`);
  console.log(`CPF: ${profissional.cpf}`);
  console.log(`Tipo: ${profissional.tipoVinculo.descricao}`);
  console.log(`Categoria: ${profissional.categoria}`);
  
  return profissional;
}
```

**Exibir na UI**:
```html
<div class="profissional-card">
  <h3>{{ profissional.nome }}</h3>
  <p><strong>CPF:</strong> {{ profissional.cpf }}</p>
  <p><strong>Tipo de Vínculo:</strong> {{ profissional.tipoVinculo.descricao }}</p>
  <p><strong>Categoria:</strong> {{ profissional.categoria }}</p>
</div>
```

---

## 🔄 Atualizar Profissional (PUT)

```javascript
async function atualizarProfissional(uuid, formData) {
  const token = localStorage.getItem('token');
  
  // Mesmo processo de lookup do ID
  const tipos = await carregarTiposVinculo();
  const tipo = tipos.find(t => t.valor === formData.tipoVinculo);
  const mapeamento = { 'PADRAO': 1, 'CLT': 2, 'PJ': 3, 'AUT': 4, 'VOL': 5 };
  const tipoVinculoId = mapeamento[tipo.valor];
  
  const body = {
    nome: formData.nome,
    cpf: formData.cpf,
    telefone: formData.telefone,
    email: formData.email,
    tipoVinculoId: tipoVinculoId,  // ID numérico
    categoria: formData.categoria,
    ativo: formData.ativo
  };
  
  const response = await fetch(`/api/profissionais/${uuid}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  });
  
  if (!response.ok) {
    throw new Error('Erro ao atualizar profissional');
  }
  
  return await response.json();
}
```

---

## 🛡️ Tratamento de Erros

### Erro 401: Não autenticado
```javascript
if (response.status === 401) {
  alert('Sessão expirada. Faça login novamente.');
  window.location.href = '/login';
}
```

### Erro 403: Sem permissão
```javascript
if (response.status === 403) {
  alert('Você não tem permissão para acessar este recurso.');
}
```

### Erro 400: Tipo de vínculo inválido
```javascript
// Backend retorna:
// {
//   "message": "Tipo de vínculo não encontrado com ID: 999"
// }

if (response.status === 400) {
  const error = await response.json();
  alert(`Erro: ${error.message}`);
}
```

### Erro 500: Erro interno do servidor
```javascript
if (response.status === 500) {
  alert('Erro no servidor. Tente novamente mais tarde.');
}
```

---

## 📋 Checklist de Integração

### Para cada formulário de Profissional:

- [ ] Substituir input hardcoded de tipo_vinculo por dropdown dinâmico
- [ ] Chamar `GET /api/profissionais/tipos-vinculo` ao carregar página
- [ ] Renderizar options com `tipos.map(t => <option value={t.valor}>{t.descricao}</option>)`
- [ ] No submit, fazer lookup do ID correspondente ao codigo selecionado
- [ ] Enviar `tipoVinculoId` (Long) no body do POST/PUT, NÃO `tipoVinculo` (String)
- [ ] No response, exibir `profissional.tipoVinculo.descricao` ao invés de enum
- [ ] Adicionar tratamento de erros para tipos inválidos
- [ ] Testar com todos os 5 tipos disponíveis

### Para listagens de Profissionais:

- [ ] Exibir `profissional.tipoVinculo.descricao` ao invés de enum
- [ ] Se `tipoVinculo` for `null`, exibir "Não informado" ou valor default
- [ ] Adicionar filtro por tipo de vínculo (multi-select com os 5 tipos)

---

## 🧪 Testes Recomendados

### Teste 1: Carregar Dropdown
1. Abrir formulário de cadastro de profissional
2. Verificar se dropdown de tipos carrega com 5 opções
3. Verificar se está ordenado alfabeticamente: Autônomo, CLT, Padrão, Pessoa Jurídica, Voluntário

### Teste 2: Criar Profissional com CLT
1. Preencher formulário com tipo = CLT
2. Submeter
3. Verificar se response inclui `tipoVinculo: {valor: "CLT", descricao: "CLT"}`

### Teste 3: Criar Profissional com VOL
1. Preencher formulário com tipo = VOL (Voluntário)
2. Submeter
3. Verificar se response inclui `tipoVinculo: {valor: "VOL", descricao: "Voluntário"}`

### Teste 4: Atualizar Tipo de Vínculo
1. Editar profissional existente
2. Mudar tipo de CLT → PJ
3. Salvar
4. Verificar se listagem exibe "Pessoa Jurídica"

### Teste 5: Validação de Tipo Inválido
1. Enviar request com `tipoVinculoId: 999` (ID inexistente)
2. Verificar se backend retorna erro 400
3. Verificar se mensagem de erro é exibida ao usuário

---

## 🚀 Deploy

### Antes de Fazer Deploy:

1. ✅ Validar que **TODOS** os formulários de profissional foram atualizados
2. ✅ Remover qualquer código que envia `tipoVinculo` como string enum
3. ✅ Testar criação, edição, e visualização de profissionais
4. ✅ Validar que tipos de vínculo antigos (FUNCIONARIO, VOLUNTARIO) foram migrados corretamente
5. ✅ Coordenar com backend: garantir que migration V56 foi aplicada em produção

### Após Deploy:

1. ✅ Verificar se dropdown carrega corretamente em produção
2. ✅ Testar criação de profissional com cada um dos 5 tipos
3. ✅ Verificar se profissionais antigos exibem tipos corretos (FUNCIONARIO→CLT, VOLUNTARIO→VOL)
4. ✅ Monitorar logs de erro para problemas de integração

---

## 📞 Suporte

Em caso de dúvidas ou problemas:
- **Documentação Backend**: Ver `TIPOS_VINCULO_REFACTORING_GUIDE.md`
- **API Docs**: Ver `README.md` seção "Endpoints de Profissionais"
- **Estrutura de Dados**: Ver `README.md` seção "Principais Tabelas"

---

## 🔗 Links Úteis

- **Changelog**: `README.md` → Versão 0.0.4-SNAPSHOT
- **Migrações**: `src/main/resources/db/migration/V55__*.sql`, `V56__*.sql`
- **Entities**: `src/main/java/.../entity/TipoVinculoEntity.java`
- **DTOs**: `src/main/java/.../dto/TipoVinculoDTO.java`

---

**Última Atualização**: 2025-11-16  
**Versão Backend**: 0.0.4-SNAPSHOT
