# Agendamentos — API Changes & Frontend Patches

Last updated: 2025-12-01

This document summarizes the recent backend changes to the Agendamentos module (patients and companions) and provides ready-to-use frontend patches and examples so your UI can be adapted quickly.

## Contents
- [Overview](#overview)
- [Healthcare Professional Types](#healthcare-professional-types)
- [Key Endpoint Changes](#key-endpoint-changes)
- [Response Changes (DTOs)](#response-changes-dtos)
- [Service Filtering by Professional Type](#service-filtering-by-professional-type)
- [Frontend Examples](#frontend-examples)
- [Suggested Frontend Patches](#suggested-frontend-patches)
- [Curl Examples](#curl-examples)
- [Notes & Next Steps](#notes--next-steps)

---

## Overview

The backend was updated to better support hospital-hosted patients ("hospedagem") and to make selecting eligible patients, companions and professionals easier for the frontend.

### Main goals achieved:
- ✅ Only patients and companions with an active `Hospedagem` are considered "eligible" for creating new agendamentos
- ✅ The agendamento response DTOs now include `hospedagemId` and a human-friendly `quartoNome` (room name)
- ✅ New helper endpoints return lists of eligible patients, eligible companions and eligible professionals filtered server-side
- ✅ Professionals lists exclude administrative roles (ADMINISTRADOR, RECEPCIONISTA, AUDITOR) and only include healthcare providers
- ✅ **NEW**: Services can be filtered by professional type — when a professional is selected, only services matching their specialty are returned

---

## Healthcare Professional Types

The system now supports the following healthcare professional types in `auth_usuarios`:

| TipoUsuario | Categoria Serviço | Description |
|-------------|-------------------|-------------|
| **MEDICO** | MEDICO | Medical doctor |
| **ENFERMEIRO** | ENFERMAGEM | Nurse |
| **DENTISTA** | ODONTOLOGICO | Dentist |
| **NUTRICIONISTA** | NUTRICAO | Nutritionist |
| **FISIOTERAPEUTA** | FISIOTERAPIA | Physical therapist |

**Excluded from scheduling** (administrative roles):
- ADMINISTRADOR
- RECEPCIONISTA
- AUDITOR

---

## Key Endpoint Changes

Note: base path is `/api`.

### 1. List eligible patients (active hospedagem)
```
GET /api/agendamentos/pacientes/pacientes-elegiveis
```
- **Roles**: ADMINISTRADOR, GERENTE, RECEPCIONISTA
- **Returns**: Array of objects with `id`, `nome`, `cpf`, `quartoNome`, `hospedagemId`

### 2. List eligible companions (active hospedagem attached to their patient)
```
GET /api/agendamentos/acompanhantes/acompanhantes-elegiveis
```
- **Roles**: ADMINISTRADOR, GERENTE, RECEPCIONISTA
- **Returns**: Array of objects with `id`, `nome`, `cpf`, `pacienteId`, `pacienteNome`, `quartoNome`, `hospedagemId`

### 3. List eligible professionals (healthcare providers only)
```
GET /api/agendamentos/pacientes/profissionais-elegiveis
GET /api/agendamentos/acompanhantes/profissionais-elegiveis
```
- **Roles**: ADMINISTRADOR, GERENTE, RECEPCIONISTA
- **Excludes**: ADMINISTRADOR, RECEPCIONISTA, AUDITOR
- **Includes**: MEDICO, ENFERMEIRO, DENTISTA, NUTRICIONISTA, FISIOTERAPEUTA
- **Returns**: Array of objects with `id`, `nome`, `email`, `tipo`

### 4. 🆕 List services filtered by professional type
```
GET /api/tipos-servico/por-profissional/{profissionalId}
```
- **Roles**: ADMINISTRADOR, GERENTE, RECEPCIONISTA
- **Parameters**: `profissionalId` (Long) - ID of the selected professional
- **Returns**: Array of `TipoServicoResponseDTO` filtered by professional's category
- **Logic**: 
  - MEDICO → only MEDICO category services
  - DENTISTA → only ODONTOLOGICO category services
  - ENFERMEIRO → only ENFERMAGEM category services
  - NUTRICIONISTA → only NUTRICAO category services
  - FISIOTERAPEUTA → only FISIOTERAPIA category services

### 5. Create agendamento endpoints (unchanged URLs, enhanced behavior)
```
POST /api/agendamentos/pacientes
POST /api/agendamentos/acompanhantes
```
- The backend will automatically find and attach the active hospedagem
- Response now includes `hospedagemId` and `quartoNome`

---

## Response Changes (DTOs)

Agendamento responses have been augmented so the frontend can show the room name and keep the hospedagem id for reference.

### Updated fields added to both DTOs:
- `Long hospedagemId` — ID of the active hospedagem (nullable)
- `String quartoNome` — Human-friendly room name (nullable)

### Example (Agendamento paciente response):

```json
{
  "id": 123,
  "uuid": "a1b2c3d4-...",
  "pacienteId": 1,
  "pacienteNome": "Maria Silva",
  "tipoServicoId": 5,
  "tipoServicoNome": "Profilaxia Dentária",
  "profissionalUsuarioId": 10,
  "profissionalNome": "Dr. João Souza",
  "hospedagemId": 42,
  "quartoNome": "Quarto 203 - Ala A",
  "dataHoraInicio": "2025-12-03T10:00:00",
  "dataHoraFim": "2025-12-03T10:30:00",
  "tipoAtendimento": "ROTINA",
  "prioridade": "NORMAL",
  "status": "AGENDADO",
  "observacoes": null,
  "confirmadoPaciente": false,
  "confirmadoProfissional": false,
  "compareceu": null,
  "createdAt": "2025-12-01T09:30:00",
  "updatedAt": "2025-12-01T09:30:00"
}
```

### TypeScript interface suggestion:

```ts
interface AgendamentoResponse {
  id: number;
  uuid: string;
  pacienteId?: number;
  acompanhanteId?: number;
  pacienteVinculadoId?: number;
  pacienteNome?: string;
  acompanhanteNome?: string;
  tipoServicoId: number;
  tipoServicoNome: string;
  profissionalUsuarioId?: number;
  profissionalNome?: string;
  hospedagemId?: number | null;
  quartoNome?: string | null;  // NEW: human-friendly room name
  dataHoraInicio: string;
  dataHoraFim: string;
  tipoAtendimento?: string;
  prioridade?: string;
  status: string;
  observacoes?: string;
  confirmadoPaciente: boolean;
  confirmadoProfissional: boolean;
  compareceu?: boolean | null;
  createdAt: string;
  updatedAt: string;
}
```

---

## Service Filtering by Professional Type

### Professional Type → Service Category Mapping

```
MEDICO         → MEDICO
DENTISTA       → ODONTOLOGICO
ENFERMEIRO     → ENFERMAGEM
NUTRICIONISTA  → NUTRICAO
FISIOTERAPEUTA → FISIOTERAPIA
```

### How to use in your frontend:

**Step 1**: User selects a professional from the dropdown
**Step 2**: Call `/api/tipos-servico/por-profissional/{profissionalId}` to get filtered services
**Step 3**: Populate the service dropdown with only compatible services

Example filtered service lists:

**DENTISTA** (ID: 10) → Returns only:
- Consulta Odontológica
- Profilaxia Dentária
- Restauração Dentária
- Triagem Odontológica
- Emergência Odontológica

**NUTRICIONISTA** (ID: 15) → Returns only:
- Consulta Nutricional
- Retorno Nutricional
- Triagem Nutricional

---

## Frontend Examples

### React - Dynamic service filtering by professional

```tsx
import { useState, useEffect } from 'react';

function AgendamentoForm({ token }: { token: string }) {
  const [professionals, setProfessionals] = useState<any[]>([]);
  const [services, setServices] = useState<any[]>([]);
  const [selectedProfessional, setSelectedProfessional] = useState<number | ''>('');

  // Load professionals on mount
  useEffect(() => {
    (async () => {
      const res = await fetch('/api/agendamentos/pacientes/profissionais-elegiveis', {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) setProfessionals(await res.json());
    })();
  }, [token]);

  // Load filtered services when professional is selected
  useEffect(() => {
    if (!selectedProfessional) {
      setServices([]);
      return;
    }
    
    (async () => {
      const res = await fetch(`/api/tipos-servico/por-profissional/${selectedProfessional}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) setServices(await res.json());
    })();
  }, [selectedProfessional, token]);

  return (
    <form>
      {/* Professional dropdown */}
      <select 
        value={selectedProfessional}
        onChange={e => setSelectedProfessional(Number(e.target.value))}
        required
      >
        <option value="">Selecione um profissional</option>
        {professionals.map(p => (
          <option key={p.id} value={p.id}>
            {p.nome} ({p.tipo})
          </option>
        ))}
      </select>

      {/* Service dropdown - only shown when professional is selected */}
      {selectedProfessional && (
        <select name="tipoServicoId" required>
          <option value="">Selecione um serviço</option>
          {services.map(s => (
            <option key={s.id} value={s.id}>
              {s.nome} ({s.duracaoMinutos} min)
            </option>
          ))}
        </select>
      )}
    </form>
  );
}
```

### Vue - Composition API with service filtering

```vue
<template>
  <form>
    <select v-model="selectedProfessional" required>
      <option value="">Selecione um profissional</option>
      <option v-for="p in professionals" :key="p.id" :value="p.id">
        {{ p.nome }} ({{ p.tipo }})
      </option>
    </select>

    <select v-if="selectedProfessional" v-model="selectedService" required>
      <option value="">Selecione um serviço</option>
      <option v-for="s in services" :key="s.id" :value="s.id">
        {{ s.nome }} ({{ s.duracaoMinutos }} min)
      </option>
    </select>
  </form>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';

const professionals = ref([]);
const services = ref([]);
const selectedProfessional = ref<number | ''>('');
const selectedService = ref<number | ''>('');
const token = localStorage.getItem('token');

onMounted(async () => {
  const res = await fetch('/api/agendamentos/pacientes/profissionais-elegiveis', {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (res.ok) professionals.value = await res.json();
});

// Watch for professional changes and load services
watch(selectedProfessional, async (profId) => {
  services.value = [];
  selectedService.value = '';
  
  if (!profId) return;
  
  const res = await fetch(`/api/tipos-servico/por-profissional/${profId}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (res.ok) services.value = await res.json();
});
</script>
```

### Angular - Service and Component

```ts
// agendamento.service.ts
@Injectable({ providedIn: 'root' })
export class AgendamentoService {
  constructor(private http: HttpClient) {}

  getEligibleProfessionals() {
    return this.http.get<any[]>('/api/agendamentos/pacientes/profissionais-elegiveis');
  }

  getServicesByProfessional(profissionalId: number) {
    return this.http.get<any[]>(`/api/tipos-servico/por-profissional/${profissionalId}`);
  }
}

// agendamento-form.component.ts
export class AgendamentoFormComponent implements OnInit {
  professionals: any[] = [];
  services: any[] = [];
  selectedProfessional: number | null = null;

  constructor(private agendamentoService: AgendamentoService) {}

  ngOnInit() {
    this.agendamentoService.getEligibleProfessionals()
      .subscribe(data => this.professionals = data);
  }

  onProfessionalChange(profissionalId: number) {
    this.selectedProfessional = profissionalId;
    this.services = [];
    
    if (!profissionalId) return;
    
    this.agendamentoService.getServicesByProfessional(profissionalId)
      .subscribe(data => this.services = data);
  }
}
```

---

## Suggested Frontend Patches

### Patch 1: Replace static service list with dynamic filtering

**Old approach** (loading all services):
```ts
// ❌ OLD: Load all services, user can select incompatible ones
const res = await fetch('/api/tipos-servico');
const allServices = await res.json();
```

**New approach** (load services based on professional):
```ts
// ✅ NEW: Load only compatible services for selected professional
const res = await fetch(`/api/tipos-servico/por-profissional/${professionalId}`);
const compatibleServices = await res.json();
```

### Patch 2: Add professional type badge in UI

```tsx
// Show professional type with badge or icon
<option value={p.id}>
  {p.nome} — 
  <span className={`badge badge-${getBadgeColor(p.tipo)}`}>
    {formatProfessionalType(p.tipo)}
  </span>
</option>

function formatProfessionalType(tipo: string): string {
  const map: Record<string, string> = {
    'MEDICO': '🩺 Médico',
    'DENTISTA': '🦷 Dentista',
    'ENFERMEIRO': '💉 Enfermeiro',
    'NUTRICIONISTA': '🥗 Nutricionista',
    'FISIOTERAPEUTA': '🏃 Fisioterapeuta'
  };
  return map[tipo] || tipo;
}
```

### Patch 3: Display room name instead of hospedagemId

```tsx
// ❌ OLD: Display raw hospedagemId
<td>{agendamento.hospedagemId || 'N/A'}</td>

// ✅ NEW: Display human-friendly room name
<td>{agendamento.quartoNome || 'Sem quarto'}</td>
```

---

## Curl Examples

### List eligible professionals
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8090/api/agendamentos/pacientes/profissionais-elegiveis
```

**Response**:
```json
[
  {
    "id": 10,
    "nome": "Dr. João Souza",
    "email": "joao.souza@hospital.com",
    "tipo": "DENTISTA"
  },
  {
    "id": 15,
    "nome": "Maria Nutricionista",
    "email": "maria.nutri@hospital.com",
    "tipo": "NUTRICIONISTA"
  }
]
```

### Get services for a dentist (profissionalId = 10)
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8090/api/tipos-servico/por-profissional/10
```

**Response**:
```json
[
  {
    "id": 4,
    "codigo": "ODONTO_CONSULTA",
    "nome": "Consulta Odontológica",
    "descricao": "Avaliação odontológica inicial",
    "categoria": "ODONTOLOGICO",
    "duracaoMinutos": 30,
    "requerProfissional": true,
    "permiteAcompanhante": true,
    "ativo": true
  },
  {
    "id": 5,
    "codigo": "ODONTO_PROFILAXIA",
    "nome": "Profilaxia Dentária",
    "descricao": "Limpeza e profilaxia",
    "categoria": "ODONTOLOGICO",
    "duracaoMinutos": 40,
    "requerProfissional": true,
    "permiteAcompanhante": true,
    "ativo": true
  }
]
```

### Create agendamento
```bash
curl -X POST http://localhost:8090/api/agendamentos/pacientes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 1,
    "tipoServicoId": 5,
    "profissionalUsuarioId": 10,
    "dataHoraInicio": "2025-12-03T10:00:00",
    "dataHoraFim": "2025-12-03T10:40:00"
  }'
```

---

## Notes & Next Steps

### ✅ Completed
- Filter professionals to exclude administrative roles
- Include all healthcare provider types (MEDICO, ENFERMEIRO, DENTISTA, NUTRICIONISTA, FISIOTERAPEUTA)
- Add service filtering by professional type
- Add `quartoNome` to response DTOs
- Add eligible lists for patients, companions and professionals

### 🔄 Recommended frontend changes
1. **Update service dropdown** to fetch from `/api/tipos-servico/por-profissional/{profissionalId}` when a professional is selected
2. **Display professional type badge** in dropdowns for better UX
3. **Show room name** (`quartoNome`) instead of raw `hospedagemId`
4. **Add validation** to prevent form submission if services don't match professional type
5. **Add loading states** when fetching filtered services

### 💡 Future enhancements (optional)
- Add pagination to eligible lists for large installations
- Add strict validation to reject creates for non-eligible patients
- Add availability calendar integration for professionals
- Add conflict warnings when scheduling overlapping appointments

---

**Questions or issues?** Contact the backend team or open an issue in the repository.
