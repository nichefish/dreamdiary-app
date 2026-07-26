/**
 * threadMembershipPeriod.ts
 * 스레드 목록·상세에 표시하는 소속 엔트리 기준일 기간 라벨.
 */

/** 목록/상세 API 기준일을 YYYY-MM-DD 로 정규화한다. */
export function normalizeThreadEntryDate(value?: string | null): string {
  if (!value) return "";
  const trimmed = value.trim();
  return trimmed.length >= 10 ? trimmed.slice(0, 10) : trimmed;
}

export type ThreadMembershipPeriodSource = {
  firstEntryDate?: string | null;
  lastEntryDate?: string | null;
};

/**
 * 소속 엔트리 기준일 기간 라벨.
 * 같은 날이면 단일 일자, 범위면 rangeRange 결과. 유효 일자가 없으면 빈 문자열(숨김).
 *
 * @param thread firstEntryDate/lastEntryDate 를 가진 스레드 DTO
 * @param formatRange 범위 포맷터 — i18n 키 `journal.thread.list.membership-period` 의 `{0} ~ {1}` 치환
 */
export function formatThreadMembershipPeriod(
  thread: ThreadMembershipPeriodSource,
  formatRange: (first: string, last: string) => string,
): string {
  const first = normalizeThreadEntryDate(thread.firstEntryDate);
  const last = normalizeThreadEntryDate(thread.lastEntryDate);
  if (!first || !last) return "";
  if (first === last) return first;
  return formatRange(first, last);
}
