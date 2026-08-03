package io.nicheblog.dreamdiary.auth.permission;

/**
 * PermissionKey
 * <pre>
 *  원자 권한 키 상수. DB {@code permission.perm_key} 및 {@code menu.required_perm_key} 와 동일 문자열을 쓴다.
 *  Spring Security {@code GrantedAuthority} / {@code @PreAuthorize("hasAuthority('…')")} 에도 같은 키를 올린다.
 * </pre>
 *
 * @author nichefish
 */
public final class PermissionKey {

    private PermissionKey() {
        // utility
    }

    public static final String MENU_VIEW_USER = "menu.view.user";
    public static final String MENU_VIEW_ADMIN = "menu.view.admin";

    public static final String MENU_ADMIN_USER_ACCOUNT = "menu.admin.user_account";
    public static final String MENU_ADMIN_AUTH_POLICY = "menu.admin.auth_policy";
    public static final String MENU_ADMIN_USER_GROUP = "menu.admin.user_group";
    public static final String MENU_ADMIN_MENU = "menu.admin.menu";
    public static final String MENU_ADMIN_CODE = "menu.admin.code";
    public static final String MENU_ADMIN_PAGE = "menu.admin.page";
    public static final String MENU_ADMIN_BOARD = "menu.admin.board";
    public static final String MENU_ADMIN_LOG = "menu.admin.log";
    public static final String MENU_ADMIN_LOG_STATS = "menu.admin.log_stats";
}
