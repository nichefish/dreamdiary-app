-- 코드 데이터 쿼리 정보를 입력한다.
-- 쿼리 줄바꿈 안됨. 무조건 한 줄에 한 쿼리 단위로 실행된다.
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 옵션 :: 분류 코드 분류 코드 추가
INSERT IGNORE INTO code_group (cl_cd, cl_cd_nm, cl_ctgr_cd, description) VALUES ('CL_CTGR_CD', '분류코드 분류 코드', 'SYS', '분류코드 분류 코드');
-- 필수 :: 분류 코드 분류 상세 코드 추가
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('CL_CTGR_CD', 'SYS', '시스템', '시스템에서 필수적으로 사용되는 코드입니다.', '1');

-- -------------------

-- 필수 :: 수정권한 분류 코드 추가
INSERT IGNORE INTO code_group (cl_cd, cl_cd_nm, cl_ctgr_cd, description) VALUES ('MDFABLE_CD', '수정권한', 'SYS', '수정권한 코드');
-- 필수 :: 수정권한 상세 코드 추가
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('MDFABLE_CD', 'REGSTR', '등록자', '등록자만 수정 가능', '1');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('MDFABLE_CD', 'MNGR', '관리자', '관리자만 수정 가능', '2');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('MDFABLE_CD', 'USER', '사용자', '사용자만 수정 가능', '3');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('MDFABLE_CD', 'ALL', '전체', '전체 수정 가능', '99');

-- -----------------------

-- 옵션 :: 일정 분류 코드 추가
INSERT IGNORE INTO code_group (cl_cd, cl_cd_nm, cl_ctgr_cd, description) VALUES ('SCHEDULE_CD', '일정', '', '일정 코드');
-- 필수 :: 수정권한 상세 코드 추가
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('SCHEDULE_CD', 'HOLYDAY', '공휴일', '공휴일', '1');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('SCHEDULE_CD', 'CEREMONY', '행사', '행사', '2');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('SCHEDULE_CD', 'BRTHDY', '생일', '생일', '3');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('SCHEDULE_CD', 'INDT', '내부일정', '내부일정', '11');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('SCHEDULE_CD', 'TLCMMT', '재택', '재택', '21');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('SCHEDULE_CD', 'OUTDT', '외근', '외근', '22');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('SCHEDULE_CD', 'VCATN', '휴가', '휴가', '23');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('SCHEDULE_CD', 'ETC', '기타', '기타', '99');

-- -----------------------

-- 필수 :: 저널 결산 구분 분류 코드 추가
INSERT IGNORE INTO code_group (cl_cd, cl_cd_nm, cl_ctgr_cd, description) VALUES ('JOURNAL_SUMRY_TY_CD', '저널 결산 구분', 'SYS', '저널 결산 구분 코드');
-- 필수 :: 저널 결산 구분 상세 코드 추가
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('JOURNAL_SUMRY_TY_CD', 'DIARY', '일기', '일기 결산', '1');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('JOURNAL_SUMRY_TY_CD', 'DREAM', '꿈', '꿈 결산', '2');

-- -----------------------

-- 공지사항 분류 코드 추가
INSERT IGNORE INTO code_group (cl_cd, cl_cd_nm, description) VALUES ('NOTICE_CTGR_CD', '공지사항 분류 코드', '공지사항 분류 코드');
-- (구) 필수 :: 공지사항 분류 상세 코드 추가
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('NOTICE_CTGR_CD', 'NOTICE', '공지', '공지', '1');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('NOTICE_CTGR_CD', 'SCHEDULE', '일정', '일정', '2');

-- -----------------------

-- 하위메뉴 확장 유형 분류 코드 추가
INSERT IGNORE INTO code_group (cl_cd, cl_cd_nm, cl_ctgr_cd, description) VALUES ('MENU_SUB_EXTEND_TY_CD', '하위메뉴 확장 유형 코드', 'SYS', '하위메뉴 확장 유형 코드');
-- 하위메뉴 확장 유형 상세 코드 추가
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('MENU_SUB_EXTEND_TY_CD', 'NO_SUB', '하위메뉴 없음', '하위메뉴 없음 (대메뉴가 링크로 기능함)', '0');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('MENU_SUB_EXTEND_TY_CD', 'LIST', '아래로 목록 표시', '아래로 목록 표시', '1');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('MENU_SUB_EXTEND_TY_CD', 'EXTEND', '우측으로 확장', '우측으로 확장', '2');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('MENU_SUB_EXTEND_TY_CD', 'COLLAPSE', '글접기', '글접기', '3');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('MENU_SUB_EXTEND_TY_CD', 'BOARD', '일반게시판', '일반게시판', '4');
-- -----------------------

-- text class code group
INSERT IGNORE INTO code_group (cl_cd, cl_cd_nm, cl_ctgr_cd, description) VALUES ('TEXT_CLASS_CD', 'Text Class', 'SYS', 'text semantic code');
-- text class detail codes
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'DEFAULT', 'DEFAULT', '', '1');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'SUCCESS', 'SUCCESS', 'text-success', '2');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'INFO', 'INFO', 'text-info', '3');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'WARNING', 'WARNING', 'text-warning', '4');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'DANGER', 'DANGER', 'text-danger', '5');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'PRIMARY', 'PRIMARY', 'text-primary', '6');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'SECONDARY', 'SECONDARY', 'text-secondary', '7');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'DARK', 'DARK', 'text-dark', '8');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'MUTED', 'MUTED', 'text-muted', '9');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'DIALOG', 'DIALOG', 'text-dialog', '10');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'NOTI', 'NOTI', 'text-noti', '11');
INSERT IGNORE INTO code_item (cl_cd, dtl_cd, dtl_cd_nm, description, sort_order) VALUES ('TEXT_CLASS_CD', 'BURNT', 'BURNT', 'text-burnt', '12');

