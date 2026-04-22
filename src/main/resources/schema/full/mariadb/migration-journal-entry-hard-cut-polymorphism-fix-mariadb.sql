-- Hard cut correction: normalized JOURNAL_ENTRY model -> polymorphic journal_entry model
-- @database : mariadb
-- @author : codex
--
-- Intent
-- 1) Correct a DB that already ran the deprecated JOURNAL_ENTRY-normalized hard-cut script.
-- 2) Restore attachable polymorphism to JOURNAL_DIARY | JOURNAL_NOTE | JOURNAL_DREAM.
-- 3) Keep the new journal_entry.id values and the unified physical journal_entry table.
--
-- Assumptions
-- - journal_entry already exists and contains migrated rows.
-- - journal_entry.id is already the canonical new id.
-- - attachable tables were already re-keyed to the new ids.
-- - journal_entry currently has entry_type + source_content_type + source_id columns.
-- - attachable rows currently use ref_content_type = 'JOURNAL_ENTRY' for migrated journal targets.
--
-- Notes
-- - Run after full DB backup.
-- - DDL is not fully transactional in MariaDB. Review before execution.
-- - This script is a repair migration for an already-mutated database.

START TRANSACTION;

-- ---------------------------------------------------------------------------
-- 1. Restore journal_entry polymorphic content_type
-- ---------------------------------------------------------------------------

UPDATE journal_entry
SET content_type = CASE
    WHEN source_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM') THEN source_content_type
    WHEN entry_type = 'DIARY' THEN 'JOURNAL_DIARY'
    WHEN entry_type = 'NOTE' THEN 'JOURNAL_NOTE'
    WHEN entry_type = 'DREAM' THEN 'JOURNAL_DREAM'
    ELSE content_type
END
WHERE content_type = 'JOURNAL_ENTRY'
  AND (
      source_content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM')
      OR entry_type IN ('DIARY', 'NOTE', 'DREAM')
  );

-- ---------------------------------------------------------------------------
-- 2. Restore attachable polymorphic content types
-- ---------------------------------------------------------------------------

UPDATE comment c
INNER JOIN journal_entry je
    ON je.id = c.ref_id
SET c.ref_content_type = je.content_type
WHERE c.ref_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE tag_content tc
INNER JOIN journal_entry je
    ON je.id = tc.ref_id
SET tc.ref_content_type = je.content_type
WHERE tc.ref_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE meta_content mc
INNER JOIN journal_entry je
    ON je.id = mc.ref_id
SET mc.ref_content_type = je.content_type
WHERE mc.ref_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE state s
INNER JOIN journal_entry je
    ON je.id = s.ref_id
SET s.ref_content_type = je.content_type
WHERE s.ref_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE history h
INNER JOIN journal_entry je
    ON je.id = h.ref_id
SET h.ref_content_type = je.content_type
WHERE h.ref_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE viewer v
INNER JOIN journal_entry je
    ON je.id = v.ref_id
SET v.ref_content_type = je.content_type
WHERE v.ref_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE managtr mg
INNER JOIN journal_entry je
    ON je.id = mg.ref_id
SET mg.ref_content_type = je.content_type
WHERE mg.ref_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE journal_interpretation ji
INNER JOIN journal_entry je
    ON je.id = ji.ref_id
SET ji.ref_content_type = je.content_type
WHERE ji.ref_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE related_content rc
INNER JOIN journal_entry je
    ON je.id = rc.left_id
SET rc.left_content_type = je.content_type
WHERE rc.left_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

UPDATE related_content rc
INNER JOIN journal_entry je
    ON je.id = rc.right_id
SET rc.right_content_type = je.content_type
WHERE rc.right_content_type = 'JOURNAL_ENTRY'
  AND je.content_type IN ('JOURNAL_DIARY', 'JOURNAL_NOTE', 'JOURNAL_DREAM');

-- ---------------------------------------------------------------------------
-- 3. Restore tag profile tables from entry_type-aware shape
-- ---------------------------------------------------------------------------

UPDATE tag_profile
SET content_type = CASE entry_type
    WHEN 'DIARY' THEN 'JOURNAL_DIARY'
    WHEN 'NOTE' THEN 'JOURNAL_NOTE'
    WHEN 'DREAM' THEN 'JOURNAL_DREAM'
    ELSE content_type
END
WHERE content_type = 'JOURNAL_ENTRY'
  AND entry_type IN ('DIARY', 'NOTE', 'DREAM');

UPDATE tag_category_profile
SET content_type = CASE entry_type
    WHEN 'DIARY' THEN 'JOURNAL_DIARY'
    WHEN 'NOTE' THEN 'JOURNAL_NOTE'
    WHEN 'DREAM' THEN 'JOURNAL_DREAM'
    ELSE content_type
END
WHERE content_type = 'JOURNAL_ENTRY'
  AND entry_type IN ('DIARY', 'NOTE', 'DREAM');

ALTER TABLE tag_profile
    DROP INDEX uk_tag_profile_entry;

ALTER TABLE tag_profile
    ADD UNIQUE KEY uk_tag_profile (tag_id, content_type, created_by);

ALTER TABLE tag_profile
    DROP COLUMN entry_type;

ALTER TABLE tag_category_profile
    DROP INDEX uk_tag_category_profile_entry;

ALTER TABLE tag_category_profile
    ADD UNIQUE KEY uk_tag_category_profile (tag_category_id, content_type, created_by);

ALTER TABLE tag_category_profile
    DROP COLUMN entry_type;

-- ---------------------------------------------------------------------------
-- 4. Remove entry_type shape from journal_entry
-- ---------------------------------------------------------------------------

ALTER TABLE journal_entry
    DROP INDEX idx_journal_entry_type;

ALTER TABLE journal_entry
    DROP INDEX idx_journal_entry_type_sort;

ALTER TABLE journal_entry
    ADD INDEX idx_journal_entry_content_type (content_type);

ALTER TABLE journal_entry
    ADD INDEX idx_journal_entry_type_sort (content_type, journal_chapter_id, sort_order);

ALTER TABLE journal_entry
    DROP COLUMN entry_type;

ALTER TABLE journal_entry
    MODIFY COLUMN content_type VARCHAR(32) NOT NULL COMMENT 'JOURNAL_DIARY | JOURNAL_NOTE | JOURNAL_DREAM';

-- ---------------------------------------------------------------------------
-- 5. Validation queries (run manually after commit)
-- ---------------------------------------------------------------------------
--


COMMIT;
