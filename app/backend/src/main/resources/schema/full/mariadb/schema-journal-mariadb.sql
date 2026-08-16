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
    diary_resolved_yn CHAR(1) DEFAULT 'N' COMMENT '일기 축 완결(Y/N). 일기·노트 쓰기 잠금',
    dream_resolved_yn CHAR(1) DEFAULT 'N' COMMENT '꿈 축 완결(Y/N). 꿈 쓰기 잠금',
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
    summary_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '시스템 요약 챕터 여부 (Y/N)',
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

-- 저널 엔트리 (journal_entry)
-- Primary Content(일기·꿈·노트). 다형은 content_type.
-- @extends: BaseAttachableEntity
-- @uses: FileEmbed, CommentEmbed, TagEmbed, StateEmbed, HistoryEmbed, PrefixEmbed
CREATE TABLE IF NOT EXISTS journal_entry (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '저널 엔트리 ID',
    content_type VARCHAR(32) NOT NULL COMMENT 'JOURNAL_DIARY | JOURNAL_DREAM (NOTE 영속도 JOURNAL_DIARY)',
    --
    journal_chapter_id INT COMMENT '저널 챕터 번호',
    --
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT '내용',
    sort_order INT DEFAULT 1 COMMENT '챕터 내 정렬',
    dreamer_name VARCHAR(64) COMMENT '지정 꿈꾼 이름. 값이 있으면 타인의 꿈',
    -- target 컬럼(nullable). Commentary Reflection 본 테이블은 journal_reflection.
    ref_id INT COMMENT 'target 엔티티 번호',
    ref_content_type VARCHAR(50) COMMENT 'target 컨텐츠 타입',
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
    CONSTRAINT ck_journal_entry_dreamer_name CHECK (content_type = 'JOURNAL_DREAM' OR dreamer_name IS NULL),
    INDEX (journal_chapter_id),
    INDEX (content_type),
    INDEX (ref_id, ref_content_type)
) COMMENT = '저널 엔트리 (일기·꿈·노트)';

-- 저널 Reflection (journal_reflection)
-- Commentary Aggregate Root. About-A(ref_id/ref_content_type) 필수.
-- @extends: BaseAttachableEntity
-- @uses: FileEmbed, CommentEmbed, StateEmbed, HistoryEmbed
CREATE TABLE IF NOT EXISTS journal_reflection (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Reflection ID',
    content_type VARCHAR(50) DEFAULT 'JOURNAL_REFLECTION' COMMENT '항상 JOURNAL_REFLECTION',
    --
    title VARCHAR(200) COMMENT '제목',
    content LONGTEXT COMMENT 'Reflection 본문',
    -- About-A
    ref_id INT NOT NULL COMMENT '대상(About-A) 엔티티 번호',
    ref_content_type VARCHAR(50) NOT NULL COMMENT '대상 컨텐츠 타입',
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
    INDEX idx_journal_reflection_target (ref_id, ref_content_type),
    INDEX idx_journal_reflection_created_by (created_by)
) COMMENT = '저널 Reflection (Commentary)';

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
    -- 말머리 선택은 journal_thread 컬럼이 아니라 prefix_content(ref_id, ref_content_type) 연결로 보유한다.
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '저널 스레드';

