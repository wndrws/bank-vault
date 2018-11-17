-- CREATE TYPE cell_size as ENUM ('SMALL','MEDIUM','BIG');
-- CREATE TYPE cell_app_status as ENUM ('CREATED','CELL_CHOSEN','APPROVED','PAID');
-- cannot use these ^^^ because of SpringData limitations

CREATE TABLE Cell (
    id          serial4     PRIMARY KEY,
    real_id     integer     NOT NULL UNIQUE,
    size        varchar     NOT NULL CHECK (size <> ''), -- cell_size
    precious_id integer     DEFAULT NULL REFERENCES Precious(id),
    client_id   integer     DEFAULT NULL REFERENCES Client(id),
    lease_begin date        ,
    lease_end   date        ,
    expired     boolean     DEFAULT FALSE,
    pending     boolean     DEFAULT FALSE
);

CREATE TABLE Precious (
    id          serial4     PRIMARY KEY,
    name        varchar     NOT NULL DEFAULT '',
    volume      integer     NOT NULL
);

CREATE TABLE Client (
    id          serial4      PRIMARY KEY,
    serial      varchar     UNIQUE,
    first_name  varchar     NOT NULL CHECK (first_name <> ''),
    last_name   varchar     NOT NULL CHECK (last_name <> ''),
    patronymic  varchar     ,
    birth_date  date        ,
    phone       varchar     ,
    email       varchar
);

CREATE TABLE CellApplication (
    id          serial4     PRIMARY KEY,
    client_id   integer     NOT NULL REFERENCES Client(id),
    cell_id     integer     DEFAULT NULL REFERENCES Cell(id),
    period_y    integer     ,
    period_m    integer     ,
    period_d    integer     ,
    status      varchar     NOT NULL CHECK (status <> ''), -- cell_app_status
    CHECK (period_y + period_m + period_d > 0)
);
