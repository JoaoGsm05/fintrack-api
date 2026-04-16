CREATE INDEX idx_accounts_user_deleted_at ON accounts(user_id, deleted_at);

CREATE INDEX idx_categories_user_deleted_at ON categories(user_id, deleted_at);
CREATE INDEX idx_categories_user_parent_deleted_at ON categories(user_id, parent_id, deleted_at);

CREATE INDEX idx_transactions_user_deleted_at ON transactions(user_id, deleted_at);
CREATE INDEX idx_transactions_user_account_deleted_at ON transactions(user_id, account_id, deleted_at);
CREATE INDEX idx_transactions_user_category_deleted_at ON transactions(user_id, category_id, deleted_at);
CREATE INDEX idx_transactions_user_type_date_deleted_at ON transactions(user_id, type, date, deleted_at);

CREATE INDEX idx_budgets_user_deleted_at ON budgets(user_id, deleted_at);
CREATE INDEX idx_budgets_user_category_deleted_at ON budgets(user_id, category_id, deleted_at);
CREATE INDEX idx_budgets_user_period_deleted_at ON budgets(user_id, start_date, end_date, deleted_at);

CREATE INDEX idx_recurring_transactions_user_deleted_at
    ON recurring_transactions(user_id, deleted_at);
CREATE INDEX idx_recurring_transactions_active_occurrence_deleted_at
    ON recurring_transactions(is_active, next_occurrence, deleted_at);
