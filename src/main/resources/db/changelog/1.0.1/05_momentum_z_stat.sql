
-- MOMENTUM Z STATS

CREATE TABLE nse.momentum_z_stats (
    id                          INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    ticker_list_name            VARCHAR(20) NOT NULL,
    price_date                  DATE NOT NULL,
    mean_mr12                   NUMERIC(10, 2),
    std_dev_mr12                NUMERIC(10, 2),
    mean_mr6                    NUMERIC(10, 2),
    std_dev_mr6                 NUMERIC(10, 2)
);


ALTER TABLE nse.momentum_z_stats
ADD CONSTRAINT mzs_unique_ticker_date UNIQUE (ticker_list_name, price_date);

CREATE INDEX idx_momentum_z_stats_ticker_date 
ON nse.momentum_z_stats (ticker_list_name, price_date);

CREATE INDEX idx_momentum_z_stats_date 
ON nse.momentum_z_stats (price_date);
