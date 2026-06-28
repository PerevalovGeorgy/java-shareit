ALTER TABLE users ALTER COLUMN id RESTART WITH 1;

INSERT INTO users (id, name, email) VALUES
(57, 'Test User 57', 'test57@example.com');