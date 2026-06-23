-- Autores
INSERT INTO author (id, name, mini_bio) VALUES
                                        (1,'Gregor Hohpe', 'Especialista em arquitetura de integração e autor renomado.'),
                                        (2, 'Bobby Woolf', 'Co-autor do clássico Enterprise Integration Patterns.'),
                                        (3, 'Martin Fowler', 'Um dos pais do Manifesto Ágil e especialista em refatoração.'),
                                        (4, 'Claus Ibsen', 'Principal committer do Apache Camel.'),
                                        (5, 'Jonathan Anstey', 'Desenvolvedor open-source e committer do Apache Camel.');

-- Ebooks
INSERT INTO ebook (id, title, description, image_url, price, page_count, isbn, publication_date, git_repo_url) VALUES
                                                                                                               (1, 'Enterprise Integration Patterns', 'O livro definitivo sobre padrões de integração de sistemas corporativos.', 'https://m.media-amazon.com/images/I/41J97lRS7WL._SY445_SX342_FMwebp_.jpg', 249.90, 736, '978-0321200686', '2003-10-20', 'git@github.com:cotubify-internal/eip-book.git'),
                                                                                                               (2, 'Refactoring', 'Melhorando o design de códigos existentes de forma segura e pragmática.', 'https://m.media-amazon.com/images/I/71e6ndHEwqL._SY522_.jpg', 199.90, 448, '978-0134757599', '2018-11-29', 'git@github.com:cotubify-internal/refactoring-book.git'),
                                                                                                               (3, 'Camel in Action', 'Guia completo e prático sobre como utilizar o framework de integração Apache Camel.', 'https://m.media-amazon.com/images/I/81qRWQsM-kL._SL1500_.jpg', 189.90, 712, '978-1617292934', '2018-02-15', 'git@github.com:cotubify-internal/camel-book.git');

-- Relacionamento Livro x Autor
INSERT INTO ebook_author (ebook_id, author_id) VALUES
                                                   (1, 1), (1, 2), -- EIP -> Gregor e Bobby
                                                   (2, 3),         -- Refactoring -> Martin Fowler
                                                   (3, 4), (3, 5); -- Camel in Action -> Claus e Jonathan