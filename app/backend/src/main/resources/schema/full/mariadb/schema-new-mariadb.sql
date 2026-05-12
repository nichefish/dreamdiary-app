-- 기능 구조 (new) 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -----------------------

-- 일정 (schedule)
-- @extends: BasePostEntity
-- @implements: TagEmbed, CommentEmbed
CREATE TABLE IF NOT EXISTS schedule (
    -- ATTACHABLE
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '글 ID',
    content_type VARCHAR(50) DEFAULT 'SCHEDULE' COMMENT '컨텐츠 타입',
    --
    schedule_cd VARCHAR(30) COMMENT '일정 코드',
    bgn_dt DATETIME DEFAULT NULL COMMENT '시작일자',
    end_dt DATETIME DEFAULT NULL COMMENT '종료일자',
    private_yn CHAR(1) DEFAULT 'N' COMMENT '개인일정 여부 (Y/N)',
    src VARCHAR(50) DEFAULT '출처',
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
) COMMENT = '일정';

-- 일정 참여자 (schedule_participant)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS schedule_participant (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '일정 참여자 ID',
    schedule_id INT COMMENT '일정 ID',
    username VARCHAR(20) COMMENT '일정 참여자 ID (user.username)',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    CONSTRAINT fk_schedule_participant_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    INDEX (schedule_id)
) COMMENT = '일정 참여자';

-- 템플릿 정의 정보
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS tmplat_def (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '템플릿 정의 ID',
    tmplat_def_cd VARCHAR(50) COMMENT '템플릿 정의 코드',
    title VARCHAR(200) COMMENT '이름',
    -- STATE
    sort_order INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
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
    content LONGTEXT COMMENT '내용',
    default_yn CHAR(1) DEFAULT 'N' COMMENT '기본 템플릿 여부',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (tmplat_def_cd)
);

-- -----------------------

-- AI 채팅 세션
-- 사용자가 나눈 대화의 방 단위. 메시지는 chat_message.session_id 로 연결된다.
CREATE TABLE IF NOT EXISTS chat_session (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '채팅 세션 ID',
    title VARCHAR(200) COMMENT '세션 제목. 사용자가 수정하거나 첫 메시지 기준으로 생성된다.',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '세션 상태. ACTIVE, ARCHIVED 등으로 확장 가능',
    model VARCHAR(100) COMMENT '세션에서 사용할 AI 모델명. 미지정 시 시스템 기본 모델 사용',
    system_prompt LONGTEXT COMMENT '세션별 시스템 프롬프트. 비어 있으면 기본 시스템 프롬프트 사용',
    last_message_at DATETIME COMMENT '마지막 메시지 작성 일시. 세션 목록 정렬 기준',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정 일시',
    deleted_at DATETIME COMMENT '삭제 일시. NULL이면 활성 데이터',
    INDEX idx_chat_session_created_by (created_by),
    INDEX idx_chat_session_last_message_at (last_message_at)
);

-- AI 채팅 설정
-- 사용자별/관리자 기본값 등 범위(scope)에 따라 채팅 동작 값을 저장한다.
CREATE TABLE IF NOT EXISTS chat_setting (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '채팅 설정 ID',
    scope VARCHAR(20) DEFAULT 'USER' COMMENT '설정 범위. USER, ADMIN_DEFAULT 등',
    scope_key VARCHAR(100) COMMENT '설정 범위 키. USER 범위에서는 사용자 ID',
    recent_message_limit INT DEFAULT 20 COMMENT 'AI 응답 생성 시 함께 전달할 최근 대화 메시지 수',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정 일시',
    deleted_at DATETIME COMMENT '삭제 일시. NULL이면 활성 데이터',
    INDEX idx_chat_setting_scope (scope, scope_key),
    INDEX idx_chat_setting_created_by (created_by)
);

-- AI 채팅 메시지
-- 사용자와 AI가 세션 안에서 주고받은 개별 메시지. role 로 발화 주체를 구분한다.
CREATE TABLE IF NOT EXISTS chat_message (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '채팅 메시지 ID',
    content_type VARCHAR(50) DEFAULT 'CHAT_MESSAGE' COMMENT '컨텐츠 타입. 첨부/이력 등 공통 기능 연결용',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '메시지 역할. USER, ASSISTANT, SYSTEM 등',
    session_id INT COMMENT '소속 채팅 세션 ID',
    seq INT COMMENT '세션 안에서의 메시지 순번',
    -- POST
    title VARCHAR(200) COMMENT '메시지 제목 또는 표시명',
    content LONGTEXT COMMENT '메시지 본문',
    category_code VARCHAR(50) COMMENT '메시지 분류 코드',
    metadata_json LONGTEXT COMMENT '메시지 부가 메타데이터 JSON',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부 파일 그룹 ID',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정 일시',
    deleted_at DATETIME COMMENT '삭제 일시. NULL이면 활성 데이터',
    INDEX idx_chat_message_session_seq (session_id, seq)
);

-- 팝업 (PopupEntity)
-- @extends: BaseAuditEntity
-- @implements: FileEmbed
CREATE TABLE IF NOT EXISTS popup (
    popup_cd VARCHAR(50) NOT NULL PRIMARY KEY COMMENT '팝업 코드',
    popup_nm VARCHAR(200) NULL COMMENT '팝업 이름',
    width INT NULL COMMENT '가로',
    height INT NULL COMMENT '세로',
    popup_start_dt DATETIME NULL COMMENT '게시시작일시',
    popup_end_dt DATETIME NULL COMMENT '게시종료일시',
    file_group_id INT NULL COMMENT '첨부파일 번호',
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '팝업';
