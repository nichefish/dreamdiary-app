-- 필수 데이터 및 코드 데이터 쿼리 정보를 입력한다.
-- 쿼리 줄바꿈 안됨. 무조건 한 줄에 한 쿼리 단위로 실행된다.
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 최상위 메뉴
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, sort_order, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn, created_by )
VALUES
(NULL, 'MAIN', '사용자', 0, 'LIST', 'MAIN', 'N','Y','Y','SYSTEM'),
(NULL, 'MAIN', '관리자', 0, 'LIST', 'ADMIN_MAIN', 'Y','Y','Y','SYSTEM');

-- 저널
INSERT INTO menu ( parent_menu_id, menu_type, menu_label, menu_name, url, sort_order, submenu_expand_type, admin_yn, protected_yn, use_yn, created_by )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', 'JOURNAL', '저널', NULL, 0, 'LIST', 'N', 'N', 'Y', 'SYSTEM'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'JOURNAL' AS upper_label )
SELECT M.id, 'SUB', '저널 일자', '/app/journal/day/monthly.do', NULL, 0, 'system', 'NO_SUB', 'JOURNAL_DAY', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'JOURNAL' AS upper_label )
SELECT M.id, 'SUB', '저널 스레드', '/app/journal/thread/list.do', NULL, 1, 'system', 'NO_SUB', 'JOURNAL_THREAD', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'JOURNAL' AS upper_label )
SELECT M.id, 'SUB', '저널 연간', '/app/journal/annual/list.do', NULL, 2, 'system', 'NO_SUB', 'JOURNAL_ANNUAL', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 일정
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', '일정', NULL, '<span class="menu-icon"><i class="bi bi-calendar3 fs-2"></i></span>', 6, 'system', 'LIST', 'SCHEDULE', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'SCHEDULE' AS upper_label )
SELECT M.id, 'SUB', '일정 달력', '/app/schedule/calendar.do', NULL, 0, 'system', 'NO_SUB', 'SCHEDULE_CAL', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 사용자 관리
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'ADMIN_MAIN' AS upper_label )
SELECT M.id, 'SUB', '사용자 관리', NULL, '<span class="menu-icon"><i class="bi bi-people fs-2"></i></span>', 11, 'nichefish', 'LIST', 'USER', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '계정 관리', '/app/user/list.do', NULL, 12, 'nichefish', 'NO_SUB', 'USER_ACCOUNT', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '계정 신청 승인관리', '/app/user/signup/list.do', NULL, 13, 'nichefish', 'NO_SUB', 'USER_SIGNUP_APPROVAL', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '인증 정책 관리', '/app/auth/policy/page.do', NULL, 14, 'nichefish', 'NO_SUB', 'AUTH_POLICY', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 사이트 관리
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'ADMIN_MAIN' AS upper_label )
SELECT M.id, 'SUB', '사이트 관리', NULL, '<span class="menu-icon"><i class="ki-duotone ki-element-11 fs-2"></i></span>', 14, 'nichefish', 'COLLAPSE', 'ADMIN', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '메뉴 관리', '/app/admin/menu/page.do', NULL, 16, 'nichefish', 'NO_SUB', 'MENU_ADMIN', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '사이트 관리', '/app/admin/page.do', NULL, 15, 'nichefish', 'NO_SUB', 'ADMIN_PAGE', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '코드 관리', '/app/admin/code/page.do', NULL, 17, 'nichefish', 'NO_SUB', 'CODE_ADMIN', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 컨텐츠 관리
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'CONTENT' AS upper_label )
SELECT M.id, 'SUB', '게시판 관리', '/app/admin/board/page.do', NULL, 22, 'nichefish', 'NO_SUB', 'BOARD_ADMIN', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

-- 로그 관리
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'ADMIN_MAIN' AS upper_label )
SELECT M.id, 'SUB', '로그 관리', NULL, '<span class="menu-icon"><i class="bi bi-justify-left fs-2"></i></span>', 25, 'nichefish', 'LIST', 'LOG', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'LOG' AS upper_label )
SELECT M.id, 'SUB', '로그 목록', '/app/log/list.do', NULL, 26, 'nichefish', 'NO_SUB', 'LOG_LIST', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;
-- 일정 달력 (기존 DB: SCHEDULE 부모만 있을 때 하위 메뉴 보강)
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
SELECT P.id, 'SUB', '일정 달력', '/app/schedule/calendar.do', NULL, 0, 'system', 'NO_SUB', 'SCHEDULE_CAL', 'N', 'N', 'Y'
FROM menu P
WHERE P.menu_label = 'SCHEDULE' AND P.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM menu C WHERE C.menu_label = 'SCHEDULE_CAL' AND C.deleted_at IS NULL);
UPDATE menu SET url = '/app/schedule/calendar.do'
WHERE menu_label = 'SCHEDULE_CAL' AND deleted_at IS NULL
  AND url = '/app/schedule/cal.do';

-- 메뉴 breadcrumb 하단 표시 문구.
UPDATE menu
SET menu_description = CASE menu_label
  WHEN 'ADMIN_PAGE' THEN '캐시, 외부 동기화, 권한, 임베딩 큐를 관리합니다.'
  WHEN 'MENU_ADMIN' THEN '사이드바와 관리자 메뉴 트리를 관리합니다.'
  WHEN 'CODE_ADMIN' THEN '분류 코드와 상세 코드를 관리합니다.'
  WHEN 'BOARD_ADMIN' THEN '게시판 그룹과 카테고리 코드, 사용 여부, 노출 순서를 관리합니다.'
  WHEN 'USER_ACCOUNT' THEN '사용자 계정과 권한을 관리합니다.'
  WHEN 'USER_SIGNUP_APPROVAL' THEN '계정 신청 승인 요청을 확인하고 승인 또는 반려합니다.'
  WHEN 'AUTH_POLICY' THEN '로그인 실패, 계정 잠금, 비밀번호 변경 주기, 세션 정책을 관리합니다.'
  WHEN 'LOG_LIST' THEN '실패, 지연, trace 흐름을 중심으로 운영 로그를 확인합니다.'
  WHEN 'LOG_STATS_USER' THEN '사용자별 활동 로그 통계를 확인합니다.'
  ELSE menu_description
END
WHERE menu_label IN (
    'ADMIN_PAGE',
    'MENU_ADMIN',
    'CODE_ADMIN',
    'BOARD_ADMIN',
    'USER_ACCOUNT',
    'USER_SIGNUP_APPROVAL',
    'AUTH_POLICY',
    'LOG_LIST',
    'LOG_STATS_USER'
  )
  AND deleted_at IS NULL
  AND (menu_description IS NULL OR menu_description = '');
