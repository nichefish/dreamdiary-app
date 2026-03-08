-- 공통 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -------------------

ALTER TABLE log_actvty RENAME COLUMN url TO request_uri;
ALTER TABLE log_actvty RENAME COLUMN mthd TO http_method;
ALTER TABLE log_actvty DROP COLUMN log_user_id;
ALTER TABLE log_actvty DROP COLUMN action_ty_cd;
ALTER TABLE log_actvty ADD trace_id VARCHAR(64) COMMENT 'Trace ID';
ALTER TABLE log_actvty ADD log_type VARCHAR(20) COMMENT '로그 타입';
ALTER TABLE log_actvty ADD signature VARCHAR(200) COMMENT '시그니처';
ALTER TABLE log_actvty ADD duration_ms long COMMENT '소요시간(ms)';
ALTER TABLE log_actvty ADD http_status long COMMENT 'HTTP Status';
ALTER TABLE log_actvty MODIFY rslt TINYINT(1) NOT NULL COMMENT '결과';

ALTER TABLE log_actvty ADD INDEX idx_trace_id (trace_id);
ALTER TABLE log_actvty ADD INDEX idx_log_dt (log_dt);
ALTER TABLE log_actvty ADD INDEX idx_user_dt (user_id, log_dt);

DROP TABLE log_actvty_url_nm;
