CREATE TABLE currency
(
    id            UUID NOT NULL,
    currency_code VARCHAR(3),
    CONSTRAINT pk_currency PRIMARY KEY (id)
);

CREATE TABLE country
(
    id           UUID NOT NULL,
    country_code VARCHAR(3),
    currency_id  UUID NOT NULL,
    CONSTRAINT pk_country PRIMARY KEY (id)
);

ALTER TABLE country
    ADD CONSTRAINT FK_COUNTRY_ON_CURRENCY FOREIGN KEY (currency_id) REFERENCES currency (id);

INSERT INTO currency (id, currency_code) VALUES ('68541477-19d6-4fc6-a570-26d6c673f3e0', 'EUR');
INSERT INTO currency (id, currency_code) VALUES ('62d0a663-c506-45bc-9b6b-ee07f9258cd1', 'PLN');
INSERT INTO country (id, country_code, currency_id) VALUES ('04ce7038-38d6-449f-99d9-6f8f5773c7e7', 'DE', '68541477-19d6-4fc6-a570-26d6c673f3e0');
INSERT INTO country (id, country_code, currency_id) VALUES ('f634cb19-7d6f-41b2-8aab-8f1a0b1133db', 'PL', '62d0a663-c506-45bc-9b6b-ee07f9258cd1');
