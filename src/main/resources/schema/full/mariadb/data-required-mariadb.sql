-- 필수 데이터 및 코드 데이터 쿼리 정보를 입력한다.
-- 쿼리 줄바꿈 안됨. 무조건 한 줄에 한 쿼리 단위로 실행된다.
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 필수 :: 권한 정보 추가
INSERT IGNORE INTO auth_role (auth_cd, auth_nm, auth_level, top_auth_cd, sort_ordr, use_yn, regstr_id) VALUES ('MNGR', '관리자', 9, null, 1, 'Y', 'system');
INSERT IGNORE INTO auth_role (auth_cd, auth_nm, auth_level, top_auth_cd, sort_ordr, use_yn, regstr_id) VALUES ('USER', '사용자', 5, null, 2, 'Y', 'system');
INSERT IGNORE INTO auth_role (auth_cd, auth_nm, auth_level, top_auth_cd, sort_ordr, use_yn, regstr_id) VALUES ('DEV', '개발자', 99, null, 99, 'Y', 'system');

-- -------------------

-- 옵션 :: 분류 코드 분류 코드 추가
INSERT IGNORE INTO cmm_cl_cd (cl_cd, cl_cd_nm, cl_ctgr_cd, dc) VALUES ('CL_CTGR_CD', '분류코드 분류 코드', 'SYS', '분류코드 분류 코드');
-- 필수 :: 분류 코드 분류 상세 코드 추가
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('CL_CTGR_CD', 'SYS', '시스템', '시스템에서 필수적으로 사용되는 코드입니다.', '1');

-- -------------------

-- 필수 :: 잔디 토픽 분류 코드 추가
INSERT IGNORE INTO cmm_cl_cd (cl_cd, cl_cd_nm, cl_ctgr_cd, dc) VALUES ('JANDI_TOPIC_CD', '잔디 토픽', '', '잔디 토픽 코드');
-- 필수 :: 잔디 토픽 상세 코드 추가
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('JANDI_TOPIC_CD', 'NOTICE', '공지사항', '공지사항', '1');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('JANDI_TOPIC_CD', 'SCHDUL', '일정공유', '일정공유', '2');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('JANDI_TOPIC_CD', 'TEST', '테스트', '테스트', '99');

-- -----------------------

-- 필수 :: 수정권한 분류 코드 추가
INSERT IGNORE INTO cmm_cl_cd (cl_cd, cl_cd_nm, cl_ctgr_cd, dc) VALUES ('MDFABLE_CD', '수정권한', 'SYS', '수정권한 코드');
-- 필수 :: 수정권한 상세 코드 추가
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('MDFABLE_CD', 'REGSTR', '등록자', '등록자만 수정 가능', '1');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('MDFABLE_CD', 'MNGR', '관리자', '관리자만 수정 가능', '2');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('MDFABLE_CD', 'USER', '사용자', '사용자만 수정 가능', '3');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('MDFABLE_CD', 'ALL', '전체', '전체 수정 가능', '99');

-- -----------------------

-- 옵션 :: 일정 분류 코드 추가
INSERT IGNORE INTO cmm_cl_cd (cl_cd, cl_cd_nm, cl_ctgr_cd, dc) VALUES ('SCHDUL_CD', '일정', '', '일정 코드');
-- 필수 :: 수정권한 상세 코드 추가
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('SCHDUL_CD', 'HLDY', '공휴일', '공휴일', '1');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('SCHDUL_CD', 'CEREMONY', '행사', '행사', '2');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('SCHDUL_CD', 'BRTHDY', '생일', '생일', '3');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('SCHDUL_CD', 'INDT', '내부일정', '내부일정', '11');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('SCHDUL_CD', 'TLCMMT', '재택', '재택', '21');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('SCHDUL_CD', 'OUTDT', '외근', '외근', '22');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('SCHDUL_CD', 'VCATN', '휴가', '휴가', '23');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('SCHDUL_CD', 'ETC', '기타', '기타', '99');

