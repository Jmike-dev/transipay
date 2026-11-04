-- V2__create_fees_table.sql
CREATE TABLE fees (
                      id UUID PRIMARY KEY,
                      company_id UUID NOT NULL REFERENCES company(id),
                      amount NUMERIC(19, 4) NOT NULL
);