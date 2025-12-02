# Frontend Implementation Guide - Hospedagem Module (Quartos & Hospedagens)

## Overview
This guide provides complete API documentation for implementing the frontend of the Hospedagem module, which manages rooms (Quartos) and patient stays (Hospedagens). The module is accessible to **ADMINISTRADOR** and **RECEPCIONISTA** roles.

---

## Base Configuration

### API Base URL
```
http://localhost:8090/api
```

### Authentication
All endpoints require JWT authentication via Bearer token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Role-Based Access
- **ADMINISTRADOR**: Full access (read + write operations)
- **RECEPCIONISTA**: Full access (read + write operations)
- **AUDITOR**: Read-only access (not covered in this guide)

---

## 1. Quartos (Rooms) API

### 1.1 Create Room
**Endpoint:** `POST /api/quartos`  
**Access:** ADMINISTRADOR only  
**Description:** Create a new room with automatic code generation

**Request Body:**
```typescript
interface QuartoRequest {
  nome: string;                    // Required, 3-100 chars (e.g., "Quarto Azul")
  tipo: "INDIVIDUAL" | "COMPARTILHADO" | "ISOLAMENTO"; // Required
  ala: "FEMININA" | "MASCULINA" | "MISTA"; // Required
  capacidadeTotal: number;         // Required, min: 1, max: 20
  codigo?: string;                 // Optional, auto-generated if null
  andar?: string;                  // Optional (e.g., "1º andar", "Térreo")
  ativo?: boolean;                 // Optional, default: true
  emManutencao?: boolean;          // Optional, default: false
  permiteSexoOposto?: boolean;     // Optional, default: false
  observacoes?: string;            // Optional, max 1000 chars
}
```

**Response (201):**
```typescript
interface QuartoResponse {
  uuid: string;
  nome: string;
  codigo: string;                  // Auto-generated format: [ALA]-[ANDAR]-[N]
  tipo: { valor: string; descricao: string };
  ala: { valor: string; descricao: string };
  andar: string | null;
  capacidadeTotal: number;
  capacidadeOcupada: number;
  vagasDisponiveis: number;
  ativo: boolean;
  emManutencao: boolean;
  permiteSexoOposto: boolean;
  observacoes: string | null;
  createdAt: string;               // ISO 8601
  createdByNome: string;
  updatedAt: string;
  updatedByNome: string;
}
```

**Example cURL:**
```bash
curl -X POST http://localhost:8090/api/quartos \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Quarto Rosa",
    "tipo": "INDIVIDUAL",
    "ala": "FEMININA",
    "capacidadeTotal": 1,
    "andar": "2º andar",
    "permiteSexoOposto": false
  }'
```

---

### 1.2 Update Room
**Endpoint:** `PUT /api/quartos/{uuid}`  
**Access:** ADMINISTRADOR only  
**Description:** Update existing room details

**Request Body:** Same as Create (QuartoRequest)

**Response (200):** Same as Create (QuartoResponse)

---

### 1.3 List Rooms (Paginated with Filters)
**Endpoint:** `GET /api/quartos`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** List rooms with pagination and optional filters

**Query Parameters:**
```typescript
interface QuartosQueryParams {
  page?: number;           // Default: 0
  size?: number;           // Default: 20
  sort?: string;           // Default: "nome,asc"
  nome?: string;           // Filter by name (partial match)
  ala?: "FEMININA" | "MASCULINA" | "MISTA";
  tipo?: "INDIVIDUAL" | "COMPARTILHADO" | "ISOLAMENTO";
  ativo?: boolean;         // true/false
}
```

