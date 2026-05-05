-- 필수 데이터 및 코드 데이터 쿼리 정보를 입력한다.
-- 쿼리 줄바꿈 안됨. 무조건 한 줄에 한 쿼리 단위로 실행된다.
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 최상위 메뉴
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, sort_order, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn, created_by )
VALUES
(NULL, 'MAIN', '사용자', 0, 'LIST', 'MAIN', 'N','Y','Y','Y','SYSTEM'),
(NULL, 'MAIN', '관리자', 0, 'LIST', 'ADMIN_MAIN', 'Y','Y','Y','Y','SYSTEM');

-- 공지사항
INSERT INTO menu ( parent_menu_id, menu_type, menu_label, menu_name, url, sort_order, submenu_expand_type, admin_yn, protected_yn, required_yn, use_yn, created_by )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', 'NOTICE', '공지사항', '/app/notice/list.do', 0, 'NO_SUB', 'N', 'N', 'N', 'Y', 'SYSTEM'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 저널
INSERT INTO menu ( parent_menu_id, menu_type, menu_label, menu_name, url, sort_order, submenu_expand_type, admin_yn, protected_yn, required_yn, use_yn, created_by )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', 'JOURNAL', '저널', NULL, 0, 'LIST', 'N', 'N', 'N', 'Y', 'SYSTEM'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'JOURNAL' AS upper_label )
SELECT M.id, 'SUB', '저널 일자', '/app/journal/day/monthly.do', NULL, 0, 'system', 'NO_SUB', 'JOURNAL_DAY', 'N', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'JOURNAL' AS upper_label )
SELECT M.id, 'SUB', '저널 주제', '/app/journal/sbjct/list.do', NULL, 1, 'system', 'NO_SUB', 'JOURNAL_SBJCT', 'N', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'JOURNAL' AS upper_label )
SELECT M.id, 'SUB', '저널 연간', '/app/journal/annual/list.do', NULL, 2, 'system', 'NO_SUB', 'JOURNAL_ANNUAL', 'N', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 일정
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', '일정', NULL, '<span class="menu-icon"><i class="bi bi-calendar3 fs-2"></i></span>', 6, 'system', 'LIST', 'SCHEDULE', 'N', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 사용자 관리
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN_MAIN' AS upper_label )
SELECT M.id, 'SUB', '사용자 관리', NULL, '<span class="menu-icon"><i class="bi bi-people fs-2"></i></span>', 11, 'nichefish', 'LIST', 'USER', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '계정 관리', '/app/user/list.do', NULL, 12, 'nichefish', 'NO_SUB', 'USER_ACCOUNT', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '계정 신청 승인관리', '/app/user/signup/list.do', NULL, 13, 'nichefish', 'NO_SUB', 'USER_SIGNUP_APPROVAL', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '인증 정책 관리', '/app/auth/policy/page.do', NULL, 14, 'nichefish', 'NO_SUB', 'AUTH_POLICY', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 사이트 관리
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN_MAIN' AS upper_label )
SELECT M.id, 'SUB', '사이트 관리', NULL, '<span class="menu-icon"><i class="ki-duotone ki-element-11 fs-2"></i></span>', 14, 'nichefish', 'COLLAPSE', 'ADMIN', 'N', 'Y', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '메뉴 관리', '/app/admin/menu/page.do', NULL, 16, 'nichefish', 'NO_SUB', 'MENU_ADMIN', 'N', 'Y', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '사이트 관리', '/app/admin/page.do', NULL, 15, 'nichefish', 'NO_SUB', 'ADMIN_PAGE', 'N', 'Y', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '코드 관리', '/app/admin/code/page.do', NULL, 17, 'nichefish', 'NO_SUB', 'CODE_ADMIN', 'N', 'Y', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 컨텐츠 관리
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'CONTENT' AS upper_label )
SELECT M.id, 'SUB', '게시판 관리', '/app/admin/board/page.do', NULL, 22, 'nichefish', 'NO_SUB', 'BOARD_ADMIN', 'N', 'Y', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 로그 관리
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'ADMIN_MAIN' AS upper_label )
SELECT M.id, 'SUB', '로그 관리', NULL, '<span class="menu-icon"><i class="bi bi-justify-left fs-2"></i></span>', 25, 'nichefish', 'LIST', 'LOG', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, required_yn, use_yn )
WITH T AS ( SELECT 'LOG' AS upper_label )
SELECT M.id, 'SUB', '로그 목록', '/app/log/list.do', NULL, 26, 'nichefish', 'NO_SUB', 'LOG_LIST', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;



-- 메뉴 라벨 마이그레이션 (기존 운영/개발 데이터 정합)
UPDATE menu
SET menu_label = 'MENU_ADMIN'
WHERE deleted_at IS NULL
  AND menu_label = 'MENU'
  AND menu_name = '메뉴 관리';

UPDATE menu
SET menu_label = 'CODE_ADMIN'
WHERE deleted_at IS NULL
  AND menu_label = 'CODE'
  AND menu_name = '코드 관리';

UPDATE menu
SET menu_label = 'USER_ACCOUNT'
WHERE deleted_at IS NULL
  AND menu_label = 'USER_INFO'
  AND menu_name = '계정 관리';

UPDATE menu
SET menu_label = 'BOARD_ADMIN'
WHERE deleted_at IS NULL
  AND menu_label = 'BOARD'
  AND menu_name = '게시판 관리';
