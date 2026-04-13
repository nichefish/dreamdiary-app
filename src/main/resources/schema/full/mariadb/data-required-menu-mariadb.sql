-- 필수 데이터 및 코드 데이터 쿼리 정보를 입력한다.
-- 쿼리 줄바꿈 안됨. 무조건 한 줄에 한 쿼리 단위로 실행된다.
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 최상위 메뉴
INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, idx, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn, regstr_id )
VALUES
(NULL, 'MAIN', '사용자', 0, 'LIST', 'MAIN', 'N','Y','Y','Y','SYSTEM'),
(NULL, 'MAIN', '관리자', 0, 'LIST', 'ADMIN_MAIN', 'Y','Y','Y','Y','SYSTEM');

-- 공지사항
INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_label, menu_nm, url, idx, menu_sub_extend_ty_cd, mngr_yn, protected_yn, required_yn, use_yn, regstr_id )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', 'NOTICE', '공지사항', '/app/notice/list.do', 0, 'NO_SUB', 'N', 'N', 'N', 'Y', 'SYSTEM'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

-- 저널
INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_label, menu_nm, url, idx, menu_sub_extend_ty_cd, mngr_yn, protected_yn, required_yn, use_yn, regstr_id )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', 'JRNL', '저널', NULL, 0, 'LIST', 'N', 'N', 'N', 'Y', 'SYSTEM'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'JRNL' AS upper_label )
SELECT M.id, 'SUB', '저널 일자', '/app/jrnl/day/monthly.do', NULL, 0, 'system', 'NO_SUB', 'JRNL_DAY', 'N', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'JRNL' AS upper_label )
SELECT M.id, 'SUB', '저널 주제', '/app/jrnl/sbjct/list.do', NULL, 1, 'system', 'NO_SUB', 'JRNL_SBJCT', 'N', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'JRNL' AS upper_label )
SELECT M.id, 'SUB', '저널 결산', '/app/jrnl/sumry/list.do', NULL, 2, 'system', 'NO_SUB', 'JRNL_SUMRY', 'N', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

-- 일정
INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', '일정', NULL, '<span class="menu-icon"><i class="bi bi-calendar3 fs-2"></i></span>', 6, 'system', 'LIST', 'SCHDUL', 'N', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

-- 사용자 관리
INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN_MAIN' AS upper_label )
SELECT M.id, 'SUB', '사용자 관리', NULL, '<span class="menu-icon"><i class="bi bi-people fs-2"></i></span>', 11, 'nichefish', 'LIST', 'USER', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '계정 관리', '/app/user/list.do', NULL, 12, 'nichefish', 'NO_SUB', 'USER_INFO', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '인증 정책 관리', '/app/auth/policy/form.do', NULL, 13, 'nichefish', 'NO_SUB', 'AUTH_POLICY', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

-- 사이트 관리
INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN_MAIN' AS upper_label )
SELECT M.id, 'SUB', '사이트 관리', NULL, '<span class="menu-icon"><i class="ki-duotone ki-element-11 fs-2"></i></span>', 14, 'nichefish', 'COLLAPSE', 'ADMIN', 'N', 'Y', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '메뉴 관리', '/app/menu/page.do', NULL, 16, 'nichefish', 'NO_SUB', 'MENU', 'N', 'Y', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '사이트 관리', '/app/admin/page.do', NULL, 15, 'nichefish', 'NO_SUB', 'ADMIN_PAGE', 'N', 'Y', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '코드 관리', '/app/cd/list.do', NULL, 17, 'nichefish', 'NO_SUB', 'CD', 'N', 'Y', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

-- 로그 관리
INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN_MAIN' AS upper_label )
SELECT M.id, 'SUB', '로그 관리', NULL, '<span class="menu-icon"><i class="bi bi-justify-left fs-2"></i></span>', 25, 'nichefish', 'LIST', 'LOG', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'LOG' AS upper_label )
SELECT M.id, 'SUB', '활동 로그', '/app/log/actvty/list.do', NULL, 26, 'nichefish', 'NO_SUB', 'LOG_ACTVTY', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';

INSERT INTO menu ( upper_menu_id, menu_ty_cd, menu_nm, url, icon, idx, regstr_id, menu_sub_extend_ty_cd, menu_label, mngr_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'LOG' AS upper_label )
SELECT M.id, 'SUB', '시스템 로그', '/app/log/sys/list.do', NULL, 27, 'nichefish', 'NO_SUB', 'LOG_SYS', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.del_yn = 'N';


