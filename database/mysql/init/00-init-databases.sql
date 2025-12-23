CREATE DATABASE IF NOT EXISTS request_service;
CREATE DATABASE IF NOT EXISTS recipient_service;

CREATE USER IF NOT EXISTS 'request_user'@'%' IDENTIFIED BY 'request_pass';
CREATE USER IF NOT EXISTS 'recipient_user'@'%' IDENTIFIED BY 'recipient_pass';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON request_service.* TO 'request_user'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON recipient_service.* TO 'recipient_user'@'%';

FLUSH PRIVILEGES;