**Response (200):**
```typescript
interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: { sorted: boolean; unsorted: boolean; empty: boolean };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  sort: { sorted: boolean; unsorted: boolean; empty: boolean };
  empty: boolean;
}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8090/api/quartos?page=0&size=10&ala=FEMININA&ativo=true" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 1.4 Get Room by UUID
**Endpoint:** `GET /api/quartos/{uuid}`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Get detailed information about a specific room

**Response (200):** QuartoResponse (see 1.1)

---

### 1.5 List Active Rooms
**Endpoint:** `GET /api/quartos/ativos`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** List all active rooms (ativo = true, no pagination)

**Response (200):** `QuartoResumo[]`

```typescript
interface QuartoResumo {
  uuid: string;
  nome: string;
  codigo: string;
  tipo: { valor: string; descricao: string };
  ala: { valor: string; descricao: string };
  andar: string | null;
  capacidadeTotal: number;
  capacidadeOcupada: number;
  vagasDisponiveis: number;
  ativo: boolean;
  emManutencao: boolean;
  permiteSexoOposto: boolean;
}
```

---

### 1.6 List Available Rooms
**Endpoint:** `GET /api/quartos/disponiveis`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** List rooms with available vacancies (vagasDisponiveis > 0)

**Response (200):** `QuartoResumo[]`

---

### 1.7 Get Room Statistics
**Endpoint:** `GET /api/quartos/estatisticas`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Comprehensive dashboard statistics for rooms and occupancy

**Response (200):**
```typescript
interface EstatisticasOcupacao {
  // Occupancy metrics
  capacidadeTotal: number;
  ocupacaoTotal: number;
  vagasDisponiveis: number;
  percentualOcupacao: number;       // 0-100
  
  // Room counts
  totalQuartos: number;              // Active + inactive
  quartosAtivos: number;
  quartosInativos: number;
  quartosEmManutencao: number;
  quartosDisponiveisAdmissao: number; // Active, not in maintenance, with vacancies
  
  // Type distribution
  quartosIndividuais: number;
  quartosCompartilhados: number;
  quartosIsolamento: number;
  
  // Operational metrics
  quartosLotados: number;            // Full capacity
  quartosVazios: number;             // Zero occupancy
  quartosParcialmenteOcupados: number;
  quartosPermitemSexoOposto: number;
  
  // Per-wing statistics
  alaFeminina: EstatisticasAla;
  alaMasculina: EstatisticasAla;
  alaMista: EstatisticasAla;
}

