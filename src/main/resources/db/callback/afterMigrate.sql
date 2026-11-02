-- Flyway runs as the postgres superuser, so all objects it creates are owned
-- by that role, while the application connects as the (non-superuser) gateway
-- role. Grant the application role full access to everything in schema public.
-- Runs after every migrate() call, is idempotent, and the "for the current role"
-- default privileges also cover objects created by future migrations.
GRANT USAGE ON SCHEMA public TO ${app_user};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${app_user};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${app_user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO ${app_user};

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO ${app_user};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO ${app_user};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO ${app_user};
