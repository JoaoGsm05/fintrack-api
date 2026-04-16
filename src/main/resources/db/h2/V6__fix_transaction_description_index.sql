-- H2 nao suporta indices parciais (partial indexes com WHERE).
-- Esta versao cria o indice composto sem a clausula WHERE.
-- A versao PostgreSQL (db/postgresql/) inclui WHERE description IS NOT NULL.

DROP INDEX IF EXISTS idx_transactions_description;

CREATE INDEX idx_transactions_user_description
    ON transactions (user_id, description);
