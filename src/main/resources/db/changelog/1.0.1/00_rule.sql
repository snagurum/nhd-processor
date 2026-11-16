
-- RULE ENGINE
ALTER TABLE re.rule ADD algo_type VARCHAR(255) default 'FROM_DATE'; -- PRICE_DATE/TICKER

ALTER TABLE re.rule ADD source_tables VARCHAR(255);

ALTER TABLE re.rule ADD destination_table VARCHAR(255);
