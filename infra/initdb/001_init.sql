-- ============================
-- users
-- ============================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role INT NOT NULL CHECK (role IN (0, 1)),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- channels
-- ============================
CREATE TABLE channels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    status INT NOT NULL CHECK (status IN (0, 1, 2)) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- channel_members
-- ============================
CREATE TABLE channel_members (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL REFERENCES channels(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_channel_user UNIQUE (channel_id, user_id)
);

-- ============================
-- messages
-- ============================
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL REFERENCES channels(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    reply_to_message_id BIGINT REFERENCES messages(id),
    request_id VARCHAR(255) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    status INT NOT NULL CHECK (status IN (0, 1, 2)) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ============================
-- operation_logs
-- ============================
CREATE TABLE operation_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    operation_type VARCHAR(50) NOT NULL,
    target VARCHAR(255),
    result VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- CHECK (operation_type IN (
    --     'login',
    --     'logout',
    --     'user_created',
    --     'user_updated',
    --     'user_deleted',
    --     'channel_created',
    --     'channel_updated',
    --     'channel_join',
    --     'channel_leave',
    --     'channel_deleted',
    --     'message_created',
    --     'message_updated',
    --     'message_deleted',
    --     'mention_added',
    --     'mention_removed',
    --     'reaction_added',
    --     'reaction_removed',
    --     'template_created',
    --     'template_updated',
    --     'template_deleted',
    --     'search'
    -- ))
);

-- ============================
-- message_template_categories
-- ============================
CREATE TABLE message_template_categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(50) NOT NULL UNIQUE,
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- message_templates
-- ============================
CREATE TABLE message_templates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES message_template_categories(id),
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- message_mentions
-- ============================
CREATE TABLE message_mentions (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id),
    mentioned_user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_message_mention UNIQUE (message_id, mentioned_user_id)
);

-- ============================
-- message_reactions
-- ============================
CREATE TABLE message_reactions (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    reaction_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_message_reaction UNIQUE (message_id, user_id, reaction_type)
);

-- ============================
-- message_attachments
-- ============================
CREATE TABLE message_attachments (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id),
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL UNIQUE,
    thumbnail_url TEXT UNIQUE,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)