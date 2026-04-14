# 🚀 Pocket Survivor – Deployment Guide

## Architecture Overview

```
┌─────────────┐     ┌──────────────────┐     ┌────────────┐
│  React SPA  │────▶│  Spring Boot API  │────▶│ PostgreSQL │
│  (Vercel /  │     │  (Railway / AWS   │     │ (Railway / │
│   Netlify)  │◀────│   / Render)       │◀────│  Supabase) │
└─────────────┘     └──────┬───────────┘     └────────────┘
                           │
                    ┌──────▼───────┐
                    │ Anthropic API │
                    │ (Claude Coach)│
                    └──────────────┘
```

---

## Option 1: Railway (Recommended – Easiest for Students)

Railway gives you backend + database in one click with a generous free tier.

### Step 1: Get Prerequisites

1. **GitHub account** – push the code to a repo
2. **Railway account** – sign up at [railway.app](https://railway.app)
3. **Anthropic API key** – get at [console.anthropic.com](https://console.anthropic.com)

### Step 2: Deploy PostgreSQL

1. Go to Railway dashboard → **New Project** → **Provision PostgreSQL**
2. Click the PostgreSQL service → **Variables** tab
3. Copy the `DATABASE_URL` (format: `postgresql://user:pass@host:port/dbname`)
4. Convert it to JDBC format: `jdbc:postgresql://host:port/dbname`

### Step 3: Deploy Backend

```bash
# From project root (pocket-survivor-backend/)
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USER/pocket-survivor.git
git push -u origin main
```

1. Railway dashboard → **New** → **GitHub Repo** → select your repo
2. Railway auto-detects the Dockerfile and builds it
3. Go to **Variables** tab and add:

```
DATABASE_URL=jdbc:postgresql://HOST:PORT/pocketsurvivor
DB_USERNAME=your_pg_user
DB_PASSWORD=your_pg_password
JWT_SECRET=generate-a-random-64-char-string
ANTHROPIC_API_KEY=sk-ant-your-key-here
CORS_ORIGINS=https://your-frontend-url.vercel.app
PORT=8080
```

4. Railway auto-deploys. Your API is live at `https://your-app.up.railway.app`

### Step 4: Deploy Frontend to Vercel

```bash
cd frontend/
```

1. Push the `frontend/` folder to its own repo (or use monorepo)
2. Go to [vercel.com](https://vercel.com) → **Import Project** → select the repo
3. Set **Framework Preset** to **Vite**
4. Add environment variable:

```
VITE_API_URL=https://your-app.up.railway.app/api
```

5. Deploy. Your app is live!

---

## Option 2: Docker Compose (Local / VPS)

For running on your own server (DigitalOcean, AWS EC2, etc.)

### Step 1: Clone & Configure

```bash
git clone https://github.com/YOUR_USER/pocket-survivor.git
cd pocket-survivor

# Create .env from template
cp .env.example .env
nano .env  # Fill in your values
```

### Step 2: Generate a JWT Secret

```bash
openssl rand -base64 32
# Paste the output as JWT_SECRET in .env
```

### Step 3: Launch

```bash
# Start everything (PostgreSQL + Backend + Frontend)
docker-compose up -d

# Check logs
docker-compose logs -f backend

# Verify health
curl http://localhost:8080/api/health
```

Your app runs at:
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080/api`
- PostgreSQL: `localhost:5432`

### Step 4: For Production VPS

```bash
# Install Docker on Ubuntu
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Use Nginx reverse proxy with SSL
sudo apt install nginx certbot python3-certbot-nginx
```

Add Nginx config at `/etc/nginx/sites-available/pocketsurvivor`:

```nginx
server {
    server_name yourdomain.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/pocketsurvivor /etc/nginx/sites-enabled/
sudo certbot --nginx -d yourdomain.com
sudo systemctl restart nginx
```

---

## Option 3: Render (Free Tier)

### Backend

1. Go to [render.com](https://render.com) → **New** → **Web Service**
2. Connect your GitHub repo
3. Settings:
   - **Runtime**: Docker
   - **Instance Type**: Free
4. Add environment variables (same as Railway Step 3)
5. Deploy

### Database

1. Render → **New** → **PostgreSQL**
2. Copy the **Internal Database URL**
3. Convert to JDBC format for `DATABASE_URL`

### Frontend

Deploy to Vercel (same as Option 1, Step 4) or Render Static Site.

---

## Option 4: Supabase (DB) + Vercel (Frontend) + Railway (Backend)

Best combo for free tiers:

1. **Supabase** – free PostgreSQL with 500MB
2. **Railway** – free backend hosting
3. **Vercel** – free frontend hosting

### Supabase Setup

1. Create project at [supabase.com](https://supabase.com)
2. Go to **Settings** → **Database** → copy the **Connection string (JDBC)**
3. The Flyway migration runs automatically on first boot

---

## Local Development (No Docker)

### Prerequisites

- Java 17+ (`brew install openjdk@17` / `sudo apt install openjdk-17-jdk`)
- PostgreSQL 15+ (`brew install postgresql` / `sudo apt install postgresql`)
- Node.js 18+ (`brew install node`)

### Step 1: Setup Database

```bash
# Create database
psql -U postgres
CREATE DATABASE pocketsurvivor;
CREATE USER pocketsurvivor WITH PASSWORD 'pocketsurvivor';
GRANT ALL PRIVILEGES ON DATABASE pocketsurvivor TO pocketsurvivor;
\q
```

### Step 2: Run Backend

```bash
cd pocket-survivor-backend/

# Set environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/pocketsurvivor
export DB_USERNAME=pocketsurvivor
export DB_PASSWORD=pocketsurvivor
export JWT_SECRET=dev-secret-key-change-in-production
export ANTHROPIC_API_KEY=sk-ant-your-key
export CORS_ORIGINS=http://localhost:3000

# Build & run
./mvnw spring-boot:run
```

Backend starts at `http://localhost:8080`

### Step 3: Run Frontend

```bash
cd frontend/

npm install
npm run dev
```

Frontend starts at `http://localhost:3000` with API proxy to `:8080`

---

## API Reference

All endpoints require `Authorization: Bearer <token>` unless marked public.

### Auth (Public)

| Method | Endpoint | Body | Returns |
|--------|----------|------|---------|
| POST | `/api/auth/register` | `{email, password, name, personality, monthlyBudget}` | `{token, user}` |
| POST | `/api/auth/login` | `{email, password}` | `{token, user}` |
| GET | `/api/health` | — | `"OK"` |

### Dashboard

| Method | Endpoint | Returns |
|--------|----------|---------|
| GET | `/api/dashboard` | Full dashboard data + coach message |
| GET | `/api/coach` | Fresh coaching message |
| GET | `/api/insights` | Charts, badges, streaks |

### Expenses

| Method | Endpoint | Body | Returns |
|--------|----------|------|---------|
| POST | `/api/expenses` | `{category, amount, timeOfDay, expenseDate?, note?}` | Created expense |
| GET | `/api/expenses?from=DATE&to=DATE` | — | Expense list |
| GET | `/api/expenses/today` | — | Today's expenses |
| DELETE | `/api/expenses/:id` | — | Success |

### Goals

| Method | Endpoint | Body | Returns |
|--------|----------|------|---------|
| POST | `/api/goals` | `{name, targetAmount, deadline?}` | Created goal |
| GET | `/api/goals` | — | All goals |
| POST | `/api/goals/:id/contribute` | `{amount}` | Updated goal |
| DELETE | `/api/goals/:id` | — | Success |

### Smart Suggestions (NLP)

| Method | Endpoint | Returns |
|--------|----------|---------|
| GET | `/api/suggestions/:timeOfDay` | `{categories: [...], prices: {...}}` |

### User

| Method | Endpoint | Body | Returns |
|--------|----------|------|---------|
| GET | `/api/user/profile` | — | User profile |
| PATCH | `/api/user/profile` | `{name?, personality?, monthlyBudget?}` | Updated profile |

### Enums

**personality**: `spender`, `balanced`, `saver`
**timeOfDay**: `morning`, `afternoon`, `evening`, `night`

---

## Database Schema (ER Diagram)

```
users
├── id (PK)
├── email (UNIQUE)
├── password_hash
├── name
├── personality (ENUM)
├── monthly_budget
└── onboarded_at

expenses
├── id (PK)
├── user_id (FK → users)
├── category
├── amount
├── time_of_day (ENUM)
├── expense_date
└── note

goals
├── id (PK)
├── user_id (FK → users)
├── name
├── target_amount
├── saved_amount
├── deadline
└── is_completed

goal_contributions
├── id (PK)
├── goal_id (FK → goals)
├── user_id (FK → users)
└── amount

learning_data (NLP)
├── id (PK)
├── user_id (FK → users)
├── time_of_day (ENUM)
├── category
└── frequency

learning_prices (NLP)
├── id (PK)
├── user_id (FK → users)
├── category
├── amount
└── frequency

streaks
├── id (PK)
├── user_id (FK → users, UNIQUE)
├── current_streak
├── best_streak
└── last_log_date

badges
├── id (PK)
├── user_id (FK → users)
├── badge_key
└── earned_at
```

---

## Checklist Before Going Live

- [ ] Generate a strong `JWT_SECRET` (64+ random characters)
- [ ] Set `ANTHROPIC_API_KEY` for Claude coaching
- [ ] Set `CORS_ORIGINS` to your actual frontend domain
- [ ] Run database migration (automatic via Flyway on first boot)
- [ ] Test `/api/health` endpoint
- [ ] Test register → login → add expense → dashboard flow
- [ ] Set up HTTPS (automatic on Railway/Vercel/Render)
- [ ] Optional: Set up a custom domain

---

## Cost Estimates (Monthly)

| Service | Free Tier | Paid |
|---------|-----------|------|
| Railway (Backend) | 500 hrs/mo | $5/mo |
| Railway (PostgreSQL) | 1GB included | $5/mo |
| Vercel (Frontend) | Unlimited | Free |
| Anthropic API | Pay per use | ~$1-5/mo for light usage |
| **Total** | **$0-5/mo** | **$10-15/mo** |

For a college project, you can run the entire stack for free or under $5/month.
