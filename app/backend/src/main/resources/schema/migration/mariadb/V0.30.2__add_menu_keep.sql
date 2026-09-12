-- Keep(전망 전수관리) 대메뉴 추가.
-- @database : mariadb
-- @author : nichefish
-- 사용자(MAIN) 하위에 Keep 섹션과 전수관리 화면(/keep) NO_SUB 자식을 추가한다. 일정 패턴과 동일.
-- 발생지: KeepPage(/keep) 전 범위 journal_todo 관리 화면.
INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'MAIN' AS upper_label )
SELECT M.id, 'SUB', 'Keep', NULL, '<span class="menu-icon"><i class="bi bi-bookmark-star fs-2"></i></span>', 7, 'system', 'LIST', 'KEEP', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL
WHERE NOT EXISTS (SELECT 1 FROM menu C WHERE C.menu_label = 'KEEP' AND C.deleted_at IS NULL);

INSERT INTO menu ( parent_menu_id, menu_type, menu_name, url, icon, sort_order, created_by, submenu_expand_type, menu_label, admin_yn, protected_yn, use_yn )
WITH T AS ( SELECT 'KEEP' AS upper_label )
SELECT M.id, 'SUB', 'Keep 전수관리', '/keep', NULL, 0, 'system', 'NO_SUB', 'KEEP_LIST', 'N', 'N', 'Y'
FROM T
INNER JOIN menu M ON M.menu_label = T.upper_label AND M.deleted_at IS NULL
WHERE NOT EXISTS (SELECT 1 FROM menu C WHERE C.menu_label = 'KEEP_LIST' AND C.deleted_at IS NULL);
