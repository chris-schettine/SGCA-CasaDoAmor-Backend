# Frontend Implementation Guide - Agendamentos Module

## Overview
This guide provides complete API documentation for implementing the frontend of the Agendamentos (Scheduling) module, which manages appointments for both patients and companions. The module supports conflict detection, automatic scheduling via database triggers, and comprehensive schedule management.

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
- **ADMIN**: Full access (all operations)
- **GERENTE**: Full access (all operations)
- **RECEPCIONISTA**: Create, read, update, cancel appointments
- **MEDICO**: Read appointments, confirm appointments
- **ENFERMEIRO**: Read appointments, confirm appointments

---

## Form Dropdowns & Data Population

### Tipos de Serviço Dropdown

The system provides 29 predefined service types organized by category. Use the `/api/tipos-servico` endpoint to populate the "Serviço" dropdown in your appointment form.

#### Fetching Service Types

**Endpoint:** `GET /api/tipos-servico`  
**Access:** All authenticated users  
**Response:** Array of service type objects

**TypeScript Interface:**
```typescript
interface TipoServico {
  id: number;
  codigo: string;
  nome: string;
  descricao: string;
  categoria: 'MEDICO' | 'ODONTOLOGICO' | 'ENFERMAGEM' | 'NUTRICAO' | 
             'FISIOTERAPIA' | 'PSICOLOGIA' | 'ASSISTENCIA_SOCIAL' | 'PEDAGOGIA';
  duracaoMinutos: number;
  requerProfissional: boolean;
  permiteAcompanhante: boolean;
  ativo: boolean;
  observacoes: string | null;
}
```

#### React Implementation Example

```typescript
import React, { useState, useEffect } from 'react';

interface TipoServico {
  id: number;
  codigo: string;
  nome: string;
  descricao: string;
  categoria: string;
  duracaoMinutos: number;
  requerProfissional: boolean;
  permiteAcompanhante: boolean;
  ativo: boolean;
  observacoes: string | null;
}

const AgendamentoForm: React.FC = () => {
  const [tiposServico, setTiposServico] = useState<TipoServico[]>([]);
  const [selectedServico, setSelectedServico] = useState<number | ''>('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTiposServico();
  }, []);

  const fetchTiposServico = async () => {
    try {
      const response = await fetch('http://localhost:8090/api/tipos-servico', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        // Only show active services
        const activeServices = data.filter((s: TipoServico) => s.ativo);
        setTiposServico(activeServices);
      }
    } catch (error) {
      console.error('Error fetching tipos de serviço:', error);
    } finally {
      setLoading(false);
    }
  };

  // Group services by category for better UX
  const groupedServices = tiposServico.reduce((acc, servico) => {
    const category = servico.categoria;
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(servico);
    return acc;
  }, {} as Record<string, TipoServico[]>);

  const categoryLabels: Record<string, string> = {
    MEDICO: '🏥 Médico',
    ODONTOLOGICO: '🦷 Odontológico',
    ENFERMAGEM: '💉 Enfermagem',
    NUTRICAO: '🥗 Nutrição',
    FISIOTERAPIA: '🏃 Fisioterapia',
    PSICOLOGIA: '🧠 Psicologia',
    ASSISTENCIA_SOCIAL: '🤝 Assistência Social',
    PEDAGOGIA: '📚 Pedagogia'
  };

  return (
    <div className="form-group">
      <label htmlFor="tipoServico">
        Serviço *
      </label>
      
      {loading ? (
        <div>Carregando serviços...</div>
      ) : (
        <select
          id="tipoServico"
          value={selectedServico}
          onChange={(e) => setSelectedServico(Number(e.target.value))}
          className="form-control"
          required
        >
          <option value="">Selecione um serviço</option>
          
          {Object.entries(groupedServices).map(([category, services]) => (
            <optgroup key={category} label={categoryLabels[category] || category}>
              {services.map((servico) => (
                <option key={servico.id} value={servico.id}>
                  {servico.nome} ({servico.duracaoMinutos} min)
                </option>
              ))}
            </optgroup>
          ))}
        </select>
      )}
      
      {/* Show service details when selected */}
      {selectedServico && (
        <div className="service-details mt-2">
          {(() => {
            const servico = tiposServico.find(s => s.id === selectedServico);
            return servico ? (
              <div className="alert alert-info">
                <strong>{servico.nome}</strong><br/>
                <small>{servico.descricao}</small><br/>
                <small>Duração estimada: {servico.duracaoMinutos} minutos</small>
              </div>
            ) : null;
          })()}
        </div>
      )}
    </div>
  );
};

export default AgendamentoForm;
```

#### Vue.js Implementation Example

```vue
<template>
  <div class="form-group">
    <label for="tipoServico">Serviço *</label>
    
    <select
      id="tipoServico"
      v-model="selectedServico"
      @change="onServiceChange"
      class="form-control"
      required
    >
      <option value="">Selecione um serviço</option>
      
      <optgroup
        v-for="(services, category) in groupedServices"
        :key="category"
        :label="categoryLabels[category] || category"
      >
        <option
          v-for="servico in services"
          :key="servico.id"
          :value="servico.id"
        >
          {{ servico.nome }} ({{ servico.duracaoMinutos }} min)
        </option>
      </optgroup>
    </select>
    
    <!-- Service details card -->
    <div v-if="selectedServiceDetails" class="alert alert-info mt-2">
      <strong>{{ selectedServiceDetails.nome }}</strong><br>
      <small>{{ selectedServiceDetails.descricao }}</small><br>
      <small>Duração estimada: {{ selectedServiceDetails.duracaoMinutos }} minutos</small>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';

interface TipoServico {
  id: number;
  codigo: string;
  nome: string;
  descricao: string;
  categoria: string;
  duracaoMinutos: number;
  requerProfissional: boolean;
  permiteAcompanhante: boolean;
  ativo: boolean;
  observacoes: string | null;
}

const tiposServico = ref<TipoServico[]>([]);
const selectedServico = ref<number | ''>('');

const categoryLabels: Record<string, string> = {
  MEDICO: '🏥 Médico',
  ODONTOLOGICO: '🦷 Odontológico',
  ENFERMAGEM: '💉 Enfermagem',
  NUTRICAO: '🥗 Nutrição',
  FISIOTERAPIA: '🏃 Fisioterapia',
  PSICOLOGIA: '🧠 Psicologia',
  ASSISTENCIA_SOCIAL: '🤝 Assistência Social',
  PEDAGOGIA: '📚 Pedagogia'
};

const groupedServices = computed(() => {
  return tiposServico.value.reduce((acc, servico) => {
    const category = servico.categoria;
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(servico);
    return acc;
  }, {} as Record<string, TipoServico[]>);
});

const selectedServiceDetails = computed(() => {
  if (!selectedServico.value) return null;
  return tiposServico.value.find(s => s.id === selectedServico.value);
});

const fetchTiposServico = async () => {
  try {
    const response = await fetch('http://localhost:8090/api/tipos-servico', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.ok) {
      const data = await response.json();
      // Only show active services
      tiposServico.value = data.filter((s: TipoServico) => s.ativo);
    }
  } catch (error) {
    console.error('Error fetching tipos de serviço:', error);
  }
};

const onServiceChange = () => {
  // Emit event or update parent component
  console.log('Selected service:', selectedServiceDetails.value);
};

onMounted(() => {
  fetchTiposServico();
});
</script>
```

