CREATE TABLE admin_sessions (
                                id BIGSERIAL PRIMARY KEY,
                                token VARCHAR(100) NOT NULL UNIQUE,
                                expires_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);