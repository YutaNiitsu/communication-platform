DELETE FROM messages;

INSERT INTO messages(channel_id, user_id, reply_to_message_id, content, status, created_at, updated_at)
VALUES
(1, 1, NULL, 'deleted1', 2, NOW(), NOW()),
(1, 1, NULL, 'deleted2', 2, NOW(), NOW());