#### Angular Implementation Example

```typescript
// tipo-servico-dropdown.component.ts
import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface TipoServico {
  id: number;
  codigo: string;
  nome: string;
  descricao: string;
  categoria: string;
  duracaoMinutos: number;
  requerProfissional: boolean;
  permiteAcompanhante: boolean;
  ativo: boolean;
  observacoes: string | null;
}

@Component({
  selector: 'app-tipo-servico-dropdown',
  template: `
    <div class="form-group">
      <label for="tipoServico">Serviço *</label>
      
      <select
        id="tipoServico"
        [(ngModel)]="selectedServico"
        (change)="onServiceChange()"
        class="form-control"
        required
      >
        <option value="">Selecione um serviço</option>
        
        <optgroup
          *ngFor="let category of categories"
          [label]="categoryLabels[category] || category"
        >
          <option
            *ngFor="let servico of getServicesByCategory(category)"
            [value]="servico.id"
          >
            {{ servico.nome }} ({{ servico.duracaoMinutos }} min)
          </option>
        </optgroup>
      </select>
      
      <div *ngIf="selectedServiceDetails" class="alert alert-info mt-2">
        <strong>{{ selectedServiceDetails.nome }}</strong><br>
        <small>{{ selectedServiceDetails.descricao }}</small><br>
        <small>Duração estimada: {{ selectedServiceDetails.duracaoMinutos }} minutos</small>
      </div>
    </div>
  `
})
export class TipoServicoDropdownComponent implements OnInit {
  @Output() serviceSelected = new EventEmitter<TipoServico>();
  
  tiposServico: TipoServico[] = [];
  selectedServico: number | '' = '';
  
  categoryLabels: Record<string, string> = {
    MEDICO: '🏥 Médico',
    ODONTOLOGICO: '🦷 Odontológico',
    ENFERMAGEM: '💉 Enfermagem',
    NUTRICAO: '🥗 Nutrição',
    FISIOTERAPIA: '🏃 Fisioterapia',
    PSICOLOGIA: '🧠 Psicologia',
    ASSISTENCIA_SOCIAL: '🤝 Assistência Social',
    PEDAGOGIA: '📚 Pedagogia'
  };
  
  get categories(): string[] {
    return [...new Set(this.tiposServico.map(s => s.categoria))];
  }
  
  get selectedServiceDetails(): TipoServico | undefined {
    if (!this.selectedServico) return undefined;
    return this.tiposServico.find(s => s.id === this.selectedServico);
  }
  
  constructor(private http: HttpClient) {}
  
  ngOnInit(): void {
    this.fetchTiposServico();
  }
  
  fetchTiposServico(): void {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    
    this.http.get<TipoServico[]>('http://localhost:8090/api/tipos-servico', { headers })
      .subscribe({
        next: (data) => {
          // Only show active services
          this.tiposServico = data.filter(s => s.ativo);
        },
        error: (error) => {
          console.error('Error fetching tipos de serviço:', error);
        }
      });
  }
  
  getServicesByCategory(category: string): TipoServico[] {
    return this.tiposServico.filter(s => s.categoria === category);
  }
  
  onServiceChange(): void {
    if (this.selectedServiceDetails) {
      this.serviceSelected.emit(this.selectedServiceDetails);
    }
  }
}
```

#### Complete List of Available Services

