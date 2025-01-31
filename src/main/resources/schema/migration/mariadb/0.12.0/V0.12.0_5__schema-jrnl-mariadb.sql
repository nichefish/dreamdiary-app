-- dreamdiary 저널 기능 관련 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -----------------------

-- 저널 일자 (jrnl_day)
-- @extends: BaseClsfEntity
-- @uses: CommentEmbed

CREATE TABLE IF NOT EXISTS jrnl_day (
    -- CLSF
    post_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 일자 번호 (PK)',
    content_type VARCHAR(32) DEFAULT 'JRNL_DAY' COMMENT '컨텐츠 타입',
    --
    jrnl_dt DATE COMMENT '저널 일자',
    dt_unknown_yn CHAR(1) DEFAULT 'N' COMMENT '날짜미상 여부 (Y/N)',
    yy INT COMMENT '년도',
    mnth INT COMMENT '월',
    aprxmt_dt DATE COMMENT '대략일자 (날짜미상시 해당일자 이후에 표기)',
    weather VARCHAR(500) COMMENT '날씨',
    -- ATCH_FILE
    atch_file_no INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (jrnl_dt),
    INDEX (aprxmt_dt),
    INDEX (yy),
    INDEX (yy, mnth)
) COMMENT = '저널 일자';

-- 저널 꿈 (jrnl_dream)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS jrnl_dream (
    -- CLSF
    post_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 꿈 번호 (PK)',
    content_type VARCHAR(32) DEFAULT 'JRNL_DREAM' COMMENT '컨텐츠 타입',
    --
    jrnl_day_no INT COMMENT '저널 일자 번호',
    idx INT DEFAULT 1 COMMENT '저널 꿈 인덱스',
    halluc_yn CHAR(1) DEFAULT  'N' COMMENT '입면환각 여부 (Y/N)',
    nhtmr_yn CHAR(1) DEFAULT  'N' COMMENT '악몽 여부 (Y/N)',
    else_dream_yn CHAR(1) DEFAULT 'N' COMMENT '타인 꿈 여부 (Y/N)',
    else_dreamer_nm VARCHAR(64) COMMENT '꿈꾼이 이름',
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
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (jrnl_day_no)
) COMMENT = '저널 꿈';

-- 저널 일기 (jrnl_diary)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS jrnl_diary (
    -- CLSF
    post_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 일기 번호 (PK)',
    content_type VARCHAR(32) DEFAULT 'JRNL_DIARY' COMMENT '컨텐츠 타입',
    --
    jrnl_day_no INT COMMENT '저널 일자 번호',
    idx INT DEFAULT 1 COMMENT '저널 일기 인덱스',
    edit_compt_yn CHAR(1) DEFAULT 'N' COMMENT '편집완료 여부 (Y/N)',
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
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (jrnl_day_no)
) COMMENT = '저널 일기';

-- 저널 주제 (jrnl_sbjct)
-- @extends: BasePostEntity
-- @implements: TagEmbed, CommentEmbed
CREATE TABLE IF NOT EXISTS jrnl_sbjct(
    -- CLSF
    post_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '글 번호 (PK)',
    content_type VARCHAR(30) COMMENT '게시판 코드 (PK)',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    cn LONGTEXT COMMENT '내용',
    ctgr_cd VARCHAR(50) COMMENT '글 분류 코드',
    imprtc_yn CHAR(1) DEFAULT 'N' COMMENT '중요 여부 (Y/N)',
    fxd_yn CHAR(1) DEFAULT 'N' COMMENT '상단고정 여부 (Y/N)',
    hit_cnt INT DEFAULT 0 COMMENT '조회수',
    mdfable CHAR(50) DEFAULT 'REGSTR' COMMENT '수정권한',
    -- ATCH_FILE
    atch_file_no INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '저널 주제';

-- 저널 결산 (jrnl_sumry)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS jrnl_sumry (
    -- CLSF
    post_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 결산 번호 (PK)',
    content_type VARCHAR(32) DEFAULT 'JRNL_SUMRY' COMMENT '컨텐츠 타입',
    --
    yy INT UNIQUE COMMENT '결산 년도',
    dream_day_cnt INT DEFAULT 0 COMMENT '꿈 일수',
    dream_cnt INT DEFAULT 0 COMMENT '꿈 개수',
    diary_day_cnt INT DEFAULT 0 COMMENT '일기 일수',
    dream_compt_yn CHAR(1) DEFAULT 'N' COMMENT '꿈 기록 완료 여부',
    -- ATCH_FILE
    atch_file_no INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '저널 결산';