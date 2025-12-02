# Agendamentos Statistics API - Complete Guide

## Overview
This guide provides comprehensive documentation for the Agendamentos Statistics endpoint, which delivers real-time metrics and analytics for appointment management. The endpoint aggregates data from both patient (`agendamentos_pacientes`) and companion (`agendamentos_acompanhantes`) appointments to provide a complete view of scheduling operations.

---

## Quick Reference

**Endpoint:** `GET /api/agendamentos/estatisticas`  
**Authentication:** Required (JWT Bearer token)  
**Authorized Roles:** ADMINISTRADOR, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO, NUTRICIONISTA, DENTISTA  
**Response Format:** JSON  
**Cache:** None (real-time data)

---

## API Endpoint

### Get Complete Statistics

```http
GET /api/agendamentos/estatisticas HTTP/1.1
Host: localhost:8090
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**cURL Example:**
```bash
curl -X GET http://localhost:8090/api/agendamentos/estatisticas \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json"
```

---

## Response Structure

### Complete Response Schema

```typescript
interface EstatisticasAgendamentoDTO {
  // ===== GENERAL STATISTICS =====
  totalAgendamentosAtivos: number;        // Total active appointments (not deleted)
  totalAgendamentosHoje: number;          // Appointments scheduled for today
  totalAgendamentosSemana: number;        // Appointments this week (Mon-Sun)
  totalAgendamentosMes: number;           // Appointments this month
  totalAgendamentosAno: number;           // Appointments this year
  
  // ===== STATISTICS BY STATUS =====
  agendamentosAgendados: number;          // Status: AGENDADO
  agendamentosConfirmados: number;        // Status: CONFIRMADO
  agendamentosEmAtendimento: number;      // Status: EM_ATENDIMENTO
  agendamentosConcluidos: number;         // Status: CONCLUIDO
  agendamentosCancelados: number;         // Status: CANCELADO
  agendamentosNaoCompareceram: number;    // Status: PACIENTE_NAO_COMPARECEU
  
  // ===== STATISTICS BY TYPE =====
  agendamentosPacientes: number;          // Patient appointments
  agendamentosAcompanhantes: number;      // Companion appointments
  agendamentosAutomaticos: number;        // Auto-generated appointments (e.g., triagem)
  
  // ===== STATISTICS BY PRIORITY =====
  agendamentosUrgentes: number;           // Priority: URGENTE
  agendamentosAltaPrioridade: number;     // Priority: ALTA
  agendamentosNormalPrioridade: number;   // Priority: NORMAL
  agendamentosBaixaPrioridade: number;    // Priority: BAIXA
  
  // ===== STATISTICS BY APPOINTMENT TYPE =====
  agendamentosPrimeiraVez: number;        // Type: PRIMEIRA_VEZ
  agendamentosRetorno: number;            // Type: RETORNO
  agendamentosEmergenciais: number;       // Type: EMERGENCIAL
  agendamentosRotina: number;             // Type: ROTINA
  agendamentosTriagem: number;            // Type: TRIAGEM
  
  // ===== CONFIRMATIONS =====
  agendamentosPendentesConfirmacao: number;     // Not confirmed by either party
  agendamentosConfirmadosPaciente: number;      // Confirmed by patient/companion
  agendamentosConfirmadosProfissional: number;  // Confirmed by professional
  agendamentosConfirmadosAmbos: number;         // Confirmed by both parties
  
  // ===== TOP PROFESSIONALS =====
  topProfissionaisPorAgendamentos: ProfissionalEstatistica[];  // Top 10 by total appointments
  topProfissionaisPorConcluidos: ProfissionalEstatistica[];    // Top 10 by completed appointments
  
  // ===== TOP SERVICES =====
  topServicosMaisSolicitados: ServicoEstatistica[];  // Top 10 most requested services
  
  // ===== DISTRIBUTION BY DAY OF WEEK =====
  agendamentosPorDiaSemana: {
    "segunda-feira": number;
    "terça-feira": number;
    "quarta-feira": number;
    "quinta-feira": number;
    "sexta-feira": number;
    "sábado": number;
    "domingo": number;
  };
  
