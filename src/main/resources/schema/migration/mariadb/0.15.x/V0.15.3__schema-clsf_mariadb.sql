-- 공통 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 상태 (state)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS state (
    state_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '상태 번호 (PK)',
    ref_post_no INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    --
    state_cd VARCHAR(64) COMMENT '상태 코드',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    UNIQUE KEY uk_state (ref_content_type, ref_post_no, state_cd)
) COMMENT = '상태';

-- -------------------

ALTER TABLE jrnl_entry DROP COLUMN cn;
ALTER TABLE jrnl_entry DROP COLUMN ctgr_cd;
ALTER TABLE jrnl_entry DROP COLUMN fxd_yn;
ALTER TABLE jrnl_entry DROP COLUMN hit_cnt;
ALTER TABLE jrnl_entry DROP COLUMN mdfable;
ALTER TABLE jrnl_entry DROP COLUMN imprtc_yn;

ALTER TABLE jrnl_diary DROP COLUMN ctgr_cd;
ALTER TABLE jrnl_diary DROP COLUMN fxd_yn;
ALTER TABLE jrnl_diary DROP COLUMN hit_cnt;
ALTER TABLE jrnl_diary DROP COLUMN mdfable;

ALTER TABLE jrnl_dream DROP COLUMN ctgr_cd;
ALTER TABLE jrnl_dream DROP COLUMN fxd_yn;
ALTER TABLE jrnl_dream DROP COLUMN hit_cnt;
ALTER TABLE jrnl_dream DROP COLUMN mdfable;

ALTER TABLE jrnl_intrpt DROP COLUMN ctgr_cd;
ALTER TABLE jrnl_intrpt DROP COLUMN fxd_yn;
ALTER TABLE jrnl_intrpt DROP COLUMN hit_cnt;
ALTER TABLE jrnl_intrpt DROP COLUMN mdfable;

START TRANSACTION;

-- jrnl_diary의 기존 '참조' 상태 마이그레이션
INSERT IGNORE INTO state ( ref_post_no, ref_content_type, state_cd )
SELECT d.post_no AS ref_post_no, 'JRNL_DIARY' AS ref_content_type, 'REFRNC' AS state_cd
FROM jrnl_diary d
WHERE d.refrnc_yn = 'Y';

-- jrnl_dream의 기존 '참조' 상태 마이그레이션
INSERT IGNORE INTO state ( ref_post_no, ref_content_type, state_cd )
SELECT d.post_no AS ref_post_no, 'JRNL_DREAM' AS ref_content_type, 'REFRNC' AS state_cd
FROM jrnl_dream d
WHERE d.refrnc_yn = 'Y';

-- 저널 일기 (jrnl_diary)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
ALTER TABLE jrnl_diary DROP COLUMN refrnc_yn;


-- 저널 꿈 (jrnl_dream)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
ALTER TABLE jrnl_dream DROP COLUMN refrnc_yn;

COMMIT;

-- -------------------

START TRANSACTION;

-- jrnl_diary의 기존 '중요' 상태 마이그레이션
INSERT IGNORE INTO state ( ref_post_no, ref_content_type, state_cd )
SELECT d.post_no AS ref_post_no, 'JRNL_DIARY' AS ref_content_type, 'IMPRTC' AS state_cd
FROM jrnl_diary d
WHERE d.imprtc_yn = 'Y';

-- jrnl_dream의 기존 '참조' 상태 마이그레이션
INSERT IGNORE INTO state ( ref_post_no, ref_content_type, state_cd )
SELECT d.post_no AS ref_post_no, 'JRNL_DREAM' AS ref_content_type, 'IMPRTC' AS state_cd
FROM jrnl_dream d
WHERE d.imprtc_yn = 'Y';

-- 저널 일기 (jrnl_diary)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
ALTER TABLE jrnl_diary DROP COLUMN imprtc_yn;

-- 저널 꿈 (jrnl_dream)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
ALTER TABLE jrnl_dream DROP COLUMN imprtc_yn;

COMMIT;