interface EstatisticasAla {
  capacidadeTotal: number;
  ocupacaoTotal: number;
  vagasDisponiveis: number;
  percentualOcupacao: number;
  totalQuartos: number;
  quartosAtivos: number;
  quartosInativos: number;
  quartosEmManutencao: number;
  quartosDisponiveisAdmissao: number;
  quartosLotados: number;
  quartosVazios: number;
  quartosParcialmenteOcupados: number;
}
```

**Example Use Case:** Display comprehensive dashboard with charts for:
- Overall occupancy percentage
- Room availability by wing
- Room type distribution
- Maintenance vs operational capacity

---

### 1.8 Deactivate Room
**Endpoint:** `PATCH /api/quartos/{uuid}/inativar`  
**Access:** ADMINISTRADOR only  
**Description:** Deactivate a room (sets ativo = false)

**Response (200):**
```typescript
interface MessageResponse {
  message: string; // "Quarto inativado com sucesso"
}
```

---

### 1.9 Activate Room
**Endpoint:** `PATCH /api/quartos/{uuid}/ativar`  
**Access:** ADMINISTRADOR only  
**Description:** Reactivate a deactivated room (sets ativo = true)

**Response (200):**
```typescript
interface MessageResponse {
  message: string; // "Quarto ativado com sucesso"
}
```

---

### 1.10 Enable Maintenance Mode
**Endpoint:** `PATCH /api/quartos/{uuid}/manutencao/ativar`  
**Access:** ADMINISTRADOR only  
**Description:** Put room into maintenance mode (blocks new admissions)

**Note:** Cannot activate maintenance if room is occupied (throws error)

**Response (200):**
```typescript
interface MessageResponse {
  message: string; // "Modo manutenção ativado com sucesso"
}
```

**Error (400):**
```json
{
  "message": "Não é possível ativar manutenção em um quarto ocupado",
  "status": 400
}
```

---

### 1.11 Disable Maintenance Mode
**Endpoint:** `PATCH /api/quartos/{uuid}/manutencao/desativar`  
**Access:** ADMINISTRADOR only  
**Description:** Remove maintenance flag (allows admissions again)

**Response (200):**
```typescript
interface MessageResponse {
  message: string; // "Modo manutenção desativado com sucesso"
}
```

---

### 1.12 Delete Room (Soft Delete)
**Endpoint:** `DELETE /api/quartos/{uuid}`  
**Access:** ADMINISTRADOR only  
**Description:** Soft delete a room (sets deleted = true in database)

**Response (204):** No content

---

### 1.13 Get Room Wings (Enum Dropdown)
**Endpoint:** `GET /api/quartos/alas`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Get available room wings for dropdowns/selects

**Response (200):**
```typescript
interface AlaQuartoOption {
  valor: string;     // "FEMININA" | "MASCULINA" | "MISTA"
  descricao: string; // "Feminina" | "Masculina" | "Mista"
}[]
```

**Example:**
```json
[
  { "valor": "FEMININA", "descricao": "Feminina" },
  { "valor": "MASCULINA", "descricao": "Masculina" },
  { "valor": "MISTA", "descricao": "Mista" }
]
```

---

### 1.14 Get Room Types (Enum Dropdown)
**Endpoint:** `GET /api/quartos/tipos`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Get available room types for dropdowns/selects

**Response (200):**
```typescript
interface TipoQuartoOption {
  valor: string;     // "INDIVIDUAL" | "COMPARTILHADO" | "ISOLAMENTO"
  descricao: string; // "Individual" | "Compartilhado" | "Isolamento"
}[]
```

---

## 2. Hospedagens (Patient Stays) API

### 2.0 Prerequisites: Getting Patient UUID from CPF

**Important:** The hospedagem endpoints require the Patient UUID, not CPF. The frontend must convert CPF to UUID before making requests.

#### **Option 1: Search Patients Endpoint (Recommended)**

Use the existing patient search endpoint to find the patient by CPF and extract the UUID:

**Endpoint:** `GET /api/pacientes/`  
**Query Parameters:**
- `searchText`: Patient CPF (will search across CPF, name, etc.)
- `limit`: 1 (only need first result)
- `offset`: 0

**Example:**
```typescript
// services/pacientes.service.ts
export const pacientesService = {
  searchByCpf: async (cpf: string) => {
    const response = await API.get('/pacientes/', {
      params: { searchText: cpf, limit: 1, offset: 0 }
    });
    
    if (response.data.data.length === 0) {
      throw new Error('Paciente não encontrado com o CPF informado');
    }
    
    return response.data.data[0]; // Returns full patient object with UUID
  }
};

// Usage in component:
async function onSubmitHospedagem(formData: { cpf: string; /* other fields */ }) {
  try {
    // 1. Get patient UUID from CPF
    const patient = await pacientesService.searchByCpf(formData.cpf);
    
    // 2. Create hospedagem with patient UUID
    const hospedagemData: HospedagemRequest = {
      pacienteId: patient.id, // Use the UUID from patient object
      quartoUuid: formData.quartoUuid,
      dataEntrada: formData.dataEntrada,
      // ... other fields
    };
    
    await hospedagensService.register(hospedagemData);
  } catch (error) {
    handleApiError(error);
  }
}
```

#### **Option 2: Add New Backend Endpoint (Better UX)**

If you want cleaner separation, add a dedicated lookup endpoint to `PacienteController`:

```java
@GetMapping("/cpf/{cpf}")
@Operation(summary = "Buscar paciente por CPF")
@PreAuthorize("hasAuthority('PACIENTES_VER') or hasRole('RECEPCIONISTA') or hasRole('ADMINISTRADOR')")
public ResponseEntity<PacienteDTO> buscarPorCpf(@PathVariable String cpf) {
    PacienteDTO paciente = pacienteService.buscarPorCpf(cpf);
    return ResponseEntity.ok(paciente);
}
```

Then use it in the frontend:

```typescript
// services/pacientes.service.ts
export const pacientesService = {
  getByCpf: (cpf: string) => API.get<PacienteResponse>(`/pacientes/cpf/${cpf}`)
};

