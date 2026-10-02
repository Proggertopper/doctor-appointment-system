UPDATE appointment_slots
SET status = 'FREE'
WHERE status = 'HELD';

ALTER TABLE appointment_slots
    DROP COLUMN IF EXISTS held_until,
    DROP COLUMN IF EXISTS hold_token;
