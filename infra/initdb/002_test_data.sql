-- ============================
-- users（テストユーザー）
-- パスワードは全て「password」
-- ============================
INSERT INTO users (username, password_hash, role, is_active)
VALUES
    ('admin_user', '$2a$10$Fk1q4HaZRUJ2RbtiH8iZduIbOAIzwaKjydWucz4rlIvZqwxPGWAy6', 0, TRUE),
    ('test_user1', '$2a$10$Fk1q4HaZRUJ2RbtiH8iZduIbOAIzwaKjydWucz4rlIvZqwxPGWAy6', 1, TRUE),
    ('test_user2', '$2a$10$Fk1q4HaZRUJ2RbtiH8iZduIbOAIzwaKjydWucz4rlIvZqwxPGWAy6', 1, TRUE),
    ('a', '$2a$10$tQnGjiHg3pI7pjU5zQX0yOwdgt1z0ldl7q1W0y16cmyjOpRt7iK2W', 0, TRUE);

-- ============================
-- channels（テストチャンネル）
-- ============================
INSERT INTO channels (name, status)
VALUES
    ('general', 0),
    ('random', 0),
    ('dev', 0),
    ('testch', 0),
    ('inactive_channel', 1);

-- ============================
-- channel_members（チャンネル参加者）
-- ============================
INSERT INTO channel_members (channel_id, user_id)
VALUES
    (1, 1),  -- general に admin_user
    (1, 2),  -- general に test_user1
    (2, 2),  -- random に test_user1
    (3, 3),  -- dev に test_user2
    (4, 1),  -- testch に admin_user
    (5, 1);  -- inactive_channel に admin_user

-- ============================
-- messages（メッセージ）
-- ============================
INSERT INTO messages (channel_id, user_id, content, status)
VALUES
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, 'こんにちは！', 0),
    (1, 1, '削除', 2),
    (1, 2, 'はじめまして！', 0),
    (2, 2, 'random チャンネルです', 0),
    (3, 3, '開発チャンネルへようこそ', 0);


-- ============================
-- operation_logs（操作ログ）
-- ============================
INSERT INTO operation_logs (user_id, operation_type, target, result)
VALUES
    (1, 'login', 'user_id=1', 'ok'),
    (2, 'channel_join', 'channel_id=1', 'ok'),
    (2, 'message_create', 'message_id=2', 'content=こんにちは'),
    (3, 'search', 'keyword:テスト', 'result_count=10');


-- ============================
-- message_templates_categories
-- ============================
INSERT INTO message_template_categories (user_id, name, is_shared) VALUES 
(1, '挨拶', TRUE),
(1, '報告', TRUE),
(1, '依頼', TRUE),
(1, '共有', TRUE),
(1, 'その他', TRUE);


-- ============================
-- message_templates
-- ============================
INSERT INTO message_templates (user_id, title, content, category_id, is_shared) VALUES 
(
    1, 
    '作業完了報告', 
    '以下の作業が完了しましたのでご報告いたします。

・作業内容：
・対応日次：
・補足事項：

ご確認をお願いいたします。', 
    (SELECT id FROM message_template_categories WHERE name = '報告' LIMIT 1), 
    TRUE
);

