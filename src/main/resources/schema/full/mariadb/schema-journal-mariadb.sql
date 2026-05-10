-- dreamdiary 저널 기능 관련 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -----------------------

-- 저널 일자 (journal_day)
-- @extends: BaseAttachableEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS journal_day (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 일자 ID',
    content_type VARCHAR(32) DEFAULT 'JOURNAL_DAY' COMMENT '컨텐츠 타입',
    --
    journal_date DATE COMMENT '저널 일자',
    journal_date_precision VARCHAR(20) DEFAULT 'EXACT' COMMENT '저널 날짜 정밀도 (EXACT | APPROXIMATE | UNKNOWN)',
    yy INT COMMENT '년도',
    mnth INT COMMENT '월',
    week_start_date DATE COMMENT '주 시작일자 (월요일 기준)',
    weather VARCHAR(500) COMMENT '날씨',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (journal_date),
    INDEX (journal_date_precision),
    INDEX (yy),
    INDEX (yy, mnth),
    INDEX(week_start_date)
) COMMENT = '저널 일자';

-- 저널 챕터 (journal_chapter)
-- @extends: BaseAttachableEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS journal_chapter (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 챕터 ID',
    content_type VARCHAR(32) DEFAULT 'JOURNAL_CHAPTER' COMMENT '컨텐츠 타입',
    chapter_type VARCHAR(30) NOT NULL DEFAULT 'DIARY' COMMENT 'container type (DIARY | DREAM)',
    --
    journal_day_id INT COMMENT '저널 일자 번호',
    --
    title VARCHAR(200) COMMENT '제목',
    category_code VARCHAR(50) COMMENT '글 분류 코드',
    sort_order INT DEFAULT 1 COMMENT '저널 챕터 인덱스',
    collapsed_yn CHAR(1) DEFAULT 'N' COMMENT '글접기 여부 (Y/N)',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (journal_day_id)
) COMMENT = '저널 챕터';

-- 저널 일기 (journal_diary)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS journal_diary (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 일기 ID',
    content_type VARCHAR(32) DEFAULT 'JOURNAL_DIARY' COMMENT '컨텐츠 타입',
    --
    journal_chapter_id INT COMMENT '저널 챕터 번호',
    --
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    sort_order INT DEFAULT 1 COMMENT '저널 일기 인덱스',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- history
    history_triggered_by VARCHAR(20) COMMENT '최종 이력 트리거 발생자',
    history_triggered_at DATETIME COMMENT '최종 이력 트리거 발생일시',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (journal_chapter_id)
) COMMENT = '저널 일기';

-- 저널 노트 (journal_note)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS journal_note (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 노트 ID',
    content_type VARCHAR(32) DEFAULT 'JOURNAL_NOTE' COMMENT '컨텐츠 타입',
    journal_chapter_id INT COMMENT '저널 챕터 번호',
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    sort_order INT DEFAULT 1 COMMENT '저널 노트 인덱스',
    file_group_id INT COMMENT '첨부파일 번호',
    history_triggered_by VARCHAR(20) COMMENT '최종 이력 트리거 발생자',
    history_triggered_at DATETIME COMMENT '최종 이력 트리거 발생일시',
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    INDEX (journal_chapter_id)
) COMMENT = '저널 노트';

-- 저널 꿈 (journal_dream)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS journal_dream (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 꿈 ID',
    content_type VARCHAR(32) DEFAULT 'JOURNAL_DREAM' COMMENT '컨텐츠 타입',
    --
    journal_chapter_id INT COMMENT '저널 챕터 번호',
    --
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    sort_order INT DEFAULT 1 COMMENT '저널 꿈 인덱스',
    halluc_yn CHAR(1) DEFAULT  'N' COMMENT '입면환각 여부 (Y/N)',
    nhtmr_yn CHAR(1) DEFAULT  'N' COMMENT '악몽 여부 (Y/N)',
    else_dream_yn CHAR(1) DEFAULT 'N' COMMENT '타인 꿈 여부 (Y/N)',
    else_dreamer_nm VARCHAR(64) COMMENT '꿈꾼이 이름',
    resolved_yn CHAR(1) DEFAULT 'N' COMMENT '정리완료 여부 (Y/N)',
    collapsed_yn CHAR(1) DEFAULT 'N' COMMENT '글접기 여부 (Y/N)',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- history
    history_triggered_by VARCHAR(20) COMMENT '최종 이력 트리거 발생자',
    history_triggered_at DATETIME COMMENT '최종 이력 트리거 발생일시',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (journal_chapter_id)
) COMMENT = '저널 꿈';

