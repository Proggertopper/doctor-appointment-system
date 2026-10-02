ALTER TABLE appointments
    ADD COLUMN client_phone_normalized VARCHAR(30);

UPDATE appointments
SET client_phone_normalized = regexp_replace(client_phone, '[^0-9]', '', 'g');

ALTER TABLE appointments
    ALTER COLUMN client_phone_normalized SET NOT NULL;

CREATE UNIQUE INDEX uk_active_appointment_per_phone
    ON appointments(client_phone_normalized)
    WHERE status = 'CREATED';