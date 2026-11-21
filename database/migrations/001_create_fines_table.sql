-- UniLib Phase 5: fine tracking
-- Run this migration once against the existing UniLib database.
-- A fine row is created only when a late book is returned.
-- Active overdue fines are calculated live from transactions.

CREATE TABLE IF NOT EXISTS fines (
    id INT NOT NULL AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    member_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_status ENUM('UNPAID', 'PAID') NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL DEFAULT NULL,
    recorded_by INT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_fines_transaction (transaction_id),
    KEY idx_fines_member (member_id),
    KEY idx_fines_status (payment_status),
    CONSTRAINT fk_fines_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT fk_fines_member
        FOREIGN KEY (member_id) REFERENCES users(id),
    CONSTRAINT fk_fines_recorded_by
        FOREIGN KEY (recorded_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
