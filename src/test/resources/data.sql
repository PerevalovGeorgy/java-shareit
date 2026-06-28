ALTER TABLE users ALTER COLUMN id RESTART WITH 1;

INSERT INTO users (id, name, email) VALUES
(42, 'Test User 42', 'test42@example.com');