-- 사용자 관련 구조 테이블 생성 쿼리 정보를 입력한다.
-- "JPA CASCADE INSERT에서는 먼저 INSERT 후 나중에 FK값을 업데이트하게 되므로 FK가 NOT_NULL이면 에러가 발생한다."
-- (=> JPA에서 다른 테이블과 연관성을 갖는 컬럼은 반드시 NULL을 허용해야 한다!) (NOT NULL이면 안된다)
-- @database : mariadb
-- @author : nichefish

-- -----------------------

-- 사용자 계정 정보 (user)
-- @extends: BaseAtchEntity
CREATE TABLE IF NOT EXISTS user (
    user_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 번호 (PK)',
    -- ACCOUNT_BASIC_INFO
    user_id VARCHAR(20) COMMENT '로그인 ID',
    password VARCHAR(64) COMMENT '비밀번호',
    nick_nm VARCHAR(50) COMMENT '사용자 표시이름',
    profl_img_url VARCHAR(256) COMMENT '프로필 이미지 경로',
    cn LONGTEXT COMMENT '사용자 설명 (관리자용)',
    email VARCHAR(100) COMMENT '이메일',        -- 기본 이메일:: 계정복구 등에 사용함
    cttpc VARCHAR(20) COMMENT '연락처',        -- 기본 연락처
    -- ACCOUNT_STATUS
    locked_yn CHAR(1) DEFAULT 'N' COMMENT '잠금 여부 (Y/N)',
    use_acs_ip_yn CHAR(1) DEFAULT 'N' COMMENT '접속IP 사용 여부 (Y/N)',
    lst_lgn_dt DATETIME COMMENT '마지막 로그인 일시',
    lgn_fail_cnt INT DEFAULT 0 COMMENT '로그인 실패 횟수',
    pw_chg_dt DATETIME COMMENT '비밀번호 변경 일시',
    dormant_bypass_yn CHAR(1) DEFAULT 'N' COMMENT '장기 미로그인 통과 여부 (Y/N)',
    needs_pw_reset CHAR(1) DEFAULT 'N' COMMENT '패스워드 변경 필요 여부 (Y/N)',
    -- REQST
    reqst_yn CHAR(1) DEFAULT 'N' COMMENT '외부신청 여부 (Y/N)',
    cf_yn CHAR(1) DEFAULT 'N' COMMENT '사용자 승인 여부 (Y/N)',
    -- ATCH_FILE
    atch_file_no INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    INDEX (user_id)
) COMMENT = '사용자 계정';

-- -----------------------

-- 권한 (auth_role)
-- @extends: BaseAuditEntity
CREATE TABLE IF NOT EXISTS auth_role (
    auth_cd VARCHAR(50) PRIMARY KEY COMMENT '권한 코드',
    auth_nm VARCHAR(50) COMMENT '권한 이름',
    auth_level INT COMMENT '권한 레벨',
    top_auth_cd VARCHAR(50) COMMENT '상위 권한 코드',
    -- STATE
    sort_ordr INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY (top_auth_cd) REFERENCES auth_role(auth_cd)
) COMMENT = '권한';

-- 사용자 권한 (user_auth_role)
-- @extends: BaseCrudEntity
CREATE TABLE user_auth_role (
    user_auth_role_no INT PRIMARY KEY AUTO_INCREMENT COMMENT '사용자 권한 번호 (PK)',
    user_no INT COMMENT '사용자 고유 번호',
    auth_cd VARCHAR(50) COMMENT '권한 코드',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY(user_no) REFERENCES user (user_no),
    FOREIGN KEY(auth_cd) REFERENCES auth_role (auth_cd),
    INDEX (user_no),
    INDEX (auth_cd)
) COMMENT = '사용자 권한';

-- -----------------------

-- 사용자 계정 정보 :: 접속 IP (user_acs_ip)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS user_acs_ip (
    user_acs_ip_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 접속 IP 고유 번호 (PK)',
    user_no INT COMMENT '사용자 고유 번호',
    acs_ip VARCHAR(20) COMMENT '접속 IP',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY(user_no) REFERENCES user (user_no)
) COMMENT = '사용자 접속IP';

-- -----------------------

-- 사용자 프로필 정보 (user_profl)
-- @extends: BaseAtchEntity
CREATE TABLE IF NOT EXISTS user_profl (
    user_profl_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 프로필 고유 번호 (PK)',
    user_no INT COMMENT '사용자 고유 번호',
    addr VARCHAR(500) COMMENT '주소',
    zipcode VARCHAR(20) COMMENT '우편번호',
    brthdy DATE COMMENT '생일',
    lunar_yn CHAR(1) DEFAULT 'N' COMMENT '음력 여부 (Y/N)',
    profl_cn VARCHAR(2000) COMMENT '프로필(자기소개)',
    -- ATCH_FILE
    atch_file_no INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY(user_no) REFERENCES user (user_no)
) COMMENT = '사용자 프로필';

-- 사용자 인사정보 (user_emplym)
-- @extends: BaseAtchEntity
CREATE TABLE IF NOT EXISTS user_emplym (
    user_emplym_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 인사정보 고유 번호 (PK)',
    user_no INT COMMENT '사용자 고유 번호',
    user_nm VARCHAR(50) COMMENT '직원명',
    emplym_cttpc VARCHAR(20) COMMENT '연락처',        -- 기본 연락처
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
    -- ATCH_FILE
    atch_file_no INT COMMENT '첨부파일 번호',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY(user_no) REFERENCES user (user_no)
) COMMENT = '사용자 인사정보';
