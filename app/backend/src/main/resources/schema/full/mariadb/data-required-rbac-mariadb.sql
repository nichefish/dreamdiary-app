-- 필수 :: 사용자 그룹/권한(RBAC) 시드.
-- 쿼리 줄바꿈 안됨. 무조건 한 줄에 한 쿼리 단위로 실행된다.
-- 시스템 롤 → permission 부여로 기존 USER/MNGR 메뉴 동작을 동치로 유지한다.
-- @database : mariadb
-- @author : nichefish

-- 권한 카탈로그
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.view.user', '사용자 메뉴 조회', '사용자 사이드바/메타 메뉴 노출', 10, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.view.admin', '관리자 메뉴 조회', '관리자 사이드바 루트 노출', 20, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.admin.user_account', '계정 관리 메뉴', '관리자 계정 관리 메뉴 노출', 30, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.admin.auth_policy', '인증 정책 메뉴', '인증 정책 관리 메뉴 노출', 40, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.admin.user_group', '사용자 그룹 메뉴', '사용자 그룹/권한 관리 메뉴 노출', 50, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.admin.menu', '메뉴 관리 메뉴', '메뉴 관리 화면 노출', 60, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.admin.code', '코드 관리 메뉴', '코드 관리 화면 노출', 70, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.admin.page', '사이트 관리 메뉴', '사이트 관리 화면 노출', 80, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.admin.board', '게시판 관리 메뉴', '게시판 그룹 관리 화면 노출', 90, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.admin.log', '로그 목록 메뉴', '운영 로그 목록 화면 노출', 100, 'Y', 'system');
INSERT IGNORE INTO permission (perm_key, perm_name, description, sort_order, use_yn, created_by) VALUES ('menu.admin.log_stats', '로그 통계 메뉴', '사용자별 로그 통계 화면 노출', 110, 'Y', 'system');

-- ROLE_USER → 사용자 메뉴
INSERT IGNORE INTO role_permission (role_id, permission_id) SELECT r.id, p.id FROM `role` r CROSS JOIN permission p WHERE r.role_key = 'USER' AND p.perm_key = 'menu.view.user' AND r.deleted_at IS NULL AND p.deleted_at IS NULL;

-- ROLE_MNGR → 전체 메뉴 권한
INSERT IGNORE INTO role_permission (role_id, permission_id) SELECT r.id, p.id FROM `role` r CROSS JOIN permission p WHERE r.role_key = 'MNGR' AND p.perm_key LIKE 'menu.%' AND r.deleted_at IS NULL AND p.deleted_at IS NULL AND p.use_yn = 'Y';

-- ROLE_DEV → 전체 메뉴 권한 (런타임 ROLE_MNGR 매핑과 별도로 permission 축 유지)
INSERT IGNORE INTO role_permission (role_id, permission_id) SELECT r.id, p.id FROM `role` r CROSS JOIN permission p WHERE r.role_key = 'DEV' AND p.perm_key LIKE 'menu.%' AND r.deleted_at IS NULL AND p.deleted_at IS NULL AND p.use_yn = 'Y';

-- 샘플 사용자 그룹 (가상 픽스처)
INSERT IGNORE INTO user_group (group_key, group_name, description, sort_order, use_yn, created_by) VALUES ('CONTENT_EDITORS', '컨텐츠 편집자', '게시판·코드 등 컨텐츠 관리 메뉴 권한 묶음 (가상 픽스처)', 10, 'Y', 'system');
INSERT IGNORE INTO group_permission (group_id, permission_id) SELECT g.id, p.id FROM user_group g CROSS JOIN permission p WHERE g.group_key = 'CONTENT_EDITORS' AND p.perm_key IN ('menu.view.admin', 'menu.admin.board', 'menu.admin.code') AND g.deleted_at IS NULL AND p.deleted_at IS NULL;

-- 메뉴 required_perm_key (현행 admin_yn 트리와 동치)
UPDATE menu SET required_perm_key = 'menu.view.user' WHERE admin_yn = 'N' AND menu_type = 'MAIN' AND deleted_at IS NULL AND (required_perm_key IS NULL OR required_perm_key = '');
UPDATE menu SET required_perm_key = 'menu.view.user' WHERE menu_label IN ('MAIN', 'JOURNAL', 'JOURNAL_DAY', 'JOURNAL_THREAD', 'JOURNAL_ANNUAL', 'SCHEDULE', 'SCHEDULE_CAL', 'USER_MY', 'BOARD') AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.view.admin' WHERE menu_label = 'ADMIN_MAIN' AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.view.admin' WHERE menu_label IN ('USER', 'ADMIN', 'LOG', 'CONTENT') AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.admin.user_account' WHERE menu_label = 'USER_ACCOUNT' AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.admin.auth_policy' WHERE menu_label = 'AUTH_POLICY' AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.admin.user_group' WHERE menu_label = 'USER_GROUP' AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.admin.menu' WHERE menu_label = 'MENU_ADMIN' AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.admin.code' WHERE menu_label = 'CODE_ADMIN' AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.admin.page' WHERE menu_label = 'ADMIN_PAGE' AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.admin.board' WHERE menu_label = 'BOARD_ADMIN' AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.admin.log' WHERE menu_label = 'LOG_LIST' AND deleted_at IS NULL;
UPDATE menu SET required_perm_key = 'menu.admin.log_stats' WHERE menu_label = 'LOG_STATS_USER' AND deleted_at IS NULL;