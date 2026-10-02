CREATE TABLE appointments (
                              id BIGSERIAL PRIMARY KEY,

                              slot_id BIGINT NOT NULL,

                              client_name VARCHAR(100) NOT NULL,
                              client_phone VARCHAR(30) NOT NULL,
                              comment TEXT,

                              status VARCHAR(30) NOT NULL,

                              cancel_token VARCHAR(100) NOT NULL UNIQUE,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              cancelled_at TIMESTAMP,

                              CONSTRAINT fk_appointments_slot
                                  FOREIGN KEY (slot_id)
                                      REFERENCES appointment_slots(id),
                              CONSTRAINT appointments_slot_id_key UNIQUE (slot_id)
);
