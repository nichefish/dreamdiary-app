import { describe, expect, it } from "vitest";
import { getVacationBadge, getVacationReasonText, isJournalDayOff } from "./journalVacation";

describe("저널 일자 휴가 표시 계약", () => {
  it("공휴일·주말 또는 전일 휴가만 날짜를 비근무일 색상으로 표시한다", () => {
    expect(isJournalDayOff({ isHolyday: true, vacationDayStatus: "NONE" })).toBe(true);
    expect(isJournalDayOff({ isHolyday: false, vacationDayStatus: "FULL_DAY" })).toBe(true);
    expect(isJournalDayOff({ isHolyday: false, vacationDayStatus: "AM_HALF" })).toBe(false);
    expect(isJournalDayOff({ isHolyday: false, vacationDayStatus: "PM_HALF" })).toBe(false);
    expect(isJournalDayOff({ isHolyday: false, vacationDayStatus: "UNKNOWN" })).toBe(false);
  });

  it("전일·오전·오후·미확정 상태를 서로 다른 배지 계약으로 노출한다", () => {
    expect(getVacationBadge("FULL_DAY")?.labelKey).toBe("journal.day.vacation.full-day");
    expect(getVacationBadge("AM_HALF")?.labelKey).toBe("journal.day.vacation.am-half");
    expect(getVacationBadge("PM_HALF")?.labelKey).toBe("journal.day.vacation.pm-half");
    expect(getVacationBadge("UNKNOWN")?.labelKey).toBe("journal.day.vacation.unknown");
    expect(getVacationBadge("NONE")).toBeNull();
  });

  it("서버가 전달한 사유 순서를 유지해 표시 문자열을 만든다", () => {
    expect(getVacationReasonText(["개인 일정", "가족 일정"])).toBe("개인 일정 · 가족 일정");
    expect(getVacationReasonText()).toBe("");
  });
});
