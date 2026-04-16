-- ============================================================
-- V3 — Create budget and recurring tables
-- Compatível com H2 (dev/test) e PostgreSQL (prod)
-- ============================================================

-- ── budgets ──────────────────────────────────────────────────
CREATE TABLE budgets (
    id               UUID          NOT NULL,
    user_id          UUID          NOT NULL,
    category_id      UUID          NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    period           VARCHAR(20)   NOT NULL,
    start_date       DATE          NOT NULL,
    end_date         DATE          NOT NULL,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    created_by       VARCHAR(100),
    last_modified_by VARCHAR(100),

    deleted_at       TIMESTAMP,

    CONSTRAINT pk_budgets          PRIMARY KEY (id),
    CONSTRAINT fk_budgets_user     FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);

-- ── recurring_transactions ───────────────────────────────────
CREATE TABLE recurring_transactions (
    id               UUID          NOT NULL,
    user_id          UUID          NOT NULL,
    account_id       UUID          NOT NULL,
    category_id      UUID,
    amount           DECIMAL(15,2) NOT NULL,
    type             VARCHAR(10)   NOT NULL,
    description      VARCHAR(255),
    frequency        VARCHAR(20)   NOT NULL,
    next_occurrence  DATE          NOT NULL,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    created_by       VARCHAR(100),
    last_modified_by VARCHAR(100),

    CONSTRAINT pk_recurring_transactions          PRIMARY KEY (id),
    CONSTRAINT fk_recurring_transactions_user     FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_recurring_transactions_account  FOREIGN KEY (account_id)  REFERENCES accounts(id),
    CONSTRAINT fk_recurring_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

ALTER TABLE recurring_transactions ADD COLUMN deleted_at TIMESTAMP;

CREATE INDEX idx_recurring_transactions_user_id ON recurring_transactions(user_id);