-- 저널 해석 (journal_interpretation)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS journal_interpretation (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 해석 ID',
    content_type VARCHAR(32) DEFAULT 'JOURNAL_INTERPRETATION' COMMENT '컨텐츠 타입',
    --
    ref_id INT COMMENT '참조 엔티티 번호',
    ref_content_type VARCHAR(50) COMMENT '참조 컨텐츠 타입 (JOURNAL_DREAM | JOURNAL_DIARY | ...)',
    journal_day_id INT COMMENT '저널 일자 번호 (정렬/필터용 비정규화)',
    --
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    sort_order INT DEFAULT 1 COMMENT '저널 해석 인덱스',
    resolved_yn CHAR(1) DEFAULT 'N' COMMENT '정리완료 여부 (Y/N)',
    collapsed_yn CHAR(1) DEFAULT 'N' COMMENT '글접기 여부 (Y/N)',
    -- POST
    imprtc_yn CHAR(1) DEFAULT 'N' COMMENT '중요 여부 (Y/N)',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- history
    history_triggered_by VARCHAR(20) COMMENT '최종 이력 트리거 발생자',
    history_triggered_at DATETIME COMMENT '최종 이력 트리거 발생일시',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (ref_id),
    INDEX (ref_content_type),
    INDEX (journal_day_id)
) COMMENT = '저널 해석';

-- 저널 할일 (journal_todo)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS journal_todo (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 결산 ID',
    content_type VARCHAR(32) DEFAULT 'JOURNAL_TODO' COMMENT '컨텐츠 타입',
    --
    sort_order INT DEFAULT 1 COMMENT '저널 일기 인덱스',
    yy INT UNIQUE COMMENT '결산 년도',
    mnth INT COMMENT '월',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    category_code VARCHAR(50) COMMENT '글 분류 코드',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '저널 할일';

-- 저널 스레드 (journal_thread)
-- @extends: BasePostEntity
-- @implements: TagEmbed, CommentEmbed
CREATE TABLE IF NOT EXISTS journal_thread(
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '글 ID',
    content_type VARCHAR(30) DEFAULT 'JOURNAL_THREAD' COMMENT '게시판 코드 (PK)',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    category_code VARCHAR(50) COMMENT '글 분류 코드',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '저널 스레드';

-- 저널 연간 (journal_annual)
-- @extends: BaseAttachableEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS journal_annual (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 결산 ID',
    content_type VARCHAR(32) DEFAULT 'JOURNAL_ANNUAL' COMMENT '컨텐츠 타입',
    --
    yy INT UNIQUE COMMENT '결산 년도',
    dream_day_cnt INT DEFAULT 0 COMMENT '꿈 일수',
    dream_cnt INT DEFAULT 0 COMMENT '꿈 개수',
    dream_compt_yn CHAR(1) DEFAULT 'N' COMMENT '꿈 기록 완료 여부',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '저널 연간';

-- 저널 연간 리뷰 (journal_annual_review)
-- @extends: BasePostEntity
-- @uses: CommentEmbed
CREATE TABLE IF NOT EXISTS journal_annual_review (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 결산 리뷰 ID',
    content_type VARCHAR(32) DEFAULT 'JOURNAL_ANNUAL_REVIEW' COMMENT '컨텐츠 타입',
    --
    journal_annual_id INT COMMENT '저널 연간 번호',
    sort_order INT DEFAULT 1 COMMENT '저널 연간 리뷰 인덱스',
    -- POST
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    category_code VARCHAR(50) COMMENT '글 분류 코드',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '저널 연간 리뷰';
