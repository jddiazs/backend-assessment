CREATE TABLE IF NOT EXISTS pricing (
    parking_id VARCHAR(32) PRIMARY KEY,
    hourly_rate_in_cents INTEGER NOT NULL,
    cap_in_cents INTEGER NOT NULL,
    first_hour_free BOOLEAN NOT NULL DEFAULT FALSE,
    cap_window_hours INTEGER NOT NULL
);

INSERT INTO pricing (parking_id, hourly_rate_in_cents, cap_in_cents, first_hour_free, cap_window_hours) VALUES
    ('P000123', 200, 1500, FALSE, 24),
    ('P000456', 300, 2000, TRUE, 12);
