CREATE TABLE author (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        mini_bio TEXT
);

CREATE TABLE ebook (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       image_url VARCHAR(512),
                       price DECIMAL(10, 2) NOT NULL,
                       page_count INT,
                       isbn VARCHAR(50),
                       publication_date DATE,
                       git_repo_url VARCHAR(512) NOT NULL
);

CREATE TABLE ebook_author (
                              ebook_id BIGINT NOT NULL,
                              author_id BIGINT NOT NULL,
                              PRIMARY KEY (ebook_id, author_id),
                              FOREIGN KEY (ebook_id) REFERENCES ebook(id) ON DELETE CASCADE,
                              FOREIGN KEY (author_id) REFERENCES author(id) ON DELETE CASCADE
);

CREATE TABLE customer_order (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                client_name VARCHAR(255) NOT NULL,
                                cpf VARCHAR(14) NOT NULL,
                                email VARCHAR(255) NOT NULL,
                                address TEXT NOT NULL,
                                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_item (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            order_id BIGINT NOT NULL,
                            ebook_id BIGINT NOT NULL,
                            purchase_price DECIMAL(10, 2) NOT NULL,
                            discount DECIMAL(10, 2) DEFAULT 0.00,
                            FOREIGN KEY (order_id) REFERENCES customer_order(id) ON DELETE CASCADE,
                            FOREIGN KEY (ebook_id) REFERENCES ebook(id)
);