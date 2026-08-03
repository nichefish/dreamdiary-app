import { describe, expect, it } from "vitest";
import { resolveJournalPrefixField } from "./journalPrefixField";

describe("저널 Prefix 필드 표시 계약", () => {
  it("선택지와 과거 선택이 없으면 빈 Prefix 필드를 숨긴다", () => {
    expect(resolveJournalPrefixField([])).toEqual({
      visible: false,
      inactivePrefix: null,
    });
  });

  it("활성 선택지가 있으면 Prefix 필드를 표시한다", () => {
    expect(resolveJournalPrefixField([{ id: 11, activeYn: "Y" }])).toEqual({
      visible: true,
      inactivePrefix: null,
    });
  });

  it("활성 목록에서 빠진 비활성 과거 선택은 별도 옵션으로 유지한다", () => {
    const inactivePrefix = { id: 12, name: "가상 말머리", activeYn: "N" };

    expect(resolveJournalPrefixField([], inactivePrefix)).toEqual({
      visible: true,
      inactivePrefix,
    });
  });

  it("비활성 과거 선택이 이미 옵션에 있으면 중복 주입하지 않는다", () => {
    const inactivePrefix = { id: 12, name: "가상 말머리", activeYn: "N" };

    expect(resolveJournalPrefixField([{ id: 12, activeYn: "N" }], inactivePrefix)).toEqual({
      visible: true,
      inactivePrefix: null,
    });
  });

  it("활성 과거 선택만 있고 현재 선택지가 없으면 필드를 표시하지 않는다", () => {
    expect(resolveJournalPrefixField([], { id: 13, activeYn: "Y" })).toEqual({
      visible: false,
      inactivePrefix: null,
    });
  });

  it("시스템 요약이나 조회 실패 문맥은 선택지가 없어도 필드를 표시한다", () => {
    expect(resolveJournalPrefixField([], null, true)).toEqual({
      visible: true,
      inactivePrefix: null,
    });
  });
});
