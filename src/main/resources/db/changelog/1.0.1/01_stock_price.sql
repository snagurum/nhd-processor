create table nse.STOCK(
    id                integer primary key generated always as identity,
    ticker           varchar(50),        -- SYMBOL
    name             varchar(200),       -- NAME OF COMPANY
    series           varchar(20),        -- SERIES
    date_of_listing  date,               -- DATE OF LISTING
    paid_up_value    varchar(20),        -- PAID UP VALUE
    market_lot       varchar(20),        -- MARKET LOT
    isin_number      varchar(20),        -- ISIN NUMBER
    face_value       varchar(20),        -- FACE VALUE
    active           boolean default true,
    crd_date         date,
    upd_date         date,
    crd_by           varchar(20),
    upd_by           varchar(20)
) ;


CREATE TABLE nse.stock_price (
    id               INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    ticker           VARCHAR(20) NOT NULL,
    price_date       DATE NOT NULL,
    series           VARCHAR(10),
    open             NUMERIC(10, 2),
    high             NUMERIC(10, 2),
    low              NUMERIC(10, 2),
    prev_close       NUMERIC(10, 2),
    close            NUMERIC(10, 2),
    vwap             NUMERIC(10, 2),
    high52           NUMERIC(10, 2),
    low52            NUMERIC(10, 2),
    volume           BIGINT,
    value            NUMERIC(15, 2),
    no_of_trades     BIGINT,
    ltp              NUMERIC(10, 2),
    ffmc             BIGINT
);

ALTER TABLE nse.stock_price 
ADD CONSTRAINT unique_ticker_date UNIQUE (ticker, price_date);

CREATE INDEX idx_stock_price_ticker_date 
ON nse.stock_price (ticker, price_date);

CREATE INDEX idx_stock_price_date 
ON nse.stock_price (price_date);

CREATE INDEX idx_stock_price_ticker_pattern_date 
ON nse.stock_price (ticker varchar_pattern_ops, price_date);

