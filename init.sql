CREATE DATABASE IF NOT EXISTS omnicharge_auth;
CREATE DATABASE IF NOT EXISTS omnicharge_users;
CREATE DATABASE IF NOT EXISTS omnicharge_recharge;
CREATE DATABASE IF NOT EXISTS omnicharge_payments;
CREATE DATABASE IF NOT EXISTS omnicharge_operators;

GRANT ALL PRIVILEGES ON omnicharge_auth.*      TO 'omnicharge'@'%';
GRANT ALL PRIVILEGES ON omnicharge_users.*     TO 'omnicharge'@'%';
GRANT ALL PRIVILEGES ON omnicharge_recharge.*  TO 'omnicharge'@'%';
GRANT ALL PRIVILEGES ON omnicharge_payments.*  TO 'omnicharge'@'%';
GRANT ALL PRIVILEGES ON omnicharge_operators.* TO 'omnicharge'@'%';
FLUSH PRIVILEGES;
