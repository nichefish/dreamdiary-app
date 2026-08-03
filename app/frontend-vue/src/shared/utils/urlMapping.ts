/**
 * urlMapping.ts
 * <p>
 * 백엔드 FreeMarker 페이지 URL → Vue Router 경로 매핑 테이블.
 * 페이지가 Vue SPA로 전환될 때마다 항목을 추가한다.
 * 매핑이 없는 URL은 원본 그대로 반환한다.
 * </p>
 */
import { normalizeRoutePath, stripVueAppPrefix } from "@/shared/utils/appPath";

/** FreeMarker URL → Vue SPA 경로 매핑 */
const URL_MAP: Record<string, string> = {
  // 저널
  "/app/journal/day/monthly.do": "/journal/monthly",
  "/app/journal/day/daily.do": "/journal/daily",
  "/app/journal/day/weekly.do": "/journal/weekly",
  "/app/journal/day/cal.do": "/journal/calendar",
  "/app/journal/day/meta.do": "/journal/meta",
  "/app/journal/thread/list.do": "/thread",
  "/app/journal/annual/list.do": "/annual",
  "/app/schedule/calendar.do": "/schedule",
  "/app/schedule/cal.do": "/schedule",
  "/app/admin/admin-page.do": "/admin",
  "/app/auth/policy/page.do": "/admin/auth-policy",
  "/app/user/group/page.do": "/admin/user-groups",
  "/app/admin/menu/page.do": "/admin/menu",
  "/app/admin/board/page.do": "/admin/board-group",
  "/app/admin/code/page.do": "/admin/code",
  "/app/user/list.do": "/admin/users",
  "/app/user/regist-form.do": "/admin/users",
  "/app/user/detail.do": "/admin/users",
  "/app/user/modify-form.do": "/admin/users",
  /* 계정 신청 승인은 계정 관리의 탭으로 흡수됐다 */
  "/app/user/signup/list.do": "/admin/users?tab=signup",
  "/app/log/list.do": "/admin/log",
  "/app/log/stats/list.do": "/admin/log/stats-user",
  "/app/user/my/page.do": "/my",
  "/app/error/error-page.do": "/error",
  "/app/error/not-found.do": "/404",
  "/app/error/access-denied.do": "/403",
};

const mapBoardPostList = (searchParams: URLSearchParams): string | null => {
  const boardKey = searchParams.get("contentType") ?? searchParams.get("boardKey");
  return boardKey ? `/board/${encodeURIComponent(boardKey)}` : null;
};

const mapJournalAnnualDetail = (pathname: string): string | null => {
  const match = pathname.match(/^\/app\/journal\/annual\/(\d+)\.do$/);
  return match ? `/annual/${match[1]}` : null;
};

const mapJournalThreadPath = (pathname: string, searchParams: URLSearchParams): string | null => {
  if (pathname === "/app/journal/thread/regist-form.do") return "/thread/new";

  const id = searchParams.get("id");
  if (!id) return null;

  if (pathname === "/app/journal/thread/detail.do") {
    return `/thread/${encodeURIComponent(id)}`;
  }

  if (pathname === "/app/journal/thread/modify-form.do") {
    return `/thread/${encodeURIComponent(id)}/edit`;
  }

  return null;
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
  const parsedUrl = new URL(url, window.location.origin);
  const mappedBoardUrl =
    parsedUrl.pathname === "/app/board/post/list.do"
      ? mapBoardPostList(parsedUrl.searchParams)
      : null;
  if (mappedBoardUrl) return normalizeRoutePath(stripVueAppPrefix(mappedBoardUrl));

  const mappedAnnualDetailUrl = mapJournalAnnualDetail(parsedUrl.pathname);
  if (mappedAnnualDetailUrl) return normalizeRoutePath(stripVueAppPrefix(mappedAnnualDetailUrl));

  const mappedThreadUrl = mapJournalThreadPath(parsedUrl.pathname, parsedUrl.searchParams);
  if (mappedThreadUrl) return normalizeRoutePath(stripVueAppPrefix(mappedThreadUrl));

  const mappedPath = URL_MAP[parsedUrl.pathname];
  const resolved = mappedPath
    ?? (url.startsWith("/") ? `${parsedUrl.pathname}${parsedUrl.search}${parsedUrl.hash}` : url);
  return normalizeRoutePath(stripVueAppPrefix(resolved));
}
