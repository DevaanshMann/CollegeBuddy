-- V1__init.sql (MariaDB/InnoDB)
--USE app_db;
-- Ensure we’re using a transactional, FK-supporting engine and modern charset
CREATE TABLE IF NOT EXISTS schools (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  domain VARCHAR(255) NOT NULL,
  name   VARCHAR(255) NOT NULL,
  CONSTRAINT pk_schools PRIMARY KEY (id),
  CONSTRAINT uq_schools_domain UNIQUE (domain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  email_verified BOOLEAN NOT NULL DEFAULT FALSE, -- BOOLEAN is TINYINT(1) in MariaDB
  school_id BIGINT UNSIGNED NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT uq_users_email UNIQUE (email),
  CONSTRAINT fk_users_school
    FOREIGN KEY (school_id)
    REFERENCES schools (id)
    ON UPDATE RESTRICT
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional: MariaDB will create an index for the FK automatically,
-- but this makes it explicit and names it deterministically.
CREATE INDEX idx_users_school_id ON users (school_id);
