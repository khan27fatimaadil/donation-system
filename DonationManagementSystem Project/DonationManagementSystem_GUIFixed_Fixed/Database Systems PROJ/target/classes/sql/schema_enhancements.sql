-- =============================================================================
-- DONATION MANAGEMENT SYSTEM — Database Enhancements
-- Views · Stored Procedures · Triggers · Indexes
-- =============================================================================

USE donation_system;

-- =============================================================================
-- SECTION 1: VIEWS
-- =============================================================================

-- ----------------------------------------------------------------------------
-- View: vw_campaign_totals
-- Pre-aggregates raised amount, donation count, and progress % per campaign.
-- Used by the Analytics and Impact Reports screens.
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_campaign_totals AS
SELECT
    c.id                                                          AS campaign_id,
    c.title                                                       AS title,
    c.goal_amount                                                 AS goal_amount,
    c.currency_code                                               AS currency_code,
    c.status                                                      AS status,
    COALESCE(SUM(d.amount), 0)                                    AS raised_amount,
    COUNT(d.id)                                                   AS donation_count,
    ROUND(
        (COALESCE(SUM(d.amount), 0) / NULLIF(c.goal_amount, 0)) * 100, 2
    )                                                             AS progress_pct
FROM campaigns c
LEFT JOIN donations d
    ON d.campaign_id = c.id
    AND d.status = 'completed'
GROUP BY c.id, c.title, c.goal_amount, c.currency_code, c.status;


-- ----------------------------------------------------------------------------
-- View: vw_donor_summary
-- One row per donor: total donated, number of donations, last donation date.
-- Used by the Analytics screen Top Donors table.
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_donor_summary AS
SELECT
    d.id                                                              AS donor_id,
    COALESCE(
        NULLIF(TRIM(CONCAT(
            COALESCE(d.first_name, ''), ' ', COALESCE(d.last_name, '')
        )), ''),
        d.organization_name,
        d.email
    )                                                                 AS display_name,
    d.email                                                           AS email,
    d.type                                                            AS donor_type,
    COALESCE(SUM(don.amount), 0)                                      AS total_donated,
    COUNT(don.id)                                                     AS donation_count,
    MAX(don.donated_at)                                               AS last_donation_at
FROM donors d
LEFT JOIN donations don
    ON don.donor_id = d.id
    AND don.status = 'completed'
GROUP BY d.id, d.first_name, d.last_name, d.organization_name, d.email, d.type;


-- ----------------------------------------------------------------------------
-- View: vw_allocation_check
-- Shows completed donations that are not fully allocated.
-- Useful for auditing unallocated funds.
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_allocation_check AS
SELECT
    d.id                                  AS donation_id,
    d.amount                              AS donation_amount,
    COALESCE(SUM(a.amount), 0)            AS allocated_amount,
    d.amount - COALESCE(SUM(a.amount), 0) AS unallocated_amount
FROM donations d
LEFT JOIN allocations a ON a.donation_id = d.id
WHERE d.status = 'completed'
GROUP BY d.id, d.amount
HAVING ABS(d.amount - COALESCE(SUM(a.amount), 0)) > 0.01;


-- ----------------------------------------------------------------------------
-- View: vw_recent_donations
-- Latest 50 donations with resolved donor name and campaign title.
-- Used by the Dashboard recent-donations table.
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_recent_donations AS
SELECT
    don.id                                                              AS donation_id,
    COALESCE(
        NULLIF(TRIM(CONCAT(
            COALESCE(d.first_name, ''), ' ', COALESCE(d.last_name, '')
        )), ''),
        d.organization_name,
        'Unknown Donor'
    )                                                                   AS donor_name,
    don.amount                                                          AS amount,
    don.currency_code                                                   AS currency_code,
    COALESCE(c.title, '(No Cause)')                                     AS campaign_title,
    don.donated_at                                                      AS donated_at,
    don.status                                                          AS status
FROM donations don
JOIN  donors    d ON don.donor_id    = d.id
LEFT  JOIN campaigns c ON don.campaign_id = c.id
ORDER BY don.donated_at DESC
LIMIT 50;


-- =============================================================================
-- SECTION 2: STORED PROCEDURES
-- =============================================================================

DROP PROCEDURE IF EXISTS sp_record_donation;
DELIMITER $$

-- ----------------------------------------------------------------------------
-- Procedure: sp_record_donation
-- Atomically inserts: donation + receipt + payment_transaction + status_log.
-- Parameters match the Donation Entry form fields.
-- ----------------------------------------------------------------------------
CREATE PROCEDURE sp_record_donation(
    IN  p_donor_id      CHAR(36),
    IN  p_campaign_id   CHAR(36),
    IN  p_fund_id       CHAR(36),
    IN  p_amount        DECIMAL(15,2),
    IN  p_currency_code CHAR(3),
    IN  p_gateway       VARCHAR(100),
    IN  p_donated_at    DATETIME,
    OUT p_donation_id   CHAR(36),
    OUT p_receipt_num   VARCHAR(100)
)
BEGIN
    DECLARE v_donation_id   CHAR(36)     DEFAULT (UUID());
    DECLARE v_receipt_id    CHAR(36)     DEFAULT (UUID());
    DECLARE v_txn_id        CHAR(36)     DEFAULT (UUID());
    DECLARE v_receipt_num   VARCHAR(100) DEFAULT CONCAT('REC-', UNIX_TIMESTAMP());
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'sp_record_donation failed — transaction rolled back.';
    END;

    START TRANSACTION;

    -- 1. Donation
    INSERT INTO donations
        (id, donor_id, campaign_id, amount, currency_code, type, status, donated_at)
    VALUES
        (v_donation_id, p_donor_id, p_campaign_id, p_amount,
         p_currency_code, 'one_time', 'completed',
         COALESCE(p_donated_at, NOW()));

    -- 2. Receipt
    INSERT INTO receipts (id, donation_id, receipt_number, issued_at)
    VALUES (v_receipt_id, v_donation_id, v_receipt_num, NOW());

    -- 3. Payment transaction
    INSERT INTO payment_transactions
        (id, donation_id, gateway, method, status, amount, currency_code, processed_at)
    VALUES
        (v_txn_id, v_donation_id,
         COALESCE(NULLIF(p_gateway, ''), 'Cash'),
         'cash', 'succeeded', p_amount, p_currency_code, NOW());

    -- 4. Status log
    INSERT INTO donation_status_log (donation_id, old_status, new_status, notes)
    VALUES (v_donation_id, 'pending', 'completed', 'Recorded via sp_record_donation');

    -- 5. Allocation (only if a fund was specified)
    IF p_fund_id IS NOT NULL THEN
        INSERT INTO allocations (donation_id, fund_id, amount)
        VALUES (v_donation_id, p_fund_id, p_amount);
    END IF;

    COMMIT;

    SET p_donation_id = v_donation_id;
    SET p_receipt_num = v_receipt_num;
