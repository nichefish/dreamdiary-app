import { describe, expect, it } from "vitest";
import { filterTagifyAutoCompleteCandidates } from "./tagifyHelper";

const FIXTURE_CATEGORY_MAP = {
  Flyway: ["TOOL"],
  frontend: ["AREA"],
  Database: ["AREA"],
};

describe("filterTagifyAutoCompleteCandidates", () => {
  /** 소문자 입력은 대문자로 시작하는 기존 태그를 원래 표기로 반환한다. */
  it("matches an uppercase tag with lowercase input", () => {
    expect(filterTagifyAutoCompleteCandidates(FIXTURE_CATEGORY_MAP, "f")).toEqual([
      "Flyway",
      "frontend",
    ]);
  });

  /** 자동완성은 대소문자를 무시하되 기존 prefix 검색 범위를 유지한다. */
  it("keeps case-insensitive prefix matching", () => {
    expect(filterTagifyAutoCompleteCandidates(FIXTURE_CATEGORY_MAP, "FLY")).toEqual(["Flyway"]);
    expect(filterTagifyAutoCompleteCandidates(FIXTURE_CATEGORY_MAP, "way")).toEqual([]);
  });
});