| ID | Nome | Categoria | Duração | Código |
|----|------|-----------|---------|--------|
| 1 | Consulta Médica Geral | MEDICO | 30 min | MED_CONSULTA_GERAL |
| 2 | Consulta com Especialista | MEDICO | 45 min | MED_CONSULTA_ESPECIALISTA |
| 3 | Retorno Médico | MEDICO | 20 min | MED_RETORNO |
| 4 | Consulta Odontológica | ODONTOLOGICO | 30 min | ODONTO_CONSULTA |
| 5 | Profilaxia Dentária | ODONTOLOGICO | 40 min | ODONTO_PROFILAXIA |
| 6 | Restauração Dentária | ODONTOLOGICO | 60 min | ODONTO_RESTAURACAO |
| 7 | Curativo | ENFERMAGEM | 20 min | ENF_CURATIVO |
| 8 | Aferição de Pressão Arterial | ENFERMAGEM | 10 min | ENF_AFERIR_PA |
| 9 | Administração de Medicação | ENFERMAGEM | 15 min | ENF_MEDICACAO |
| 10 | Coleta de Exames | ENFERMAGEM | 15 min | ENF_COLETA |
| 11 | Consulta Nutricional | NUTRICAO | 40 min | NUT_CONSULTA |
| 12 | Retorno Nutricional | NUTRICAO | 30 min | NUT_RETORNO |
| 13 | Sessão de Fisioterapia | FISIOTERAPIA | 45 min | FISIO_SESSAO |
| 14 | Avaliação Fisioterapêutica | FISIOTERAPIA | 50 min | FISIO_AVALIACAO |
| 15 | Atendimento Psicológico | PSICOLOGIA | 50 min | PSI_ATENDIMENTO |
| 16 | Avaliação Psicológica | PSICOLOGIA | 60 min | PSI_AVALIACAO |
| 17 | Atendimento Social | ASSISTENCIA_SOCIAL | 40 min | AS_ATENDIMENTO |
| 18 | Atendimento Pedagógico | PEDAGOGIA | 45 min | PED_ATENDIMENTO |
| 19 | Triagem | ENFERMAGEM | 15 min | ENF_TRIAGEM |
| 20 | Triagem | NUTRICAO | 15 min | NUT_TRIAGEM |
| 21 | Triagem Médica | MEDICO | 20 min | MED_TRIAGEM |
| 22 | Atendimento de Emergência | MEDICO | 30 min | MED_EMERGENCIA |
| 23 | Triagem Odontológica | ODONTOLOGICO | 15 min | ODONTO_TRIAGEM |
| 24 | Emergência Odontológica | ODONTOLOGICO | 30 min | ODONTO_EMERGENCIA |
| 25 | Avaliação Fisioterapêutica Inicial | FISIOTERAPIA | 50 min | FISIO_AVALIACAO_INICIAL |
| 26 | Triagem Psicológica | PSICOLOGIA | 30 min | PSI_TRIAGEM |
| 27 | Avaliação Psicológica Inicial | PSICOLOGIA | 60 min | PSI_AVALIACAO_INICIAL |
| 28 | Triagem Social | ASSISTENCIA_SOCIAL | 30 min | AS_TRIAGEM |
| 29 | Avaliação Social Inicial | ASSISTENCIA_SOCIAL | 45 min | AS_AVALIACAO_INICIAL |

#### Tips for Better UX

1. **Group by Category**: Use `<optgroup>` to organize services by category (Médico, Odontológico, etc.)
2. **Show Duration**: Display estimated duration next to each service name
3. **Filter Active Only**: Only show services where `ativo: true`
4. **Show Details**: Display service description when a service is selected
5. **Auto-populate Duration**: Automatically set the appointment duration based on selected service
6. **Category Icons**: Use emoji or icons to make categories more visually distinct
7. **Search/Filter**: For better UX, consider adding a search box for large lists
8. **Validation**: Ensure the selected service is compatible with the selected professional's category

---

## 1. Agendamentos de Pacientes (Patient Appointments) API

### 1.1 Create Patient Appointment
**Endpoint:** `POST /api/agendamentos/pacientes`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA  
**Description:** Create a new appointment for a patient with automatic conflict detection

**Request Body:**
```typescript
interface AgendamentoPacienteRequest {
  pacienteId: number;                    // Required (Long)
  tipoServicoId: number;                 // Required (Long)
  profissionalUsuarioId: number;         // Required (Long)
  dataHoraInicio: string;                // Required, ISO 8601 format
  dataHoraFim: string;                   // Required, ISO 8601 format
  hospedagemId?: number;                 // Optional (Long)
  tipoAtendimento?: "PRIMEIRA_VEZ" | "RETORNO" | "EMERGENCIAL" | "ROTINA" | "TRIAGEM";
  prioridade?: "BAIXA" | "NORMAL" | "ALTA" | "URGENTE";
  status?: "AGENDADO" | "CONFIRMADO" | "EM_ATENDIMENTO" | "CONCLUIDO" | "CANCELADO";
  observacoes?: string;                  // Optional
  motivoCancelamento?: string;           // Optional
  confirmadoPaciente?: boolean;          // Optional, default: false
  confirmadoProfissional?: boolean;      // Optional, default: false
}
```

**Response (201):**
```typescript
interface AgendamentoPacienteResponse {
  id: number;
  uuid: string;
  pacienteId: number;
  pacienteNome: string;
  tipoServicoId: number;
  tipoServicoNome: string;
  profissionalUsuarioId: number;
  profissionalNome: string;
  hospedagemId: number | null;
  dataHoraInicio: string;                // ISO 8601
  dataHoraFim: string;                   // ISO 8601
  tipoAtendimento: string | null;
  prioridade: string | null;
  status: string;
  observacoes: string | null;
  motivoCancelamento: string | null;
  confirmadoPaciente: boolean;
  confirmadoProfissional: boolean;
  compareceu: boolean | null;
  horaChegada: string | null;            // ISO 8601
  horaInicioAtendimento: string | null;  // ISO 8601
  horaFimAtendimento: string | null;     // ISO 8601
  agendamentoRemarcarId: number | null;
  geradoAutomaticamente: boolean;
  motivoGeracaoAutomatica: string | null;
  createdAt: string;                     // ISO 8601
  updatedAt: string;                     // ISO 8601
}
```

**Example cURL:**
```bash
curl -X POST http://localhost:8090/api/agendamentos/pacientes \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 1,
    "tipoServicoId": 5,
    "profissionalUsuarioId": 10,
    "dataHoraInicio": "2024-12-15T10:00:00",
    "dataHoraFim": "2024-12-15T11:00:00",
    "tipoAtendimento": "PRIMEIRA_VEZ",
    "prioridade": "NORMAL",
    "observacoes": "Paciente com restrições alimentares"
  }'
```

**Error Responses:**
- `400 Bad Request`: Validation errors or conflict detected
- `404 Not Found`: Patient, professional, or service type not found
- `500 Internal Server Error`: Server error

---

### 1.2 Get Patient Appointment by UUID
**Endpoint:** `GET /api/agendamentos/pacientes/{uuid}`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO  
**Description:** Retrieve details of a specific appointment

**Path Parameters:**
- `uuid` (string): Appointment UUID

**Response (200):** Same as AgendamentoPacienteResponse

**Example:**
```bash
curl -X GET http://localhost:8090/api/agendamentos/pacientes/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 1.3 List Appointments by Patient
**Endpoint:** `GET /api/agendamentos/pacientes/paciente/{pacienteId}`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO  
**Description:** List all appointments for a specific patient

**Path Parameters:**
- `pacienteId` (string): Patient ID

**Response (200):**
```typescript
AgendamentoPacienteResponse[]
```

**Example:**
```bash
curl -X GET http://localhost:8090/api/agendamentos/pacientes/paciente/PAC001 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 1.4 List Appointments by Professional
**Endpoint:** `GET /api/agendamentos/pacientes/profissional/{profissionalId}`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO  
**Description:** List all appointments for a professional within a date range

