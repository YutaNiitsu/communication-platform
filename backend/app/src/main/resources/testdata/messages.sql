INSERT INTO messages(channel_id, user_id, reply_to_message_id, content, status, created_at, updated_at) VALUES
-- status=0 (active)
(1, 1, NULL, 'hello world', 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(1, 1, 5, 'reply to 5', 0, '2024-01-01 11:00:00', '2024-01-01 11:00:00'),
(1, 2, NULL, 'test message', 0, '2024-01-02 09:00:00', '2024-01-02 09:00:00'),

-- status=1 (archived)
(2, 1, NULL, 'archived message', 1, '2024-01-03 10:00:00', '2024-01-03 10:00:00'),

-- status=2 (deleted)
(1, 1, NULL, 'deleted message', 2, '2024-01-04 10:00:00', '2024-01-04 10:00:00'),

-- for new messages test
(1, 1, NULL, 'old message', 0, '2024-01-05 10:00:00', '2024-01-05 10:00:00'),
(1, 1, NULL, 'new message 1', 0, '2024-01-06 10:00:00', '2024-01-06 10:00:00'),
(1, 1, NULL, 'new message 2', 0, '2024-01-07 10:00:00', '2024-01-07 10:00:00'),

(1, 1, NULL, 'hello world', 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(1, 1, NULL, 'hello world', 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(1, 1, NULL, 'hello world', 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(1, 1, NULL, 'hello world', 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00');
