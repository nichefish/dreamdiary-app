-- 공통 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -----------------------

-- 시퀀스 (cmm_sequence)
-- 복합키 요소에 대한 시퀀스 :: AUTO_INCREMENT가 먹지 않는 복합키 요소에 대하여 사용
CREATE TABLE IF NOT EXISTS cmm_sequence (
    seq_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '시퀀스 ID',
    seq_nm VARCHAR(30) COMMENT '시퀀스 이름',
    seq_val INT NOT NULL DEFAULT 0 COMMENT '시퀀스 값'
) COMMENT = '복합키 시퀀스';

-- -----------------------

-- 분류 코드 (cl_cd)
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS cmm_cl_cd  (
    cl_cd VARCHAR(50) NOT NULL PRIMARY KEY COMMENT '분류 코드',
    cl_cd_nm VARCHAR(50) COMMENT '분류 코드 이름',
    cl_ctgr_cd VARCHAR(50) COMMENT '분류 코드 분류 코드',
    dc VARCHAR(1000) COMMENT '분류 코드 설명',
    -- STATE (module)
    idx INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    protected_yn CHAR(1) DEFAULT 'N' COMMENT '시스템 보호 여부',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '분류 코드';

-- 상세 코드 (dtl_cd)
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS cmm_dtl_cd (
    cl_cd VARCHAR(50) COMMENT '분류 코드',
    dtl_cd VARCHAR(50) COMMENT '상세 코드',
    dtl_cd_nm VARCHAR(40) COMMENT '상세 코드 이름',
    dc VARCHAR(1000) COMMENT '상세 코드 설명',
    -- STATE (module)
    idx INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    protected_yn CHAR(1) DEFAULT 'N' COMMENT '시스템 보호 여부',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    PRIMARY KEY (cl_cd, dtl_cd)
) COMMENT = '상세 코드';

-- -----------------------

-- 메뉴 (menu)
-- @extends: BaseAuditEntity
-- @implements: StateEmbed
CREATE TABLE IF NOT EXISTS menu (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '메뉴 ID',
    upper_menu_id VARCHAR(10) COMMENT '상위 메뉴 번호',
    menu_ty_cd VARCHAR(50) COMMENT '메뉴 구분코드',
    mngr_yn CHAR(1) DEFAULT 'N' COMMENT '관리자 메뉴 여부 (Y/N)',
    menu_nm VARCHAR(200) COMMENT '메뉴명',
    menu_label VARCHAR(200) COMMENT '메뉴 라벨 (약어표시)',
    url VARCHAR(500) COMMENT '연결 URL',
    icon VARCHAR(1000) COMMENT '아이콘',
    unread_cnt_nm VARCHAR(200) COMMENT '미열람 카운트 이름 (model)',
    menu_sub_extend_ty_cd VARCHAR(50) COMMENT '하위메뉴 확장 유형 코드',
    required_yn CHAR(1) DEFAULT 'N' COMMENT '필수 여부',
    protected_yn CHAR(1) DEFAULT 'N' COMMENT '시스템 보호 여부',
    -- STATE
    idx INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '메뉴';

-- 인증 정책 (auth_policy)
-- @extends: BaseAuditEntity
CREATE TABLE IF NOT EXISTS auth_policy (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '인증 정책 ID',
    lgn_try_lmt INT COMMENT '로그인 시도 제한 횟수',
    pw_chg_dy INT COMMENT '패스워드 변경 주기',
    lgn_lock_dy INT COMMENT '계정 잠금 주기',
    pw_for_reset VARCHAR(20) COMMENT '리셋할 패스워드',
    -- AUDIT
    created_by VARCHAR(20) comment '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '인증 정책';

-- -----------------------

-- 첨부파일 (atch_file)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS atch_file (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '첨부파일 ID',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '첨부파일';

-- 첨부파일 상세 (atch_file_dtl)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS atch_file_dtl (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '첨부파일 상세 ID',
    atch_file_id INT COMMENT '첨부파일 번호',
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
    FOREIGN KEY(atch_file_id) REFERENCES atch_file (id)
) COMMENT = '첨부파일 상세';

-- -----------------------

-- 활동 로그 (log_actvty)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS log_actvty (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '활동 로그 ID',
    log_dt DATETIME COMMENT '로그 기록 일시',
    username VARCHAR(20) COMMENT '로그 사용자 ID',
    trace_id VARCHAR(64) COMMENT 'Trace ID',
    log_type VARCHAR(20) COMMENT '로그 타입',
    actvty_ctgr_cd VARCHAR(50) COMMENT '활동 카테고리 코드',
    http_method VARCHAR(400) COMMENT 'HTTP 메소드',
    request_uri VARCHAR(400) COMMENT '요청 URI',
    param VARCHAR(500) COMMENT '요청 파라미터',

    ip_addr VARCHAR(20) COMMENT 'IP 주소',
    referer VARCHAR(1000) COMMENT '리퍼러 URL',

    cn LONGTEXT COMMENT '내용',

    http_status int COMMENT 'HTTP 상태',
    duration_ms long COMMENT '소요시간(ms)',
    rslt TINYINT NOT NULL COMMENT '결과',
    rslt_msg VARCHAR(50) COMMENT '결과 메시지',
    exception_nm VARCHAR(100) COMMENT '예외 이름',
    exception_msg VARCHAR(4000) COMMENT '예외 메시지',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX idx_trace_id (trace_id),
    INDEX idx_log_dt (log_dt),
    INDEX idx_user_dt (username, log_dt)
) COMMENT = '활동 로그';

-- 시스템 로그 (log_sys)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS log_sys (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '시스템 로그 ID',
    log_dt DATETIME COMMENT '로그 날짜 및 시간',
    username VARCHAR(20) COMMENT '로그 기록 사용자 ID',
    actvty_ctgr_cd VARCHAR(50) COMMENT '활동 카테고리 코드',
    cn LONGTEXT COMMENT '내용',
    rslt TINYINT NOT NULL COMMENT '결과',
    rslt_msg VARCHAR(500) COMMENT '결과 메시지',
    exception_nm VARCHAR(100) COMMENT '예외 이름',
    exception_msg VARCHAR(4000) COMMENT '예외 메시지',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시'
) COMMENT = '시스템 로그';
