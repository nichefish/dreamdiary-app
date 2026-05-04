-- Hard cut migration: journal_diary / journal_note / journal_dream -> journal_entry
-- @database : mariadb
-- @author : codex
--
-- Intent
-- 1) Make journal_entry the single physical post table for diary/dream/note.
-- 2) Re-key every attachable reference to the new journal_entry.id.
-- 3) Preserve attachable polymorphism with existing content_type values.
--
-- Notes
-- - Run after full DB backup.
-- - Review in staging before production-like execution.
-- - This script assumes old tables still exist and journal_entry does not yet exist.
-- - If the deprecated JOURNAL_ENTRY-normalized script already ran, do not re-run this file.
--   Use migration-journal-entry-hard-cut-polymorphism-fix-mariadb.sql instead.

START TRANSACTION;

-- ---------------------------------------------------------------------------
-- 1. Canonical table
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS journal_entry (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'journal entry id',
    content_type VARCHAR(32) NOT NULL COMMENT 'JOURNAL_DIARY | JOURNAL_NOTE | JOURNAL_DREAM',
    source_content_type VARCHAR(32) COMMENT 'migration source content type',
    source_id INT COMMENT 'migration source id',

    journal_chapter_id INT COMMENT 'journal chapter id',

    title VARCHAR(200) COMMENT 'title',
    content LONGTEXT COMMENT 'content',
    sort_order INT DEFAULT 1 COMMENT 'sort order',

    file_group_id INT COMMENT 'file group id',
    history_triggered_by VARCHAR(20) COMMENT 'history triggered by',
    history_triggered_at DATETIME COMMENT 'history triggered at',

    -- temporary dream-specific columns
    nhtmr_yn CHAR(1) DEFAULT 'N' COMMENT 'nightmare',
    halluc_yn CHAR(1) DEFAULT 'N' COMMENT 'hypnagogic hallucination',
    else_dream_yn CHAR(1) DEFAULT 'N' COMMENT 'dream for another person',
    else_dreamer_nm VARCHAR(64) COMMENT 'dreamer name',

    created_by VARCHAR(20) COMMENT 'created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'created at',
    updated_by VARCHAR(20) COMMENT 'updated by',
    updated_at DATETIME COMMENT 'updated at',
    deleted_at DATETIME COMMENT 'deleted at',

    INDEX idx_journal_entry_chapter (journal_chapter_id),
    INDEX idx_journal_entry_content_type (content_type),
    INDEX idx_journal_entry_type_sort (content_type, journal_chapter_id, sort_order),
    UNIQUE KEY uk_journal_entry_source (source_content_type, source_id),
    INDEX idx_journal_entry_created_by (created_by),
    INDEX idx_journal_entry_deleted_at (deleted_at)
) COMMENT='canonical journal entry';

CREATE TABLE IF NOT EXISTS journal_entry_id_map (
    old_content_type VARCHAR(32) NOT NULL COMMENT 'old content type',
    old_id INT NOT NULL COMMENT 'old table pk',
    new_entry_id INT NOT NULL COMMENT 'journal_entry.id',
    migrated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'migrated at',
    PRIMARY KEY (old_content_type, old_id),
    UNIQUE KEY uk_journal_entry_id_map_new_entry (new_entry_id)
) COMMENT='old journal id to journal_entry id mapping';

-- ---------------------------------------------------------------------------
-- 2. Canonical row insert
-- ---------------------------------------------------------------------------

INSERT INTO journal_entry (
    content_type,
    source_content_type,
    source_id,
    journal_chapter_id,
    title,
    content,
    sort_order,
    file_group_id,
    history_triggered_by,
    history_triggered_at,
    nhtmr_yn,
    halluc_yn,
    else_dream_yn,
    else_dreamer_nm,
    created_by,
    created_at,
    updated_by,
    updated_at,
    deleted_at
)
SELECT
    'JOURNAL_DIARY',
    'JOURNAL_DIARY',
    d.id,
    d.journal_chapter_id,
    d.title,
    d.content,
    d.sort_order,
    d.file_group_id,
    d.history_triggered_by,
    d.history_triggered_at,
    'N',
    'N',
    'N',
    NULL,
    d.created_by,
    d.created_at,
    d.updated_by,
    d.updated_at,
    d.deleted_at
FROM journal_diary d;

INSERT INTO journal_entry (
    content_type,
    source_content_type,
    source_id,
    journal_chapter_id,
    title,
    content,
    sort_order,
    file_group_id,
    history_triggered_by,
    history_triggered_at,
    nhtmr_yn,
    halluc_yn,
    else_dream_yn,
    else_dreamer_nm,
    created_by,
    created_at,
    updated_by,
    updated_at,
    deleted_at
)
SELECT
    'JOURNAL_NOTE',
    'JOURNAL_NOTE',
    n.id,
    n.journal_chapter_id,
    n.title,
    n.content,
    n.sort_order,
    n.file_group_id,
    n.history_triggered_by,
    n.history_triggered_at,
    'N',
    'N',
    'N',
    NULL,
    n.created_by,
    n.created_at,
    n.updated_by,
    n.updated_at,
    n.deleted_at
FROM journal_note n;

