CREATE TABLE rule (
    id SERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE rule_item (
    id SERIAL PRIMARY KEY,
    condition TEXT NOT NULL,
    then_value TEXT NOT NULL,
    else_value TEXT,
    rule_id INT NOT NULL,
    CONSTRAINT fk_rule FOREIGN KEY (rule_id) REFERENCES rule(id) ON DELETE CASCADE
);

CREATE INDEX idx_rule_items_rule_id ON rule_item (rule_id);

