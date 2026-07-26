/**
 * i18nCatalogService — catalog 조회 시 빈 문자열 번역 보존.
 */
import { describe, expect, it } from "vitest";
import i18nCatalogService from "@/shared/utils/i18nCatalogService";

describe("i18nCatalogService.t", () => {
  it("키가 없으면 key 문자열을 반환한다", () => {
    expect(i18nCatalogService.t({}, "missing.key")).toBe("missing.key");
  });

  it("빈 문자열 번역을 유효한 값으로 보존한다", () => {
    const catalog = { "date.suffix.after-month-number": "" };
    expect(i18nCatalogService.t(catalog, "date.suffix.after-month-number")).toBe("");
  });

  it("일반 문자열 번역을 반환한다", () => {
    const catalog = { "date.suffix.after-month-number": "월" };
    expect(i18nCatalogService.t(catalog, "date.suffix.after-month-number")).toBe("월");
  });
});
