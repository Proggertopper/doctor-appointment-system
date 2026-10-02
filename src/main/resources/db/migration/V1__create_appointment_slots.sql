CREATE TABLE appointment_slots (
                                   id BIGSERIAL PRIMARY KEY,
                                   slot_date DATE NOT NULL,
                                   start_time TIME NOT NULL,
                                   end_time TIME NOT NULL,
                                   type VARCHAR(20) NOT NULL,
                                   status VARCHAR(20) NOT NULL,

                                   client_name VARCHAR(100),
                                   client_phone VARCHAR(30),
                                   comment TEXT,

                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT uk_slot_datetime_type UNIQUE (slot_date, start_time, type)
);