**Path Parameters:**
- `profissionalId` (number): Professional user ID

**Query Parameters:**
```typescript
interface ProfissionalAgendaParams {
  inicio: string;    // Required, ISO 8601 format (e.g., "2024-12-01T00:00:00")
  fim: string;       // Required, ISO 8601 format (e.g., "2024-12-31T23:59:59")
}
```

**Response (200):**
```typescript
AgendamentoPacienteResponse[]
```

**Example:**
```bash
curl -X GET "http://localhost:8090/api/agendamentos/pacientes/profissional/10?inicio=2024-12-01T00:00:00&fim=2024-12-31T23:59:59" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 1.5 Check Scheduling Conflict
**Endpoint:** `POST /api/agendamentos/pacientes/verificar-conflito`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA  
**Description:** Check if a time slot is available for a professional

**Query Parameters:**
```typescript
interface ConflictCheckParams {
  profissionalId: number;    // Required
  inicio: string;            // Required, ISO 8601
  fim: string;               // Required, ISO 8601
}
```

**Response (200):**
```typescript
interface ConflictCheckResponse {
  temConflito: boolean;
  mensagem: string;
  details?: {
    tipo: "AGENDAMENTO" | "BLOQUEIO" | "FORA_HORARIO";
    descricao: string;
    conflitanteInicio: string;    // ISO 8601
    conflitanteFim: string;       // ISO 8601
    profissionalNome: string;
  };
}
```

**Example (No Conflict):**
```bash
curl -X POST "http://localhost:8090/api/agendamentos/pacientes/verificar-conflito?profissionalId=10&inicio=2024-12-15T10:00:00&fim=2024-12-15T11:00:00" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Response:
```json
{
  "temConflito": false,
  "mensagem": "Horário disponível",
  "details": null
}
```

**Example (With Conflict):**
Response:
```json
{
  "temConflito": true,
  "mensagem": "Profissional já possui agendamento neste horário",
  "details": {
    "tipo": "AGENDAMENTO",
    "descricao": "Consulta com paciente Pedro Costa",
    "conflitanteInicio": "2024-12-15T10:00:00",
    "conflitanteFim": "2024-12-15T11:00:00",
    "profissionalNome": "Dr. Maria Santos"
  }
}
```

---

### 1.6 Confirm Appointment
**Endpoint:** `PUT /api/agendamentos/pacientes/{uuid}/confirmar`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO  
**Description:** Confirm an appointment (by patient or professional)

**Path Parameters:**
- `uuid` (string): Appointment UUID

**Query Parameters:**
```typescript
interface ConfirmarParams {
  confirmadoPeloPaciente?: boolean;  // Default: true
}
```

**Response (200):** AgendamentoPacienteResponse with updated confirmation fields

**Example:**
```bash
# Confirm by patient
curl -X PUT "http://localhost:8090/api/agendamentos/pacientes/550e8400-e29b-41d4-a716-446655440000/confirmar?confirmadoPeloPaciente=true" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Confirm by professional
curl -X PUT "http://localhost:8090/api/agendamentos/pacientes/550e8400-e29b-41d4-a716-446655440000/confirmar?confirmadoPeloPaciente=false" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 1.7 Cancel Appointment
**Endpoint:** `DELETE /api/agendamentos/pacientes/{uuid}`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA  
**Description:** Cancel an existing appointment

**Path Parameters:**
- `uuid` (string): Appointment UUID

**Query Parameters:**
```typescript
interface CancelarParams {
  motivo: string;    // Required, cancellation reason
}
```

**Response (200):**
```typescript
interface MessageResponse {
  success: boolean;
  message: string;
}
```

**Example:**
```bash
curl -X DELETE "http://localhost:8090/api/agendamentos/pacientes/550e8400-e29b-41d4-a716-446655440000?motivo=Paciente%20solicitou%20cancelamento" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Response:
```json
{
  "success": true,
  "message": "Agendamento cancelado com sucesso"
}
```

---

## 2. Agendamentos de Acompanhantes (Companion Appointments) API

### 2.1 Create Companion Appointment
**Endpoint:** `POST /api/agendamentos/acompanhantes`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA  
**Description:** Create a new appointment for a companion

**Request Body:**
```typescript
interface AgendamentoAcompanhanteRequest {
  acompanhanteId: number;                // Required (Long)
  tipoServicoId: number;                 // Required (Long)
  profissionalUsuarioId: number;         // Required (Long)
  dataHoraInicio: string;                // Required, ISO 8601 format
  dataHoraFim: string;                   // Required, ISO 8601 format
  pacienteVinculadoId?: number;          // Optional (Long)
  tipoAtendimento?: "PRIMEIRA_VEZ" | "RETORNO" | "EMERGENCIAL" | "ROTINA" | "TRIAGEM";
  prioridade?: "BAIXA" | "NORMAL" | "ALTA" | "URGENTE";
  status?: "AGENDADO" | "CONFIRMADO" | "EM_ATENDIMENTO" | "CONCLUIDO" | "CANCELADO";
  observacoes?: string;                  // Optional
  motivoCancelamento?: string;           // Optional
  confirmadoAcompanhante?: boolean;      // Optional, default: false
  confirmadoProfissional?: boolean;      // Optional, default: false
}
```

