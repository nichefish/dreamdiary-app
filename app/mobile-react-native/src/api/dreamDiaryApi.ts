import { request } from "./client";
import type { AjaxResponse, AuthUser } from "../types/auth";
import type { JournalDay, JournalEntry } from "../types/journalDay";
import { normalizeDateStr } from "../utils/date";

// ─── 인증 ─────────────────────────────────────────────

/** 현재 인증 사용자 정보 조회 (JWT 쿠키 기반) */
export function getAuthAccount() {
  return request<AjaxResponse<AuthUser>>("/api/auth/get-auth-account");
}

/** 로그인 — POST /api/auth/login (응답 Authorization 헤더에 access JWT) */
export function login(username: string, password: string) {
  return request<AjaxResponse<AuthUser>>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
    captureAccessToken: true
  });
}

/** access JWT 재발급 — POST /api/auth/refresh (쿠키 refresh 기반, WebSocket 토큰 갱신용) */
export function refreshAccessToken() {
  return request<AjaxResponse>("/api/auth/refresh", {
    method: "POST",
    body: JSON.stringify({}),
    captureAccessToken: true
  });
}

/** 로그아웃 — POST /api/auth/logout-json */
export function logout() {
  return request<AjaxResponse>("/api/auth/logout-json", {
    method: "POST",
    body: JSON.stringify({})
  });
}

// ─── 저널 일자 ─────────────────────────────────────────

interface DailyListResponse {
  rslt: boolean;
  rsltList?: JournalDay[];
}

/**
 * 특정 날짜의 저널 일자 조회.
 * GET /api/journal/days?viewType=DAILY&stdrdDt=YYYY-MM-DD
 */
export function getDailyJournalDay(dateStr: string) {
  const safeDate = normalizeDateStr(dateStr);
  if (!safeDate) throw new Error("유효하지 않은 날짜 형식입니다.");
  return request<DailyListResponse>("/api/journal/days", {
    query: { viewType: "DAILY", stdrdDt: safeDate }
  });
}

/**
 * 월별 저널 일자 목록 조회 (달력 도트 표시용).
 * GET /api/journal/days?viewType=MONTHLY&stdrdDt=YYYY-MM-01
 * @param yearMonth "YYYY-MM" 형식
 */
export function getMonthlyJournalDays(yearMonth: string) {
  if (!/^\d{4}-\d{2}$/.test(yearMonth)) {
    throw new Error("유효하지 않은 연월 형식입니다.");
  }
  const safeMonthStart = normalizeDateStr(`${yearMonth}-01`);
  if (!safeMonthStart) throw new Error("유효하지 않은 연월 형식입니다.");
  return request<DailyListResponse>("/api/journal/days", {
    query: { viewType: "MONTHLY", stdrdDt: safeMonthStart }
  });
}

/**
 * 주별 저널 일자 목록 조회.
 * GET /api/journal/days?viewType=WEEKLY&weekStartDt=YYYY-MM-DD
 * @param weekStartDt 주 시작일(월요일) YYYY-MM-DD
 */
export function getWeeklyJournalDays(weekStartDt: string) {
  const safeWeekStart = normalizeDateStr(weekStartDt);
  if (!safeWeekStart) throw new Error("유효하지 않은 주 시작일 형식입니다.");
  return request<DailyListResponse>("/api/journal/days", {
    query: { viewType: "WEEKLY", weekStartDt: safeWeekStart }
  });
}

/**
 * 일자 태그로 저널 일자 검색.
 * GET /api/journal/days?viewType=SEARCH&tagId=&yy=
 */
export function searchJournalDaysByTag(tagId: number, yy: number) {
  if (!Number.isFinite(tagId) || !Number.isFinite(yy)) {
    throw new Error("유효하지 않은 태그 또는 연도입니다.");
  }
  return request<DailyListResponse>("/api/journal/days", {
    query: { viewType: "SEARCH", tagId, yy }
  });
}

/**
 * 일자 태그가 붙은 연도 목록.
 * GET /api/journal/day/tag/{tagId}/years
 */
export function getJournalDayTagYears(tagId: number) {
  if (!Number.isFinite(tagId)) {
    throw new Error("유효하지 않은 태그 ID입니다.");
  }
  return request<{ rsltList?: Array<number | string> }>(`/api/journal/day/tag/${tagId}/years`);
}

// ─── 태그 클라우드 (월간) ────────────────────────────────

export interface TagCloudItem {
  id: number | string;
  name: string;
  ctgr?: string;
  contentSize: number;
}

interface TagListResponse {
  rslt: boolean;
  rsltList?: TagCloudItem[];
}

function normalizeTagList(rawList: unknown): TagCloudItem[] {
  if (!Array.isArray(rawList)) return [];

  function isTagCloudItem(item: TagCloudItem | null): item is TagCloudItem {
    return item != null;
  }

  return rawList
    .map((raw): TagCloudItem | null => {
      const item = raw as Record<string, unknown>;
      const id = item.id;
      const name = String(item.name ?? "");
      if (id === undefined || id === "" || !name) return null;
      return {
        id: id as number | string,
        name,
        ctgr: item.ctgr != null ? String(item.ctgr) : undefined,
        contentSize: Number(item.contentSize ?? 0)
      };
    })
    .filter(isTagCloudItem);
}

/**
 * 월간 일자 태그 목록.
 * GET /api/journal/day/tags?yy=&mnth=
 */
export async function getJournalDayTags(yy: number, mnth: number) {
  const res = await request<TagListResponse>("/api/journal/day/tags", {
    query: { yy, mnth }
  });
  return normalizeTagList(res.rsltList);
}

/**
 * 월간 일기/꿈 엔트리 태그 목록.
 * GET /api/journal/entry/tags?yy=&mnth=&type=DIARY|DREAM
 */
export async function getJournalEntryTags(yy: number, mnth: number, type: "DIARY" | "DREAM") {
  const res = await request<TagListResponse>("/api/journal/entry/tags", {
    query: { yy, mnth, type }
  });
  return normalizeTagList(res.rsltList);
}

// ─── 저널 엔트리 검색 ──────────────────────────────────

interface EntrySearchResponse {
  rslt: boolean;
  rsltList?: JournalEntry[];
}

export interface EntrySearchParams {
  keyword?: string;
  type?: "DREAM" | "DIARY";
  tagIds?: number[];
}

/**
 * 저널 엔트리 검색 (키워드 또는 tagIds, 둘 중 하나 이상 필요).
 * GET /api/journal/entries
 */
export function searchEntries(params: EntrySearchParams) {
  const keyword = params.keyword?.trim();
  const tagIds = params.tagIds?.filter((id) => Number.isFinite(id));
  if (!keyword && (!tagIds || tagIds.length === 0)) {
    throw new Error("검색 조건이 없습니다.");
  }
  return request<EntrySearchResponse>("/api/journal/entries", {
    query: {
      ...(keyword ? { searchKeywords: [keyword] } : {}),
      ...(tagIds && tagIds.length > 0 ? { tagIds } : {}),
      ...(params.type ? { type: params.type } : {})
    }
  });
}