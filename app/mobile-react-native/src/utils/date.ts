/**
 * 날짜 문자열 유틸.
 * JS Date의 `new Date("YYYY-MM-DD")` UTC 파싱 차이를 피하기 위해
 * 날짜 전용 문자열은 숫자 파싱 후 로컬 Date로 처리한다.
 */

export function toDateStr(d: Date): string {
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}-${mm}-${dd}`;
}

export function parseDateOnly(dateStr: string): Date {
  const [y, m, d] = dateStr.split("-").map(Number);
  return new Date(y, (m || 1) - 1, d || 1);
}

export function normalizeDateStr(dateStr: string): string | null {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return null;
  const date = parseDateOnly(dateStr);
  const normalized = toDateStr(date);
  return normalized === dateStr ? normalized : null;
}

export function addDays(dateStr: string, delta: number): string {
  const safeDate = normalizeDateStr(dateStr);
  const date = parseDateOnly(safeDate ?? toDateStr(new Date()));
  date.setDate(date.getDate() + delta);
  return toDateStr(date);
}

export function formatDateDots(dateStr: string): string {
  return dateStr.replace(/-/g, ".");
}

/** 오늘 날짜(YYYY-MM-DD)와 동일한지 비교 */
export function isToday(dateStr: string): boolean {
  return dateStr === toDateStr(new Date());
}

/**
 * 유효한 과거·오늘 날짜로 수렴. 미래·비정상 형식은 today.
 */
export function clampDateToToday(dateStr: string): string {
  const today = toDateStr(new Date());
  const normalized = normalizeDateStr(dateStr);
  if (!normalized) return today;
  return normalized > today ? today : normalized;
}

