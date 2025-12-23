USE recipient_service;

CREATE TABLE IF NOT EXISTS recipients (
  id                VARCHAR(36)   NOT NULL,
  digital_address   VARCHAR(256)  NOT NULL,
  status            VARCHAR(16)   NOT NULL,
  created_at        DATETIME(3)   NOT NULL,
  updated_at        DATETIME(3)   NOT NULL,

  PRIMARY KEY (id),

  UNIQUE KEY uk_recipients_address (digital_address)

) ENGINE=InnoDB;

ALTER TABLE recipients
  ADD CONSTRAINT chk_recipients_status
  CHECK (status IN ('VALID','INVALID'));


CREATE TABLE IF NOT EXISTS recipients_status_audit (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  recipient_id VARCHAR(36)  NOT NULL,
  old_status   VARCHAR(16)  NOT NULL,
  new_status   VARCHAR(16)  NOT NULL,
  updated_at   DATETIME(3)  NOT NULL,

  PRIMARY KEY (id),

  CONSTRAINT fk_audit_recipient
    FOREIGN KEY (recipient_id) REFERENCES recipients(id)
    ON DELETE CASCADE

) ENGINE=InnoDB;