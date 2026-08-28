-- tag_content 중복 정리 및 유니크 제약 추가.
-- (tag_id, ref_id, ref_content_type, created_by) 활성+삭제 통틀어 중복 여분과
-- ref_content_type 무효(NULL/빈) 행을 물리 제거한 뒤, deleted_at 을 제외한
-- 유니크 키를 추가한다(related_content·journal_thread_entry 와 동일 관례).
-- 소프트 삭제된 연결의 재등록은 INSERT 가 아니라 복원(revive)으로 처리한다.

-- 1) (tag_id, ref_id, ref_content_type, created_by) 중복 여분 물리 삭제 (각 그룹 MIN(id) 정본 유지)
DELETE t FROM tag_content t
JOIN (
    SELECT MIN(id) AS keep_id, tag_id, ref_id, ref_content_type, created_by
    FROM tag_content
    WHERE tag_id IS NOT NULL AND ref_id IS NOT NULL
      AND ref_content_type IS NOT NULL AND ref_content_type <> ''
    GROUP BY tag_id, ref_id, ref_content_type, created_by
    HAVING COUNT(*) > 1
) g
  ON t.tag_id = g.tag_id AND t.ref_id = g.ref_id
 AND t.ref_content_type = g.ref_content_type AND t.created_by = g.created_by
WHERE t.id <> g.keep_id;

-- 2) ref_content_type 무효(NULL/빈) 행 물리 삭제
DELETE FROM tag_content WHERE ref_content_type IS NULL OR ref_content_type = '';

-- 3) 유니크 제약 추가 (deleted_at 제외 — 소프트 삭제 재등록은 복원으로 처리)
ALTER TABLE tag_content
    ADD UNIQUE KEY uk_tag_content_pair (tag_id, ref_id, ref_content_type, created_by);