CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;

create table if not exists users (
  id serial PRIMARY KEY,
  username citext NOT NULL UNIQUE,
  password text NOT NULL,
  is_admin boolean NOT NULL DEFAULT FALSE
)
