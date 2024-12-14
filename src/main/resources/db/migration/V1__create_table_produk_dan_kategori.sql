--create table product
CREATE  TABLE  product(
    product_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL ,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL ,
    stock_quantity INT NOT NULL ,
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--create table category
CREATE TABLE category(
    category_id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(255) NOT NULL ,
    description TEXT,
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP NULL
);

--create table product_category
CREATE TABLE product_category (
    product_id BIGINT,
    category_id BIGINT,
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP NULL,
    PRIMARY KEY (product_id, category_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);

--add index
CREATE INDEX idx_product_name ON product(name);
CREATE INDEX idx_category_name ON category(name);