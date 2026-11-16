-- 既存データをクリア
DELETE FROM message_attachments;

-- 1件目（最古・最小サイズ）
INSERT INTO message_attachments (
    message_id,
    original_file_name,
    stored_file_name,
    file_url,
    thumbnail_url,
    file_type,
    file_size,
    created_at
) VALUES (
    100,
    'file1.pdf',
    'file1_stored.pdf',
    '/files/file1_stored.pdf',
    NULL,
    'pdf',
    12345,
    '2024-01-01 10:00:00'
);

-- 2件目（サイズ最大）
INSERT INTO message_attachments (
    message_id,
    original_file_name,
    stored_file_name,
    file_url,
    thumbnail_url,
    file_type,
    file_size,
    created_at
) VALUES (
    100,
    'file2.pdf',
    'file2_stored.pdf',
    '/files/file2_stored.pdf',
    NULL,
    'pdf',
    99999,
    '2024-01-01 11:00:00'
);

-- 3件目（別 messageId）
INSERT INTO message_attachments (
    message_id,
    original_file_name,
    stored_file_name,
    file_url,
    thumbnail_url,
    file_type,
    file_size,
    created_at
) VALUES (
    200,
    'file3.pdf',
    'file3_stored.pdf',
    '/files/file3_stored.pdf',
    NULL,
    'pdf',
    50000,
    '2024-01-02 09:00:00'
);