// Usage:
const patient = await pacientesService.getByCpf(formData.cpf);
const hospedagemData = {
  pacienteId: patient.data.id,
  // ... other fields
};
```

#### **Option 3: Patient Autocomplete Component (Best UX)**

Create a reusable autocomplete component that searches as the user types:

```typescript
// components/PatientAutocomplete.vue
<template>
  <div>
    <input 
      v-model="searchQuery"
      @input="debounceSearch"
      placeholder="Digite CPF ou nome do paciente"
    />
    <ul v-if="suggestions.length > 0">
      <li 
        v-for="patient in suggestions" 
        :key="patient.id"
        @click="selectPatient(patient)"
      >
        {{ patient.dadoPessoal.nome }} - CPF: {{ formatCpf(patient.dadoPessoal.cpf) }}
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { pacientesService } from '@/services/pacientes.service';
import { debounce } from 'lodash';

const searchQuery = ref('');
const suggestions = ref([]);
const selectedPatient = ref(null);

const emit = defineEmits(['patient-selected']);

const searchPatients = async () => {
  if (searchQuery.value.length < 3) return;
  
  const response = await pacientesService.search({
    searchText: searchQuery.value,
    limit: 5,
    offset: 0
  });
  
  suggestions.value = response.data.data;
};

const debounceSearch = debounce(searchPatients, 300);

const selectPatient = (patient: any) => {
  selectedPatient.value = patient;
  searchQuery.value = `${patient.dadoPessoal.nome} - ${patient.dadoPessoal.cpf}`;
  suggestions.value = [];
  emit('patient-selected', patient); // Emits full patient object with UUID
};
</script>
```

**Usage in Hospedagem Form:**
```vue
<PatientAutocomplete @patient-selected="onPatientSelected" />

<script setup>
const selectedPatientUuid = ref(null);

function onPatientSelected(patient) {
  selectedPatientUuid.value = patient.id; // Store UUID for submission
}

async function submitHospedagem() {
  await hospedagensService.register({
    pacienteId: selectedPatientUuid.value, // Use stored UUID
    // ... other fields
  });
}
</script>
```

---

### 2.1 Register Patient Entry
**Endpoint:** `POST /api/hospedagens`  
**Access:** ADMINISTRADOR only  
**Description:** Register a new patient admission to a room

**Request Body:**
```typescript
interface HospedagemRequest {
  pacienteId: string;                // Required (Patient UUID, not CPF!)
  quartoUuid?: string;               // Optional (if null, auto-assigned)
  dataEntrada: string;               // Required, ISO date (YYYY-MM-DD), past or today
  horaEntrada?: string;              // Optional, ISO time (HH:mm:ss)
  dataSaidaPrevista?: string;        // Optional, ISO date, must be future
  observacoesEntrada?: string;       // Optional, max 1000 chars
  observacoesGerais?: string;        // Optional, max 1000 chars
}
```

**Response (201):**
```typescript
interface HospedagemResponse {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  pacienteCpf: string;
  quartoUuid: string;
  quartoNome: string;
  quartoCodigo: string;
  dataEntrada: string;               // ISO date
  horaEntrada: string | null;        // ISO time
  dataSaidaPrevista: string | null;
  dataSaida: string | null;
  horaSaida: string | null;
  status: "ATIVA" | "ENCERRADA" | "TRANSFERIDA";
  motivoSaida: string | null;
  observacoesEntrada: string | null;
  observacoesSaida: string | null;
  observacoesGerais: string | null;
  createdAt: string;                 // ISO 8601
  createdByNome: string;
  updatedAt: string;
  updatedByNome: string;
}
```

**Example:**
```bash
curl -X POST http://localhost:8090/api/hospedagens \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "quartoUuid": "550e8400-e29b-41d4-a716-446655440000",
    "dataEntrada": "2024-01-15",
    "horaEntrada": "14:30:00",
    "dataSaidaPrevista": "2024-02-15",
    "observacoesEntrada": "Paciente admitido para recuperação"
  }'
