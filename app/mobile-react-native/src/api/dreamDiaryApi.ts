import { request } from "./client";
import type { AjaxResponse, AuthUser } from "../types/auth";
import type { JournalChapter, JournalDay, JournalEntry } from "../types/journalDay";
import type { CaptureMode } from "../types/journal";
import type { JournalInterpretation } from "../types/interpretation";
import type { ChatSession, ChatMessage } from "../types/chat";
import { normalizeDateStr, toDateStr } from "../utils/date";

/** YYYY-MM-DD 형식의 오늘 날짜 문자열 */
function todayStr(): string {
  return toDateStr(new Date());
}

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

/**
 * 저널 일자 등록.
 * POST /api/journal/days (multipart/form-data)
 */
export function createJournalDay(dateStr: string) {
  const safeDate = normalizeDateStr(dateStr);
  if (!safeDate) throw new Error("유효하지 않은 날짜 형식입니다.");
  const form = new FormData();
  form.append("journalDate", safeDate);
  form.append("journalDatePrecision", "EXACT");
  form.append("diaryResolvedYn", "N");
  return request<AjaxResponse<JournalDay>>("/api/journal/days", {
    method: "POST",
    body: form
  });
}

// ─── 저널 챕터 ─────────────────────────────────────────

interface ChapterListResponse {
  rslt: boolean;
  rsltList?: JournalChapter[];
}

/**
 * 일자 기준 챕터 목록 조회.
 * GET /api/journal/chapters?journalDayId={id}
 */
export function getChapters(journalDayId: number) {
  return request<ChapterListResponse>("/api/journal/chapters", {
    query: { journalDayId }
  });
}

/**
 * 기본 챕터 등록.
 * POST /api/journal/chapters (multipart/form-data)
 */
export function createChapter(journalDayId: number, title = "모바일") {
  const form = new FormData();
  form.append("journalDayId", String(journalDayId));
  form.append("title", title);
  return request<AjaxResponse<JournalChapter>>("/api/journal/chapters", {
    method: "POST",
    body: form
  });
}

/**
 * 꿈 챕터 자동 생성/조회 (이미 있으면 기존 반환).
 * POST /api/journal/chapters/dream-auto?journalDayId={id}
 */
export function createDreamAutoChapter(journalDayId: number) {
  return request<AjaxResponse<JournalChapter>>("/api/journal/chapters/dream-auto", {
    method: "POST",
    query: { journalDayId }
  });
}

// ─── 저널 엔트리 ───────────────────────────────────────

interface SaveEntryParams {
  contentType: string;
  journalDayId: number;
  journalChapterId: number;
  content: string;
  title?: string;
}

/**
 * 저널 엔트리 저장.
 * POST /api/journal/entries (multipart/form-data)
 */
export function saveEntry(params: SaveEntryParams) {
  const form = new FormData();
  form.append("contentType", params.contentType);
  form.append("journalDayId", String(params.journalDayId));
  form.append("journalChapterId", String(params.journalChapterId));
  form.append("content", params.content);
  if (params.title) form.append("title", params.title);
  return request<AjaxResponse>("/api/journal/entries", {
    method: "POST",
    body: form
  });
}

/**
 * 저널 엔트리 수정.
 * PUT /api/journal/entries/{id} (multipart/form-data)
 */
export function updateEntry(id: number, params: { content: string; title?: string }) {
  const form = new FormData();
  form.append("content", params.content);
  if (params.title !== undefined) form.append("title", params.title);
  return request<AjaxResponse>(`/api/journal/entries/${id}`, {
    method: "PUT",
    body: form
  });
}

/**
 * 저널 엔트리 삭제.
 * DELETE /api/journal/entries/{id}
 */
