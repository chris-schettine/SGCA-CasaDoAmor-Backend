# 🚀 Quick Start Guide - SGCA Backend

## Prerequisites
- Java 21
- Docker & Docker Compose
- Maven (optional, included via wrapper)

## Initial Setup

### 1. Clone & Configure Environment

```bash
# Clone the repository
git clone https://github.com/chris-schettine/SGCA-CasaDoAmor-Backend.git
cd SGCA-CasaDoAmor-Backend

# Create your .env file from the example
cp .env.example .env
```

### 2. Edit `.env` with Your Passwords

```bash
# Open .env and set secure passwords
SGCA_DB_PASSWORD=your_secure_password_here
MYSQL_ROOT_PASSWORD=your_root_password_here
MYSQL_DATABASE=sgca
MYSQL_USER=sgca_user
```

**⚠️ IMPORTANT:** Use strong passwords! Never commit the `.env` file.

### 3. Run with Docker Compose

```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

The application will be available at:
- **API:** http://localhost:8090
- **Swagger UI:** http://localhost:8090/docs
- **Health Check:** http://localhost:8090/actuator/health

### 4. Run Locally (Without Docker)

```bash
# Set environment variable (PowerShell)
$env:SGCA_DB_PASSWORD="your_password"

# Or (Linux/Mac)
export SGCA_DB_PASSWORD="your_password"

# Run with Maven
./mvnw spring-boot:run

# Or on Windows
./mvnw.cmd spring-boot:run
```

## Development Workflow

### Building the Project

```bash
# Clean and build
./mvnw clean package

# Skip tests
./mvnw clean package -DskipTests

# Run tests only
./mvnw test
```

### Docker Commands

```bash
# View logs
docker-compose logs -f sgca

# Restart services
docker-compose restart

# Stop services
docker-compose down

# Stop and remove volumes (⚠️ deletes data)
docker-compose down -v

# Rebuild specific service
docker-compose up --build sgca
```

### Database Access

```bash
# Connect to MySQL container
docker exec -it mysql_sgca mysql -u sgca_user -p

# Or use MySQL Workbench
Host: localhost
Port: 3316
User: sgca_user
Password: <your SGCA_DB_PASSWORD>
```

## API Endpoints

### Currently Available

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/pacientes/` | Register new patient | ✅ Yes |
| PATCH | `/pacientes/{id}` | Update patient | ✅ Yes |
| GET | `/pacientes/` | List patients (paginated) | ✅ Yes |
| GET | `/docs` | Swagger UI | ❌ No |
| GET | `/actuator/health` | Health check | ❌ No |

### Testing Endpoints

Since authentication is not fully implemented, you may need to temporarily disable security for testing:

**Option 1:** Comment out `.anyRequest().authenticated()` in `SecurityConfig.java`

**Option 2:** Implement a test authentication endpoint

## Common Issues & Solutions

### Issue: "Access Denied" on all endpoints
**Solution:** Authentication is now required. Either:
1. Implement JWT authentication
2. Temporarily modify `SecurityConfig.java` to permit all requests

### Issue: Database connection failed
**Solution:** 
1. Check if `.env` file exists with correct password
2. Verify MySQL container is running: `docker ps`
3. Check logs: `docker-compose logs mysql`

### Issue: Port already in use
**Solution:** 
```bash
# Change ports in docker-compose.yml
ports:
  - '8091:8080'  # Change 8090 to 8091
```

### Issue: Permission denied (Docker)
**Solution:** Make sure Docker daemon is running and you have permissions

## Testing with cURL

### Health Check
```bash
curl http://localhost:8090/actuator/health
```

### Register Patient (requires auth token when implemented)
```bash
curl -X POST http://localhost:8090/pacientes/ \
  -H "Content-Type: application/json" \
  -d '{
    "dadoPessoal": {
      "nome": "João Silva",
      "dataNascimento": "1990-01-01",
      "cpf": "12345678901",
      "rg": "1234567890",
      "telefone": "+5511987654321"
    },
    "endereco": {
      "logradouro": "Rua Exemplo",
      "numero": 123,
      "bairro": "Centro",
      "cidade": "São Paulo",
      "estado": "SP",
      "cep": "01234-567"
    }
  }'
```

## Project Structure

```
sgca-backend/
├── src/
│   ├── main/
│   │   ├── java/br/com/casadoamor/sgca/
│   │   │   ├── config/          # Security, CORS, Swagger
│   │   │   ├── controller/      # REST endpoints
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Database access
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── dto/             # Data transfer objects
│   │   │   └── mapper/          # Entity-DTO converters
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/    # Flyway migrations
│   └── test/                    # Unit & integration tests
├── docker-compose.yml           # Docker orchestration
├── Dockerfile                   # Application container
├── .env.example                 # Environment variables template
└── pom.xml                      # Maven dependencies
```

## Next Steps

1. ✅ Complete JWT authentication implementation
2. ✅ Add comprehensive tests
3. ✅ Implement role-based authorization
4. ✅ Add more controllers (Usuario, ProfissionalSaude, etc.)

## Support

For issues or questions:
- Check `SECURITY_IMPROVEMENTS.md` for security-related tasks
- Review Swagger docs at http://localhost:8090/docs
- Check application logs: `docker-compose logs -f sgca`

## Security Reminders

- ⚠️ **Never commit `.env` file**
- ⚠️ **Use strong passwords**
- ⚠️ **Keep dependencies updated**
- ⚠️ **This is NOT production-ready yet** - authentication needed!

Happy coding! 🎉
