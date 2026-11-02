CREATE TABLE payment (
                         id                    UUID PRIMARY KEY,
                         vehicle_id            UUID           NOT NULL,
                         number_plate          VARCHAR(20)    NOT NULL,   -- snapshot at time of payment
                         phone_number          VARCHAR(15)    NOT NULL,
                         amount                NUMERIC(12, 2) NOT NULL,
                         status                VARCHAR(20)    NOT NULL,   -- PENDING | SUCCESS | FAILED | CANCELLED
                         merchant_request_id   VARCHAR(100),
                         checkout_request_id   VARCHAR(100)   UNIQUE,
                         mpesa_receipt_number  VARCHAR(50)    UNIQUE,
                         result_code           INTEGER,
                         result_desc           VARCHAR(255),
                         transaction_date      TIMESTAMPTZ,
                         created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
                         updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
                         CONSTRAINT fk_payment_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id)
);

CREATE INDEX idx_payment_vehicle_id ON payment (vehicle_id);
CREATE INDEX idx_payment_status ON payment (status);