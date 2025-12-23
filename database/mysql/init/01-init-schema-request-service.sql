USE request_service;

CREATE TABLE IF NOT EXISTS requests (
  id              VARCHAR(36)  NOT NULL,
  delivery_type   VARCHAR(16)  NOT NULL,
  status          VARCHAR(24)  NOT NULL,
  created_at      DATETIME(3)  NOT NULL,
  updated_at      DATETIME(3)  NOT NULL,
  failure_code    VARCHAR(32)  NULL,
  failure_message VARCHAR(512) NULL,

  PRIMARY KEY (id)

) ENGINE=InnoDB;

ALTER TABLE requests
  ADD CONSTRAINT chk_requests_status
  CHECK (status IN ('IN_ATTESA','IN_ELABORAZIONE','COMPLETATA','FALLITA'));


CREATE TABLE IF NOT EXISTS request_recipients (
  id                 BIGINT       NOT NULL AUTO_INCREMENT,
  request_id         VARCHAR(36)  NOT NULL,
  recipient_address  VARCHAR(256) NOT NULL,

  PRIMARY KEY (id),

  UNIQUE KEY uk_req_rec (request_id, recipient_address),

  CONSTRAINT fk_req_rec_request
    FOREIGN KEY (request_id) REFERENCES requests(id)
    ON DELETE CASCADE

) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS request_documents (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  request_id   VARCHAR(36)  NOT NULL,
  document_ref VARCHAR(512) NOT NULL,

  PRIMARY KEY (id),

  UNIQUE KEY uk_req_doc (request_id, document_ref),

  CONSTRAINT fk_req_doc_request
    FOREIGN KEY (request_id) REFERENCES requests(id)
    ON DELETE CASCADE

) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS outbox_events (
  id              VARCHAR(36)  NOT NULL,
  event_type      VARCHAR(64)  NOT NULL,
  destination     VARCHAR(64)  NOT NULL,
  key_value       VARCHAR(36)  NOT NULL,
  payload         JSON         NOT NULL,
  status          VARCHAR(16)  NOT NULL,
  attempts        INT          NOT NULL DEFAULT 0,
  next_attempt_at DATETIME(3)  NULL,
  last_error      VARCHAR(512) NULL,
  created_at      DATETIME(3)  NOT NULL,
  sent_at         DATETIME(3)  NULL,

  PRIMARY KEY (id)

) ENGINE=InnoDB;

ALTER TABLE outbox_events
  ADD CONSTRAINT chk_outbox_status
  CHECK (status IN ('PENDING','IN_PROGRESS','SENT','DEAD'));


CREATE TABLE IF NOT EXISTS consumed_events (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  consumer_name VARCHAR(64)  NOT NULL,
  event_id      VARCHAR(36)  NOT NULL,
  received_at   DATETIME(3)  NOT NULL,

  PRIMARY KEY (id),
  UNIQUE KEY uk_consumer_event (consumer_name, event_id)

) ENGINE=InnoDB;
