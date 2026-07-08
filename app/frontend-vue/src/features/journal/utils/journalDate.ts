/**
 * 저널 날짜 유틸.
 * 주간 뷰용 (백엔드 DateUtils.getWeekStartDateStr 와 동일: 월요일 시작) +
 * 클립보드 복사 등에서 사용하는 요일 문자열 변환.
 */

/** YYYY-MM-DD → 요일 한자(일/월/화/수/목/금/토). stdrdDt 가 없으면 빈 문자열.
 * 변경 후: 기존 한국어 한자 표기를 보존하면서 현재 locale의 공용 요일 카탈로그를 사용한다. */
export function getWeekDayStr(stdrdDt: string | null | undefined, t: (key: string) => string): string {
  if (!stdrdDt?.trim()) return "";
  const days = ["sun", "mon", "tue", "wed", "thu", "fri", "sat"] as const;
  const d = new Date(stdrdDt.includes("T") ? stdrdDt : `${stdrdDt}T12:00:00`);
  const day = days[d.getDay()];
  return day ? t(`journal.weekday.${day}`) : "";
}

/** Date → YYYY-MM-DD (로컬) */
export function formatLocalDateStr(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

/**
 * 기준일이 속한 주의 월요일(YYYY-MM-DD).
 * @param stdrdDt YYYY-MM-DD 등; 없으면 오늘
 */
export function getWeekStartDateStr(stdrdDt?: string | null): string {
  const base = stdrdDt?.trim()
    ? new Date(stdrdDt.includes("T") ? stdrdDt : `${stdrdDt}T12:00:00`)
    : new Date();
  const d = new Date(base);
  const dow = d.getDay();
  const offset = dow === 0 ? -6 : 1 - dow;
  d.setDate(d.getDate() + offset);
  return formatLocalDateStr(d);
}

/**
 * 주간 조회용 weekStartDt. 백엔드는 weekStartDt·stdrdDt 가 모두 비면 빈 목록을 반환한다.
 */
export function resolveWeekStartDt(opts: {
  stdrdDt?: string | null;
  yy?: number;
  mnth?: number;
}): string {
  if (opts.stdrdDt?.trim()) {
    return getWeekStartDateStr(opts.stdrdDt);
  }
  const now = new Date();
  if (opts.yy != null && opts.mnth != null) {
    if (opts.yy === now.getFullYear() && opts.mnth === now.getMonth() + 1) {
      return getWeekStartDateStr(formatLocalDateStr(now));
    }
    const mm = String(opts.mnth).padStart(2, "0");
    return getWeekStartDateStr(`${opts.yy}-${mm}-01`);
  }
  return getWeekStartDateStr(formatLocalDateStr(now));
}