INSERT INTO journal_entry (
    content_type,
    source_content_type,
    source_id,
    journal_chapter_id,
    title,
    content,
    sort_order,
    file_group_id,
    history_triggered_by,
    history_triggered_at,
    nhtmr_yn,
    halluc_yn,
    else_dream_yn,
    else_dreamer_nm,
    created_by,
    created_at,
    updated_by,
    updated_at,
    deleted_at
)
SELECT
    'JOURNAL_DREAM',
    'JOURNAL_DREAM',
    dr.id,
    dr.journal_chapter_id,
    dr.title,
    dr.content,
    dr.sort_order,
    dr.file_group_id,
    dr.history_triggered_by,
    dr.history_triggered_at,
    COALESCE(dr.nhtmr_yn, 'N'),
    COALESCE(dr.halluc_yn, 'N'),
    COALESCE(dr.else_dream_yn, 'N'),
    dr.else_dreamer_nm,
    dr.created_by,
    dr.created_at,
    dr.updated_by,
    dr.updated_at,
    dr.deleted_at
FROM journal_dream dr;

-- ---------------------------------------------------------------------------
-- 3. Build old -> new id map
-- ---------------------------------------------------------------------------

INSERT INTO journal_entry_id_map (old_content_type, old_id, new_entry_id)
SELECT je.source_content_type, je.source_id, je.id
FROM journal_entry je
WHERE je.source_content_type = 'JOURNAL_DIARY';

INSERT INTO journal_entry_id_map (old_content_type, old_id, new_entry_id)
SELECT je.source_content_type, je.source_id, je.id
FROM journal_entry je
WHERE je.source_content_type = 'JOURNAL_NOTE';

INSERT INTO journal_entry_id_map (old_content_type, old_id, new_entry_id)
SELECT je.source_content_type, je.source_id, je.id
FROM journal_entry je
WHERE je.source_content_type = 'JOURNAL_DREAM';

-- ---------------------------------------------------------------------------
-- 4. Re-key attachable references
-- ---------------------------------------------------------------------------

UPDATE comment c
INNER JOIN journal_entry_id_map m
    ON m.old_content_type = c.ref_content_type
   AND m.old_id = c.ref_id
SET
    c.ref_id = m.new_entry_id
WHERE c.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE tag_content tc
INNER JOIN journal_entry_id_map m
    ON m.old_content_type = tc.ref_content_type
   AND m.old_id = tc.ref_id
SET
    tc.ref_id = m.new_entry_id
WHERE tc.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE meta_content mc
INNER JOIN journal_entry_id_map m
    ON m.old_content_type = mc.ref_content_type
   AND m.old_id = mc.ref_id
SET
    mc.ref_id = m.new_entry_id
WHERE mc.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE state s
INNER JOIN journal_entry_id_map m
    ON m.old_content_type = s.ref_content_type
   AND m.old_id = s.ref_id
SET
    s.ref_id = m.new_entry_id
WHERE s.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE history h
INNER JOIN journal_entry_id_map m
    ON m.old_content_type = h.ref_content_type
   AND m.old_id = h.ref_id
SET
    h.ref_id = m.new_entry_id
WHERE h.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE viewer v
INNER JOIN journal_entry_id_map m
    ON m.old_content_type = v.ref_content_type
   AND m.old_id = v.ref_id
SET
    v.ref_id = m.new_entry_id
WHERE v.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE managtr mg
INNER JOIN journal_entry_id_map m
    ON m.old_content_type = mg.ref_content_type
   AND m.old_id = mg.ref_id
SET
    mg.ref_id = m.new_entry_id
WHERE mg.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE journal_interpretation ji
INNER JOIN journal_entry_id_map m
    ON m.old_content_type = ji.ref_content_type
   AND m.old_id = ji.ref_id
SET
    ji.ref_id = m.new_entry_id
WHERE ji.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE related_content rc
INNER JOIN journal_entry_id_map ml
    ON ml.old_content_type = rc.left_content_type
   AND ml.old_id = rc.left_id
SET
    rc.left_id = ml.new_entry_id
WHERE rc.left_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE related_content rc
INNER JOIN journal_entry_id_map mr
    ON mr.old_content_type = rc.right_content_type
   AND mr.old_id = rc.right_id
SET
    rc.right_id = mr.new_entry_id
WHERE rc.right_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

-- ---------------------------------------------------------------------------
-- 5. Validation queries (run manually after commit)
-- ---------------------------------------------------------------------------
--
-- SELECT content_type, COUNT(*) FROM journal_entry GROUP BY content_type;
--
-- SELECT old_content_type, COUNT(*) FROM journal_entry_id_map GROUP BY old_content_type;
--
-- SELECT COUNT(*) AS orphan_comment
-- FROM comment c
-- LEFT JOIN journal_entry je
--   ON je.id = c.ref_id AND je.content_type = c.ref_content_type
-- WHERE c.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM')
--   AND je.id IS NULL;
--
-- SELECT COUNT(*) AS orphan_tag_content
-- FROM tag_content tc
-- LEFT JOIN journal_entry je
--   ON je.id = tc.ref_id AND je.content_type = tc.ref_content_type
-- WHERE tc.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM')
--   AND je.id IS NULL;
--
-- SELECT COUNT(*) AS orphan_interpretation_ref
-- FROM journal_interpretation ji
-- LEFT JOIN journal_entry je
--   ON je.id = ji.ref_id AND je.content_type = ji.ref_content_type
-- WHERE ji.ref_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM')
--   AND je.id IS NULL;
--
-- SELECT COUNT(*) AS orphan_related_left
-- FROM related_content rc
-- LEFT JOIN journal_entry je
--   ON je.id = rc.left_id AND je.content_type = rc.left_content_type
-- WHERE rc.left_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM')
--   AND je.id IS NULL;
--
-- SELECT COUNT(*) AS orphan_related_right
-- FROM related_content rc
-- LEFT JOIN journal_entry je
--   ON je.id = rc.right_id AND je.content_type = rc.right_content_type
-- WHERE rc.right_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM')
--   AND je.id IS NULL;

COMMIT;
