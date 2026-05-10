-- SmartTask API — Sample Data (PostgreSQL)
--
-- NOTE: The application auto-seeds via DataInitializer.java on first startup.
--       Use this script only for manual/re-seed scenarios.
--
-- To execute manually:
--   1. Ensure Hibernate has already created the schema (run the app once, then stop it)
--   2. Set in application.properties:
--        spring.sql.init.mode=always
--        spring.jpa.defer-datasource-initialization=true
--   3. Restart the app (or run: psql -U smarttask_user -d smarttask_db -f data.sql)
--
-- Passwords (BCrypt strength 10):
--   admin   → admin123
--   johndoe → user1234

INSERT INTO users (username, email, password, role, created_at)
VALUES
    ('admin',   'admin@smarttask.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FXDrfIOAN/C2', 'ADMIN', NOW()),
    ('johndoe', 'john@smarttask.com',  '$2a$10$ByY6./Jp7EvLlALhNlPyqeXROnTXTgA.l7D0Rfxl2w6gEV2M4U3C.',  'USER',  NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO tasks (title, description, status, priority, due_date, created_at, updated_at, user_id)
SELECT 'Set up project structure',
       'Initialize Spring Boot project with Maven dependencies and package layout',
       'DONE', 'HIGH',
       NOW() - INTERVAL '7 days', NOW(), NOW(), id
FROM users WHERE username = 'johndoe'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (title, description, status, priority, due_date, created_at, updated_at, user_id)
SELECT 'Implement JWT authentication',
       'Add JWT-based security using Spring Security 6 and jjwt 0.12',
       'DONE', 'HIGH',
       NOW() - INTERVAL '3 days', NOW(), NOW(), id
FROM users WHERE username = 'johndoe'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (title, description, status, priority, due_date, created_at, updated_at, user_id)
SELECT 'Build task CRUD endpoints',
       'Create REST controllers and service layer for full task lifecycle',
       'IN_PROGRESS', 'MEDIUM',
       NOW() + INTERVAL '2 days', NOW(), NOW(), id
FROM users WHERE username = 'johndoe'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (title, description, status, priority, due_date, created_at, updated_at, user_id)
SELECT 'Write unit and integration tests',
       'Cover service and controller layers with JUnit 5 and MockMvc',
       'IN_PROGRESS', 'MEDIUM',
       NOW() + INTERVAL '5 days', NOW(), NOW(), id
FROM users WHERE username = 'johndoe'
ON CONFLICT DO NOTHING;

INSERT INTO tasks (title, description, status, priority, due_date, created_at, updated_at, user_id)
SELECT 'Deploy to production',
       'Containerise with Docker and deploy to cloud infrastructure',
       'TODO', 'LOW',
       NOW() + INTERVAL '14 days', NOW(), NOW(), id
FROM users WHERE username = 'johndoe'
ON CONFLICT DO NOTHING;
