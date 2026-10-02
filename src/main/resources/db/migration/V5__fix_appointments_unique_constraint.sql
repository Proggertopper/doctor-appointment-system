ALTER TABLE appointments
DROP CONSTRAINT appointments_slot_id_key;

CREATE UNIQUE INDEX uk_active_appointment_per_slot
    ON appointments(slot_id)
    WHERE status = 'CREATED';