-- 게시판 관련 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- ---------- --

-- 게시판 정의 (board_def)
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS board_def (
    board_def VARCHAR(30) PRIMARY KEY COMMENT '게시판 분류 (PK)',
    board_nm VARCHAR(120) NOT NULL COMMENT '게시판 이름',
    ctgr_cl_cd VARCHAR(30) COMMENT '분류 코드',
    dc VARCHAR(2000) COMMENT '설명',
    -- STATE (module)
    sort_ordr INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '게시판 정의';

-- ---------- --

-- 게시판 게시물 (board_post)
-- @extends: BasePostEntity
-- @implements: TagEmbed, CommentEmbed, ManagtEmbed, ViewerEmbed
CREATE TABLE IF NOT EXISTS board_post(
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
    -- BOARD_POST
    board_def VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '게시판 분류',
    -- MANAGT (module)
    managtr_id VARCHAR(20) COMMENT '작업자 ID',
    managt_dt DATETIME COMMENT '작업일시',
    -- ATCH_FILE
    atch_file_no INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY (board_def) REFERENCES board_def(board_def),
    INDEX (board_def)
) COMMENT = '게시판 게시물';

-- ---------- --

-- 댓글 (comment)
-- @extends: BasePostEntity
-- @implements: CommentEmbed
CREATE TABLE IF NOT EXISTS comment (
    -- CLSF
    post_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 번호 (PK)',
    content_type VARCHAR(32) DEFAULT 'COMMENT' COMMENT '컨텐츠 타입',
    --
    ref_post_no INT COMMENT '참초 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
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
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (ref_post_no, ref_content_type)
) COMMENT = '댓글';

-- ---------- --

-- 단락 (sectn)
-- @extends: BasePostEntity
-- @implements: SectnEmbed
CREATE TABLE IF NOT EXISTS sectn (
    -- CLSF
    post_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '단락 번호 (PK)',
    content_type VARCHAR(32) DEFAULT 'SECTN' COMMENT '컨텐츠 타입',
    --
    ref_post_no INT COMMENT '참초 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    deprc_yn CHAR(1) DEFAULT 'N' COMMENT '만료 여부 (Y/N)',
    -- STATE (module)
    sort_ordr INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
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
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (ref_post_no, ref_content_type)
) COMMENT = '단락';

-- ---------- --

-- 태그 (tag)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS tag (
    tag_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '태그 번호 (PK)',
    tag_nm VARCHAR(64) COMMENT '태그',
    ctgr VARCHAR(100) COMMENT '카테고리',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    UNIQUE (tag_nm, ctgr),
    INDEX (tag_nm),
    INDEX (tag_nm, ctgr)
) COMMENT = '태그';

-- 컨텐츠 태그 (content_tag)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS content_tag (
    content_tag_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '컨텐츠 태그 번호 (PK)',
    ref_tag_no INT COMMENT '참조 태그 번호',
    ref_post_no INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY (ref_tag_no) REFERENCES tag(tag_no),
    INDEX (ref_content_type),
    INDEX (ref_post_no, ref_content_type),
    INDEX (ref_post_no, ref_content_type, regstr_id)
) COMMENT = '컨텐츠 태그';

-- 태그 속성 (tag_property)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS tag_property (
    tag_property_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '태그 속성 번호 (PK)',
    tag_no INT COMMENT '태그 번호',
    content_type VARCHAR(30) COMMENT '컨텐츠 타입',
    --
    css_class VARCHAR(20) COMMENT 'CSS 클래스',
    css_style VARCHAR(500) COMMENT '스타일시트',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY (tag_no) REFERENCES tag(tag_no),
    INDEX (content_type),
    INDEX (tag_no, content_type)
) COMMENT = '컨텐츠 태그';

-- ---------- --

-- 조치자 (managtr)
-- @extends: BaseAuditRegEntity
CREATE TABLE IF NOT EXISTS managtr (
    managtr_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '조치자 번호 (PK)',
    ref_post_no INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (ref_post_no, ref_content_type)
) COMMENT = '작업자';

-- ---------- --

-- 열람자 (viewer)
-- @extends: BaseAuditRegEntity
CREATE TABLE IF NOT EXISTS viewer (
    viewer_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '열람자 번호 (PK)',
    ref_post_no INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    lst_visit_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '최종방문일시',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (ref_post_no, ref_content_type),
    CONSTRAINT UC_user_post UNIQUE (regstr_id, ref_post_no, ref_content_type)  -- 유니크 제약 추가
) COMMENT = '열람자';


