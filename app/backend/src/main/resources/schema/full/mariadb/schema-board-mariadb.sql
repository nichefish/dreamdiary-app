-- Board schema for MariaDB
-- FK columns that may be populated after insert should remain nullable.
-- @database : mariadb
-- @author : nichefish

-- ---------- --

-- Board definition
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS board (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'board id',
    board_key VARCHAR(30) NOT NULL COMMENT 'board key',
    board_name VARCHAR(120) NOT NULL COMMENT 'board name',
    description VARCHAR(2000) COMMENT 'description',
    -- STATE (module)
    sort_order INT DEFAULT 0 COMMENT 'sort order',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT 'use yn',
    -- AUDIT
    created_by VARCHAR(20) COMMENT 'created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'created at',
    updated_by VARCHAR(20) COMMENT 'updated by',
    updated_at DATETIME COMMENT 'updated at',
    deleted_at DATETIME COMMENT 'deleted at',
    -- CONSTRAINT
    UNIQUE KEY uk_board_board_key (board_key)
) COMMENT = 'board definition';

-- ---------- --

-- Board post
-- @extends: BasePostEntity
-- @implements: TagEmbed, CommentEmbed, ViewerEmbed, PrefixEmbed
CREATE TABLE IF NOT EXISTS board_post(
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'post id',
    content_type VARCHAR(30) COMMENT 'board key via content type',
    -- POST
    title VARCHAR(200) COMMENT 'title',
    content LONGTEXT COMMENT 'content',
    -- FILE_GROUP
    file_group_id INT COMMENT 'file group id',
    -- AUDIT
    created_by VARCHAR(20) COMMENT 'created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'created at',
    updated_by VARCHAR(20) COMMENT 'updated by',
    updated_at DATETIME COMMENT 'updated at',
    deleted_at DATETIME COMMENT 'deleted at',
    -- CONSTRAINT
    CONSTRAINT fk_board_post_content_type
        FOREIGN KEY (content_type) REFERENCES board(board_key),
    INDEX board_post_content_type_idx (content_type)
) COMMENT = 'board post';