END$$

DELIMITER ;


DROP PROCEDURE IF EXISTS sp_get_campaign_report;
DELIMITER $$

-- ----------------------------------------------------------------------------
-- Procedure: sp_get_campaign_report
-- Returns a full summary row for one campaign.
-- Used by the Impact Reports detail view.
-- ----------------------------------------------------------------------------
CREATE PROCEDURE sp_get_campaign_report(IN p_campaign_id CHAR(36))
BEGIN
    SELECT
        c.id                                          AS campaign_id,
        c.title                                       AS title,
        c.goal_amount                                 AS goal_amount,
        c.currency_code                               AS currency_code,
        c.status                                      AS status,
        c.start_date                                  AS start_date,
        c.end_date                                    AS end_date,
        COALESCE(SUM(d.amount), 0)                    AS raised_amount,
        COUNT(d.id)                                   AS donation_count,
        COALESCE(SUM(a.amount), 0)                    AS funds_allocated,
        ROUND(
            (COALESCE(SUM(d.amount), 0) /
             NULLIF(c.goal_amount, 0)) * 100, 2
        )                                             AS progress_pct
    FROM campaigns c
    LEFT JOIN donations  d ON d.campaign_id = c.id AND d.status = 'completed'
    LEFT JOIN allocations a ON a.donation_id = d.id
    WHERE c.id = p_campaign_id
    GROUP BY c.id, c.title, c.goal_amount, c.currency_code,
             c.status, c.start_date, c.end_date;
END$$

DELIMITER ;


-- =============================================================================
-- SECTION 3: TRIGGERS
-- =============================================================================

-- ----------------------------------------------------------------------------
-- Trigger: trg_donation_status_log_on_update
-- Automatically writes to donation_status_log whenever a donation's status
-- changes — no application code needed for the audit trail.
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_donation_status_log_on_update;

DELIMITER $$
CREATE TRIGGER trg_donation_status_log_on_update
AFTER UPDATE ON donations
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO donation_status_log
            (donation_id, old_status, new_status, notes)
        VALUES
            (NEW.id, OLD.status, NEW.status, 'Automatic status change log');
    END IF;
END$$
DELIMITER ;


-- ----------------------------------------------------------------------------
-- Trigger: trg_campaign_status_log_on_update
-- Writes to campaign_status_log when a campaign's status changes.
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_campaign_status_log_on_update;

DELIMITER $$
CREATE TRIGGER trg_campaign_status_log_on_update
AFTER UPDATE ON campaigns
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO campaign_status_log
            (campaign_id, old_status, new_status, notes)
        VALUES
            (NEW.id, OLD.status, NEW.status, 'Automatic status change log');
    END IF;
END$$
DELIMITER ;


-- ----------------------------------------------------------------------------
-- Trigger: trg_prevent_over_allocation
-- Prevents allocating more than the donation amount to a single donation.
-- Enforces data integrity at the database level.
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_prevent_over_allocation;

DELIMITER $$
CREATE TRIGGER trg_prevent_over_allocation
BEFORE INSERT ON allocations
FOR EACH ROW
BEGIN
    DECLARE v_donation_amount  DECIMAL(15,2);
    DECLARE v_already_allocated DECIMAL(15,2);

    SELECT amount INTO v_donation_amount
    FROM donations WHERE id = NEW.donation_id;

    SELECT COALESCE(SUM(amount), 0) INTO v_already_allocated
    FROM allocations WHERE donation_id = NEW.donation_id;

    IF (v_already_allocated + NEW.amount) > v_donation_amount THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT =
                'Over-allocation prevented: total allocations would exceed donation amount.';
    END IF;
END$$
DELIMITER ;


-- =============================================================================
-- SECTION 4: ADDITIONAL INDEXES (performance)
-- =============================================================================

-- donors: full-name search (first_name + last_name composite)
DROP INDEX IF EXISTS idx_donors_name ON donors;
CREATE INDEX idx_donors_name
    ON donors (first_name, last_name);

-- donations: date-range queries
DROP INDEX IF EXISTS idx_donations_donated_at_status ON donations;
CREATE INDEX idx_donations_donated_at_status
    ON donations (donated_at, status);

-- allocations: lookup total allocated per fund
DROP INDEX IF EXISTS idx_allocations_fund_amount ON allocations;
CREATE INDEX idx_allocations_fund_amount
    ON allocations (fund_id, amount);

-- recurring_plans: find upcoming charges efficiently
DROP INDEX IF EXISTS idx_recurring_next_charge ON recurring_plans;
CREATE INDEX idx_recurring_next_charge
    ON recurring_plans (next_charge_date, status);

