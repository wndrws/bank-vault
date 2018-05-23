CREATE TYPE cell_size as ENUM ('SMALL','MEDIUM','BIG');
CREATE TYPE cell_app_status as ENUM ('CREATED','CELL_CHOSEN','APPROVED','PAID');

CREATE TABLE Cell (
    id          integer     PRIMARY KEY,
    size        cell_size   NOT NULL,
    precious_id integer     DEFAULT NULL REFERENCES Precious(id)
);

CREATE TABLE Precious (
    id          integer     PRIMARY KEY,
    name        varchar     NOT NULL DEFAULT '',
    volume      integer     NOT NULL
);

CREATE TABLE Client (
    id          integer     PRIMARY KEY,
    serial      varchar     UNIQUE,
    first_name  varchar     NOT NULL CHECK (first_name <> ''),
    last_name   varchar     NOT NULL CHECK (last_name <> ''),
    patronymic  varchar     ,
    birth_date  date        ,
    phone       varchar     ,
    email       varchar
);

CREATE TABLE CellApplication (
    id          integer     PRIMARY KEY,
    client_id   integer     NOT NULL REFERENCES Client(id),
    cell_id     integer     DEFAULT NULL REFERENCES Cell(id),
    period_y    integer     ,
    period_m    integer     ,
    period_d    integer     ,
    status      cell_app_status NOT NULL,
    CHECK (period_y + period_m + period_d > 0)
);