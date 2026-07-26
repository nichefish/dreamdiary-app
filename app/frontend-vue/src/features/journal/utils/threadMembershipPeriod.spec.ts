import { describe, expect, it } from "vitest";
import {
  formatThreadMembershipPeriod,
  normalizeThreadEntryDate,
} from "./threadMembershipPeriod";

describe("스레드 소속 기간 라벨", () => {
  it("YYYY-MM-DD 접두만 남긴다", () => {
    expect(normalizeThreadEntryDate("2026-07-20T00:00:00")).toBe("2026-07-20");
    expect(normalizeThreadEntryDate("  ")).toBe("");
  });

  it("같은 날은 단일 일자, 범위는 formatRange를 쓴다", () => {
    const formatRange = (a: string, b: string) => `${a} ~ ${b}`;
    expect(formatThreadMembershipPeriod(
      { firstEntryDate: "2026-03-10", lastEntryDate: "2026-03-10" },
      formatRange,
    )).toBe("2026-03-10");
    expect(formatThreadMembershipPeriod(
      { firstEntryDate: "2026-01-05", lastEntryDate: "2026-07-20" },
      formatRange,
    )).toBe("2026-01-05 ~ 2026-07-20");
  });

  it("유효 일자가 없으면 빈 문자열", () => {
    expect(formatThreadMembershipPeriod({}, (a, b) => `${a}~${b}`)).toBe("");
  });
});
