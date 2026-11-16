INSERT INTO message_templates(user_id, title, content, category_id, is_shared, created_at, updated_at) VALUES
(1, 'test', 'content1', 1, true,  '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(1, 'private1', 'content2', 1, false, '2024-01-02 10:00:00', '2024-01-02 10:00:00'),
(2, 'shared2', 'content3', 2, true, '2024-01-03 10:00:00', '2024-01-03 10:00:00'),
(2, 'private2', 'content4', 2, false, '2024-01-04 10:00:00', '2024-01-04 10:00:00');
