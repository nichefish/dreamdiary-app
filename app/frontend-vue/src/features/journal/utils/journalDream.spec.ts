import { describe, expect, it } from "vitest";
import { hasDreamerName, normalizeDreamerName } from "./journalDream";

const FIXTURE_DREAMER = "Alice";

describe("journalDream", () => {
  it("꿈꾼 이름을 트림하고 빈 값을 제거한다", () => {
    expect(normalizeDreamerName(`  ${FIXTURE_DREAMER}  `)).toBe(FIXTURE_DREAMER);
    expect(normalizeDreamerName("   ")).toBe("");
    expect(normalizeDreamerName(null)).toBe("");
  });

  it("타인 꿈 여부를 dreamerName 존재 여부에서 파생한다", () => {
    expect(hasDreamerName({ dreamerName: FIXTURE_DREAMER })).toBe(true);
    expect(hasDreamerName({ dreamerName: " " })).toBe(false);
    expect(hasDreamerName({})).toBe(false);
  });
});