  // ===== DISTRIBUTION BY HOUR =====
  agendamentosPorHora: {
    [hour: 0-23]: number;  // Appointments per hour (24-hour format)
  };
  
  // ===== ATTENDANCE RATES =====
  taxaComparecimento: number;          // Attendance rate (%)
  taxaNaoComparecimento: number;       // No-show rate (%)
  taxaCancelamento: number;            // Cancellation rate (%)
  
  // ===== AVERAGE TIME =====
  duracaoMediaMinutos: number;         // Average appointment duration in minutes
  tempoMedioEsperaMinutos: number;     // Average waiting time (reserved for future use)
  
  // ===== METADATA =====
  dataHoraConsulta: string;            // Timestamp when statistics were calculated (ISO 8601)
  periodoAnalisado: string;            // Analysis period description (e.g., "Geral")
}

interface ProfissionalEstatistica {
  profissionalId: number;              // Professional's user ID
  profissionalNome: string;            // Professional's full name
  especialidade: string;               // Specialty/Type (MEDICO, ENFERMEIRO, etc.)
  totalAgendamentos: number;           // Total appointments assigned
  agendamentosConcluidos: number;      // Successfully completed appointments
  taxaConclusao: number;               // Completion rate (%)
}

interface ServicoEstatistica {
  servicoId: number;                   // Service type ID
  servicoNome: string;                 // Service name
  categoria: string;                   // Service category
  totalAgendamentos: number;           // Total times this service was requested
  percentualTotal: number;             // Percentage of total appointments (%)
}
```

---

## Example Response

```json
{
  "totalAgendamentosAtivos": 245,
  "totalAgendamentosHoje": 18,
  "totalAgendamentosSemana": 67,
  "totalAgendamentosMes": 189,
  "totalAgendamentosAno": 1420,
  "agendamentosAgendados": 45,
  "agendamentosConfirmados": 72,
  "agendamentosEmAtendimento": 8,
  "agendamentosConcluidos": 980,
  "agendamentosCancelados": 320,
  "agendamentosNaoCompareceram": 120,
  "agendamentosPacientes": 195,
  "agendamentosAcompanhantes": 50,
  "agendamentosAutomaticos": 38,
  "agendamentosUrgentes": 15,
  "agendamentosAltaPrioridade": 45,
  "agendamentosNormalPrioridade": 165,
  "agendamentosBaixaPrioridade": 20,
  "agendamentosPrimeiraVez": 58,
  "agendamentosRetorno": 142,
  "agendamentosEmergenciais": 22,
  "agendamentosRotina": 155,
  "agendamentosTriagem": 38,
  "agendamentosPendentesConfirmacao": 28,
  "agendamentosConfirmadosPaciente": 95,
  "agendamentosConfirmadosProfissional": 88,
  "agendamentosConfirmadosAmbos": 82,
  "topProfissionaisPorAgendamentos": [
    {
      "profissionalId": 10,
      "profissionalNome": "Dr. João Pedro Silva",
      "especialidade": "MEDICO",
      "totalAgendamentos": 78,
      "agendamentosConcluidos": 72,
      "taxaConclusao": 92.31
    },
    {
      "profissionalId": 15,
      "profissionalNome": "Dra. Maria Santos Costa",
      "especialidade": "NUTRICIONISTA",
      "totalAgendamentos": 65,
      "agendamentosConcluidos": 61,
      "taxaConclusao": 93.85
    },
    {
      "profissionalId": 22,
      "profissionalNome": "Enf. Carlos Alberto Souza",
      "especialidade": "ENFERMEIRO",
      "totalAgendamentos": 52,
      "agendamentosConcluidos": 48,
      "taxaConclusao": 92.31
    }
  ],
  "topProfissionaisPorConcluidos": [
    {
      "profissionalId": 10,
      "profissionalNome": "Dr. João Pedro Silva",
      "especialidade": "MEDICO",
      "totalAgendamentos": 0,
      "agendamentosConcluidos": 72,
      "taxaConclusao": 0.0
    },
    {
      "profissionalId": 15,
      "profissionalNome": "Dra. Maria Santos Costa",
      "especialidade": "NUTRICIONISTA",
      "totalAgendamentos": 0,
      "agendamentosConcluidos": 61,
      "taxaConclusao": 0.0
    }
  ],
  "topServicosMaisSolicitados": [
    {
      "servicoId": 5,
      "servicoNome": "Consulta Médica Geral",
      "categoria": "CONSULTA_MEDICA",
      "totalAgendamentos": 78,
      "percentualTotal": 31.84
    },
    {
      "servicoId": 8,
      "servicoNome": "Avaliação Nutricional",
      "categoria": "NUTRICAO",
      "totalAgendamentos": 52,
      "percentualTotal": 21.22
    },
    {
      "servicoId": 12,
      "servicoNome": "Triagem de Enfermagem",
      "categoria": "ENFERMAGEM",
      "totalAgendamentos": 38,
      "percentualTotal": 15.51
    }
  ],
  "agendamentosPorDiaSemana": {
    "segunda-feira": 42,
    "terça-feira": 48,
    "quarta-feira": 45,
    "quinta-feira": 38,
    "sexta-feira": 40,
    "sábado": 18,
    "domingo": 14
  },
  "agendamentosPorHora": {
    "0": 0,
    "1": 0,
    "2": 0,
    "3": 0,
    "4": 0,
    "5": 0,
    "6": 0,
    "7": 2,
    "8": 25,
    "9": 32,
    "10": 28,
    "11": 22,
    "12": 8,
    "13": 15,
    "14": 30,
    "15": 28,
    "16": 24,
    "17": 18,
    "18": 8,
    "19": 3,
    "20": 2,
    "21": 0,
    "22": 0,
    "23": 0
  },
  "taxaComparecimento": 89.09,
  "taxaNaoComparecimento": 10.91,
  "taxaCancelamento": 13.06,
  "duracaoMediaMinutos": 47.5,
  "tempoMedioEsperaMinutos": 0.0,
  "dataHoraConsulta": "2025-12-01T15:38:29.938114674",
  "periodoAnalisado": "Geral"
}
```

---

## Metrics Explanation

### 1. General Statistics

| Metric | Description | Calculation |
|--------|-------------|-------------|
| `totalAgendamentosAtivos` | All active appointments | Count where `deletedAt IS NULL` |
| `totalAgendamentosHoje` | Today's appointments | Count where `DATE(dataHoraInicio) = TODAY` |
| `totalAgendamentosSemana` | This week's appointments | Count where `WEEK(dataHoraInicio) = CURRENT_WEEK` |
| `totalAgendamentosMes` | This month's appointments | Count where `MONTH(dataHoraInicio) = CURRENT_MONTH` |
| `totalAgendamentosAno` | This year's appointments | Count where `YEAR(dataHoraInicio) = CURRENT_YEAR` |

### 2. Status Distribution

Counts appointments by their current status:
- **AGENDADO**: Created, awaiting confirmation
- **CONFIRMADO**: Confirmed by patient/companion and/or professional
- **EM_ATENDIMENTO**: Currently in progress
- **CONCLUIDO**: Successfully completed
- **CANCELADO**: Cancelled (with reason)
- **PACIENTE_NAO_COMPARECEU**: Patient/companion no-show

### 3. Type Classification

- **Patients vs Companions**: Separate counts for patient and companion appointments
- **Automatic Appointments**: Generated by system triggers (e.g., triagem on admission)

### 4. Priority Levels

Distribution across four priority levels:
- **URGENTE**: Urgent care required
- **ALTA**: High priority
- **NORMAL**: Standard priority (default)
- **BAIXA**: Low priority

### 5. Appointment Types

Classification by clinical context:
- **PRIMEIRA_VEZ**: First-time appointment
- **RETORNO**: Follow-up appointment
- **EMERGENCIAL**: Emergency care
- **ROTINA**: Routine care
- **TRIAGEM**: Screening/triage

### 6. Confirmation Status

Tracks confirmation by both parties:
- **Pending**: Neither party has confirmed
- **Patient/Companion Confirmed**: Confirmed by patient or companion
- **Professional Confirmed**: Confirmed by healthcare professional
- **Both Confirmed**: Confirmed by both parties (ideal state)

### 7. Performance Metrics

#### Attendance Rate
```
taxaComparecimento = (agendamentosConcluidos / (agendamentosConcluidos + agendamentosNaoCompareceram)) × 100
```

#### No-Show Rate
```
taxaNaoComparecimento = (agendamentosNaoCompareceram / (agendamentosConcluidos + agendamentosNaoCompareceram)) × 100
```

#### Cancellation Rate
```
taxaCancelamento = (agendamentosCancelados / totalAgendamentosAtivos) × 100
```

#### Average Duration
```
duracaoMediaMinutos = AVG(dataHoraFim - dataHoraInicio) in minutes
```

### 8. Top Professionals

Returns **Top 10** professionals sorted by:
1. **By Total Appointments**: Most assigned appointments
2. **By Completed Appointments**: Most successfully completed appointments

Each includes:
- Professional ID and name
- Specialty/Type
- Total and completed appointments
- Completion rate percentage

### 9. Top Services

Returns **Top 10** most requested services with:
- Service ID, name, and category
- Total appointment count
- Percentage of all appointments

### 10. Time Distributions

#### Weekly Distribution
Appointments grouped by day of week (Portuguese):
- segunda-feira (Monday)
- terça-feira (Tuesday)
- quarta-feira (Wednesday)
- quinta-feira (Thursday)
- sexta-feira (Friday)
- sábado (Saturday)
- domingo (Sunday)

#### Hourly Distribution
Appointments grouped by hour (0-23):
- Shows peak hours
- Helps identify busy periods
- Useful for capacity planning

---

## Dashboard Implementation Guide

### Recommended Widgets

#### 1. Summary Cards (KPIs)
```typescript
// Display as large cards at the top
const summaryCards = [
  {
    title: "Hoje",
    value: stats.totalAgendamentosHoje,
    icon: "📅",
    color: "blue"
  },
  {
    title: "Esta Semana",
    value: stats.totalAgendamentosSemana,
    icon: "📊",
    color: "green"
  },
  {
    title: "Este Mês",
    value: stats.totalAgendamentosMes,
    icon: "📈",
    color: "purple"
  },
  {
    title: "Pendentes",
    value: stats.agendamentosPendentesConfirmacao,
    icon: "⏳",
    color: "orange"
  }
];
```

#### 2. Status Breakdown (Donut/Pie Chart)
```typescript
const statusData = {
  labels: ['Agendados', 'Confirmados', 'Em Atendimento', 'Concluídos', 'Cancelados', 'Não Compareceram'],
  datasets: [{
    data: [
      stats.agendamentosAgendados,
      stats.agendamentosConfirmados,
      stats.agendamentosEmAtendimento,
      stats.agendamentosConcluidos,
      stats.agendamentosCancelados,
      stats.agendamentosNaoCompareceram
    ],
    backgroundColor: ['#FFA500', '#4CAF50', '#2196F3', '#8BC34A', '#F44336', '#9E9E9E']
  }]
};
```

#### 3. Weekly Distribution (Bar Chart)
```typescript
const weeklyData = {
  labels: Object.keys(stats.agendamentosPorDiaSemana),
  datasets: [{
    label: 'Agendamentos por Dia',
    data: Object.values(stats.agendamentosPorDiaSemana),
    backgroundColor: '#4CAF50'
  }]
};
```

#### 4. Hourly Distribution (Line Chart)
```typescript
const hourlyData = {
  labels: Object.keys(stats.agendamentosPorHora).map(h => `${h}:00`),
  datasets: [{
    label: 'Agendamentos por Hora',
    data: Object.values(stats.agendamentosPorHora),
    borderColor: '#2196F3',
    fill: true,
    backgroundColor: 'rgba(33, 150, 243, 0.1)'
  }]
};
```

#### 5. Top Professionals Table
```tsx
<table>
  <thead>
    <tr>
      <th>Profissional</th>
      <th>Especialidade</th>
      <th>Agendamentos</th>
      <th>Concluídos</th>
      <th>Taxa de Conclusão</th>
    </tr>
  </thead>
  <tbody>
    {stats.topProfissionaisPorAgendamentos.map(prof => (
      <tr key={prof.profissionalId}>
        <td>{prof.profissionalNome}</td>
        <td>{prof.especialidade}</td>
        <td>{prof.totalAgendamentos}</td>
        <td>{prof.agendamentosConcluidos}</td>
        <td>{prof.taxaConclusao.toFixed(2)}%</td>
      </tr>
    ))}
  </tbody>
