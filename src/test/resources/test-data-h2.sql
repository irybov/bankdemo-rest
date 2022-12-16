INSERT INTO bankdemo.operations(id, created_at, action, amount, currency, sender, recipient)
VALUES('1', now(), 'deposit', '500.00' , 'RUB', '0', '1'),
('2', now(), 'withdraw', '100.00' , 'RUB', '1', '0'),
('3', now(), 'transfer', '300.00' , 'RUB', '1', '2'),
('4', now(), 'deposit', '400.00' , 'RUB', '0', '3'),
('5', now(), 'withdraw', '700.00' , 'RUB', '3', '0'),
('6', now(), 'transfer', '800.00' , 'RUB', '2', '3');