**Response (201):**
```typescript
interface AgendamentoAcompanhanteResponse {
  id: number;
  uuid: string;
  acompanhanteId: number;
  acompanhanteNome: string;
  tipoServicoId: number;
  tipoServicoNome: string;
  profissionalUsuarioId: number;
  profissionalNome: string;
  pacienteVinculadoId: number | null;
  pacienteVinculadoNome: string | null;
  dataHoraInicio: string;                // ISO 8601
  dataHoraFim: string;                   // ISO 8601
  tipoAtendimento: string | null;
  prioridade: string | null;
  status: string;
  observacoes: string | null;
  motivoCancelamento: string | null;
  confirmadoAcompanhante: boolean;
  confirmadoProfissional: boolean;
  compareceu: boolean | null;
  horaChegada: string | null;            // ISO 8601
  horaInicioAtendimento: string | null;  // ISO 8601
  horaFimAtendimento: string | null;     // ISO 8601
  agendamentoRemarcarId: number | null;
  createdAt: string;                     // ISO 8601
  updatedAt: string;                     // ISO 8601
}
```

**Example cURL:**
```bash
curl -X POST http://localhost:8090/api/agendamentos/acompanhantes \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "acompanhanteId": 5,
    "tipoServicoId": 8,
    "profissionalUsuarioId": 12,
    "dataHoraInicio": "2024-12-16T14:00:00",
    "dataHoraFim": "2024-12-16T15:00:00",
    "pacienteVinculadoId": 1,
    "tipoAtendimento": "PRIMEIRA_VEZ",
    "prioridade": "NORMAL"
  }'
```

---

### 2.2 Get Companion Appointment by UUID
**Endpoint:** `GET /api/agendamentos/acompanhantes/{uuid}`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO  
**Description:** Retrieve details of a specific companion appointment

**Path Parameters:**
- `uuid` (string): Appointment UUID

**Response (200):** Same as AgendamentoAcompanhanteResponse

---

### 2.3 List Appointments by Companion
**Endpoint:** `GET /api/agendamentos/acompanhantes/acompanhante/{acompanhanteId}`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO  
**Description:** List all appointments for a specific companion

**Path Parameters:**
- `acompanhanteId` (string): Companion ID

**Response (200):**
```typescript
AgendamentoAcompanhanteResponse[]
```

---

### 2.4 List Appointments by Professional
**Endpoint:** `GET /api/agendamentos/acompanhantes/profissional/{profissionalId}`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO  
**Description:** List companion appointments for a professional within a date range

**Path Parameters:**
- `profissionalId` (number): Professional user ID

**Query Parameters:** Same as patient appointments

**Response (200):**
```typescript
AgendamentoAcompanhanteResponse[]
```

---

### 2.5 Check Scheduling Conflict
**Endpoint:** `POST /api/agendamentos/acompanhantes/verificar-conflito`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA  
**Description:** Check if a time slot is available (same as patient appointments)

**Parameters & Response:** Same as patient appointments conflict check

---

### 2.6 Confirm Companion Appointment
**Endpoint:** `PUT /api/agendamentos/acompanhantes/{uuid}/confirmar`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO  
**Description:** Confirm a companion appointment

**Path Parameters:**
- `uuid` (string): Appointment UUID

**Query Parameters:**
```typescript
interface ConfirmarParams {
  confirmadoPeloAcompanhante?: boolean;  // Default: true
}
```

**Response (200):** AgendamentoAcompanhanteResponse with updated confirmation

---

### 2.7 Cancel Companion Appointment
**Endpoint:** `DELETE /api/agendamentos/acompanhantes/{uuid}`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA  
**Description:** Cancel a companion appointment

**Parameters & Response:** Same as patient appointment cancellation

---

## 3. Enums Reference

### TipoAtendimento
```typescript
enum TipoAtendimento {
  PRIMEIRA_VEZ = "Primeira Vez",
  RETORNO = "Retorno",
  EMERGENCIAL = "Emergencial",
  ROTINA = "Rotina",
  TRIAGEM = "Triagem"
}
```

### Prioridade
```typescript
enum Prioridade {
  BAIXA = "Baixa",
  NORMAL = "Normal",
  ALTA = "Alta",
  URGENTE = "Urgente"
}
```

### StatusAgendamento
```typescript
enum StatusAgendamento {
  AGENDADO = "Agendado",
  CONFIRMADO = "Confirmado",
  EM_ATENDIMENTO = "Em Atendimento",
  CONCLUIDO = "Concluído",
  CANCELADO = "Cancelado",
  REMARCADO = "Remarcado",
  FALTOSO = "Faltoso",
  PACIENTE_NAO_COMPARECEU = "Paciente Não Compareceu"
}
```

### EspecialidadeProfissional
```typescript
enum EspecialidadeProfissional {
  MEDICO = "Médico",
  ENFERMAGEM = "Enfermagem",
  NUTRICAO = "Nutrição",
  ODONTOLOGICO = "Odontológico",
  FISIOTERAPIA = "Fisioterapia",
  PSICOLOGIA = "Psicologia",
  ASSISTENCIA_SOCIAL = "Assistência Social"
}
```

---

## 4. Frontend Implementation Patterns

### 4.1 Scheduling Workflow
```typescript
// 1. Check for conflicts before creating appointment
async function checkAndCreateAppointment(data: AgendamentoPacienteRequest) {
  // Step 1: Check conflict
  const conflictCheck = await fetch(
    `/api/agendamentos/pacientes/verificar-conflito?` +
    `profissionalId=${data.profissionalUsuarioId}&` +
    `inicio=${data.dataHoraInicio}&` +
    `fim=${data.dataHoraFim}`,
    { method: 'POST', headers: authHeaders }
  );
  
  const conflict = await conflictCheck.json();
  
  if (conflict.temConflito) {
    // Show conflict message to user
    alert(`Conflito: ${conflict.mensagem}`);
    return;
  }
  
  // Step 2: Create appointment
  const response = await fetch('/api/agendamentos/pacientes', {
    method: 'POST',
    headers: { ...authHeaders, 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  
  if (response.ok) {
    const appointment = await response.json();
    console.log('Appointment created:', appointment.uuid);
  }
}
```

