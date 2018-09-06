CREATE TABLE accounts (
    accountId UUID NOT NULL,
    balance INT NOT NULL DEFAULT 0,
    version INT NOT NULL
);