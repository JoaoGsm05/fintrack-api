-- Substitui o indice incorreto criado em V5 (coluna unica sem prefixo user_id).
-- Indice parcial: exclui linhas com description NULL, reduzindo o tamanho do indice.

DROP INDEX IF EXISTS idx_transactions_description;

CREATE INDEX idx_transactions_user_description
    ON transactions (user_id, lower(description))
    WHERE description IS NOT NULL;
