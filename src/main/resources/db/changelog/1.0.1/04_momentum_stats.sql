
-- MOMENTUM STATS

CREATE TABLE nse.momentum_stats (
    id                          INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    ticker                      VARCHAR(20) NOT NULL,
    price_date                  DATE NOT NULL,
    daily_return                NUMERIC(10, 2),
    ln_daily_return             NUMERIC(10, 2),
    mean_1yr_ln_daily_return    NUMERIC(10, 2),
    sum_1yr_ln_daily_return     NUMERIC(10, 2),
    n_1yr                       NUMERIC(10),
    std_dev_1yr_ln_daily_return NUMERIC(10, 2),
    std_Dev_1yr_Annualized      NUMERIC(10, 2),
    price_return_1yr            NUMERIC(10, 2),
    mr12                        NUMERIC(10, 2),
    
    mean_6m_ln_daily_return     NUMERIC(10, 2),
    sum_6m_ln_daily_return      NUMERIC(10, 2),
    n_6m                        NUMERIC(10, 2),
    std_dev_6m_ln_daily_return  NUMERIC(10, 2),
    std_dev_6m_annualized       NUMERIC(10, 2),
    price_return_6m             NUMERIC(10, 2),
    mr6                         NUMERIC(10, 2)
);


ALTER TABLE nse.momentum_stats
ADD CONSTRAINT ms_unique_ticker_date UNIQUE (ticker, price_date);

CREATE INDEX idx_momentum_stats_ticker_date 
ON nse.momentum_stats (ticker, price_date);

CREATE INDEX idx_momentum_stats_date 
ON nse.momentum_stats (price_date);

