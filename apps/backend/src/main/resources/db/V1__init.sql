CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS schools (
  id BIGSERIAL PRIMARY KEY,
  domain citext NOT NULL UNIQUE,
  name   text   NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  email citext NOT NULL UNIQUE,
  password_hash text NOT NULL,
  email_verified boolean NOT NULL DEFAULT false,
  school_id bigint NOT NULL REFERENCES schools(id) ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_users_school_id ON users (school_id);
