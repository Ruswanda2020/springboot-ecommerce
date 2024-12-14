-- Rename columns in product table
ALTER TABLE product RENAME COLUMN create_at TO created_at;
ALTER TABLE product RENAME COLUMN update_at TO updated_at;

-- Rename columns in category table
ALTER TABLE category RENAME COLUMN create_at TO created_at;
ALTER TABLE category RENAME COLUMN update_at TO updated_at;

-- Rename columns in product_category table
ALTER TABLE product_category RENAME COLUMN create_at TO created_at;
ALTER TABLE product_category RENAME COLUMN update_at TO updated_at;
