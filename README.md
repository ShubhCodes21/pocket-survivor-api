<div align="center">

# 💸 Pocket Survivor

### *Smart College Expense Manager with AI-Powered Financial Coaching*

A full-stack web application that helps students master their money through personality-based coaching, one-tap expense tracking, goal visualization, and Claude-powered spending insights.

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Claude API](https://img.shields.io/badge/Claude%20API-Anthropic-D97757?style=for-the-badge&logo=anthropic&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

</div>

---

## ✨ Features

- 🧠 **AI Financial Coach** — Personalized advice powered by Anthropic's Claude API, adapted to Savage / Balanced / Supportive coaching personalities.
- 💨 **Bubble Quick-Entry** — Log expenses in under 2 seconds with category-aware floating bubbles.
- 🎯 **Smart Goal Tracking** — Set savings goals, track contributions, and get daily-savings math done for you.
- 🔥 **Streaks & Badges** — Gamified discipline tracking with achievement badges.
- 📊 **Dashboard Insights** — Real-time spending breakdowns, category charts, weekend-vs-weekday trends.
- 🤖 **NLP Smart Suggestions** — Time-of-day aware suggestions powered by learning data + historical prices.
- 🔐 **JWT Authentication** — Stateless token-based auth with Spring Security and BCrypt hashing.
- 🗄️ **Versioned Migrations** — Clean schema evolution via Flyway.
- 🐳 **Dockerized** — One-command local setup with Docker Compose.

---

## 🏗️ Architecture

```
┌─────────────────┐      HTTPS / JWT       ┌──────────────────────┐
│                 │ ─────────────────────► │                      │
│  React + Vite   │                        │   Spring Boot 3.2    │
│   Frontend      │ ◄───────────────────── │     REST API         │
│   (Recharts)    │        JSON            │  (Spring Security)   │
└─────────────────┘                        └──────────┬───────────┘
                                                      │
                                  ┌───────────────────┼────────────────┐
                                  │                   │                │
                                  ▼                   ▼                ▼
                        ┌──────────────────┐ ┌───────────────┐ ┌────────────────┐
                        │   PostgreSQL 16  │ │  Claude API   │ │ Flyway Migrate │
                        │  (JPA/Hibernate) │ │  (Anthropic)  │ │   (schema)     │
                        └──────────────────┘ └───────────────┘ └────────────────┘
```

---

## 🛠️ Tech Stack

| Layer        | Technology                                                            |
| ------------ | --------------------------------------------------------------------- |
| **Frontend** | React 18 · Vite · Recharts · Lucide Icons · Axios                     |
| **Backend**  | Java 21 · Spring Boot 3.2 · Spring Security · Spring Data JPA         |
| **Database** | PostgreSQL 16 · Flyway Migrations · Hibernate                         |
| **AI**       | Anthropic Claude API (personality-based coaching + NLP suggestions)   |
| **Auth**     | JWT (JSON Web Tokens) · BCrypt                                        |
| **DevOps**   | Docker · Docker Compose · Render · Maven Wrapper                      |
| **Testing**  | JUnit 5 · Spring Boot Test                                            |

---

## 📡 API Endpoints

| Method   | Endpoint                           | Description                              | Auth |
| -------- | ---------------------------------- | ---------------------------------------- | :--: |
| `POST`   | `/api/auth/register`               | Register a new user                      |  ❌  |
| `POST`   | `/api/auth/login`                  | Login and receive JWT                    |  ❌  |
| `GET`    | `/api/user/profile`                | Get current user profile                 |  ✅  |
| `GET`    | `/api/health`                      | Health check                             |  ❌  |
| `GET`    | `/api/dashboard`                   | Full dashboard (expenses, goals, coach)  |  ✅  |
| `GET`    | `/api/insights`                    | Spending analytics & trends              |  ✅  |
| `GET`    | `/api/coach`                       | AI coaching message (Claude)             |  ✅  |
| `GET`    | `/api/suggestions/{timeOfDay}`     | Context-aware expense suggestions        |  ✅  |
| `POST`   | `/api/expenses`                    | Log a new expense                        |  ✅  |
| `GET`    | `/api/expenses`                    | List all expenses                        |  ✅  |
| `GET`    | `/api/expenses/today`              | Today's expenses                         |  ✅  |
| `DELETE` | `/api/expenses/{id}`               | Delete an expense                        |  ✅  |
| `POST`   | `/api/goals`                       | Create a savings goal                    |  ✅  |
| `GET`    | `/api/goals`                       | List active goals                        |  ✅  |
| `POST`   | `/api/goals/{id}/contribute`       | Contribute to a goal                     |  ✅  |
| `DELETE` | `/api/goals/{id}`                  | Delete a goal                            |  ✅  |

---

## 🗃️ Database Schema

```
┌──────────────────┐        ┌──────────────────┐        ┌──────────────────┐
│     users        │ 1    ∞ │    expenses      │        │   learning_data  │
├──────────────────┤───────►├──────────────────┤        ├──────────────────┤
│ id (PK)          │        │ id (PK)          │        │ id (PK)          │
│ email            │        │ user_id (FK)     │        │ item_name        │
│ password_hash    │        │ amount           │        │ category         │
│ personality      │        │ category         │        │ frequency        │
│ created_at       │        │ note             │        └──────────────────┘
└────────┬─────────┘        │ created_at       │
         │                  └──────────────────┘        ┌──────────────────┐
         │ 1                                            │  learning_prices │
         │                  ┌──────────────────┐        ├──────────────────┤
         │ ∞                │     goals        │        │ id (PK)          │
         ├─────────────────►├──────────────────┤        │ item_name        │
         │                  │ id (PK)          │        │ price            │
         │                  │ user_id (FK)     │◄──┐    │ observed_at      │
         │                  │ title            │   │    └──────────────────┘
         │                  │ target_amount    │   │
         │                  │ saved_amount     │   │    ┌──────────────────┐
         │                  │ deadline         │   │    │ goal_contributions│
         │                  └──────────────────┘   │ 1  ├──────────────────┤
         │                                         └───►│ id (PK)          │
         │ 1                ┌──────────────────┐   ∞    │ goal_id (FK)     │
         ├─────────────────►│     streaks      │        │ amount           │
         │ 1                ├──────────────────┤        │ contributed_at   │
         │                  │ user_id (FK)     │        └──────────────────┘
         │                  │ current_streak   │
         │ 1                │ longest_streak   │        ┌──────────────────┐
         └─────────────────►│ last_logged_at   │        │     badges       │
           ∞                └──────────────────┘        ├──────────────────┤
                                                        │ id (PK)          │
                                                        │ user_id (FK)     │
                                                        │ badge_type       │
                                                        │ earned_at        │
                                                        └──────────────────┘
```

---

## 📸 Screenshots

> _Screenshots coming soon — placeholders below._

| Dashboard | Bubble Entry | AI Coach |
| :-------: | :----------: | :------: |
| ![Dashboard](docs/screenshots/dashboard.png) | ![Bubble Entry](docs/screenshots/bubbles.png) | ![Coach](docs/screenshots/coach.png) |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- PostgreSQL 15+
- Node.js 18+
- Anthropic API key ([get one here](https://console.anthropic.com))

### 1. Clone & Configure

```bash
git clone https://github.com/ShubhCodes21/pocket-survivor-api.git
cd pocket-survivor-api
cp .env.example .env   # fill in ANTHROPIC_API_KEY and JWT_SECRET
```

### 2. Run with Docker (recommended)

```bash
docker-compose up -d
```

### 3. Or Run Locally

```bash
# Database
psql -U postgres -c "CREATE DATABASE pocketsurvivor;"

# Backend
export DATABASE_URL=jdbc:postgresql://localhost:5432/pocketsurvivor
export DB_USERNAME=postgres DB_PASSWORD=postgres
export JWT_SECRET=$(openssl rand -base64 32)
export ANTHROPIC_API_KEY=sk-ant-your-key-here
./mvnw spring-boot:run

# Frontend
cd frontend && npm install && npm run dev
```

Open **http://localhost:3000** and start tracking. 🎉

See **[DEPLOY.md](DEPLOY.md)** for full production deployment instructions.

---

## 📁 Project Structure

```
pocket-survivor-backend/
├── src/main/java/com/pocketsurvivor/
│   ├── config/          # JWT, Spring Security, CORS
│   ├── controller/      # REST endpoints (Auth, Dashboard, Expense, Goal)
│   ├── dto/             # Request/Response records
│   ├── model/           # JPA entities
│   ├── repository/      # Spring Data JPA repositories
│   └── service/         # Business logic + Claude integration
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/    # Flyway SQL migrations
├── frontend/            # React 18 + Vite SPA
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## 📜 License

Released under the MIT License.

---

<div align="center">

### Built with ☕ and 🤖 by **Shubh Agarwal**

[![GitHub](https://img.shields.io/badge/GitHub-ShubhCodes21-181717?style=for-the-badge&logo=github)](https://github.com/ShubhCodes21)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/shubh-agarwal)
[![Email](https://img.shields.io/badge/Email-Contact-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:shubhagarwalval21@gmail.com)

⭐ Star this repo if you find it useful!

</div>
