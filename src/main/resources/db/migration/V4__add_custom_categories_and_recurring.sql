-- Custom categories: user-defined spending categories
CREATE TABLE custom_categories (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(50)     NOT NULL,
    icon        VARCHAR(30)     DEFAULT 'Receipt',
    time_of_day VARCHAR(20),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, name)
);

CREATE INDEX idx_custom_categories_user ON custom_categories(user_id);

-- Recurring expenses: auto-logged on matching days
CREATE TABLE recurring_expenses (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category        VARCHAR(50)     NOT NULL,
    amount          INTEGER         NOT NULL CHECK (amount > 0),
    time_of_day     VARCHAR(20)     NOT NULL,
    note            VARCHAR(255),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    days_of_week    VARCHAR(20)     NOT NULL DEFAULT 'weekdays',
    last_auto_logged DATE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recurring_expenses_user ON recurring_expenses(user_id);
CREATE INDEX idx_recurring_expenses_active ON recurring_expenses(user_id, is_active);
