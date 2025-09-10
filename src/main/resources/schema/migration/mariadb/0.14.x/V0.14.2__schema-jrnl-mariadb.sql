-- 공통 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 저널 일자에 일기 정리완료 여부 (Y/N) 추가.
ALTER TABLE jrnl_day ADD COLUMN IF NOT EXISTS diary_resolved_yn CHAR(1) DEFAULT 'N' COMMENT '일기 정리완료 여부 (Y/N)';