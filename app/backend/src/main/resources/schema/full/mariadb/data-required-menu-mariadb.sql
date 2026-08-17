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
SELECT M.id, 'SUB', '저널 일자', '/app/journal/day/home', NULL, 0, 'system', 'NO_SUB', 'JOURNAL_DAY', 'N', 'N', 'Y'
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

-- 사용자 숨김/시스템 메뉴 (사이드바에는 표시하지 않지만 breadcrumb/권한/화면 메타 원천으로 사용)
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, sidebar_visible_yn, use_yn )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', '내 정보', '/app/user/my/page.do', NULL, 90, 'system', 'NO_SUB', 'USER_MY', 'N', 'Y', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL
WHERE NOT EXISTS (SELECT 1 FROM menu C WHERE C.menu_label = 'USER_MY' AND C.deleted_at IS NULL);

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

-- 계정 신청 승인관리 메뉴는 두지 않는다.
-- 해당 화면은 계정 관리(/admin/users)의 `계정 신청 승인` 탭으로 흡수됐다. (V0.24.5 에서 기존 행 제거)

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '인증 정책 관리', '/app/auth/policy/page.do', NULL, 14, 'nichefish', 'NO_SUB', 'AUTH_POLICY', 'N', 'Y', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL;
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn, required_perm_key )
WITH T AS ( SELECT 'USER' AS upper_label )
SELECT M.id, 'SUB', '사용자 그룹 관리', '/app/user/group/page.do', NULL, 13, 'system', 'NO_SUB', 'USER_GROUP', 'N', 'Y', 'Y', 'menu.admin.user_group'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL
WHERE NOT EXISTS (SELECT 1 FROM menu C WHERE C.menu_label = 'USER_GROUP' AND C.deleted_at IS NULL);


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

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'ADMIN' AS upper_label )
SELECT M.id, 'SUB', '템플릿 관리', '/app/admin/tmplat/page.do', NULL, 18, 'nichefish', 'NO_SUB', 'TMPLAT_ADMIN', 'N', 'Y', 'Y'
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
  WHEN 'TMPLAT_ADMIN' THEN '저널 작성용 사전입력 템플릿을 관리합니다.'
  WHEN 'BOARD_ADMIN' THEN '게시판 그룹과 카테고리 코드, 사용 여부, 노출 순서를 관리합니다.'
  WHEN 'USER_ACCOUNT' THEN '사용자 계정과 권한을 관리합니다.'
  WHEN 'AUTH_POLICY' THEN '로그인 실패, 계정 잠금, 비밀번호 변경 주기, 세션 정책을 관리합니다.'
  WHEN 'USER_GROUP' THEN '사용자 그룹, 멤버십, 그룹 권한을 관리합니다.'
  WHEN 'LOG_LIST' THEN '실패, 지연, trace 흐름을 중심으로 운영 로그를 확인합니다.'
  WHEN 'LOG_STATS_USER' THEN '사용자별 활동 로그 통계를 확인합니다.'
  WHEN 'USER_MY' THEN '내 계정과 프로필 정보를 확인하고 관리합니다.'
  ELSE menu_description
END
WHERE menu_label IN (
    'ADMIN_PAGE',
    'MENU_ADMIN',
    'CODE_ADMIN',
    'TMPLAT_ADMIN',
    'BOARD_ADMIN',
    'USER_ACCOUNT',
    'AUTH_POLICY',
    'USER_GROUP',
    'LOG_LIST',
    'LOG_STATS_USER',
    'USER_MY'
  )
  AND deleted_at IS NULL
  AND (menu_description IS NULL OR menu_description = '');
