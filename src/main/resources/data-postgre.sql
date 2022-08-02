INSERT INTO bankdemo.accounts(id, active, birthday, name, password, phone, surname, timestamp)
VALUES('0', '1', '1900-01-01', 'Admin', crypt('superadmin', gen_salt('bf', 4)), '0000000000', 'Adminov', now());
INSERT INTO bankdemo.accounts(active, birthday, name, password, phone, surname, timestamp)
VALUES('1', '2022-05-09', 'Nicole', crypt('straponmilf', gen_salt('bf', 4)), '1111111111', 'Moore', now()),
('1', '2022-05-09', 'Marsha', crypt('bustyblonde', gen_salt('bf', 4)), '2222222222', 'Jordan', now()),
('1', '2022-05-09', 'Nasty', crypt('gingerchick', gen_salt('bf', 4)), '3333333333', 'Crown', now()),
('1', '2022-05-09', 'Ursula', crypt('supervixen', gen_salt('bf', 4)), '4444444444', 'Digard', now())
ON CONFLICT ON CONSTRAINT accounts_phone_key DO NOTHING;
INSERT INTO bankdemo.roles(account_id, roles) SELECT id, 'ADMIN' FROM bankdemo.accounts WHERE phone='0000000000'
UNION SELECT id, 'ADMIN' FROM bankdemo.accounts WHERE phone='1111111111'
UNION SELECT id, 'CLIENT' FROM bankdemo.accounts WHERE phone='2222222222'
UNION SELECT id, 'CLIENT' FROM bankdemo.accounts WHERE phone='3333333333'
UNION SELECT id, 'ADMIN' FROM bankdemo.accounts WHERE phone='3333333333'
UNION SELECT id, 'CLIENT' FROM bankdemo.accounts WHERE phone='4444444444';