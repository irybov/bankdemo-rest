TRUNCATE TABLE bankdemo.operations;
UPDATE bankdemo.accounts 
SET password = 'superadmin' 
WHERE phone='0000000000';