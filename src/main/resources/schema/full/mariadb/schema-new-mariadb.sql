-- 기능 구조 (new) 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -----------------------

-- 공지사항 (notice)
-- @extends: BasePostEntity
-- @implements: TagEmbed, CommentEmbed, ManagtEmbed, ViewerEmbed
CREATE TABLE IF NOT EXISTS notice (
    -- CLSF
    post_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '글 ID',
    content_type VARCHAR(32) DEFAULT 'NOTICE' COMMENT '컨텐츠 타입',
    --
    popup_yn CHAR(1) DEFAULT 'N' COMMENT '팝업 여부 (Y/N)',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    cn LONGTEXT COMMENT '내용',
    ctgr_cd VARCHAR(50) COMMENT '글 분류 코드',
    imprtc_yn CHAR(1) DEFAULT 'N' COMMENT '중요 여부 (Y/N)',
    fxd_yn CHAR(1) DEFAULT 'N' COMMENT '상단고정 여부 (Y/N)',
    hit_cnt INT DEFAULT 0 COMMENT '조회수',
    mdfable CHAR(50) DEFAULT 'REGSTR' COMMENT '수정권한',
    -- MANAGT (module)
    managtr_id VARCHAR(20) COMMENT '작업자 ID',
    managt_dt DATETIME COMMENT '작업일시',
    -- ATCH_FILE
    atch_file_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '공지사항';

-- ---------- --

-- 일정 (schdul)
-- @extends: BasePostEntity
-- @implements: TagEmbed, CommentEmbed
CREATE TABLE IF NOT EXISTS schdul (
    -- CLSF
    post_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '글 ID',
    content_type VARCHAR(32) DEFAULT 'SCHDUL' COMMENT '컨텐츠 타입',
    --
    schdul_cd VARCHAR(30) COMMENT '일정 코드',
    bgn_dt DATETIME DEFAULT NULL COMMENT '시작일자',
    end_dt DATETIME DEFAULT NULL COMMENT '종료일자',
    prvt_yn CHAR(1) DEFAULT 'N' COMMENT '개인일정 여부 (Y/N)',
    src VARCHAR(50) DEFAULT '출처',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    cn LONGTEXT COMMENT '내용',
    ctgr_cd VARCHAR(50) COMMENT '글 분류 코드',
    imprtc_yn CHAR(1) DEFAULT 'N' COMMENT '중요 여부 (Y/N)',
    fxd_yn CHAR(1) DEFAULT 'N' COMMENT '상단고정 여부 (Y/N)',
    hit_cnt INT DEFAULT 0 COMMENT '조회수',
    mdfable CHAR(50) DEFAULT 'REGSTR' COMMENT '수정권한',
    -- ATCH_FILE
    atch_file_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '일정';

-- 일정 참여자 (schdul_prtcpnt)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS schdul_prtcpnt (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '일정 참여자 ID',
    schdul_id INT COMMENT '일정 ID',
    username VARCHAR(30) COMMENT '일정 참여자 ID',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    CONSTRAINT fk_schdul_prtcpnt FOREIGN KEY (schdul_id) REFERENCES schdul (post_no),
    INDEX (schdul_id)
) COMMENT = '일정 참여자';

-- 템플릿 정의 정보
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS tmplat_def (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '템플릿 정의 ID',
    tmplat_def_cd VARCHAR(50) COMMENT '템플릿 정의 코드',
    title VARCHAR(200) COMMENT '이름',
    -- STATE
    idx INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (tmplat_def_cd)
);

-- 템플릿 항목(텍스트에디터) 정보
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS tmplat_txt (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '템플릿(텍스트) ID',
    tmplat_def_cd VARCHAR(50) COMMENT '템플릿 정의 코드',
    -- ctgr_cd VARCHAR(50) COMMENT '글분류 코드',
    title VARCHAR(200) COMMENT '이름',
    cn LONGTEXT COMMENT '내용',
    default_yn CHAR(1) DEFAULT 'N' COMMENT '기본 템플릿 여부',
    -- ATCH_FILE
    atch_file_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (tmplat_def_cd)
);

-- -----------------------

-- 파일시스템 메타 (flsys_meta)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS flsys_meta (
    -- CLSF
    post_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '글 ID',
    content_type VARCHAR(32) DEFAULT 'FLSYS_META' COMMENT '컨텐츠 타입',
    --
    file_path VARCHAR(500) UNIQUE COMMENT '파일 경로',
    upper_file_path VARCHAR(500) COMMENT '상위 파일 경로',
    -- TITLE
    title VARCHAR(200) COMMENT '제목',
    cn LONGTEXT COMMENT '내용',
    ctgr_cd VARCHAR(50) COMMENT '글분류 코드',
    imprtc_yn CHAR(1) DEFAULT 'N' COMMENT '중요 여부 (Y/N)',
    fxd_yn CHAR(1) DEFAULT 'N' COMMENT '상단고정 여부 (Y/N)',
    mdfable CHAR(50) DEFAULT 'REGSTR' COMMENT '수정권한',
    -- MANAGT (module)
    managtr_id VARCHAR(20) COMMENT '작업자 ID',
    managt_dt DATETIME COMMENT '작업일시',
    -- ATCH_FILE
    atch_file_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    --
    INDEX(file_path)
);

-- -----------------------

-- 채팅 메세지
-- @extends: BasePostEntity
CREATE TABLE IF NOT EXISTS chat_msg (
    post_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '글 ID',
    content_type VARCHAR(30) DEFAULT 'CHAT' COMMENT '게시판 코드 (PK)',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    cn LONGTEXT COMMENT '내용',
    imprtc_yn CHAR(1) DEFAULT 'N' COMMENT '중요 여부 (Y/N)',
    fxd_yn CHAR(1) DEFAULT 'N' COMMENT '상단고정 여부 (Y/N)',
    -- ATCH_FILE
    atch_file_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
);
