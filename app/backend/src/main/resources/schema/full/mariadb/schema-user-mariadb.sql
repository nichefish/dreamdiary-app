-- 사용자 관련 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -----------------------

-- 사용자 계정 정보 (user)
-- @extends: BaseAtchEntity
CREATE TABLE IF NOT EXISTS user (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 ID',
    -- ACCOUNT_BASIC_INFO
    username VARCHAR(20) COMMENT '로그인 ID',
    password VARCHAR(64) COMMENT '비밀번호',
    nickname VARCHAR(50) COMMENT '사용자 표시이름',
    profile_image_url VARCHAR(1000) COMMENT '프로필 이미지 경로',
    content LONGTEXT COMMENT '사용자 설명 (관리자용)',
    email VARCHAR(100) COMMENT '이메일',        -- 기본 이메일:: 계정복구 등에 사용함
    phone_number VARCHAR(20) COMMENT '연락처',        -- 기본 연락처
    -- ACCOUNT_STATUS
    use_allowed_ip_yn CHAR(1) DEFAULT 'N' COMMENT '접속IP 사용 여부 (Y/N)',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (username)
) COMMENT = '사용자 계정';

-- 사용자 상태 (user_state)
-- @extends: state table
CREATE TABLE IF NOT EXISTS user_state (
    user_id INT PRIMARY KEY COMMENT '사용자 고유 ID',
    refresh_token_hash VARCHAR(64) COMMENT '리프레시 토큰 해시',
    refresh_token_issued_at DATETIME COMMENT '리프레시 토큰 발급일시',
    refresh_token_expires_at DATETIME COMMENT '리프레시 토큰 만료일시',
    locked_yn CHAR(1) DEFAULT 'N' COMMENT '잠금 여부 (Y/N)',
    last_login_at DATETIME COMMENT '마지막 로그인 일시',
    login_fail_cnt INT DEFAULT 0 COMMENT '로그인 실패 횟수',
    login_fail_window_started_at DATETIME COMMENT '로그인 실패 카운트 윈도우 시작 시각',
    lock_expires_at DATETIME COMMENT '계정 잠금 만료 시각',
    password_changed_at DATETIME COMMENT '비밀번호 변경 일시',
    needs_password_reset CHAR(1) DEFAULT 'N' COMMENT '패스워드 변경 필요 여부 (Y/N)',
    password_token VARCHAR(64) COMMENT '패스워드 리셋 토큰 해시',
    password_reset_token_issued_at DATETIME COMMENT '패스워드 리셋 토큰 발급 시각',
    -- CONSTRAINT
    FOREIGN KEY(user_id) REFERENCES user (id)
) COMMENT = '사용자 상태';

-- 사용자 가입 신청 (user_signup_request)
CREATE TABLE IF NOT EXISTS user_signup_request (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 가입 신청 ID',
    username VARCHAR(20) COMMENT '로그인 ID',
    password VARCHAR(64) COMMENT '비밀번호',
    nickname VARCHAR(50) COMMENT '사용자 표시이름',
    email VARCHAR(100) COMMENT '이메일',
    phone_number VARCHAR(20) COMMENT '연락처',
    content LONGTEXT COMMENT '신청 메모',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '신청 상태',
    approved_at DATETIME COMMENT '승인 일시',
    rejected_at DATETIME COMMENT '반려 일시',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    INDEX (username),
    INDEX (status)
) COMMENT = '사용자 가입 신청';

-- -----------------------

-- 권한 (role)
-- @extends: BaseAuditEntity
CREATE TABLE IF NOT EXISTS `role` (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '권한 ID',
    role_key VARCHAR(50) COMMENT '권한 코드',
    role_name VARCHAR(50) COMMENT '권한 이름',
    auth_level INT COMMENT '권한 레벨',
    parent_role_id INT NULL COMMENT '상위 권한 ID',
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
    UNIQUE KEY uk_role_role_key (role_key),
    FOREIGN KEY (parent_role_id) REFERENCES `role` (id)
) COMMENT = '권한';

-- 사용자 권한 (user_role)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS user_role (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '사용자 권한 ID',
    user_id INT COMMENT '사용자 고유 번호',
    role_id INT COMMENT '권한 ID',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY(user_id) REFERENCES user (id),
    FOREIGN KEY(role_id) REFERENCES `role` (id),
    INDEX (user_id),
    INDEX (role_id)
) COMMENT = '사용자 권한';

-- -----------------------

-- 사용자 계정 정보 :: 접속 IP (user_allowed_ip)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS user_allowed_ip (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 접속 IP 고유 ID',
    user_id INT COMMENT '사용자 고유 번호',
    allowed_ip VARCHAR(20) COMMENT '접속 IP',
    -- AUDIT
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY(user_id) REFERENCES user (id)
) COMMENT = '사용자 접속IP';

-- -----------------------

-- 사용자 프로필 정보 (user_profile)
-- @extends: BaseAtchEntity
CREATE TABLE IF NOT EXISTS user_profile (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 프로필 고유 ID',
    user_id INT COMMENT '사용자 고유 번호',
    addr VARCHAR(500) COMMENT '주소',
    zipcode VARCHAR(20) COMMENT '우편번호',
    brthdy DATE COMMENT '생일',
    lunar_yn CHAR(1) DEFAULT 'N' COMMENT '음력 여부 (Y/N)',
    profl_cn VARCHAR(2000) COMMENT '프로필(자기소개)',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY(user_id) REFERENCES user (id)
) COMMENT = '사용자 프로필';

-- 사용자 인사정보 (user_emplym)
-- @extends: BaseAtchEntity
CREATE TABLE IF NOT EXISTS user_emplym (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 인사정보 고유 ID',
    user_id INT COMMENT '사용자 고유 번호',
    user_nm VARCHAR(50) COMMENT '직원명',
    emplym_phone_number VARCHAR(20) COMMENT '연락처',        -- 기본 연락처
    emplym_email VARCHAR(100) COMMENT '이메일',        -- 기본 이메일:: 계정복구 등에 사용함
    cmpy_cd VARCHAR(30) COMMENT '회사 코드',
    team_cd VARCHAR(30) COMMENT '팀 코드',
    emplym_cd VARCHAR(30) COMMENT '재직구분 코드',
    ecny_dt DATE COMMENT '입사일',
    retire_yn CHAR(1) DEFAULT 'N' COMMENT '퇴사 여부 (Y/N)',
    retire_dt DATETIME COMMENT '퇴사일',
    rank_cd VARCHAR(30) COMMENT '직급코드',
    apntc_yn CHAR(1) DEFAULT 'N' COMMENT '수습 여부 (Y/N)',
    acnt_bank VARCHAR(50) COMMENT '급여 은행',
    acnt_no VARCHAR(50) COMMENT '급여 계좌번호',
    emplym_cn VARCHAR(2000) COMMENT '인사정보 비고',
    -- FILE_GROUP
    file_group_id INT COMMENT '첨부파일 번호',
    -- AUDIT
    created_by VARCHAR(20) COMMENT '등록자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(20) COMMENT '수정자 ID',
    updated_at DATETIME COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시',
    -- CONSTRAINT
    FOREIGN KEY(user_id) REFERENCES user (id)
) COMMENT = '사용자 인사정보';