```

---

### 2.2 Register Patient Exit
**Endpoint:** `PUT /api/hospedagens/{uuid}/saida`  
**Access:** ADMINISTRADOR only  
**Description:** Register patient checkout/exit from stay

**Request Body:**
```typescript
interface HospedagemSaidaRequest {
  dataSaida: string;        // Required, ISO date, past or today
  horaSaida?: string;       // Optional, ISO time
  motivoSaida: string;      // Required, max 255 chars (e.g., "Alta médica", "Transferência")
  observacoesSaida?: string; // Optional, max 1000 chars
}
```

**Response (200):** HospedagemResponse (see 2.1)

---

### 2.3 Transfer Patient
**Endpoint:** `PUT /api/hospedagens/{uuid}/transferir`  
**Access:** ADMINISTRADOR only  
**Description:** Transfer patient from current room to another room

**Query Parameter:**
- `novoQuartoUuid` (required): UUID of destination room

**Example:**
```bash
curl -X PUT "http://localhost:8090/api/hospedagens/{uuid}/transferir?novoQuartoUuid=NEW_ROOM_UUID" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response (200):** HospedagemResponse (see 2.1)

---

### 2.4 Get Stay by UUID
**Endpoint:** `GET /api/hospedagens/{uuid}`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Get detailed information about a specific stay

**Response (200):** HospedagemResponse (see 2.1)

---

### 2.5 List Active Stays
**Endpoint:** `GET /api/hospedagens/ativas`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** List all currently active patient stays

**Response (200):** `HospedagemResponse[]`

---

### 2.6 Get Patient Stay History
**Endpoint:** `GET /api/hospedagens/paciente/{id}`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Get complete stay history for a specific patient

**Path Parameter:** `id` = Patient UUID

**Response (200):** `HospedagemResponse[]`

---

### 2.7 Get Room Stay History
**Endpoint:** `GET /api/hospedagens/quarto/{uuid}`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Get all stays associated with a specific room

**Response (200):** `HospedagemResponse[]`

---

### 2.8 Filter Stays by Date Range
**Endpoint:** `GET /api/hospedagens/periodo`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Filter stays by date range

**Query Parameters:**
- `dataInicio` (required): ISO date (YYYY-MM-DD)
- `dataFim` (required): ISO date (YYYY-MM-DD)

**Example:**
```bash
curl -X GET "http://localhost:8090/api/hospedagens/periodo?dataInicio=2024-01-01&dataFim=2024-01-31" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response (200):** `HospedagemResponse[]`

---

### 2.9 List Overdue Stays
**Endpoint:** `GET /api/hospedagens/previsao-vencida`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** List stays where dataSaidaPrevista has passed but status is still ATIVA

**Response (200):** `HospedagemResponse[]`

---

### 2.10 List Paginated Stays
**Endpoint:** `GET /api/hospedagens/paginated`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Get paginated list of all stays

**Query Parameters:**
- `page` (optional): Page number, default 0
- `size` (optional): Page size, default 20
- `sort` (optional): Sort criteria, default "createdAt,desc"

**Response (200):** `PageResponse<HospedagemResponse>` (see 1.3 for PageResponse structure)

---

### 2.11 Check Active Stay for Patient
**Endpoint:** `GET /api/hospedagens/paciente/{id}/ativa`  
**Access:** ADMINISTRADOR, RECEPCIONISTA  
**Description:** Check if patient currently has an active stay

**Path Parameter:** `id` = Patient UUID

**Response (200):**
```typescript
interface ActiveStayCheck {
  temHospedagemAtiva: boolean;
  hospedagem?: HospedagemResponse; // Present if temHospedagemAtiva = true
}
```

---

### 2.12 Delete Stay (Soft Delete)
**Endpoint:** `DELETE /api/hospedagens/{uuid}`  
**Access:** ADMINISTRADOR only  
**Description:** Soft delete a stay record (sets deleted = true)

**Response (204):** No content

---

## 3. Frontend Implementation Recommendations

### 3.1 TypeScript Type Definitions
Create a `types/hospedagem.ts` file with all interfaces listed above.

### 3.2 API Client Setup (axios example)
```typescript
// services/api.ts
import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8090/api',
});

// Add JWT token to all requests
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default API;
```

### 3.3 API Service Methods
```typescript
// services/quartos.service.ts
import API from './api';
import type { QuartoRequest, QuartoResponse, PageResponse, QuartoResumo } from '@/types/hospedagem';

