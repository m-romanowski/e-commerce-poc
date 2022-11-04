CREATE TABLE IF NOT EXISTS "order"
(
    id         UUID         NOT NULL,
    user_id    VARCHAR(255),
    total      DECIMAL      NOT NULL,
    status     VARCHAR(255) NOT NULL,
    payment_id VARCHAR(255) NOT NULL,
    version    BIGINT,
    CONSTRAINT pk_orderentity PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS product
(
    id       UUID NOT NULL,
    order_id UUID NOT NULL,
    version  BIGINT,
    CONSTRAINT pk_orderproductsentity PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS order_outbox
(
    id      SERIAL       NOT NULL,
    key     UUID         NOT NULL,
    payload TEXT         NOT NULL,
    type    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_orderoutboxentity PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS invoice
(
    id         UUID                        NOT NULL,
    order_id   UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version    BIGINT,
    CONSTRAINT pk_invoiceentity PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS invoice_outbox
(
    id      SERIAL       NOT NULL,
    key     UUID         NOT NULL,
    payload TEXT         NOT NULL,
    type    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_invoiceoutboxentity PRIMARY KEY (id)
);
