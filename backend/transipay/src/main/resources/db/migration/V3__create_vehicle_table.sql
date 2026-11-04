CREATE SEQUENCE vehicle_code_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE vehicle (
                         id           UUID PRIMARY KEY,
                         code         VARCHAR(20)  NOT NULL UNIQUE,
                         number_plate VARCHAR(20)  NOT NULL UNIQUE,
                         company_id   UUID         NOT NULL,
                         CONSTRAINT fk_vehicle_company
                             FOREIGN KEY (company_id) REFERENCES company (id)
);

CREATE INDEX idx_vehicle_company_id ON vehicle (company_id);