### 4.2 Professional Schedule View
```typescript
async function loadProfessionalSchedule(profissionalId: number, date: Date) {
  const startOfDay = new Date(date);
  startOfDay.setHours(0, 0, 0, 0);
  
  const endOfDay = new Date(date);
  endOfDay.setHours(23, 59, 59, 999);
  
  // Load patient appointments
  const patientsResponse = await fetch(
    `/api/agendamentos/pacientes/profissional/${profissionalId}?` +
    `inicio=${startOfDay.toISOString()}&` +
    `fim=${endOfDay.toISOString()}`,
    { headers: authHeaders }
  );
  
  // Load companion appointments
  const companionsResponse = await fetch(
    `/api/agendamentos/acompanhantes/profissional/${profissionalId}?` +
    `inicio=${startOfDay.toISOString()}&` +
    `fim=${endOfDay.toISOString()}`,
    { headers: authHeaders }
  );
  
  const patients = await patientsResponse.json();
  const companions = await companionsResponse.json();
  
  // Merge and sort by time
  const allAppointments = [...patients, ...companions].sort(
    (a, b) => new Date(a.dataHoraInicio).getTime() - new Date(b.dataHoraInicio).getTime()
  );
  
  return allAppointments;
}
```

### 4.3 Confirmation Handler
```typescript
async function confirmAppointment(
  uuid: string,
  confirmedBy: 'patient' | 'professional',
  type: 'patient' | 'companion'
) {
  const endpoint = type === 'patient'
    ? `/api/agendamentos/pacientes/${uuid}/confirmar`
    : `/api/agendamentos/acompanhantes/${uuid}/confirmar`;
  
  const paramName = type === 'patient'
    ? 'confirmadoPeloPaciente'
    : 'confirmadoPeloAcompanhante';
  
  const confirmByPatientOrCompanion = confirmedBy === 'patient';
  
  const response = await fetch(
    `${endpoint}?${paramName}=${confirmByPatientOrCompanion}`,
    { method: 'PUT', headers: authHeaders }
  );
  
  if (response.ok) {
    const updated = await response.json();
    console.log('Appointment confirmed:', updated);
  }
}
```

### 4.4 Cancel Appointment
```typescript
async function cancelAppointment(
  uuid: string,
  motivo: string,
  type: 'patient' | 'companion'
) {
  const endpoint = type === 'patient'
    ? `/api/agendamentos/pacientes/${uuid}`
    : `/api/agendamentos/acompanhantes/${uuid}`;
  
  const response = await fetch(
    `${endpoint}?motivo=${encodeURIComponent(motivo)}`,
    { method: 'DELETE', headers: authHeaders }
  );
  
  if (response.ok) {
    const result = await response.json();
    console.log(result.message);
  }
}
```

---

## 5. UI Component Suggestions

### 5.1 Schedule Calendar View
- **Weekly/Daily View**: Display appointments in a calendar grid
- **Color Coding**: 
  - Green: Confirmed appointments
  - Yellow: Pending confirmation
  - Red: Conflicts or cancelled
  - Blue: Auto-generated appointments
- **Time Slots**: Show 30-minute or 1-hour intervals
- **Drag & Drop**: Allow rescheduling (with conflict check)

### 5.2 Appointment Creation Form
**Fields:**
- Patient/Companion selector (autocomplete)
- Service type dropdown
- Professional selector (filtered by specialty)
- Date & time pickers (start/end)
- Priority selector
- Appointment type selector
- Notes textarea

**Validations:**
- Check if end time > start time
- Run conflict check before submitting
- Show available time slots

### 5.3 Conflict Indicator
```tsx
// Example React component
function ConflictIndicator({ conflict }: { conflict: ConflictCheckResponse }) {
  if (!conflict.temConflito) {
    return <span className="text-green-600">✓ Horário disponível</span>;
  }
  
  return (
    <div className="text-red-600">
      <p>⚠️ {conflict.mensagem}</p>
      {conflict.details && (
        <small>
          {conflict.details.tipo}: {conflict.details.descricao}
        </small>
      )}
    </div>
  );
}
```

### 5.4 Appointment Card
Display appointment details with:
- Patient/Companion name
- Service type
- Professional name
- Date & time
- Status badge
- Confirmation checkboxes
- Action buttons (confirm, cancel, reschedule)

---

## 6. Database Features (Backend Context)

### 6.1 Automatic Scheduling Trigger
When a patient is admitted with `sonda = TRUE`, the database automatically creates a `NUT_TRIAGEM` (Nutrition Screening) appointment via trigger `trg_agendar_triagem_enfermagem_after_hospedagem`.

**Frontend Implication:** 
- Display auto-generated appointments with special badge
- Show `motivoGeracaoAutomatica` field
- Mark with `geradoAutomaticamente = true`

### 6.2 Conflict Detection
The system checks for:
1. **Professional schedule blocks** (`bloqueios_agenda`): Vacations, meetings, training
2. **Professional working hours** (`horarios_profissionais`): Mon-Fri working schedule
3. **Existing appointments**: Overlapping time slots

**Frontend Best Practice:**
Always call `/verificar-conflito` endpoint before showing the create appointment form submit button.

### 6.3 Soft Deletes
Appointments use soft deletes (`deletedAt` field). Cancelled appointments remain in database for audit trails.

**Frontend Implication:**
- Filter out deleted appointments in lists
- Show cancellation reason in appointment history

---

## 7. Error Handling

### Common Error Responses

**400 Bad Request - Validation Error:**
```json
{
  "timestamp": "2024-12-01T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "dataHoraInicio",
      "message": "Data e hora de início são obrigatórias"
    }
  ]
}
```

**404 Not Found:**
```json
{
  "timestamp": "2024-12-01T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Paciente não encontrado"
}
```

**409 Conflict:**
```json
{
  "timestamp": "2024-12-01T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Conflito de horário: Profissional já possui agendamento neste horário"
}
```

### Frontend Error Handling Pattern
```typescript
async function handleApiCall<T>(
  fetch: () => Promise<Response>
): Promise<T | null> {
  try {
    const response = await fetch();
    
    if (!response.ok) {
      const error = await response.json();
      
      if (response.status === 400 && error.errors) {
        // Validation errors
        showValidationErrors(error.errors);
      } else {
        // Generic error
        showErrorToast(error.message);
      }
      
      return null;
    }
    
    return await response.json();
  } catch (err) {
    showErrorToast('Erro de conexão com o servidor');
    return null;
  }
}
```

---

## 8. Testing Checklist

