CREATE SCHEMA IF NOT EXISTS db;

DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id int,
    name varchar(250),
    password_hash varchar(250)
);

CREATE TABLE messages (
    message_id int,
    belongs_to_user int,
    message varchar(250)
);

INSERT INTO users (user_id, name, password_hash) VALUES (1, 'savva.voloshin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO users (user_id, name, password_hash) VALUES (2, 'savva.voloshin.2', '8bb0cf6eb9b17d0f7d22b456f121257dc1254e1f01665370476383ea776df414');

INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 1');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 2');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 3');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 4');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 5');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 6');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 7');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 8');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 9');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 10');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 11');
INSERT INTO messages (message_id, belongs_to_user, message) VALUES (1, 1, 'test message 12');