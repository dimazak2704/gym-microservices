CREATE TABLE invalidated_tokens (
                                    jti VARCHAR(255) NOT NULL PRIMARY KEY,
                                    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_invalidated_tokens_expires_at ON invalidated_tokens (expires_at);