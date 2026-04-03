-- 저널 일자 주 시작일 저장 컬럼 추가
-- @database : mariadb
-- @author : nichefish

ALTER TABLE jrnl_day ADD COLUMN week_start_dt DATE COMMENT '주 시작일자 (월요일 기준)' AFTER mnth;

CREATE INDEX idx_jrnl_day_week_start_dt ON jrnl_day (week_start_dt);

UPDATE jrnl_day
SET week_start_dt = DATE_SUB(COALESCE(jrnl_dt, aprxmt_dt), INTERVAL WEEKDAY(COALESCE(jrnl_dt, aprxmt_dt)) DAY)
WHERE COALESCE(jrnl_dt, aprxmt_dt) IS NOT NULL;