export const quartosService = {
  create: (data: QuartoRequest) => 
    API.post<QuartoResponse>('/quartos', data),
  
  update: (uuid: string, data: QuartoRequest) => 
    API.put<QuartoResponse>(`/quartos/${uuid}`, data),
  
  list: (params?: { page?: number; size?: number; nome?: string; ala?: string; tipo?: string; ativo?: boolean }) =>
    API.get<PageResponse<QuartoResponse>>('/quartos', { params }),
  
  getByUuid: (uuid: string) => 
    API.get<QuartoResponse>(`/quartos/${uuid}`),
  
  listActive: () => 
    API.get<QuartoResumo[]>('/quartos/ativos'),
  
  listAvailable: () => 
    API.get<QuartoResumo[]>('/quartos/disponiveis'),
  
  getStatistics: () => 
    API.get('/quartos/estatisticas'),
  
  deactivate: (uuid: string) => 
    API.patch(`/quartos/${uuid}/inativar`),
  
  activate: (uuid: string) => 
    API.patch(`/quartos/${uuid}/ativar`),
  
  enableMaintenance: (uuid: string) => 
    API.patch(`/quartos/${uuid}/manutencao/ativar`),
  
  disableMaintenance: (uuid: string) => 
    API.patch(`/quartos/${uuid}/manutencao/desativar`),
  
  delete: (uuid: string) => 
    API.delete(`/quartos/${uuid}`),
  
  getAlas: () => 
    API.get('/quartos/alas'),
  
  getTipos: () => 
    API.get('/quartos/tipos'),
};

// services/hospedagens.service.ts
import API from './api';
import type { HospedagemRequest, HospedagemResponse, HospedagemSaidaRequest } from '@/types/hospedagem';

export const hospedagensService = {
  register: (data: HospedagemRequest) => 
    API.post<HospedagemResponse>('/hospedagens', data),
  
  registerExit: (uuid: string, data: HospedagemSaidaRequest) => 
    API.put<HospedagemResponse>(`/hospedagens/${uuid}/saida`, data),
  
  transfer: (uuid: string, novoQuartoUuid: string) => 
    API.put<HospedagemResponse>(`/hospedagens/${uuid}/transferir`, null, { params: { novoQuartoUuid } }),
  
  getByUuid: (uuid: string) => 
    API.get<HospedagemResponse>(`/hospedagens/${uuid}`),
  
  listActive: () => 
    API.get<HospedagemResponse[]>('/hospedagens/ativas'),
  
  getPatientHistory: (pacienteUuid: string) => 
    API.get<HospedagemResponse[]>(`/hospedagens/paciente/${pacienteUuid}`),
  
  getRoomHistory: (uuid: string) => 
    API.get<HospedagemResponse[]>(`/hospedagens/quarto/${uuid}`),
  
  filterByPeriod: (dataInicio: string, dataFim: string) => 
    API.get<HospedagemResponse[]>('/hospedagens/periodo', { params: { dataInicio, dataFim } }),
  
  listOverdue: () => 
    API.get<HospedagemResponse[]>('/hospedagens/previsao-vencida'),
  
  listPaginated: (params?: { page?: number; size?: number; sort?: string }) =>
    API.get<PageResponse<HospedagemResponse>>('/hospedagens/paginated', { params }),
  
  checkActiveStay: (pacienteUuid: string) => 
    API.get(`/hospedagens/paciente/${pacienteUuid}/ativa`),
  
  delete: (uuid: string) => 
    API.delete(`/hospedagens/${uuid}`),
};
```

### 3.4 Recommended Routes Structure
```typescript
// router/routes.ts
const routes = [
  // Quartos routes (accessible by ADMINISTRADOR and RECEPCIONISTA)
  {
    path: '/quartos',
    component: () => import('@/pages/quartos/QuartosList.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR', 'RECEPCIONISTA'] }
  },
  {
    path: '/quartos/novo',
    component: () => import('@/pages/quartos/QuartoForm.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR'] } // Only admin can create
  },
  {
    path: '/quartos/:uuid',
    component: () => import('@/pages/quartos/QuartoDetail.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR', 'RECEPCIONISTA'] }
  },
  {
    path: '/quartos/:uuid/editar',
    component: () => import('@/pages/quartos/QuartoForm.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR'] } // Only admin can edit
  },
  {
    path: '/quartos/estatisticas',
    component: () => import('@/pages/quartos/QuartosStatistics.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR', 'RECEPCIONISTA'] }
  },
  
  // Hospedagens routes
  {
    path: '/hospedagens',
    component: () => import('@/pages/hospedagens/HospedagensList.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR', 'RECEPCIONISTA'] }
  },
  {
    path: '/hospedagens/nova',
    component: () => import('@/pages/hospedagens/HospedagemForm.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR'] }
  },
  {
    path: '/hospedagens/:uuid',
    component: () => import('@/pages/hospedagens/HospedagemDetail.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR', 'RECEPCIONISTA'] }
  },
  {
    path: '/hospedagens/ativas',
    component: () => import('@/pages/hospedagens/HospedagensAtivas.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR', 'RECEPCIONISTA'] }
  },
];
```

### 3.5 Route Guard Example
```typescript
// router/guards.ts
import { useAuthStore } from '@/stores/auth';

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return next('/login');
  }
  
  if (to.meta.roles && !to.meta.roles.includes(authStore.user.role)) {
    return next('/unauthorized');
  }
  
  next();
});
```

### 3.6 Error Handling
```typescript
// utils/errorHandler.ts
import { AxiosError } from 'axios';