-- 저널 스레드-엔트리 소속 (journal_thread_entry)
-- @extends: BaseAuditRegEntity
-- 스레드를 컨테이너로, 엔트리를 그 멤버로 잇는 N:M 조인 테이블.
-- 한 엔트리가 여러 스레드에 속할 수 있다 (related_content 의 FLOW 간선 모델이 표현하지 못하던 지점).
-- created_by 를 UNIQUE 키에 포함해 본인 소유 범위 안에서만 묶인다 (tag_content·related_content 와 동일한 관례).
-- UNIQUE 키가 deleted_at 을 포함하지 않으므로, 해제(소프트 삭제)한 소속의 재등록은
-- INSERT 가 아니라 기존 행 복원으로 처리한다. (JournalThreadEntryRepository.findAnyByPair / reviveById)
CREATE TABLE IF NOT EXISTS journal_thread_entry (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '스레드-엔트리 소속 ID',
    thread_id INT NOT NULL COMMENT '저널 스레드 ID (journal_thread.id)',
    entry_id INT NOT NULL COMMENT '저널 엔트리 ID (journal_entry.id)',
    sort_order INT COMMENT '스레드 내 표시 순서. NULL 이면 엔트리 일자순으로 정렬한다',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    UNIQUE KEY uk_journal_thread_entry (thread_id, entry_id, created_by),
    INDEX idx_journal_thread_entry_thread (thread_id, created_by),
    INDEX idx_journal_thread_entry_entry (entry_id, created_by)
) COMMENT = '저널 스레드-엔트리 소속';

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

-- 저널 엔트리 임베딩 작업 테이블
-- journal_entry 기준으로 임베딩 대상 텍스트, 검색용 메타데이터, 생성된 벡터를 보관한다.
-- @extends: BaseAuditEntity
CREATE TABLE IF NOT EXISTS journal_entry_embedding (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '임베딩 작업 ID',
    journal_entry_id INT NOT NULL COMMENT '원본 journal_entry ID',
    content_type VARCHAR(50) NOT NULL COMMENT '원본 컨텐츠 타입. JOURNAL_DIARY, JOURNAL_DREAM, JOURNAL_NOTE 등',
    content_kind VARCHAR(20) NOT NULL COMMENT '검색 가중치 분류. DIARY, DREAM, NOTE, UNKNOWN',
    journal_date DATE COMMENT '저널 기준 일자. 검색에서 의미상 시점으로 사용',
    journal_date_precision VARCHAR(20) COMMENT '저널 일자 정밀도. DAY, MONTH, YEAR, UNKNOWN 등',
    retrieval_weight DECIMAL(5,2) DEFAULT 1.00 COMMENT '검색 결과 랭킹에 곱할 타입별 가중치',
    embedding_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '임베딩 처리 상태. PENDING, PROCESSING, EMBEDDED, FAILED, SKIPPED',
    embedding_model VARCHAR(100) COMMENT '벡터를 생성한 임베딩 모델명',
    embedding_text LONGTEXT COMMENT '임베딩 모델에 실제로 전달하는 정규화된 텍스트',
    embedding_payload_json LONGTEXT COMMENT '검색/스코어링/디버깅에 사용하는 구조화 메타데이터 JSON',
    embedding_vector_json LONGTEXT COMMENT '임베딩 모델이 생성한 벡터 JSON 배열',
    content_hash VARCHAR(64) COMMENT 'embedding_text 기준 SHA-256 해시. 변경 감지 및 재임베딩 판단용',
    embedded_at DATETIME COMMENT '벡터 생성 완료 일시',
    error_message LONGTEXT COMMENT '임베딩 실패 또는 스킵 사유',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '원본 작성자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '의미상 생성 일시. journal_date 를 우선 반영',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정 일시',
    deleted_at DATETIME COMMENT '삭제 일시. NULL이면 활성 데이터',
    -- CONSTRAINT
    UNIQUE KEY uk_journal_entry_embedding_entry (journal_entry_id),
    INDEX idx_journal_entry_embedding_status (embedding_status),
    INDEX idx_journal_entry_embedding_kind (content_kind),
    INDEX idx_journal_entry_embedding_journal_date (journal_date),
    INDEX idx_journal_entry_embedding_created_at (created_at),
    INDEX idx_journal_entry_embedding_created_by (created_by),
    INDEX idx_journal_entry_embedding_content_hash (content_hash),
    INDEX idx_journal_entry_embedding_deleted_at (deleted_at)
) COMMENT = '저널 엔트리 임베딩 작업 테이블';

