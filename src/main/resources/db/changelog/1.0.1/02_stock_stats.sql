

CREATE TABLE nse.stock_stats (
    id               INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    ticker           VARCHAR(20) NOT NULL,
    price_date       DATE NOT NULL,
    daily_return     NUMERIC(10, 2),
    ln_daily_return  NUMERIC(10, 2)
);

ALTER TABLE nse.stock_stats
ADD CONSTRAINT ss_unique_ticker_date UNIQUE (ticker, price_date);

CREATE INDEX idx_stock_stats_ticker_date 
ON nse.stock_stats (ticker, price_date);

CREATE INDEX idx_stock_stats_date 
ON nse.stock_stats (price_date);

CREATE INDEX idx_stock_stats_ticker_pattern_date 
ON nse.stock_stats (ticker varchar_pattern_ops, price_date);

