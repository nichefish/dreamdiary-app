import type { JournalDayDto, VacationDayStatus } from "@/features/journal/stores/journal";

/** 휴가 상태 배지의 locale key와 Bootstrap 색상 계약. */
export interface VacationBadgePresentation {
  labelKey: string;
  className: string;
}

/**
 * 날짜를 전일 비근무일 색상으로 표시할지 판정한다.
 * 반차와 미확정 휴가는 날짜 전체를 빨갛게 만들지 않는다.
 */
export function isJournalDayOff(day: Pick<JournalDayDto, "isHolyday" | "vacationDayStatus">): boolean {
  return day.isHolyday === true || day.vacationDayStatus === "FULL_DAY";
}

/** 서버가 확정한 휴가 상태를 표시 계약으로 변환한다. */
export function getVacationBadge(status?: VacationDayStatus): VacationBadgePresentation | null {
  switch (status) {
    case "FULL_DAY":
      return { labelKey: "journal.day.vacation.full-day", className: "badge-light-danger text-danger" };
    case "AM_HALF":
      return { labelKey: "journal.day.vacation.am-half", className: "badge-light-info text-info" };
    case "PM_HALF":
      return { labelKey: "journal.day.vacation.pm-half", className: "badge-light-primary text-primary" };
    case "UNKNOWN":
      return { labelKey: "journal.day.vacation.unknown", className: "badge-light-warning text-warning" };
    case "NONE":
    case undefined:
      return null;
  }
}

/** 백엔드가 중복 제거한 휴가 일정 제목을 표시 문자열로 연결한다. */
export function getVacationReasonText(reasonList?: string[]): string {
  return reasonList?.join(" · ") ?? "";
}
