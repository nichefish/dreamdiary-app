-- 공통 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 저널 할일 (jrnl_todo)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS jrnl_todo (
    -- CLSF
    post_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 할일 번호 (PK)',
    content_type VARCHAR(32) DEFAULT 'JRNL_TODO' COMMENT '컨텐츠 타입',
    --
    idx INT DEFAULT 1 COMMENT '저널 일기 인덱스',
    yy INT COMMENT '년도',
    mnth INT COMMENT '월',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    cn LONGTEXT COMMENT '내용',
    ctgr_cd VARCHAR(50) COMMENT '글 분류 코드',
    fxd_yn CHAR(1) DEFAULT 'N' COMMENT '상단고정 여부 (Y/N)',
    hit_cnt INT DEFAULT 0 COMMENT '조회수',
    imprtc_yn CHAR(1) DEFAULT 'N' COMMENT '중요 여부 (Y/N)',
    mdfable CHAR(50) DEFAULT 'REGSTR' COMMENT '수정권한',
    -- ATCH_FILE
    atch_file_no INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '저널 할일';