</table>
```

#### 6. Performance Metrics (Progress Bars/Gauges)
```typescript
const performanceMetrics = [
  {
    label: "Taxa de Comparecimento",
    value: stats.taxaComparecimento,
    max: 100,
    color: stats.taxaComparecimento >= 90 ? "green" : "orange",
    icon: "✅"
  },
  {
    label: "Taxa de Não Comparecimento",
    value: stats.taxaNaoComparecimento,
    max: 100,
    color: stats.taxaNaoComparecimento <= 10 ? "green" : "red",
    icon: "❌"
  },
  {
    label: "Taxa de Cancelamento",
    value: stats.taxaCancelamento,
    max: 100,
    color: stats.taxaCancelamento <= 15 ? "green" : "red",
    icon: "🚫"
  }
];
```

#### 7. Priority Distribution (Horizontal Bar Chart)
```typescript
const priorityData = {
  labels: ['Urgente', 'Alta', 'Normal', 'Baixa'],
  datasets: [{
    label: 'Por Prioridade',
    data: [
      stats.agendamentosUrgentes,
      stats.agendamentosAltaPrioridade,
      stats.agendamentosNormalPrioridade,
      stats.agendamentosBaixaPrioridade
    ],
    backgroundColor: ['#F44336', '#FF9800', '#4CAF50', '#9E9E9E']
  }]
};
```

#### 8. Type Distribution (Stacked Bar Chart)
```typescript
const typeData = {
  labels: ['Primeira Vez', 'Retorno', 'Emergencial', 'Rotina', 'Triagem'],
  datasets: [{
    label: 'Por Tipo',
    data: [
      stats.agendamentosPrimeiraVez,
      stats.agendamentosRetorno,
      stats.agendamentosEmergenciais,
      stats.agendamentosRotina,
      stats.agendamentosTriagem
    ],
    backgroundColor: '#2196F3'
  }]
};
```

#### 9. Average Duration Indicator
```tsx
<div className="metric-card">
  <h3>Duração Média</h3>
  <div className="metric-value">
    {stats.duracaoMediaMinutos.toFixed(0)} min
  </div>
  <p className="metric-label">Por Atendimento</p>