CREATE TABLE IF NOT EXISTS journal_entry_embedding_sync_job (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Embedding sync job ID',
    job_key VARCHAR(100) NOT NULL COMMENT 'Job key',
    status VARCHAR(20) NOT NULL DEFAULT 'IDLE' COMMENT 'IDLE, RUNNING, COMPLETED, FAILED',
    phase VARCHAR(30) NOT NULL DEFAULT 'IDLE' COMMENT 'Current phase',
    processed_count BIGINT DEFAULT 0 COMMENT 'Processed entry count',
    total_count BIGINT DEFAULT 0 COMMENT 'Total entry count',
    started_at DATETIME COMMENT 'Job started at',
    finished_at DATETIME COMMENT 'Job finished at',
    heartbeat_at DATETIME COMMENT 'Job heartbeat at',
    locked_by VARCHAR(120) COMMENT 'Worker node',
    result_json LONGTEXT COMMENT 'Job result JSON',
    error_message LONGTEXT COMMENT 'Job error message',
    created_by VARCHAR(20) COMMENT 'Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created at',
    updated_by VARCHAR(20) COMMENT 'Updated by',
    updated_at DATETIME COMMENT 'Updated at',
    deleted_at DATETIME COMMENT 'Deleted at',
    UNIQUE KEY uk_journal_entry_embedding_sync_job_key (job_key),
    INDEX idx_journal_entry_embedding_sync_job_status (status),
    INDEX idx_journal_entry_embedding_sync_job_heartbeat_at (heartbeat_at),
    INDEX idx_journal_entry_embedding_sync_job_deleted_at (deleted_at)
) COMMENT = 'Journal entry embedding sync job';

CREATE TABLE IF NOT EXISTS journal_entity (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Journal entity ID',
    entity_type VARCHAR(30) NOT NULL COMMENT 'Entity type. PERSON first, then EVENT/PLACE/ORG/SYMBOL later',
    canonical_label VARCHAR(200) NOT NULL COMMENT 'Canonical display label',
    normalized_label VARCHAR(200) NOT NULL COMMENT 'Normalized label for dedupe and lookup',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, MERGED, IGNORED',
    created_by VARCHAR(20) COMMENT 'Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created at',
    updated_by VARCHAR(20) COMMENT 'Updated by',
    updated_at DATETIME COMMENT 'Updated at',
    deleted_at DATETIME COMMENT 'Deleted at',
    UNIQUE KEY uk_journal_entity_type_label (entity_type, normalized_label),
    INDEX idx_journal_entity_status (status),
    INDEX idx_journal_entity_created_by (created_by),
    INDEX idx_journal_entity_deleted_at (deleted_at)
) COMMENT = 'Journal entity catalog';

CREATE TABLE IF NOT EXISTS journal_entry_entity_ref (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Journal entry entity reference ID',
    journal_entry_id INT NOT NULL COMMENT 'Referenced journal_entry ID',
    journal_entity_id INT NOT NULL COMMENT 'Referenced journal_entity ID',
    surface_text VARCHAR(200) NOT NULL COMMENT 'Original surface text in the entry',
    mention_type VARCHAR(30) NOT NULL DEFAULT 'DIRECT' COMMENT 'DIRECT, HONORIFIC, ALIAS, INFERRED',
    evidence_snippet TEXT COMMENT 'Evidence snippet for this mention',
    confidence DECIMAL(5,4) DEFAULT 1.0000 COMMENT 'Extraction confidence',
    start_offset INT COMMENT 'Optional start offset in source text',
    end_offset INT COMMENT 'Optional end offset in source text',
    sort_order INT DEFAULT 1 COMMENT 'Mention order in the source entry',
    created_by VARCHAR(20) COMMENT 'Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created at',
    updated_by VARCHAR(20) COMMENT 'Updated by',
    updated_at DATETIME COMMENT 'Updated at',
    deleted_at DATETIME COMMENT 'Deleted at',
    INDEX idx_journal_entry_entity_ref_entry_id (journal_entry_id),
    INDEX idx_journal_entry_entity_ref_entity_id (journal_entity_id),
    INDEX idx_journal_entry_entity_ref_mention_type (mention_type),
    INDEX idx_journal_entry_entity_ref_created_by (created_by),
    INDEX idx_journal_entry_entity_ref_deleted_at (deleted_at)
) COMMENT = 'Journal entry to entity reference';

