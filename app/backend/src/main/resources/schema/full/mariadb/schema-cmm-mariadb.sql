-- 공통 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -----------------------

-- 시퀀스 (sequence)
-- 복합키 요소에 대한 시퀀스 :: AUTO_INCREMENT가 먹지 않는 복합키 요소에 대하여 사용
CREATE TABLE IF NOT EXISTS sequence (
    seq_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '시퀀스 ID',
    seq_nm VARCHAR(30) COMMENT '시퀀스 이름',
    seq_val INT NOT NULL DEFAULT 0 COMMENT '시퀀스 값'
) COMMENT = '복합키 시퀀스';

-- -----------------------

CREATE TABLE IF NOT EXISTS system_info (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'System info ID',
    created_by VARCHAR(20) COMMENT 'Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created at',
    updated_by VARCHAR(20) COMMENT 'Updated by',
    updated_at DATETIME COMMENT 'Updated at',
    deleted_at DATETIME COMMENT 'Deleted at'
) COMMENT = 'System info';

-- -----------------------

-- 분류 코드 (code_group)
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS code_group  (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '분류 코드 ID',
    group_code VARCHAR(50) NOT NULL COMMENT '분류 코드',
    group_name VARCHAR(50) COMMENT '분류 코드 이름',
    description VARCHAR(1000) COMMENT '분류 코드 설명',
    -- STATE (module)
    sort_order INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    protected_yn CHAR(1) DEFAULT 'N' COMMENT '시스템 보호 여부',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    UNIQUE KEY uk_code_group_group_code (group_code)
) COMMENT = '분류 코드';

-- 상세 코드 (code_item)
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS code_item (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '상세 코드 ID',
    group_code VARCHAR(50) COMMENT '분류 코드',
    code VARCHAR(50) COMMENT '상세 코드',
    code_name VARCHAR(40) COMMENT '상세 코드 이름',
    description VARCHAR(1000) COMMENT '상세 코드 설명',
    -- STATE (module)
    sort_order INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    protected_yn CHAR(1) DEFAULT 'N' COMMENT '시스템 보호 여부',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    UNIQUE KEY uk_code_item_group_code_code (group_code, code)
) COMMENT = '상세 코드';

-- -----------------------

-- 메뉴 (menu)
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS menu (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '메뉴 ID',
    parent_menu_id VARCHAR(10) COMMENT '부모 메뉴 번호',
    menu_type VARCHAR(50) COMMENT '메뉴 유형',
    admin_yn CHAR(1) DEFAULT 'N' COMMENT '관리자 메뉴 여부 (Y/N)',
    menu_name VARCHAR(200) COMMENT '메뉴명',
    menu_label VARCHAR(200) COMMENT '메뉴 라벨 (약어표시)',
    menu_description VARCHAR(1000) COMMENT '메뉴 설명',
    url VARCHAR(500) COMMENT '연결 URL',
    icon VARCHAR(1000) COMMENT '아이콘',
    unread_cnt_nm VARCHAR(200) COMMENT '미열람 카운트 이름 (model)',
    submenu_expand_type VARCHAR(50) COMMENT '하위메뉴 확장 유형',
    protected_yn CHAR(1) DEFAULT 'N' COMMENT '시스템 보호 여부',
    -- STATE
    sort_order INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    CONSTRAINT chk_menu_menu_type CHECK (menu_type IN ('MAIN', 'SUB')),
    CONSTRAINT chk_menu_submenu_expand_type CHECK (submenu_expand_type IN ('NO_SUB', 'LIST', 'EXTEND', 'COLLAPSE', 'BOARD'))
) COMMENT = '메뉴';

