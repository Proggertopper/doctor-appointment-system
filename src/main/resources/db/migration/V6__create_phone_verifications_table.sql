CREATE TABLE phone_verifications (
                                     id BIGSERIAL PRIMARY KEY,
                                     phone VARCHAR(30) NOT NULL,
                                     code VARCHAR(10) NOT NULL,
                                     used BOOLEAN NOT NULL DEFAULT FALSE,
                                     expires_at TIMESTAMP NOT NULL,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);