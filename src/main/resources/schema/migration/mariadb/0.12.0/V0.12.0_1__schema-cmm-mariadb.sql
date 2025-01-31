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
    sort_ordr INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
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
    sort_ordr INT DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
    -- AUDIT
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    PRIMARY KEY (cl_cd, dtl_cd)
) COMMENT = '상세 코드';

-- -----------------------

-- 로그인 정책 (lgn_policy)
-- @extends: BaseAuditEntity
CREATE TABLE IF NOT EXISTS lgn_policy (
    lgn_policy_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '로그인 정책 번호 (PK)',
    lgn_try_lmt INT COMMENT '로그인 시도 제한 횟수',
    pw_chg_dy INT COMMENT '패스워드 변경 주기',
    lgn_lock_dy INT COMMENT '계정 잠금 주기',
    pw_for_reset VARCHAR(20) COMMENT '리셋할 패스워드',
    -- AUDIT
    regstr_id VARCHAR(20) comment '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    mdfusr_id VARCHAR(20) COMMENT '수정자 ID',
    mdf_dt DATETIME COMMENT '수정일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '로그인 정책';

-- -----------------------

-- 첨부파일 (atch_file)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS atch_file (
    atch_file_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '첨부파일 번호 (PK)',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '첨부파일';

-- 첨부파일 상세 (atch_file_dtl)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS atch_file_dtl (
    atch_file_dtl_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '첨부파일 상세 번호 (PK)',
    atch_file_no INT COMMENT '첨부파일 번호',
    file_sn INT COMMENT '파일 순번',
    orgn_file_nm VARCHAR(200) COMMENT '원본파일명',
    stre_file_nm VARCHAR(200) COMMENT '저장파일명',
    file_extn VARCHAR(10) COMMENT '파일 확장자',
    file_stre_path VARCHAR(200) COMMENT '파일 저장 경로',
    file_size INT COMMENT '파일 크기(BYTE)',
    url VARCHAR(500) COMMENT '파일 URL',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    -- CONSTRAINT
    FOREIGN KEY(atch_file_no) REFERENCES atch_file (atch_file_no)
) COMMENT = '첨부파일 상세';

-- -----------------------

-- 활동 로그 (log_actvty)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS log_actvty (
    log_actvty_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '활동 로그 번호 (PK)',
    log_dt DATETIME COMMENT '로그 기록 일시',
    user_id VARCHAR(20) COMMENT '로그 사용자 ID',
    actvty_ctgr_cd VARCHAR(50) COMMENT '활동 카테고리 코드',
    action_ty_cd VARCHAR(50) COMMENT '액션 타입 코드',
    url VARCHAR(400) COMMENT '요청 URL',
    mthd VARCHAR(400) COMMENT '요청 메소드',
    param VARCHAR(500) COMMENT '요청 파라미터',
    cn LONGTEXT COMMENT '내용',
    ip_addr VARCHAR(20) COMMENT 'IP 주소',
    referer VARCHAR(1000) COMMENT '리퍼러 URL',
    rslt CHAR(1) COMMENT '결과',
    rslt_msg VARCHAR(50) COMMENT '결과 메시지',
    exception_nm VARCHAR(100) COMMENT '예외 이름',
    exception_msg VARCHAR(4000) COMMENT '예외 메시지',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '활동 로그';

-- 활동 로그 URL명 (log_actvty_url_nm)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS log_actvty_url_nm (
    log_actvty_url_nm_no INT AUTO_INCREMENT PRIMARY KEY COMMENT '활동 로그 URL명 번호 (PK)',
    url VARCHAR(200) UNIQUE COMMENT 'URL',
    url_nm VARCHAR(200) COMMENT 'URL 이름',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '활동 로그 URL명';

-- 시스템 로그 (log_sys)
-- @extends: BaseCrudEntity
CREATE TABLE IF NOT EXISTS log_sys (
    log_sys_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '시스템 로그 번호 (PK)',
    log_dt DATETIME COMMENT '로그 날짜 및 시간',
    user_id VARCHAR(20) COMMENT '로그 기록 사용자 ID',
    actvty_ctgr_cd VARCHAR(50) COMMENT '활동 카테고리 코드',
    cn LONGTEXT COMMENT '내용',
    rslt CHAR(1) COMMENT '결과',
    rslt_msg VARCHAR(500) COMMENT '결과 메시지',
    exception_nm VARCHAR(100) COMMENT '예외 이름',
    exception_msg VARCHAR(4000) COMMENT '예외 메시지',
    -- AUDIT
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)'
) COMMENT = '시스템 로그';