### API Testing
- [ ] Create patient appointment successfully
- [ ] Create companion appointment successfully
- [ ] Conflict detection returns correct response
- [ ] Confirm appointment by patient
- [ ] Confirm appointment by professional
- [ ] Cancel appointment with reason
- [ ] List appointments by patient
- [ ] List appointments by companion
- [ ] List appointments by professional with date range
- [ ] Get appointment by UUID
- [ ] Handle validation errors (missing required fields)
- [ ] Handle 404 errors (invalid IDs)
- [ ] Handle conflict errors

### UI/UX Testing
- [ ] Calendar displays appointments correctly
- [ ] Time slot selection works properly
- [ ] Conflict indicator shows warnings
- [ ] Confirmation toggles update correctly
- [ ] Cancellation dialog captures reason
- [ ] Auto-generated appointments have special badge
- [ ] Loading states during API calls
- [ ] Error messages display appropriately
- [ ] Success notifications after operations
- [ ] Date/time pickers validate input
- [ ] Responsive design on mobile devices

### Role-Based Testing
- [ ] ADMIN can perform all operations
- [ ] GERENTE can perform all operations
- [ ] RECEPCIONISTA can create/update/cancel
- [ ] MEDICO can only read and confirm
- [ ] ENFERMEIRO can only read and confirm
- [ ] Unauthorized roles receive 403 Forbidden

---

## 9. Sample Data for Testing

### Test Patient Appointment
```json
{
  "pacienteId": 1,
  "tipoServicoId": 5,
  "profissionalUsuarioId": 10,
  "dataHoraInicio": "2024-12-15T10:00:00",
  "dataHoraFim": "2024-12-15T11:00:00",
  "tipoAtendimento": "PRIMEIRA_VEZ",
  "prioridade": "NORMAL",
  "status": "AGENDADO",
  "observacoes": "Primeira consulta - paciente nervoso"
}
```

### Test Companion Appointment
```json
{
  "acompanhanteId": 3,
  "tipoServicoId": 8,
  "profissionalUsuarioId": 12,
  "dataHoraInicio": "2024-12-16T14:00:00",
  "dataHoraFim": "2024-12-16T15:00:00",
  "pacienteVinculadoId": 1,
  "tipoAtendimento": "ROTINA",
  "prioridade": "BAIXA"
}
```

---

## 10. Swagger/OpenAPI Documentation

Access the interactive API documentation at:
```
http://localhost:8090/swagger-ui/index.html
```

Select **"public"** from the dropdown menu to see all scheduling endpoints with:
- Complete request/response schemas
- Try-it-out functionality
- Authentication configuration
- Example values

---

## 11. Statistics & Dashboard Endpoint

### 11.1 Get Complete Statistics
**Endpoint:** `GET /api/agendamentos/estatisticas`  
**Access:** ADMIN, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO, NUTRICIONISTA, DENTISTA  
**Description:** Retrieve comprehensive statistics and metrics for the scheduling dashboard

**Response (200):**
```typescript
interface EstatisticasAgendamentoDTO {
  // ===== GENERAL STATISTICS =====
  totalAgendamentosAtivos: number;
  totalAgendamentosHoje: number;
  totalAgendamentosSemana: number;
  totalAgendamentosMes: number;
  totalAgendamentosAno: number;
  
  // ===== STATISTICS BY STATUS =====
  agendamentosAgendados: number;
  agendamentosConfirmados: number;
  agendamentosEmAtendimento: number;
  agendamentosConcluidos: number;
  agendamentosCancelados: number;
  agendamentosNaoCompareceram: number;
  
  // ===== STATISTICS BY TYPE =====
  agendamentosPacientes: number;
  agendamentosAcompanhantes: number;
  agendamentosAutomaticos: number;
  
  // ===== STATISTICS BY PRIORITY =====
  agendamentosUrgentes: number;
  agendamentosAltaPrioridade: number;
  agendamentosNormalPrioridade: number;
  agendamentosBaixaPrioridade: number;
  
  // ===== STATISTICS BY APPOINTMENT TYPE =====
  agendamentosPrimeiraVez: number;
  agendamentosRetorno: number;
  agendamentosEmergenciais: number;
  agendamentosRotina: number;
  agendamentosTriagem: number;
  
  // ===== CONFIRMATIONS =====
  agendamentosPendentesConfirmacao: number;
  agendamentosConfirmadosPaciente: number;
  agendamentosConfirmadosProfissional: number;
  agendamentosConfirmadosAmbos: number;
  
  // ===== TOP PROFESSIONALS =====
  topProfissionaisPorAgendamentos: ProfissionalEstatistica[];
  topProfissionaisPorConcluidos: ProfissionalEstatistica[];
  
  // ===== TOP SERVICES =====
  topServicosMaisSolicitados: ServicoEstatistica[];
  
  // ===== DISTRIBUTION BY DAY OF WEEK =====
  agendamentosPorDiaSemana: { [key: string]: number };
  
  // ===== DISTRIBUTION BY HOUR =====
  agendamentosPorHora: { [hour: number]: number };
  
  // ===== ATTENDANCE RATES =====
  taxaComparecimento: number;          // Percentage
  taxaNaoComparecimento: number;       // Percentage
  taxaCancelamento: number;            // Percentage
  
  // ===== AVERAGE TIME =====
  duracaoMediaMinutos: number;
  tempoMedioEsperaMinutos: number;
  
  // ===== METADATA =====
  dataHoraConsulta: string;            // ISO 8601
  periodoAnalisado: string;
}

interface ProfissionalEstatistica {
  profissionalId: number;
  profissionalNome: string;
  especialidade: string;
  totalAgendamentos: number;
  agendamentosConcluidos: number;
  taxaConclusao: number;               // Percentage
}

interface ServicoEstatistica {
  servicoId: number;
  servicoNome: string;
  categoria: string;
  totalAgendamentos: number;
  percentualTotal: number;             // Percentage
}
```