</div>
```

---

## Integration Examples

### React/TypeScript Implementation

```typescript
import { useState, useEffect } from 'react';

interface DashboardProps {
  authToken: string;
}

const AgendamentosDashboard: React.FC<DashboardProps> = ({ authToken }) => {
  const [stats, setStats] = useState<EstatisticasAgendamentoDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStatistics = async () => {
      try {
        const response = await fetch(
          'http://localhost:8090/api/agendamentos/estatisticas',
          {
            headers: {
              'Authorization': `Bearer ${authToken}`,
              'Content-Type': 'application/json'
            }
          }
        );

        if (!response.ok) {
          throw new Error('Failed to fetch statistics');
        }

        const data = await response.json();
        setStats(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchStatistics();
    
    // Refresh every 5 minutes
    const interval = setInterval(fetchStatistics, 5 * 60 * 1000);
    
    return () => clearInterval(interval);
  }, [authToken]);

  if (loading) return <div>Carregando estatísticas...</div>;
  if (error) return <div>Erro: {error}</div>;
  if (!stats) return null;

  return (
    <div className="dashboard">
      <h1>Dashboard de Agendamentos</h1>
      
      {/* Summary Cards */}
      <div className="summary-grid">
        <MetricCard 
          title="Hoje" 
          value={stats.totalAgendamentosHoje} 
          icon="📅" 
        />
        <MetricCard 
          title="Esta Semana" 
          value={stats.totalAgendamentosSemana} 
          icon="📊" 
        />
        <MetricCard 
          title="Este Mês" 
          value={stats.totalAgendamentosMes} 
          icon="📈" 
        />
        <MetricCard 
          title="Pendentes" 
          value={stats.agendamentosPendentesConfirmacao} 
          icon="⏳" 
        />
      </div>

      {/* Charts */}
      <div className="charts-grid">
        <StatusDonutChart data={stats} />
        <WeeklyBarChart data={stats.agendamentosPorDiaSemana} />
        <HourlyLineChart data={stats.agendamentosPorHora} />
        <PriorityDistribution data={stats} />
      </div>

      {/* Tables */}
      <div className="tables-grid">
        <TopProfessionalsTable 
          professionals={stats.topProfissionaisPorAgendamentos} 
        />
        <TopServicesTable 
          services={stats.topServicosMaisSolicitados} 
        />
      </div>

      {/* Performance Metrics */}
      <PerformanceMetrics 
        attendance={stats.taxaComparecimento}
        noShow={stats.taxaNaoComparecimento}
        cancellation={stats.taxaCancelamento}
        avgDuration={stats.duracaoMediaMinutos}
      />
    </div>
  );
};
```

### Vue.js Implementation

```vue
<template>
  <div class="dashboard">
    <h1>Dashboard de Agendamentos</h1>
    
    <div v-if="loading">Carregando...</div>
    <div v-else-if="error">{{ error }}</div>
    
    <template v-else>
      <!-- Summary Cards -->
      <div class="summary-grid">
        <metric-card 
          v-for="card in summaryCards" 
          :key="card.title"
          :title="card.title"
          :value="card.value"
          :icon="card.icon"
        />
      </div>

      <!-- Charts and more -->
      <div class="charts-grid">
        <status-chart :data="stats" />
        <weekly-chart :data="stats.agendamentosPorDiaSemana" />
        <hourly-chart :data="stats.agendamentosPorHora" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const stats = ref<EstatisticasAgendamentoDTO | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const summaryCards = computed(() => {
  if (!stats.value) return [];
  
  return [
    { title: 'Hoje', value: stats.value.totalAgendamentosHoje, icon: '📅' },
    { title: 'Esta Semana', value: stats.value.totalAgendamentosSemana, icon: '📊' },
    { title: 'Este Mês', value: stats.value.totalAgendamentosMes, icon: '📈' },
    { title: 'Pendentes', value: stats.value.agendamentosPendentesConfirmacao, icon: '⏳' }
  ];
});

const fetchStatistics = async () => {
  try {
    const response = await fetch('/api/agendamentos/estatisticas', {
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) throw new Error('Failed to fetch');
    
    stats.value = await response.json();
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchStatistics();
  setInterval(fetchStatistics, 5 * 60 * 1000); // Refresh every 5 minutes
});
</script>
```

---

## Best Practices

### 1. Caching Strategy
```typescript
// Cache statistics for 5 minutes
const CACHE_DURATION = 5 * 60 * 1000;

class StatisticsService {
  private cache: { data: any; timestamp: number } | null = null;

  async getStatistics(token: string): Promise<EstatisticasAgendamentoDTO> {
    const now = Date.now();
    
    if (this.cache && (now - this.cache.timestamp) < CACHE_DURATION) {
      return this.cache.data;
    }

    const response = await fetch('/api/agendamentos/estatisticas', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    const data = await response.json();
    this.cache = { data, timestamp: now };
    
    return data;
  }
}
```

### 2. Error Handling
```typescript
async function fetchStatisticsWithRetry(token: string, retries = 3) {
  for (let i = 0; i < retries; i++) {
    try {
      const response = await fetch('/api/agendamentos/estatisticas', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error('Unauthorized - please login again');
        }
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      return await response.json();
    } catch (error) {
      if (i === retries - 1) throw error;
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
    }
  }
}
```

### 3. Loading States
```tsx
const LoadingDashboard = () => (
  <div className="dashboard-loading">
    <Spinner size="large" />
    <p>Carregando estatísticas...</p>
  </div>
);

const DashboardError = ({ error, onRetry }: { error: string; onRetry: () => void }) => (
  <div className="dashboard-error">
    <ErrorIcon />
    <h3>Erro ao carregar estatísticas</h3>
    <p>{error}</p>
    <button onClick={onRetry}>Tentar Novamente</button>
  </div>
);
```

### 4. Responsive Design
```css
.dashboard {
  display: grid;
  gap: 1.5rem;
  padding: 1.5rem;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 1.5rem;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
```

---

## Performance Considerations

### Database Impact
- The endpoint aggregates data from multiple tables
- Runs complex calculations on potentially large datasets
- **Recommended**: Cache results for 3-5 minutes
- **Consider**: Background job to pre-calculate statistics

### Query Optimization
Current implementation:
- Loads all appointments into memory
- Filters and calculates in application layer
- **Good for**: Small to medium datasets (< 10,000 appointments)
- **Future optimization**: Use database aggregation queries for large datasets

### Response Time Expectations
- **< 1,000 appointments**: < 500ms
- **1,000 - 10,000 appointments**: 500ms - 2s
- **> 10,000 appointments**: Consider optimization

---

## Troubleshooting

### Common Issues

#### 1. 404 or 500 Error: "No static resource agendamentos/estatisticas"
**Cause**: Missing `/api` prefix in the request URL  
**Solution**: 
- ❌ **WRONG**: `/agendamentos/estatisticas`
- ✅ **CORRECT**: `/api/agendamentos/estatisticas`

**Fix in your service:**
```typescript
// ❌ WRONG
const response = await fetch('/agendamentos/estatisticas', { ... });

// ✅ CORRECT
const response = await fetch('/api/agendamentos/estatisticas', { ... });

// ✅ BETTER (with base URL)
const API_BASE = 'http://localhost:8090/api';
const response = await fetch(`${API_BASE}/agendamentos/estatisticas`, { ... });
```

#### 2. Empty Response (All Zeros)
**Cause**: No appointment data in database  
**Solution**: Verify appointments exist and are not soft-deleted

#### 3. Authorization Error (401/403)
**Cause**: Invalid or expired JWT token, or insufficient role  
**Solution**: 
- Check token validity
- Verify user has one of: ADMINISTRADOR, GERENTE, RECEPCIONISTA, MEDICO, ENFERMEIRO, NUTRICIONISTA, DENTISTA
- Ensure token is sent in Authorization header: `Bearer <token>`

#### 4. CORS Error
**Cause**: Cross-origin request blocked  
**Solution**: 
- Verify CORS configuration in Spring Boot
- Check `CorsConfig.java` allows your frontend origin
- Use same domain or configure proper CORS headers

#### 5. Slow Response
**Cause**: Large dataset or complex calculations  
**Solution**:
- Implement caching
- Consider pagination or date range filters (future enhancement)
- Optimize database queries

#### 6. Missing Top Professionals/Services
**Cause**: Null values in database or insufficient data  
**Solution**: Verify foreign key relationships and data integrity

---

## Future Enhancements

### Planned Features
1. **Date Range Filtering**: `/api/agendamentos/estatisticas?inicio=2025-01-01&fim=2025-01-31`
2. **Professional Filtering**: `/api/agendamentos/estatisticas?profissionalId=10`
3. **Service Type Filtering**: `/api/agendamentos/estatisticas?tipoServicoId=5`
4. **Export Formats**: CSV, Excel, PDF reports
5. **Real-time Updates**: WebSocket integration for live dashboard
6. **Comparative Analysis**: Month-over-month, year-over-year comparisons
7. **Predictive Analytics**: Appointment trends and capacity forecasting

---

## Related Documentation

- **Main API Guide**: `AGENDAMENTOS_FRONTEND_IMPLEMENTATION_GUIDE.md`
- **Endpoints Reference**: `SCHEDULING_API_ENDPOINTS.md`
- **Database Schema**: `V64__create_new_scheduling_system.sql`
- **Swagger UI**: `http://localhost:8090/swagger-ui/index.html`

---

## Support & Contact

For questions, issues, or suggestions regarding the Statistics API:

1. **Swagger Documentation**: Test endpoint interactively at `/swagger-ui/index.html`
2. **Source Code**: Review implementation in `EstatisticasAgendamentoService.java`
3. **API Testing**: Use provided cURL examples or Postman collection

---

**Version:** 1.0  
**Last Updated:** December 1, 2025  
**Endpoint Status:** ✅ Production Ready  
**Documentation Status:** ✅ Complete
