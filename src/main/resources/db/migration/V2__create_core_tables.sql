-- ============================================================
-- V2 — Core tables: accounts, categories, transactions
-- Compatível com H2 (dev/test) e PostgreSQL (prod)
-- ============================================================

-- ── accounts ────────────────────────────────────────────────
CREATE TABLE accounts (
    id               UUID          NOT NULL,
    user_id          UUID          NOT NULL,
    name             VARCHAR(100)  NOT NULL,
    type             VARCHAR(30)   NOT NULL,
    balance          DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency         VARCHAR(3)    NOT NULL DEFAULT 'BRL',
    deleted_at       TIMESTAMP,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    created_by       VARCHAR(100),
    last_modified_by VARCHAR(100),

    CONSTRAINT pk_accounts      PRIMARY KEY (id),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- ── categories ──────────────────────────────────────────────
CREATE TABLE categories (
    id               UUID         NOT NULL,
    user_id          UUID         NOT NULL,
    parent_id        UUID,
    name             VARCHAR(100) NOT NULL,
    icon             VARCHAR(50),
    color            VARCHAR(7),
    deleted_at       TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(100),
    last_modified_by VARCHAR(100),

    CONSTRAINT pk_categories        PRIMARY KEY (id),
    CONSTRAINT fk_categories_user   FOREIGN KEY (user_id)   REFERENCES users(id),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id)
);

CREATE INDEX idx_categories_user_id   ON categories(user_id);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);

-- ── transactions ─────────────────────────────────────────────
CREATE TABLE transactions (
    id               UUID          NOT NULL,
    user_id          UUID          NOT NULL,
    account_id       UUID          NOT NULL,
    category_id      UUID,
    type             VARCHAR(10)   NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    description      VARCHAR(255),
    date             DATE          NOT NULL,
    deleted_at       TIMESTAMP,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    created_by       VARCHAR(100),
    last_modified_by VARCHAR(100),

    CONSTRAINT pk_transactions           PRIMARY KEY (id),
    CONSTRAINT fk_transactions_user      FOREIGN KEY (user_id)      REFERENCES users(id),
    CONSTRAINT fk_transactions_account   FOREIGN KEY (account_id)   REFERENCES accounts(id),
    CONSTRAINT fk_transactions_category  FOREIGN KEY (category_id)  REFERENCES categories(id)
);

CREATE INDEX idx_transactions_user_id    ON transactions(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_date       ON transactions(date);