**Example cURL:**
```bash
curl -X GET http://localhost:8090/api/agendamentos/estatisticas \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Example Response:**
```json
{
  "totalAgendamentosAtivos": 150,
  "totalAgendamentosHoje": 12,
  "totalAgendamentosSemana": 45,
  "totalAgendamentosMes": 120,
  "totalAgendamentosAno": 800,
  "agendamentosAgendados": 30,
  "agendamentosConfirmados": 50,
  "agendamentosEmAtendimento": 5,
  "agendamentosConcluidos": 600,
  "agendamentosCancelados": 100,
  "agendamentosNaoCompareceram": 65,
  "agendamentosPacientes": 120,
  "agendamentosAcompanhantes": 30,
  "agendamentosAutomaticos": 25,
  "agendamentosUrgentes": 10,
  "agendamentosAltaPrioridade": 25,
  "agendamentosNormalPrioridade": 100,
  "agendamentosBaixaPrioridade": 15,
  "agendamentosPrimeiraVez": 40,
  "agendamentosRetorno": 80,
  "agendamentosEmergenciais": 15,
  "agendamentosRotina": 90,
  "agendamentosTriagem": 25,
  "agendamentosPendentesConfirmacao": 20,
  "agendamentosConfirmadosPaciente": 60,
  "agendamentosConfirmadosProfissional": 55,
  "agendamentosConfirmadosAmbos": 50,
  "topProfissionaisPorAgendamentos": [
    {
      "profissionalId": 10,
      "profissionalNome": "Dr. João Silva",
      "especialidade": "MEDICO",
      "totalAgendamentos": 45,
      "agendamentosConcluidos": 40,
      "taxaConclusao": 88.89
    },
    {
      "profissionalId": 12,
      "profissionalNome": "Dra. Maria Santos",
      "especialidade": "NUTRICIONISTA",
      "totalAgendamentos": 38,
      "agendamentosConcluidos": 35,
      "taxaConclusao": 92.11
    }
  ],
  "topProfissionaisPorConcluidos": [
    {
      "profissionalId": 10,
      "profissionalNome": "Dr. João Silva",
      "especialidade": "MEDICO",
      "totalAgendamentos": 0,
      "agendamentosConcluidos": 40,
      "taxaConclusao": 0.0
    }
  ],
  "topServicosMaisSolicitados": [
    {
      "servicoId": 5,
      "servicoNome": "Consulta Médica",
      "categoria": "CONSULTA",
      "totalAgendamentos": 60,
      "percentualTotal": 40.0
    },
    {
      "servicoId": 8,
      "servicoNome": "Nutrição - Triagem",
      "categoria": "TRIAGEM",
      "totalAgendamentos": 25,
      "percentualTotal": 16.67
    }
  ],
  "agendamentosPorDiaSemana": {
    "segunda-feira": 25,
    "terça-feira": 30,
    "quarta-feira": 28,
    "quinta-feira": 22,
    "sexta-feira": 27,
    "sábado": 10,
    "domingo": 8
  },
  "agendamentosPorHora": {
    "8": 15,
    "9": 20,
    "10": 18,
    "11": 12,
    "13": 10,
    "14": 22,
    "15": 18,
    "16": 15,
    "17": 10
  },
  "taxaComparecimento": 90.23,
  "taxaNaoComparecimento": 9.77,
  "taxaCancelamento": 12.5,
  "duracaoMediaMinutos": 45.5,
  "tempoMedioEsperaMinutos": 0.0,
  "dataHoraConsulta": "2024-12-01T15:30:00",
  "periodoAnalisado": "Geral"
}
```

### 11.2 Dashboard Implementation

**Example Dashboard Layout:**

```typescript
async function loadDashboardStatistics() {
  const response = await fetch('/api/agendamentos/estatisticas', {
    headers: authHeaders
  });
  
  const stats: EstatisticasAgendamentoDTO = await response.json();
  
  return {
    // Summary Cards
    todayAppointments: stats.totalAgendamentosHoje,
    weekAppointments: stats.totalAgendamentosSemana,
    monthAppointments: stats.totalAgendamentosMes,
    
    // Status Breakdown
    statusBreakdown: {
      scheduled: stats.agendamentosAgendados,
      confirmed: stats.agendamentosConfirmados,
      inProgress: stats.agendamentosEmAtendimento,
      completed: stats.agendamentosConcluidos,
      cancelled: stats.agendamentosCancelados,
      noShow: stats.agendamentosNaoCompareceram
    },
    
    // Charts Data
    weeklyDistribution: stats.agendamentosPorDiaSemana,
    hourlyDistribution: stats.agendamentosPorHora,
    
    // Performance Metrics
    attendanceRate: stats.taxaComparecimento,
    cancellationRate: stats.taxaCancelamento,
    averageDuration: stats.duracaoMediaMinutos,
    
    // Top Lists
    topProfessionals: stats.topProfissionaisPorAgendamentos,
    topServices: stats.topServicosMaisSolicitados
  };
}
```

**Dashboard Widgets:**

1. **Summary Cards:**
   - Today's Appointments
   - This Week's Appointments
   - This Month's Appointments
   - Pending Confirmations

2. **Status Pie Chart:**
   - Breakdown by status (Scheduled, Confirmed, In Progress, Completed, Cancelled, No-Show)

3. **Weekly Distribution Bar Chart:**
   - Appointments per day of week

4. **Hourly Distribution Line Chart:**
   - Appointments per hour (peak times)

5. **Top Professionals Table:**
   - Name, Specialty, Total Appointments, Completion Rate

6. **Top Services Table:**
   - Service Name, Category, Total Appointments, Percentage

7. **KPI Metrics:**
   - Attendance Rate (with trend indicator)
   - Cancellation Rate
   - Average Duration
   - Automatic Appointments Count

8. **Priority Distribution:**
   - Urgent, High, Normal, Low

9. **Type Distribution:**
   - First Time, Return, Emergency, Routine, Screening

---

## Questions & Support

For additional questions or clarifications about the Agendamentos API:
1. Check the Swagger documentation at `/swagger-ui/index.html`
2. Review the implementation guide: `SCHEDULING_API_ENDPOINTS.md`
3. Examine the controller source code in `src/main/java/br/com/casadoamor/sgca/modules/agendamento/controller/`

---

**Last Updated:** December 1, 2025  
**API Version:** v1.1  
**Backend Commit:** Latest on `dev` branch with statistics endpoint
