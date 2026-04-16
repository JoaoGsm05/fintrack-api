ALTER TABLE budgets
    ADD COLUMN alert_80_sent_at TIMESTAMP NULL;

ALTER TABLE budgets
    ADD COLUMN alert_100_sent_at TIMESTAMP NULL;
