-- Add read_at column to track when messages are read
ALTER TABLE messages ADD COLUMN read_at TIMESTAMP NULL;

-- Index for efficient unread message queries
CREATE INDEX idx_messages_read_at ON messages(read_at);
