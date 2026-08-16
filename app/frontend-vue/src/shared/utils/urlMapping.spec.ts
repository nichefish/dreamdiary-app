import { afterAll, beforeAll, describe, expect, it, vi } from "vitest";
import { toVuePath } from "./urlMapping";

describe("toVuePath", () => {
  beforeAll(() => {
    vi.stubGlobal("window", { location: { origin: "http://localhost" } });
  });

  afterAll(() => {
    vi.unstubAllGlobals();
  });

  it("기술 중립적인 저널 일자 제품 URL을 현재 Vue route로 연결한다", () => {
    expect(toVuePath("/app/journal/day/home")).toBe("/journal/day/home");
  });

  it("레거시 저널 화면 URL의 .do 계약도 대응하는 Vue route로 연결한다", () => {
    expect(toVuePath("/app/journal/day/weekly.do")).toBe("/journal/weekly");
  });
});
