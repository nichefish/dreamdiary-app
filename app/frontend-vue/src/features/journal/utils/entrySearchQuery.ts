/**
 * 저널 기록 검색(JournalEntrySearchPage)의 URL query 파싱/조립 유틸.
 * 검색 조건의 SSOT 는 URL query 이며, route 파싱(syncFromRoute)·API 파라미터(loadEntries/exportTxt)·
 * URL 갱신(pushQuery)이 모두 이 모듈의 동일한 규칙을 공유한다.
 * (JournalEntrySearchPage.vue 인라인 로직에서 추출 — 동작 동일, 위치만 이동.)
 */

/** 검색 조건 묶음. sort 는 "asc" | "desc" 소문자만 유효값으로 본다. */
export interface EntrySearchCondition {
  type: string;
  sort: string;
  tagIds: string[];
  searchKeywords: string[];
}

/** route query 값(단일값/배열/콤마 구분 문자열 혼재)을 trim 된 문자열 배열로 정규화한다. */
export function normalizeQueryList(value: unknown): string[] {
  const rawList = Array.isArray(value) ? value : [value];
  return rawList
    .flatMap((item) => String(item ?? "").split(","))
    .map((item) => item.trim())
    .filter(Boolean);
}

/**
 * route query → 검색 조건.
 * type 은 기본 DIARY 에 대문자화, sort 는 "asc"(대소문자 무관)만 인정하고 그 외는 desc.
 */
export function parseEntrySearchQuery(query: Record<string, unknown>): EntrySearchCondition {
  return {
    type: String(query.type ?? "DIARY").toUpperCase(),
    sort: String(query.sort ?? "desc").toLowerCase() === "asc" ? "asc" : "desc",
    tagIds: normalizeQueryList(query.tagIds),
    searchKeywords: normalizeQueryList(query.searchKeywords),
  };
}

/**
 * 검색 조건 → API 조회/TXT 내보내기용 URLSearchParams.
 * tagIds/searchKeywords 는 반복 파라미터(tagIds=1&tagIds=2)로 직렬화한다 — Spring 배열 바인딩 계약.
 */
export function buildEntrySearchParams(cond: EntrySearchCondition): URLSearchParams {
  const params = new URLSearchParams();
  params.set("type", cond.type);
  params.set("sort", cond.sort);
  cond.tagIds.forEach((tagId) => params.append("tagIds", tagId));
  cond.searchKeywords.forEach((kw) => params.append("searchKeywords", kw));
  return params;
}

/**
 * 검색 조건 → router.replace 용 query 객체.
 * 기본값은 생략해 URL 을 최소로 유지한다: sort 는 asc 일 때만, 목록은 비어 있지 않을 때만 포함.
 */
export function buildEntrySearchRouteQuery(cond: EntrySearchCondition): Record<string, string | string[]> {
  const query: Record<string, string | string[]> = { type: cond.type };
  if (cond.sort === "asc") query.sort = "asc";
  if (cond.tagIds.length > 0) query.tagIds = cond.tagIds;
  if (cond.searchKeywords.length > 0) query.searchKeywords = cond.searchKeywords;
  return query;
}
