ALTER TABLE nse.stock_stats ADD column return_1yr numeric(10,2);
ALTER TABLE nse.stock_stats ADD column return_6m numeric(10,2);
ALTER TABLE nse.stock_stats ADD column return_3m numeric(10,2);
ALTER TABLE nse.stock_stats ADD column return_1m numeric(10,2);

ALTER TABLE nse.stock_stats ADD column std_dev_1yr numeric(10,2);
ALTER TABLE nse.stock_stats ADD column std_dev_6m numeric(10,2);
ALTER TABLE nse.stock_stats ADD column std_dev_3m numeric(10,2);
ALTER TABLE nse.stock_stats ADD column std_dev_1m numeric(10,2);

