
CREATE TABLE product (
    id BIGINT PRIMARY KEY,              -- Product ID
    title VARCHAR(255) NOT NULL,         -- Product title (e.g., "Pace Running Shorts")
    product_type VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_variants (
         id BIGINT PRIMARY KEY,              -- Variant ID
         product_id BIGINT NOT NULL,         -- Foreign key to products
         title VARCHAR(255) NOT NULL,        -- Variant title (e.g., "Black / M")
         price DECIMAL(10,2) NOT NULL,       -- Price
         image_src TEXT,                     -- Image URL
         FOREIGN KEY (product_id) REFERENCES product(id),
         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);