/**
 * urlMapping.ts
 * <p>
 * 백엔드 FreeMarker 페이지 URL → Vue Router 경로 매핑 테이블.
 * 페이지가 Vue SPA로 전환될 때마다 항목을 추가한다.
 * 매핑이 없는 URL은 원본 그대로 반환한다.
 * </p>
 */

/** FreeMarker URL → Vue SPA 경로 매핑 */
const URL_MAP: Record<string, string> = {
  // 저널
  "/app/journal/day/monthly.do": "/journal",
  "/app/journal/day/weekly.do": "/journal/weekly",
  "/app/journal/day/cal.do": "/journal/calendar",
  "/app/journal/day/meta.do": "/journal/meta",
  "/app/schedule/cal.do": "/schedule",
  "/app/admin/admin-page.do": "/admin",
  "/app/auth/policy/page.do": "/admin/auth-policy",
  "/app/admin/menu/page.do": "/admin/menu",
  "/app/admin/board/page.do": "/admin/board-group",
  "/app/admin/code/page.do": "/admin/code",
  "/app/user/list.do": "/admin/users",
  "/app/user/regist-form.do": "/admin/users",
  "/app/user/detail.do": "/admin/users",
  "/app/user/modify-form.do": "/admin/users",
  "/app/log/list.do": "/admin/log",
  "/app/log/stats/list.do": "/admin/log/stats-user",
  "/app/user/my/page.do": "/my",
  "/app/error/error-page.do": "/error",
  "/app/error/not-found.do": "/404",
  "/app/error/access-denied.do": "/403",
};

/**
 * 백엔드 URL을 Vue Router 경로로 변환한다.
 * 매핑이 없으면 원본 URL을 반환한다.
 *
 * @param url 백엔드 메뉴 URL
 * @returns Vue Router 경로 또는 원본 URL
 */
export function toVuePath(url: string | undefined): string {
  if (!url) return "/";
  return URL_MAP[url] ?? url;
}
