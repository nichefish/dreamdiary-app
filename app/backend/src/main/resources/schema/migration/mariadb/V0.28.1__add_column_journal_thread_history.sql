-- journal_thread 본문 이력 스냅샷 컬럼.
-- JournalThreadEntity HistoryEmbed가 조회하는 owner 컬럼이며, 값이 있을 때 스레드 이력 액션이 활성화된다.
-- 기존 행은 NULL로 둔다. 이후 본문이 실제로 바뀔 때 history_triggered_at이 채워진다.

ALTER TABLE journal_thread
    ADD COLUMN history_triggered_by VARCHAR(20) COMMENT '최종 이력 트리거 발생자' AFTER file_group_id,
    ADD COLUMN history_triggered_at DATETIME COMMENT '최종 이력 트리거 발생일시' AFTER history_triggered_by;