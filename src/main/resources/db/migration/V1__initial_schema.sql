-- ═══════════════════════════════════════════════════════════════════
-- Pocket Survivor - Database Schema V1
-- PostgreSQL 15+
-- ═══════════════════════════════════════════════════════════════════

-- ── ENUM TYPES ──────────────────────────────────────────────────
CREATE TYPE personality_type AS ENUM ('spender', 'balanced', 'saver');
CREATE TYPE time_of_day AS ENUM ('morning', 'afternoon', 'evening', 'night');

-- ── USERS ───────────────────────────────────────────────────────
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    personality     personality_type NOT NULL DEFAULT 'balanced',
    monthly_budget  INTEGER NOT NULL DEFAULT 10000,        -- in rupees
    onboarded_at    DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- ── EXPENSES ────────────────────────────────────────────────────
CREATE TABLE expenses (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category        VARCHAR(50) NOT NULL,
    amount          INTEGER NOT NULL CHECK (amount > 0),    -- in rupees
    time_of_day     time_of_day NOT NULL,
    expense_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    note            VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date DESC);
CREATE INDEX idx_expenses_user_month ON expenses(user_id, expense_date);
CREATE INDEX idx_expenses_category  ON expenses(user_id, category);

-- ── GOALS ───────────────────────────────────────────────────────
CREATE TABLE goals (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    target_amount   INTEGER NOT NULL CHECK (target_amount > 0),
    saved_amount    INTEGER NOT NULL DEFAULT 0 CHECK (saved_amount >= 0),
    deadline        DATE,
    is_completed    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_goals_user ON goals(user_id, is_completed);

-- ── GOAL CONTRIBUTIONS (savings log) ────────────────────────────
CREATE TABLE goal_contributions (
    id              BIGSERIAL PRIMARY KEY,
    goal_id         BIGINT NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount          INTEGER NOT NULL CHECK (amount > 0),
    contributed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contributions_goal ON goal_contributions(goal_id);

-- ── LEARNING DATA (NLP frequency tracking) ──────────────────────
CREATE TABLE learning_data (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    time_of_day     time_of_day NOT NULL,
    category        VARCHAR(50) NOT NULL,
    frequency       INTEGER NOT NULL DEFAULT 1,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, time_of_day, category)
);

CREATE INDEX idx_learning_user_time ON learning_data(user_id, time_of_day);

-- ── LEARNING PRICE DATA (preferred amounts per category) ────────
CREATE TABLE learning_prices (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category        VARCHAR(50) NOT NULL,
    amount          INTEGER NOT NULL,
    frequency       INTEGER NOT NULL DEFAULT 1,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, category, amount)
);

CREATE INDEX idx_learning_prices_user_cat ON learning_prices(user_id, category);

-- ── STREAKS ─────────────────────────────────────────────────────
CREATE TABLE streaks (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    current_streak  INTEGER NOT NULL DEFAULT 0,
    best_streak     INTEGER NOT NULL DEFAULT 0,
    last_log_date   DATE,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── BADGES ──────────────────────────────────────────────────────
CREATE TABLE badges (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_key       VARCHAR(50) NOT NULL,
    earned_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, badge_key)
);

CREATE INDEX idx_badges_user ON badges(user_id);

-- ── UPDATED_AT TRIGGER FUNCTION ─────────────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_goals_updated_at
    BEFORE UPDATE ON goals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_streaks_updated_at
    BEFORE UPDATE ON streaks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_learning_updated_at
    BEFORE UPDATE ON learning_data
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_learning_prices_updated_at
    BEFORE UPDATE ON learning_prices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ── USEFUL VIEWS ────────────────────────────────────────────────

-- Daily spending summary per user
CREATE VIEW v_daily_spending AS
SELECT
    user_id,
    expense_date,
    SUM(amount) AS total_spent,
    COUNT(*)    AS expense_count
FROM expenses
GROUP BY user_id, expense_date;

-- Monthly spending summary per user
CREATE VIEW v_monthly_spending AS
SELECT
    user_id,
    DATE_TRUNC('month', expense_date)::DATE AS month,
    SUM(amount) AS total_spent,
    COUNT(*)    AS expense_count
FROM expenses
GROUP BY user_id, DATE_TRUNC('month', expense_date);

-- Category spending breakdown for current month
CREATE VIEW v_category_spending AS
SELECT
    user_id,
    category,
    DATE_TRUNC('month', expense_date)::DATE AS month,
    SUM(amount) AS total_spent,
    COUNT(*)    AS frequency
FROM expenses
GROUP BY user_id, category, DATE_TRUNC('month', expense_date);
