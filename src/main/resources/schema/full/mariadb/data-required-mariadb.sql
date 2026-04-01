-- 필수 데이터 쿼리 정보를 입력한다.
-- 쿼리 줄바꿈 안됨. 무조건 한 줄에 한 쿼리 단위로 실행된다.
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 필수 :: 권한 정보 추가
INSERT IGNORE INTO auth_role (auth_cd, auth_nm, auth_level, top_auth_cd, idx, use_yn, regstr_id) VALUES ('MNGR', '관리자', 9, null, 1, 'Y', 'system');
INSERT IGNORE INTO auth_role (auth_cd, auth_nm, auth_level, top_auth_cd, idx, use_yn, regstr_id) VALUES ('USER', '사용자', 5, null, 2, 'Y', 'system');
INSERT IGNORE INTO auth_role (auth_cd, auth_nm, auth_level, top_auth_cd, idx, use_yn, regstr_id) VALUES ('DEV', '개발자', 99, null, 99, 'Y', 'system');

