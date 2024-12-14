-- Add column 'weight' to table 'product'
ALTER TABLE product
    ADD COLUMN weight DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