-- -----------------------

-- 필수 :: 저널 결산 구분 분류 코드 추가
INSERT IGNORE INTO cmm_cl_cd (cl_cd, cl_cd_nm, cl_ctgr_cd, dc) VALUES ('JRNL_SUMRY_TY_CD', '저널 결산 구분', 'SYS', '저널 결산 구분 코드');
-- 필수 :: 저널 결산 구분 상세 코드 추가
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('JRNL_SUMRY_TY_CD', 'DIARY', '일기', '일기 결산', '1');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('JRNL_SUMRY_TY_CD', 'DREAM', '꿈', '꿈 결산', '2');

-- -----------------------

-- (구) 필수 :: 휴가 분류 코드 추가
INSERT IGNORE INTO cmm_cl_cd (cl_cd, cl_cd_nm, dc) VALUES ('VCATN_CD', '휴가', '휴가');
-- (구) 필수 :: 휴가 상세 코드 추가
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('VCATN_CD', 'ANNUAL', '연차', '연차', '1');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('VCATN_CD', 'AM_HALF', '오전반차', '오전반차', '2');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('VCATN_CD', 'PM_HALF', '오후반차', '오후반차', '3');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('VCATN_CD', 'PBLEN', '공가', '공가', '4');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('VCATN_CD', 'CTSNN', '경조휴가', '경조휴가', '5');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('VCATN_CD', 'MNSTR', '생리휴가', '생리휴가', '6');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('VCATN_CD', 'UNPAID', '무급휴가', '무급휴가', '7');

-- -----------------------

-- 공지사항 분류 코드 추가
INSERT IGNORE INTO cmm_cl_cd (cl_cd, cl_cd_nm, dc) VALUES ('NOTICE_CTGR_CD', '공지사항 분류 코드', '공지사항 분류 코드');
-- (구) 필수 :: 공지사항 분류 상세 코드 추가
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('NOTICE_CTGR_CD', 'NOTICE', '공지', '공지', '1');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('NOTICE_CTGR_CD', 'SCHDUL', '일정', '일정', '2');

-- -----------------------

-- (구) 필수 :: 공지사항 분류 코드 추가
INSERT IGNORE INTO cmm_cl_cd (cl_cd, cl_cd_nm, dc) VALUES ('NOTICE_CTGR_CD', '공지사항 분류 코드', '공지사항 분류 코드');
-- (구) 필수 :: 공지사항 분류 상세 코드 추가
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('NOTICE_CTGR_CD', 'NOTICE', '공지', '공지', '1');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('NOTICE_CTGR_CD', 'SCHDUL', '일정', '일정', '2');

-- -----------------------

-- 하위메뉴 확장 유형 분류 코드 추가
INSERT IGNORE INTO cmm_cl_cd (cl_cd, cl_cd_nm, cl_ctgr_cd, dc) VALUES ('MENU_SUB_EXTEND_TY_CD', '하위메뉴 확장 유형 코드', 'SYS', '하위메뉴 확장 유형 코드');
-- 하위메뉴 확장 유형 상세 코드 추가
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('MENU_SUB_EXTEND_TY_CD', 'NO_SUB', '하위메뉴 없음', '하위메뉴 없음 (대메뉴가 링크로 기능함)', '0');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('MENU_SUB_EXTEND_TY_CD', 'LIST', '아래로 목록 표시', '아래로 목록 표시', '1');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('MENU_SUB_EXTEND_TY_CD', 'EXTEND', '우측으로 확장', '우측으로 확장', '2');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('MENU_SUB_EXTEND_TY_CD', 'COLLAPSE', '글접기', '글접기', '3');
INSERT IGNORE INTO cmm_dtl_cd (cl_cd, dtl_cd, dtl_cd_nm, dc, sort_ordr) VALUES ('MENU_SUB_EXTEND_TY_CD', 'BOARD', '일반게시판', '일반게시판', '4');