export const handleApiError = (error: unknown) => {
  if (error instanceof AxiosError) {
    const status = error.response?.status;
    const message = error.response?.data?.message || 'Erro desconhecido';
    
    switch (status) {
      case 400:
        return `Requisição inválida: ${message}`;
      case 401:
        return 'Sessão expirada. Faça login novamente.';
      case 403:
        return 'Você não tem permissão para esta ação.';
      case 404:
        return 'Recurso não encontrado.';
      case 500:
        return 'Erro interno do servidor. Tente novamente mais tarde.';
      default:
        return message;
    }
  }
  return 'Erro de conexão. Verifique sua internet.';
};
```

---

## 4. Key UI Components Needed

### 4.1 Quartos Module
- **QuartosList**: Table/grid with filters (nome, ala, tipo, ativo), pagination
- **QuartoForm**: Create/edit form with validation, dropdown for ala/tipo
- **QuartoDetail**: Display room info, occupancy status, action buttons (activate, deactivate, maintenance)
- **QuartosStatistics**: Dashboard with charts/cards for statistics endpoint data
- **RoomStatusBadge**: Visual indicators (Active/Inactive, Maintenance, Occupancy level)

### 4.2 Hospedagens Module
- **HospedagensList**: Paginated table with filters, search
- **HospedagemForm**: Multi-step form (patient selection, room selection, dates)
- **HospedagemDetail**: Display stay info, patient details, room history
- **HospedagensAtivas**: Real-time view of active stays
- **ExitModal**: Form for registering patient exit (dataSaida, motivoSaida)
- **TransferModal**: Room selection for patient transfer
- **OverdueList**: Alert view for stays with overdue exit dates

### 4.3 Shared Components
- **DatePicker**: For date fields (dataEntrada, dataSaida, etc.)
- **TimePicker**: For time fields (horaEntrada, horaSaida)
- **ConfirmDialog**: For delete/deactivate actions
- **LoadingSpinner**: For async operations
- **ErrorAlert**: Display API error messages
- **SuccessToast**: Confirmation messages

---

## 5. State Management Recommendations

### 5.1 Pinia Store Example (quartos)
```typescript
// stores/quartos.ts
import { defineStore } from 'pinia';
import { quartosService } from '@/services/quartos.service';
import type { QuartoResponse, QuartoResumo, EstatisticasOcupacao } from '@/types/hospedagem';

