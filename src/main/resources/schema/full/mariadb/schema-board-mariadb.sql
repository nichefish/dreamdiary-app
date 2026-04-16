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
    description VARCHAR(2000) COMMENT '설명',
    -- STATE (module)
    sort_order INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '게시판 정의';

-- ---------- --

-- 게시판 게시물 (board_post)
-- @extends: BasePostEntity
-- @implements: TagEmbed, CommentEmbed, ManagtEmbed, ViewerEmbed
CREATE TABLE IF NOT EXISTS board_post(
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '글 ID',
    content_type VARCHAR(30) COMMENT '게시판 코드 (PK)',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
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
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY (board_def) REFERENCES board_def(board_def),
    INDEX (board_def)
) COMMENT = '게시판 게시물';