export function deleteEntry(id: number) {
  return request<AjaxResponse>(`/api/journal/entries/${id}`, {
    method: "DELETE"
  });
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

// ─── 저널 해석 ─────────────────────────────────────────

interface InterpretationListResponse {
  rslt: boolean;
  rsltList?: JournalInterpretation[];
}

/**
 * 특정 엔트리의 꿈 해석 목록을 조회한다.
 * GET /api/journal/interpretations?refId={entryId}&refContentType=JOURNAL_DREAM
 * @param entryId JournalEntry.id
 */
export function getInterpretations(entryId: number) {
  return request<InterpretationListResponse>("/api/journal/interpretations", {
    query: { refId: entryId, refContentType: "JOURNAL_DREAM" }
  });
}

/**
 * 꿈 해석을 등록한다.
 * POST /api/journal/interpretations (multipart/form-data)
 * @param entryId JournalEntry.id
 * @param content 해석 본문
 */
export function createInterpretation(entryId: number, content: string) {
  const form = new FormData();
  form.append("refId", String(entryId));
  form.append("refContentType", "JOURNAL_DREAM");
  form.append("content", content);
  return request<{ rslt: boolean; message?: string }>("/api/journal/interpretations", {
    method: "POST",
    body: form
  });
}

/**
 * 꿈 해석을 삭제한다.
 * DELETE /api/journal/interpretation/{id}
 * @param id JournalInterpretation.id
 */
export function deleteInterpretation(id: number) {
  return request<{ rslt: boolean; message?: string }>(`/api/journal/interpretation/${id}`, {
    method: "DELETE"
  });
}
// ─── 모바일 캡처 파사드 ────────────────────────────────

/**
 * 날짜 지정 빠른 입력 파사드 (공통 내부 구현).
 * JournalDay 조회/생성 → 챕터 조회/생성 → Entry 저장.
 * @param date YYYY-MM-DD 형식 날짜 문자열
 */
export async function captureEntryForDate(mode: CaptureMode, content: string, date: string): Promise<void> {
  const safeDate = normalizeDateStr(date);
  if (!safeDate) throw new Error("유효하지 않은 날짜 형식입니다.");

  // 1. 일자 조회 또는 생성
  let dayId: number;
  const dayRes = await getDailyJournalDay(safeDate);
  if (dayRes.rsltList && dayRes.rsltList.length > 0) {
    dayId = dayRes.rsltList[0].id;
  } else {
    const created = await createJournalDay(safeDate);
    if (!created.rslt || !created.rsltObj?.id) throw new Error("저널 일자 생성에 실패했습니다.");
    dayId = created.rsltObj.id;
  }

  // 2. 챕터 조회 또는 생성
  let chapterId: number;
  if (mode === "dream") {
    // 꿈 챕터는 dream-auto 로 처리 (없으면 자동 생성)
    const dreamChapter = await createDreamAutoChapter(dayId);
    if (!dreamChapter.rslt || !dreamChapter.rsltObj?.id) throw new Error("꿈 챕터 생성에 실패했습니다.");
    chapterId = dreamChapter.rsltObj.id;
  } else {
    // 일기/감정: 비DREAM 챕터 중 첫 번째 사용 또는 새로 생성
    const chaptersRes = await getChapters(dayId);
    const diaryChapter = chaptersRes.rsltList?.find(c => c.contentType !== "JOURNAL_DREAM");
    if (diaryChapter) {
      chapterId = diaryChapter.id;
    } else {
      const newChapter = await createChapter(dayId, "모바일");
      if (!newChapter.rslt || !newChapter.rsltObj?.id) throw new Error("챕터 생성에 실패했습니다.");
      chapterId = newChapter.rsltObj.id;
    }
  }

  // 3. 엔트리 저장
  const contentType = mode === "dream" ? "JOURNAL_DREAM" : "JOURNAL_DIARY";
  const result = await saveEntry({ contentType, journalDayId: dayId, journalChapterId: chapterId, content });
  if (!result.rslt) throw new Error(result.message ?? "저장에 실패했습니다.");
}

/**
 * 오늘 날짜 빠른 입력 파사드.
 * captureEntryForDate 에 오늘 날짜를 전달하는 편의 래퍼.
 */
export function captureEntry(mode: CaptureMode, content: string): Promise<void> {
  return captureEntryForDate(mode, content, todayStr());
}
// ─── AI 채팅 세션/메시지 (REST) ────────────────────────────

/**
 * 내 채팅 세션 목록을 조회한다.
 * GET /chat/sessions
 */
export function getChatSessions() {
  return request<{ rslt: boolean; rsltList?: ChatSession[] }>("/chat/sessions");
}

/**
 * 새 채팅 세션을 생성한다.
 * POST /chat/sessions (JSON)
 * @param title 세션 제목 (생략 시 서버 기본값)
 */
export function createChatSession(title?: string) {
  return request<{ rslt: boolean; rsltObj?: ChatSession }>("/chat/sessions", {
    method: "POST",
    body: JSON.stringify(title ? { title } : {})
  });
}

/**
 * 채팅 세션을 삭제한다.
 * DELETE /chat/sessions/{id}
 * @param sessionId 삭제할 세션 ID
 */
export function deleteChatSession(sessionId: number) {
  return request<{ rslt: boolean }>(`/chat/sessions/${sessionId}`, { method: "DELETE" });
}

/**
 * 세션의 메시지 목록을 조회한다.
 * GET /chat/sessions/{id}/messages
 * @param sessionId 조회할 세션 ID
 */
export function getChatMessages(sessionId: number) {
  return request<{ rslt: boolean; rsltList?: ChatMessage[] }>(`/chat/sessions/${sessionId}/messages`);
}