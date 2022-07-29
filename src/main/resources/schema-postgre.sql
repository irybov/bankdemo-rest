CREATE SCHEMA IF NOT EXISTS bankdemo;
CREATE TABLE IF NOT EXISTS bankdemo.accounts(
	id SERIAL PRIMARY KEY,
	timestamp TIMESTAMP NOT NULL,
	active BOOLEAN NOT NULL,
	name VARCHAR(20) NOT NULL,
	surname VARCHAR(40) NOT NULL,
	phone CHAR(10) UNIQUE NOT NULL,
	birthday DATE NOT NULL,
	password VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS bankdemo.bills(
	id SERIAL PRIMARY KEY,
	timestamp TIMESTAMP NOT NULL,
	active BOOLEAN NOT NULL,
	balance NUMERIC(19,2) NOT NULL,
	currency CHAR(3) NOT NULL,
	account_id INTEGER REFERENCES bankdemo.accounts(id)
);
CREATE TABLE IF NOT EXISTS bankdemo.operations(
	id BIGSERIAL PRIMARY KEY,
	timestamp TIMESTAMP NOT NULL,
	amount DOUBLE PRECISION NOT NULL,
	action VARCHAR(50) NOT NULL,
	currency CHAR(3) NOT NULL,
	sender INTEGER,
	recipient INTEGER
);
CREATE TABLE IF NOT EXISTS bankdemo.roles(
	roles VARCHAR(50) NOT NULL,
	account_id INTEGER REFERENCES bankdemo.accounts(id) NOT NULL
);
CREATE extension IF NOT EXISTS pgcrypto;