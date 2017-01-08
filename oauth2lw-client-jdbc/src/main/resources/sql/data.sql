INSERT INTO user (username,email, password, activated) VALUES ('admin', 'admin@mail.me', 'admin', true);
INSERT INTO user (username,email, password, activated) VALUES ('user', 'user@mail.me', 'user', true);

INSERT INTO authority (name) VALUES ('ROLE_USER');
INSERT INTO authority (name) VALUES ('ROLE_ADMIN');

INSERT INTO user_authority (username,authority) VALUES ('user', 'ROLE_USER');
INSERT INTO user_authority (username,authority) VALUES ('admin', 'ROLE_USER');
INSERT INTO user_authority (username,authority) VALUES ('admin', 'ROLE_ADMIN');