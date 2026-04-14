# 💸 Pocket Survivor

**Smart College Expense Manager** – A full-stack web app with personality-based financial coaching, bubble-based quick expense entry, goal tracking, and NLP-powered smart suggestions.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18 + Vite + Recharts + Lucide Icons |
| Backend | Java 17 + Spring Boot 3.2 + Spring Security |
| Database | PostgreSQL 16 + Flyway Migrations |
| AI Coach | Anthropic Claude API |
| Auth | JWT (JSON Web Tokens) |
| Deploy | Docker + Docker Compose |

## Quick Start (Local Development)

### Prerequisites
- Java 17+ 
- PostgreSQL 15+
- Node.js 18+

### 1. Setup Database

```bash
psql -U postgres -c "CREATE DATABASE pocketsurvivor;"
psql -U postgres -c "CREATE USER pocketsurvivor WITH PASSWORD 'pocketsurvivor';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE pocketsurvivor TO pocketsurvivor;"
psql -U postgres -d pocketsurvivor -c "GRANT ALL ON SCHEMA public TO pocketsurvivor;"
```

### 2. Run Backend

```bash
# Set environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/pocketsurvivor
export DB_USERNAME=pocketsurvivor
export DB_PASSWORD=pocketsurvivor
export JWT_SECRET=$(openssl rand -base64 32)
export ANTHROPIC_API_KEY=sk-ant-your-key-here
export CORS_ORIGINS=http://localhost:3000

# Run with Maven wrapper
./mvnw spring-boot:run
```

### 3. Run Frontend

```bash
cd frontend/
npm install
npm run dev
```

Open **http://localhost:3000** 🚀

## Docker Quick Start

```bash
cp .env.example .env
# Edit .env with your ANTHROPIC_API_KEY and JWT_SECRET

docker-compose up -d
```

## Project Structure

```
pocket-survivor-backend/
├── src/main/java/com/pocketsurvivor/
│   ├── PocketSurvivorApplication.java
│   ├── config/          # JWT, Security, CORS
│   ├── controller/      # REST API endpoints
│   ├── dto/             # Request/Response records
│   ├── model/           # JPA Entities
│   ├── repository/      # Spring Data JPA repos
│   └── service/         # Business logic
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/    # Flyway SQL migrations
├── frontend/
│   ├── src/
│   │   ├── api.js       # API client with JWT auth
│   │   └── App.jsx      # Full React application
│   └── vite.config.js
├── docker-compose.yml
├── Dockerfile
├── DEPLOY.md            # Full deployment guide
└── pom.xml
```

## API Endpoints

See **[DEPLOY.md](DEPLOY.md)** for full API reference.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | Register new user |
| `/api/auth/login` | POST | Login + get JWT |
| `/api/dashboard` | GET | Full dashboard + coach |
| `/api/expenses` | POST | Log expense (bubble entry) |
| `/api/goals` | POST | Create savings goal |
| `/api/goals/:id/contribute` | POST | Add savings |
| `/api/insights` | GET | Charts + badges + streaks |
| `/api/suggestions/:time` | GET | NLP smart suggestions |

## Features

- **🫧 Bubble Pop Expense Entry** – Log in under 2 seconds
- **🤖 AI Spending Coach** – Personality-based (Savage / Balanced / Supportive)
- **🎯 Goal Tracker** – With smart daily savings calculations
- **🧠 NLP Learning** – Reorders bubbles based on your habits
- **📊 Insights** – Charts, streaks, badges, category breakdown
- **📅 Weekend vs Weekday** – Context-aware suggestions
- **🏆 Gamification** – Streaks, badges, discipline scores

## License

MIT