CREATE TABLE IF NOT EXISTS journal_entry_entity_role (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Journal entry entity role ID',
    journal_entry_entity_ref_id INT NOT NULL COMMENT 'Referenced journal_entry_entity_ref ID',
    role_type VARCHAR(40) NOT NULL COMMENT 'COLLABORATION, TENSION, EVALUATION, CARE, CONFLICT, DESIRE, SYMBOLIC_FIGURE, UNKNOWN',
    evidence_snippet TEXT COMMENT 'Evidence snippet for this role judgment',
    confidence DECIMAL(5,4) DEFAULT 1.0000 COMMENT 'Role extraction confidence',
    created_by VARCHAR(20) COMMENT 'Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created at',
    updated_by VARCHAR(20) COMMENT 'Updated by',
    updated_at DATETIME COMMENT 'Updated at',
    deleted_at DATETIME COMMENT 'Deleted at',
    INDEX idx_journal_entry_entity_role_ref_id (journal_entry_entity_ref_id),
    INDEX idx_journal_entry_entity_role_role_type (role_type),
    INDEX idx_journal_entry_entity_role_created_by (created_by),
    INDEX idx_journal_entry_entity_role_deleted_at (deleted_at)
) COMMENT = 'Journal entry entity role evidence';

CREATE TABLE IF NOT EXISTS journal_entry_entity_job (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Entity sync queue row ID',
    journal_entry_id INT NOT NULL COMMENT 'Source journal_entry ID',
    job_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Sync status. PENDING, PROCESSING, SYNCED, FAILED, SKIPPED',
    content_hash VARCHAR(64) COMMENT 'Content hash used to skip unchanged rows',
    locked_by VARCHAR(200) COMMENT 'Worker node name when the row is currently processing',
    processed_at DATETIME COMMENT 'Time when the queue row was last fully processed',
    error_message LONGTEXT COMMENT 'Last worker error, if any',
    created_by VARCHAR(20) COMMENT 'Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created at',
    updated_by VARCHAR(20) COMMENT 'Updated by',
    updated_at DATETIME COMMENT 'Updated at',
    deleted_at DATETIME COMMENT 'Deleted at',
    UNIQUE KEY uk_journal_entry_entity_job_entry_id (journal_entry_id),
    INDEX idx_journal_entry_entity_job_status (job_status),
    INDEX idx_journal_entry_entity_job_updated_at (updated_at),
    INDEX idx_journal_entry_entity_job_deleted_at (deleted_at)
) COMMENT = 'Queue rows for asynchronous journal entity ref and role sync';


-- -----------------------
-- journal_setting
-- 저널 도메인 설정. ADMIN/GLOBAL 전역 정책과 USER/username 사용자 정책을 관리한다.
-- -----------------------
CREATE TABLE IF NOT EXISTS journal_setting (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '저널 설정 ID',
    scope VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '설정 범위 (ADMIN/USER)',
    scope_key VARCHAR(100) NOT NULL DEFAULT 'GLOBAL' COMMENT '범위 키 (ADMIN=GLOBAL, USER=username)',
    embedding_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'AI 임베딩 활성화 여부 (1=ON, 0=OFF)',
    default_entry_view VARCHAR(20) COMMENT '사용자별 저널 기본 진입 화면 (DAILY/WEEKLY/MONTHLY)',
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    UNIQUE KEY uk_journal_setting_scope (scope, scope_key)
) COMMENT = '저널 도메인 설정';

INSERT INTO journal_setting (scope, scope_key, embedding_enabled, created_by)
VALUES ('ADMIN', 'GLOBAL', 1, 'system');