export const useQuartosStore = defineStore('quartos', {
  state: () => ({
    quartos: [] as QuartoResponse[],
    quartosAtivos: [] as QuartoResumo[],
    quartosDisponiveis: [] as QuartoResumo[],
    estatisticas: null as EstatisticasOcupacao | null,
    currentQuarto: null as QuartoResponse | null,
    loading: false,
    error: null as string | null,
  }),
  
  actions: {
    async fetchQuartos(params?: any) {
      this.loading = true;
      try {
        const response = await quartosService.list(params);
        this.quartos = response.data.content;
      } catch (error) {
        this.error = handleApiError(error);
      } finally {
        this.loading = false;
      }
    },
    
    async fetchQuartosAtivos() {
      try {
        const response = await quartosService.listActive();
        this.quartosAtivos = response.data;
      } catch (error) {
        this.error = handleApiError(error);
      }
    },
    
    async fetchEstatisticas() {
      try {
        const response = await quartosService.getStatistics();
        this.estatisticas = response.data;
      } catch (error) {
        this.error = handleApiError(error);
      }
    },
    
    async createQuarto(data: QuartoRequest) {
      this.loading = true;
      try {
        const response = await quartosService.create(data);
        this.quartos.push(response.data);
        return response.data;
      } catch (error) {
        this.error = handleApiError(error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // Add other actions...
  },
});
```

---

## 6. Testing Checklist

### 6.1 Unit Tests
- [ ] Form validation (required fields, max length, date constraints)
- [ ] API service methods (mock axios responses)
- [ ] Store actions and mutations
- [ ] Utility functions (date formatting, error handling)

### 6.2 Integration Tests
- [ ] Create room flow (form → API → list update)
- [ ] Patient admission flow (select room → register entry)
- [ ] Patient exit flow (register saida → room vacancy update)
- [ ] Transfer patient flow (select new room → update both rooms)
- [ ] Statistics dashboard data loading
- [ ] Pagination and filtering

### 6.3 E2E Tests
- [ ] Admin can create/edit/delete room
- [ ] Recepcionista can view but not edit rooms
- [ ] Admin can register patient admission
- [ ] Admin can register patient exit
- [ ] Admin can transfer patient
- [ ] Both roles can view statistics
- [ ] Proper error messages on validation failures

---

## 7. Accessibility & UX Guidelines

- **Loading States**: Show spinners during API calls
- **Empty States**: Display helpful messages when lists are empty
- **Error Messages**: Clear, actionable error feedback
- **Confirmation Dialogs**: Require confirmation for destructive actions (delete, deactivate)
- **Keyboard Navigation**: Ensure all forms are keyboard-accessible
- **ARIA Labels**: Add proper labels for screen readers
- **Responsive Design**: Mobile-friendly layouts for all views
- **Color Coding**: Use consistent colors for status badges (green=active, yellow=maintenance, red=inactive)

---

## 8. Performance Optimization

- **Lazy Loading**: Load routes/components on demand
- **Pagination**: Use backend pagination (don't load all records at once)
- **Debounce**: Debounce search inputs to reduce API calls
- **Caching**: Cache dropdown options (alas, tipos) in local storage
- **Optimistic Updates**: Update UI immediately, rollback on error
- **Virtual Scrolling**: For large lists (if needed)

---

## Appendix: Enum Values Reference

### AlaQuarto
- `FEMININA` - Feminina
- `MASCULINA` - Masculina
- `MISTA` - Mista

### TipoQuarto
- `INDIVIDUAL` - Individual (1 bed)
- `COMPARTILHADO` - Compartilhado (multiple beds)
- `ISOLAMENTO` - Isolamento (isolation room)

### StatusHospedagem
- `ATIVA` - Active stay
- `ENCERRADA` - Completed stay
- `TRANSFERIDA` - Transferred to another room

---

## Support & Troubleshooting

### Common Issues

**Issue:** 401 Unauthorized  
**Solution:** Check JWT token expiration, refresh token if needed

**Issue:** 403 Forbidden  
**Solution:** Verify user has ADMINISTRADOR or RECEPCIONISTA role

**Issue:** 400 Bad Request on room creation  
**Solution:** Validate all required fields (nome, tipo, ala, capacidadeTotal)

**Issue:** Cannot activate maintenance on occupied room  
**Solution:** Check if `capacidadeOcupada > 0`, discharge patients first

**Issue:** Date validation errors  
**Solution:** Ensure dates are in ISO 8601 format (YYYY-MM-DD)

---

## Contact
For backend API issues, contact the backend team or check application logs at `/var/log/sgca-backend/`.

Good luck with the frontend implementation! 🚀
