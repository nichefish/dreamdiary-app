/**
 * entrySearchQuery 유틸 단위 테스트.
 * 검색 조건 SSOT 인 URL query 의 파싱·API 파라미터·route query 조립 규칙을 고정한다.
 * 픽스처는 가상 태그 id/일반 명사 키워드만 사용한다 (개인 정보 금지 룰).
 */
import { describe, expect, it } from "vitest";
import {
  buildEntrySearchParams,
  buildEntrySearchRouteQuery,
  normalizeQueryList,
  parseEntrySearchQuery,
} from "./entrySearchQuery";

describe("normalizeQueryList", () => {
  it("단일 문자열을 1개짜리 배열로 만든다", () => {
    expect(normalizeQueryList("11")).toEqual(["11"]);
  });

  it("배열·콤마 구분 문자열이 섞여 있어도 평탄화한다", () => {
    expect(normalizeQueryList("11,22")).toEqual(["11", "22"]);
    expect(normalizeQueryList(["11", "22,33"])).toEqual(["11", "22", "33"]);
  });

  it("공백을 trim 하고 빈 항목은 버린다", () => {
    expect(normalizeQueryList(" 11 , ,22 ")).toEqual(["11", "22"]);
  });

  it("null/undefined 는 빈 배열이 된다", () => {
    expect(normalizeQueryList(undefined)).toEqual([]);
    expect(normalizeQueryList(null)).toEqual([]);
    expect(normalizeQueryList([null])).toEqual([]);
  });
});

describe("parseEntrySearchQuery", () => {
  it("빈 query 는 기본값 DIARY/desc/빈 목록", () => {
    expect(parseEntrySearchQuery({})).toEqual({
      type: "DIARY",
      sort: "desc",
      tagIds: [],
      searchKeywords: [],
    });
  });

  it("type 은 대문자화한다", () => {
    expect(parseEntrySearchQuery({ type: "dream" }).type).toBe("DREAM");
  });

  it("sort 는 asc(대소문자 무관)만 인정하고 그 외는 desc", () => {
    expect(parseEntrySearchQuery({ sort: "asc" }).sort).toBe("asc");
    expect(parseEntrySearchQuery({ sort: "ASC" }).sort).toBe("asc");
    expect(parseEntrySearchQuery({ sort: "descending" }).sort).toBe("desc");
    expect(parseEntrySearchQuery({ sort: "" }).sort).toBe("desc");
  });

  it("tagIds/searchKeywords 는 normalizeQueryList 규칙으로 파싱한다", () => {
    const cond = parseEntrySearchQuery({ tagIds: "1,2", searchKeywords: ["바다", "여행"] });
    expect(cond.tagIds).toEqual(["1", "2"]);
    expect(cond.searchKeywords).toEqual(["바다", "여행"]);
  });
});

describe("buildEntrySearchParams", () => {
  it("tagIds/searchKeywords 를 반복 파라미터로 직렬화한다 (Spring 배열 바인딩 계약)", () => {
    const params = buildEntrySearchParams({
      type: "DIARY",
      sort: "desc",
      tagIds: ["1", "2"],
      searchKeywords: ["sea"],
    });
    expect(params.toString()).toBe("type=DIARY&sort=desc&tagIds=1&tagIds=2&searchKeywords=sea");
  });

  it("한글 키워드도 값 그대로 보존한다 (인코딩은 직렬화 시점에만)", () => {
    const params = buildEntrySearchParams({
      type: "DREAM",
      sort: "asc",
      tagIds: [],
      searchKeywords: ["바다"],
    });
    expect(params.getAll("searchKeywords")).toEqual(["바다"]);
    expect(params.getAll("tagIds")).toEqual([]);
  });
});

describe("buildEntrySearchRouteQuery", () => {
  it("기본값(desc·빈 목록)은 생략하고 type 만 남긴다", () => {
    expect(buildEntrySearchRouteQuery({
      type: "DIARY",
      sort: "desc",
      tagIds: [],
      searchKeywords: [],
    })).toEqual({ type: "DIARY" });
  });

  it("asc 정렬·비어 있지 않은 목록만 query 에 포함한다", () => {
    expect(buildEntrySearchRouteQuery({
      type: "DREAM",
      sort: "asc",
      tagIds: ["1"],
      searchKeywords: ["바다", "여행"],
    })).toEqual({
      type: "DREAM",
      sort: "asc",
      tagIds: ["1"],
      searchKeywords: ["바다", "여행"],
    });
  });

  it("조립한 query 를 다시 파싱하면 같은 조건이 된다 (round-trip 불변식)", () => {
    const cond = { type: "DREAM", sort: "asc", tagIds: ["1", "2"], searchKeywords: ["여행"] };
    expect(parseEntrySearchQuery(buildEntrySearchRouteQuery(cond))).toEqual(cond);
  });
});
