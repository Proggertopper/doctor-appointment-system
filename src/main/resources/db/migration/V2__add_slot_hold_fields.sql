ALTER TABLE appointment_slots
    ADD COLUMN held_until TIMESTAMP;

ALTER TABLE appointment_slots
    ADD COLUMN hold_token VARCHAR(100);
