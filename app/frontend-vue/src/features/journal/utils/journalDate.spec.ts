/**
 * journalDate 유틸 단위 테스트.
 * 요일/주 시작일 계산은 로컬 타임존 정오(T12:00:00) 보정 기준이므로 날짜 리터럴만으로 결정적이다.
 * "오늘" 폴백 분기는 fake timer 로 시스템 시각을 고정해 검증한다.
 * 고정 사실: 2026-07-06(월) ~ 2026-07-12(일) 주간, 2026-03-01 은 일요일.
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  formatLocalDateStr,
  getWeekDayStr,
  getWeekStartDateStr,
  resolveWeekStartDt,
} from "./journalDate";

/** i18n t 대역 — 카탈로그 키를 그대로 반환해 키 선택 로직만 검증한다. */
const fakeT = (key: string): string => key;

describe("getWeekDayStr", () => {
  it("YYYY-MM-DD 를 요일 카탈로그 키로 변환한다", () => {
    expect(getWeekDayStr("2026-07-06", fakeT)).toBe("journal.weekday.mon");
    expect(getWeekDayStr("2026-07-09", fakeT)).toBe("journal.weekday.thu");
    expect(getWeekDayStr("2026-07-12", fakeT)).toBe("journal.weekday.sun");
  });

  it("T 포함 일시 문자열은 그대로 파싱한다", () => {
    expect(getWeekDayStr("2026-07-10T09:30:00", fakeT)).toBe("journal.weekday.fri");
  });

  it("stdrdDt 가 없거나 공백이면 빈 문자열을 반환한다", () => {
    expect(getWeekDayStr(null, fakeT)).toBe("");
    expect(getWeekDayStr(undefined, fakeT)).toBe("");
    expect(getWeekDayStr("", fakeT)).toBe("");
    expect(getWeekDayStr("   ", fakeT)).toBe("");
  });
});

describe("formatLocalDateStr", () => {
  it("Date 를 YYYY-MM-DD 로 변환하고 월/일을 0 패딩한다", () => {
    expect(formatLocalDateStr(new Date(2026, 0, 5))).toBe("2026-01-05");
    expect(formatLocalDateStr(new Date(2026, 11, 31))).toBe("2026-12-31");
  });
});

describe("getWeekStartDateStr", () => {
  it("주중 날짜는 그 주 월요일을 반환한다 (백엔드 DateUtils 와 동일: 월요일 시작)", () => {
    expect(getWeekStartDateStr("2026-07-06")).toBe("2026-07-06"); // 월요일은 자기 자신
    expect(getWeekStartDateStr("2026-07-08")).toBe("2026-07-06"); // 수요일
    expect(getWeekStartDateStr("2026-07-12")).toBe("2026-07-06"); // 일요일은 지난 월요일로
  });

  it("월 경계를 넘어 이전 달 월요일을 반환할 수 있다", () => {
    expect(getWeekStartDateStr("2026-07-01")).toBe("2026-06-29");
  });

  describe("stdrdDt 미지정 시 오늘 기준", () => {
    beforeEach(() => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date(2026, 6, 9, 12, 0, 0)); // 2026-07-09(목) 정오
    });
    afterEach(() => {
      vi.useRealTimers();
    });

    it("인자가 없으면 오늘이 속한 주의 월요일을 반환한다", () => {
      expect(getWeekStartDateStr()).toBe("2026-07-06");
      expect(getWeekStartDateStr(null)).toBe("2026-07-06");
      expect(getWeekStartDateStr("  ")).toBe("2026-07-06");
    });
  });
});

describe("resolveWeekStartDt", () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 6, 9, 12, 0, 0)); // 2026-07-09(목) 정오
  });
  afterEach(() => {
    vi.useRealTimers();
  });

  it("stdrdDt 가 있으면 yy/mnth 보다 우선한다", () => {
    expect(resolveWeekStartDt({ stdrdDt: "2026-03-05", yy: 2030, mnth: 12 })).toBe("2026-03-02");
  });

  it("yy/mnth 가 현재 년-월이면 오늘 기준 주 시작일을 반환한다", () => {
    expect(resolveWeekStartDt({ yy: 2026, mnth: 7 })).toBe("2026-07-06");
  });

  it("yy/mnth 가 다른 달이면 그 달 1일 기준 주 시작일을 반환한다", () => {
    // 2026-03-01 은 일요일 → 주 시작일은 전달 2026-02-23(월)
    expect(resolveWeekStartDt({ yy: 2026, mnth: 3 })).toBe("2026-02-23");
  });

  it("stdrdDt/yy/mnth 모두 없으면 오늘 기준 주 시작일을 반환한다", () => {
    expect(resolveWeekStartDt({})).toBe("2026-07-06");
    expect(resolveWeekStartDt({ yy: 2026 })).toBe("2026-07-06"); // mnth 누락 시에도 오늘 폴백
  });
});
