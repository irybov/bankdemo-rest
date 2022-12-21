CREATE SCHEMA bankdemo;
CREATE TABLE bankdemo.accounts(
	id SERIAL PRIMARY KEY,
	created_at TIMESTAMP,
	updated_at TIMESTAMP,
	is_active BOOLEAN NOT NULL,
	name VARCHAR(20) NOT NULL,
	surname VARCHAR(40) NOT NULL,
	phone VARCHAR(10) UNIQUE NOT NULL,
	birthday DATE NOT NULL,
	password VARCHAR(100) NOT NULL
);
CREATE TABLE bankdemo.bills(
	id SERIAL PRIMARY KEY,
	created_at TIMESTAMP,
	updated_at TIMESTAMP,
	is_active BOOLEAN NOT NULL,
	balance NUMERIC(19,2) NOT NULL,
	currency VARCHAR(3) NOT NULL CONSTRAINT currencychk CHECK (char_length(currency) = 3),
	account_id INTEGER REFERENCES bankdemo.accounts(id)
);
CREATE TABLE bankdemo.operations(
	id BIGSERIAL PRIMARY KEY,
	created_at TIMESTAMP NOT NULL,
	amount DOUBLE PRECISION NOT NULL,
	action VARCHAR(10) NOT NULL,
	currency VARCHAR(3) NOT NULL CONSTRAINT currencychk CHECK (char_length(currency) = 3),
	sender INTEGER,
	recipient INTEGER
);
CREATE TABLE bankdemo.roles(
	roles VARCHAR(10) NOT NULL,
	account_id INTEGER REFERENCES bankdemo.accounts(id)
);