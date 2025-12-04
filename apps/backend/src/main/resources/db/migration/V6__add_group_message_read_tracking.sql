-- Add last_read_message_id column to group_members table for tracking which messages each member has read
ALTER TABLE group_members
ADD COLUMN last_read_message_id BIGINT;

-- Add foreign key constraint (optional, but good practice)
ALTER TABLE group_members
ADD CONSTRAINT fk_group_members_last_read_message
FOREIGN KEY (last_read_message_id) REFERENCES group_messages(id) ON DELETE SET NULL;

-- Add index for better query performance
CREATE INDEX idx_group_members_last_read ON group_members(last_read_message_id);
