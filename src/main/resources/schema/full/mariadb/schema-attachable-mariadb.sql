-- 분류 기능 관련 테이블 생성 쿼리 정보를 입력한다.
-- JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK 값을 업데이트하므로,
-- 연관관계 컬럼이 NOT NULL이면 에러가 발생할 수 있다.
-- 따라서 JPA가 관리하는 연관관계 FK 컬럼은 NULL 허용을 권장한다.
-- @database : mariadb
-- @author : nichefish

-- ---------- --

-- 댓글(comment)
-- @extends: BasePostEntity
-- @implements: CommentEmbed
CREATE TABLE IF NOT EXISTS comment (
    -- ATTACHABLE
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 ID',
    content_type VARCHAR(32) DEFAULT 'COMMENT' COMMENT '컨텐츠 타입',
    --
    ref_id INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    ctgr_cd VARCHAR(50) COMMENT '글 분류 코드',
    imprtc_yn CHAR(1) DEFAULT 'N' COMMENT '중요 여부 (Y/N)',
    fxd_yn CHAR(1) DEFAULT 'N' COMMENT '상단 고정 여부 (Y/N)',
    hit_cnt INT DEFAULT 0 COMMENT '조회수',
    mdfable CHAR(50) DEFAULT 'REGSTR' COMMENT '수정 권한 범위',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    visual_semantic VARCHAR(30) NOT NULL DEFAULT 'DEFAULT' COMMENT '시각 의미',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (ref_id, ref_content_type)
) COMMENT = '댓글';

-- ---------- --

-- 단락(sectn)
-- @extends: BasePostEntity
-- @implements: SectnEmbed
CREATE TABLE IF NOT EXISTS sectn (
    -- ATTACHABLE
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '단락 ID',
    content_type VARCHAR(32) DEFAULT 'SECTN' COMMENT '컨텐츠 타입',
    --
    ref_id INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    deprc_yn CHAR(1) DEFAULT 'N' COMMENT '만료 여부 (Y/N)',
    -- STATE (module)
    sort_order INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    ctgr_cd VARCHAR(50) COMMENT '글 분류 코드',
    imprtc_yn CHAR(1) DEFAULT 'N' COMMENT '중요 여부 (Y/N)',
    fxd_yn CHAR(1) DEFAULT 'N' COMMENT '상단 고정 여부 (Y/N)',
    hit_cnt INT DEFAULT 0 COMMENT '조회수',
    mdfable CHAR(50) DEFAULT 'REGSTR' COMMENT '수정 권한 범위',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (ref_id, ref_content_type)
) COMMENT = '단락';

-- ---------- --

-- 태그(tag)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS tag (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '태그 ID',
    tag_nm VARCHAR(64) COMMENT '태그명',
    tag_category_id INT COMMENT '태그 카테고리 ID',
    tag_category_key INT AS (IFNULL(tag_category_id, 0)) PERSISTENT,
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    UNIQUE KEY uk_tag_tag_nm_category (tag_nm, tag_category_key),
    INDEX idx_tag_tag_nm (tag_nm),
    INDEX idx_tag_tag_category_id (tag_category_id)
) COMMENT = '태그';

-- 태그 카테고리(tag_category)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS tag_category (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '태그 카테고리 ID',
    ctgr_nm VARCHAR(100) COMMENT '태그 카테고리명',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    UNIQUE KEY uk_tag_category_ctgr_nm (ctgr_nm),
    INDEX idx_tag_category_ctgr_nm (ctgr_nm)
) COMMENT = '태그 카테고리';

ALTER TABLE tag
    ADD CONSTRAINT fk_tag_tag_category
    FOREIGN KEY (tag_category_id) REFERENCES tag_category(id);

-- 태그-컨텐츠(tag_content)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS tag_content (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '태그-컨텐츠 ID',
    tag_id INT COMMENT '태그 ID',
    ref_id INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY (tag_id) REFERENCES tag(id),
    INDEX (ref_content_type),
    INDEX (ref_id, ref_content_type),
    INDEX (ref_id, ref_content_type, created_by)
) COMMENT = '태그-컨텐츠';

-- 태그 프로필(tag_profile)
-- @extends: BaseAuditRegEntity
CREATE TABLE IF NOT EXISTS tag_profile (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '태그 프로필 ID',
    tag_id INT NOT NULL COMMENT '태그 ID',
    content_type VARCHAR(50) NOT NULL COMMENT '컨텐츠 타입',
    --
    content LONGTEXT COMMENT '내용',
    text_class VARCHAR(30) NULL COMMENT '시각 의미 (NULL=카테고리/기본 상속)',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY (tag_id) REFERENCES tag(id),
    UNIQUE KEY uk_tag_profile (tag_id, content_type, created_by),
    INDEX (content_type)
) COMMENT = '태그 프로필';