-- 인증 정책 (auth_policy)
-- @extends: BaseAuditEntity
CREATE TABLE IF NOT EXISTS auth_policy (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '인증 정책 ID',
    login_attempt_limit INT COMMENT '로그인 시도 제한 횟수',
    login_attempt_window_minutes INT COMMENT '로그인 시도 누적 시간 창(분)',
    account_lock_duration_minutes INT COMMENT '계정 잠금 지속 시간(분)',
    password_change_cycle_days INT COMMENT '패스워드 변경 주기(일)',
    inactive_lock_days INT COMMENT '미로그인 시 잠금 일수',
    password_reset_token_expiry_minutes INT COMMENT '비밀번호 재설정 토큰 만료 시간(분)',
    duplicate_login_allowed_yn CHAR(1) DEFAULT 'N' COMMENT 'Duplicate login allowed Y/N',
    -- AUDIT
    created_by VARCHAR(20) comment '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '인증 정책';

-- -----------------------

-- 첨부파일 (file_group)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS file_group (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '첨부파일 ID',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '첨부파일';

-- 첨부파일 상세 (file_record)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS file_record (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '첨부파일 상세 ID',
    file_group_id INT COMMENT '첨부파일 번호',
    file_sn INT COMMENT '파일 순번',
    orgn_file_nm VARCHAR(200) COMMENT '원본파일명',
    stre_file_nm VARCHAR(200) COMMENT '저장파일명',
    file_extn VARCHAR(10) COMMENT '파일 확장자',
    file_stre_path VARCHAR(200) COMMENT '파일 저장 경로',
    file_size INT COMMENT '파일 크기(BYTE)',
    url VARCHAR(500) COMMENT '파일 URL',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY(file_group_id) REFERENCES file_group (id)
) COMMENT = '첨부파일 상세';

-- -----------------------

-- 통합 로그 (log) — 기존 log_actvty + log_sys
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS `log` (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '로그 ID',
    log_type VARCHAR(20) NULL COMMENT '로그 유형 (PAGE/VIEW/ACTION/SYSTEM)',
    activity_code VARCHAR(400) NULL COMMENT '활동 카테고리 코드',
    message LONGTEXT NULL COMMENT '본문/결과 통합 메시지',
    result TINYINT(1) NULL COMMENT '성공 여부',
    http_status INT NULL COMMENT 'HTTP 상태',
    username VARCHAR(20) NULL COMMENT '작업자',
    ip_address VARCHAR(20) NULL COMMENT 'IP',
    http_method VARCHAR(1000) NULL COMMENT 'HTTP 메소드',
    request_uri VARCHAR(400) NULL COMMENT '요청 URI',
    request_param VARCHAR(1000) NULL COMMENT '요청 파라미터',
    referer VARCHAR(1000) NULL COMMENT '리퍼러',
    trace_id VARCHAR(72) NULL COMMENT 'Trace ID',
    signature VARCHAR(200) NULL COMMENT '시그니처',
    duration_ms BIGINT NULL COMMENT '소요시간(ms)',
    exception_name VARCHAR(255) NULL COMMENT '예외 이름',
    exception_message LONGTEXT NULL COMMENT '예외 메시지',
    created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 일시',
    deleted_at DATETIME NULL COMMENT '삭제일시',
    INDEX idx_log_created (created_at),
    INDEX idx_log_user_dt (username, created_at),
    INDEX idx_log_trace (trace_id),
    INDEX idx_log_type (log_type)
) COMMENT = '통합 로그';

-- -----------------------

-- 릴리즈 히스토리 (release_info)
-- @extends: BaseAuditRegEntity
CREATE TABLE IF NOT EXISTS release_info (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '릴리즈 히스토리 ID',
    event_type VARCHAR(20) NOT NULL COMMENT '이벤트 타입 (SERVER_START/DEPLOY)',
    app_version VARCHAR(50) NOT NULL COMMENT '애플리케이션 버전',
    commit_hash VARCHAR(100) NOT NULL COMMENT '커밋 해시',
    release_key VARCHAR(160) NOT NULL COMMENT '릴리즈 식별 키 (version+commit)',
    started_at DATETIME NULL COMMENT '서버 시작 시각',
    deployed_at DATETIME NULL COMMENT '배포 판정 시각',
    profile VARCHAR(20) NULL COMMENT '실행 프로필',
    host_name VARCHAR(255) NULL COMMENT '호스트 이름',
    instance_id VARCHAR(255) NULL COMMENT '인스턴스 식별자',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- INDEX
    INDEX idx_release_info_started_at (started_at),
    INDEX idx_release_info_event_type (event_type),
    INDEX idx_release_info_release_key (release_key)
) COMMENT = '서버 시작 및 배포 히스토리';
