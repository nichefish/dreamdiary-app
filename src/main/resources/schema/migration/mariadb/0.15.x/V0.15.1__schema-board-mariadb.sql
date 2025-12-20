-- 공통 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 태그-컨텐츠 (tag_content)
-- @extends: BaseCrudEntity
RENAME TABLE content_tag TO tag_content;
ALTER TABLE tag_content COMMENT = '태그-컨텐츠';
ALTER TABLE tag_content CHANGE COLUMN content_tag_no tag_content_no INT NOT NULL AUTO_INCREMENT COMMENT '태그-컨텐츠 번호 (PK)';

-- 메타 (meta)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS meta (
    meta_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '메타 번호 (PK)',
    meta_nm VARCHAR(64) COMMENT '메타',
    ctgr VARCHAR(100) COMMENT '카테고리',
    label VARCHAR(100) COMMENT '라벨',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    UNIQUE (meta_nm, ctgr, label),
    INDEX (meta_nm),
    INDEX (meta_nm, ctgr, label)
) COMMENT = '메타';

-- 메타-컨텐츠 (meta_content)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS meta_content (
    meta_content_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '메타-컨텐츠 번호 (PK)',
    ref_meta_no INT COMMENT '참조 메타 번호',
    ref_post_no INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    value VARCHAR(64) COMMENT '메타 값',
    unit VARCHAR(20) COMMENT '단위',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY (ref_meta_no) REFERENCES meta(meta_no),
    INDEX (ref_content_type),
    INDEX (ref_post_no, ref_content_type),
    INDEX (ref_post_no, ref_content_type, regstr_id)
) COMMENT = '메타-컨텐츠';