-- 태그 카테고리 프로필(tag_category_profile)
-- @extends: BaseAuditRegEntity
CREATE TABLE IF NOT EXISTS tag_category_profile (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '태그 카테고리 프로필 ID',
    tag_category_id INT NOT NULL COMMENT '태그 카테고리 ID',
    content_type VARCHAR(50) NOT NULL COMMENT '컨텐츠 타입',
    --
    text_class VARCHAR(30) NOT NULL DEFAULT 'DEFAULT' COMMENT '시각 의미',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY (tag_category_id) REFERENCES tag_category(id),
    UNIQUE KEY uk_tag_category_profile (tag_category_id, content_type, created_by),
    INDEX (content_type)
) COMMENT = '태그 카테고리 프로필';

-- 메타(meta)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS meta (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '메타 ID',
    meta_nm VARCHAR(64) COMMENT '메타명',
    ctgr VARCHAR(100) COMMENT '카테고리',
    label VARCHAR(100) COMMENT '라벨',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    UNIQUE (meta_nm, ctgr, label),
    INDEX (meta_nm),
    INDEX (meta_nm, ctgr, label)
) COMMENT = '메타';

-- 메타-컨텐츠(meta_content)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS meta_content (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '메타-컨텐츠 ID',
    meta_id INT COMMENT '메타 ID',
    ref_id INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    value VARCHAR(64) COMMENT '메타 값',
    unit VARCHAR(20) COMMENT '단위',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY (meta_id) REFERENCES meta(id),
    INDEX (ref_content_type),
    INDEX (ref_id, ref_content_type),
    INDEX (ref_id, ref_content_type, created_by)
) COMMENT = '메타-컨텐츠';

-- ---------- --

-- 상태(state)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS state (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '상태 ID',
    ref_id INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    --
    state_cd VARCHAR(64) COMMENT '상태 코드',
    deleted_at DATETIME COMMENT '삭제일시',
    UNIQUE KEY uk_state (ref_content_type, ref_id, state_cd)
) COMMENT = '상태';

-- ---------- --

-- 조치자(managtr)
-- @extends: BaseAuditRegEntity
CREATE TABLE IF NOT EXISTS managtr (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '조치자 ID',
    ref_id INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (ref_id, ref_content_type)
) COMMENT = '조치자';

-- ---------- --

-- 이력(history)
-- @extends: BaseAuditRegEntity
CREATE TABLE history (
    id INT AUTO_INCREMENT COMMENT '이력 ID',
    ref_id INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(255) COMMENT '참조 컨텐츠 타입',
    content LONGTEXT COMMENT '이력 내용 요약',
    history_type VARCHAR(20) NOT NULL DEFAULT 'CHANGE' COMMENT '이력 타입',
    from_history_id INT COMMENT '복구 원본 이력 번호',
    -- AUDIT
    reg_id VARCHAR(50) COMMENT '등록자 ID',
    created_at DATETIME COMMENT '등록 일시',
    deleted_at DATETIME COMMENT '삭제일시',

    PRIMARY KEY (id),
    INDEX(ref_id, ref_content_type)
);

-- ---------- --

-- 열람자(viewer)
-- @extends: BaseAuditRegEntity
CREATE TABLE IF NOT EXISTS viewer (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '열람자 ID',
    ref_id INT COMMENT '참조 글 번호',
    ref_content_type VARCHAR(30) COMMENT '참조 컨텐츠 타입',
    last_visited_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '최종 방문 일시',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (ref_id, ref_content_type),
    CONSTRAINT UC_user_post UNIQUE (created_by, ref_id, ref_content_type)
) COMMENT = '열람자';

-- ---------- --

-- 관련글(related_content)
-- @extends: BaseAuditRegEntity
CREATE TABLE IF NOT EXISTS related_content (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '관련글 ID',
    left_id INT NOT NULL COMMENT '좌측 글 번호',
    left_content_type VARCHAR(30) NOT NULL COMMENT '좌측 컨텐츠 타입',
    right_id INT NOT NULL COMMENT '우측 글 번호',
    right_content_type VARCHAR(30) NOT NULL COMMENT '우측 컨텐츠 타입',
    relation_type VARCHAR(30) NOT NULL COMMENT '관계 타입',
    reason VARCHAR(255) COMMENT '관계 사유',
    origin_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '관계 생성 출처',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    UNIQUE KEY uk_related_content_pair (left_content_type, left_id, right_content_type, right_id, created_by),
    INDEX idx_related_content_left (left_id, left_content_type, created_by),
    INDEX idx_related_content_right (right_id, right_content_type, created_by),
    INDEX idx_related_content_type (relation_type),
    INDEX idx_related_content_origin (origin_type)
) COMMENT = '관련글';
