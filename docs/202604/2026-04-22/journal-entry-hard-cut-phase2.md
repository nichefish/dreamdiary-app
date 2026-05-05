# Journal Entry Hard Cut: Phase 2

## Goal

Replace the `journal_diary`, `journal_note`, and `journal_dream` tables with a single physical table: `journal_entry`.

Core rules:

- one physical post table: `journal_entry`
- attachable polymorphism stays as `ref_id + ref_content_type`
- `content_type` remains `JOURNAL_DIARY | JOURNAL_NOTE | JOURNAL_DREAM`
- do not keep a compatibility layer that mimics the old persistence model

## Final Data Shape

### Physical storage

- physical table = `journal_entry`

### Polymorphic type

- `content_type = JOURNAL_DIARY | JOURNAL_NOTE | JOURNAL_DREAM`

### Temporary subtype columns

Dream-specific toggles and properties stay on `journal_entry` for phase 2.

- `nhtmr_yn`
- `halluc_yn`
- `else_dream_yn`
- `else_dreamer_nm`

They can be moved into state/extension structures in phase 3.

## Hard-Cut Migration Order

1. Create `journal_entry` and `journal_entry_id_map`
2. Load old journal rows into `journal_entry`
3. Build old `(content_type, id)` -> new `journal_entry.id` mapping
4. Re-key attachable tables by `ref_id` only
5. Run orphan and row-count validation
6. Hard-cut the application code
7. Drop old journal tables after final validation

If the deprecated `content_type = JOURNAL_ENTRY` migration already ran, do not restart from the original migration file.
Use [migration-journal-entry-hard-cut-polymorphism-fix-mariadb.sql](/c:/Dev/Project/dreamdiary/workspace/src/main/resources/schema/full/mariadb/migration-journal-entry-hard-cut-polymorphism-fix-mariadb.sql) to repair the already-migrated database into the final polymorphic model.

## Tables Re-Keyed To journal_entry IDs

The following tables keep their original polymorphic `ref_content_type`, but their
`ref_id` is remapped to the new `journal_entry.id`.

- `comment`
- `tag_content`
- `meta_content`
- `state`
- `history`
- `viewer`
- `managtr`
- `journal_interpretation`

The following table also keeps left/right content types as-is, but left/right ids
must be remapped to `journal_entry.id`.

- `related_content`

## Code Refactoring Order

### Step 1. Keep journal polymorphism as-is

- keep `ContentType.JOURNAL_DIARY`
- keep `ContentType.JOURNAL_NOTE`
- keep `ContentType.JOURNAL_DREAM`
- adding `ContentType.JOURNAL_ENTRY` is optional and must not replace the existing journal polymorphic types

`ChapterType` stays for now. Chapter type still matters for page structure and editing UX.

### Step 2. Create the canonical model

Add:

- `JournalEntryEntity`
- `JournalEntrySmpEntity`
- `JournalEntryDto`
- `JournalEntryPostDto`
- `JournalEntryRepository`
- `JournalEntryMapper`
- `JournalEntrySpec`
- `JournalEntryService`

`JournalEntryEntity` directly maps `journal_entry`.

Required fields:

- `id`
- `contentType`
- `sourceContentType` (temporary migration metadata)
- `sourceId` (temporary migration metadata)
- `journalChapterId`
- `title`
- `content`
- `sortOrder`
- temporary dream columns
- audit fields

### Step 3. Rewrite chapter relations

Current:

- `journalDiaryList`
- `journalNoteList`
- `journalDreamList`
- `journalElseDreamList`

Target:

- `journalEntryList`

If needed, split lists only in DTO/view helper projection.

Split rules:

- `contentType == JOURNAL_DIARY`
- `contentType == JOURNAL_NOTE`
- `contentType == JOURNAL_DREAM && elseDreamYn != 'Y'`
- `contentType == JOURNAL_DREAM && elseDreamYn == 'Y'`

Classification should happen in projection code, not in persistence mapping.

### Step 4. Replace repository access points

Move the following services to `JournalEntryRepository`.

- diary service
- dream service
- note service
- interpretation target lookup
- related-content title lookup
- day query enrichment

At this stage, diary/dream/note services may still exist as temporary facades, but all reads and writes should already be backed by `journal_entry`.

### Step 5. Replace MyBatis mappers

Rewrite these mapper SQL files against `journal_entry`.

- `JournalDiaryMapper.xml`
- `JournalDreamMapper.xml`
- `JournalNoteMapper.xml`

Rules:

- post table name must be `journal_entry`
- type filtering must use `content_type`
- dream-specific split must use `else_dream_yn`

Examples:

- diary reorder query: `WHERE content_type = 'JOURNAL_DIARY'`
- note reorder query: `WHERE content_type = 'JOURNAL_NOTE'`
- dream reorder query: `WHERE content_type = 'JOURNAL_DREAM'`

### Step 6. Replace attachable content-type assumptions

Keep `contentType`-based branching for attachable semantics.

- state policy
- tag profile service
- tag profile lookup
- journal tag cache worker
- interpretation target validation

Guideline:

- physical storage axis: `journal_entry`
- attachable polymorphism axis: `contentType = JOURNAL_DIARY | JOURNAL_NOTE | JOURNAL_DREAM`

### Step 7. Collapse duplicated services

After the temporary facade stage, merge:

- `JournalDiaryService`
- `JournalDreamService`
- `JournalNoteService`

Final target:

- `JournalEntryService`

Only facade/controller code may remain separate if desired.

### Step 8. Controller strategy

Two valid choices:

- keep current endpoints: `/journal/diary`, `/journal/dream`, `/journal/note`
- add new endpoints: `/journal/entry`, `/journal/entries`

Recommended:

- keep current endpoints in phase 2
- route them internally to `JournalEntryService`
- decide later whether to merge endpoints in phase 3

This is not for compatibility safety. It is to reduce the migration surface and make validation more exact.

## Validation Checklist

- total `journal_entry` row count matches the sum of old three tables
- `content_type` row counts match old subtype row counts
- `deleted_at IS NULL` counts match
- row counts in attachable tables do not change
- attachable orphan rows = 0
- `related_content` orphan rows = 0
- `journal_interpretation.ref_id/ref_content_type` orphan rows = 0
- diary/dream/note detail reads still work
- reorder behavior still works
- tag/state/history reads still work

## Exit Criteria

Old journal tables can be removed only when:

- application code no longer reads `journal_diary`, `journal_note`, or `journal_dream`
- batch/search/tag/state/history/related reads all use `journal_entry` ids while preserving journal content types
- tests pass
- manual validation is complete

Then remove:

- migration metadata columns on `journal_entry` if no longer needed
- old entity classes
- old repositories
- old mappers
- old duplicated services
- old tables
