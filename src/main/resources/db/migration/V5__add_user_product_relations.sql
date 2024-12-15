--add user_id column to product table
ALTER TABLE product
ADD COLUMN user_id BIGINT;

--add foreign key constraint
ALTER TABLE product
ADD CONSTRAINT fk_product_user
FOREIGN KEY (user_id) REFERENCES users(user_id);

--add index on user_id for better performance
CREATE INDEX idx_product_user_id ON product(user_id);