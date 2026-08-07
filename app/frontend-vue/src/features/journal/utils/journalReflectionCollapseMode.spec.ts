import { describe, expect, it } from "vitest";
import { resolveReflectionCollapsed } from "./journalReflectionCollapseMode";

describe("resolveReflectionCollapsed", () => {
  const base = {
    localOverride: null as boolean | null,
    forceSignal: null as "expand" | "collapse" | null,
    lifecycleKey: "OPEN",
    serverCollapsed: false,
    defaultCollapsed: false,
  };

  it("OFF 모드에서 OPEN은 서버 COLLAPSED만 따른다", () => {
    expect(resolveReflectionCollapsed({ ...base })).toBe(false);
    expect(resolveReflectionCollapsed({ ...base, serverCollapsed: true })).toBe(true);
  });

  it("ON 모드에서 OPEN은 기본 접힘이다", () => {
    expect(resolveReflectionCollapsed({ ...base, defaultCollapsed: true })).toBe(true);
  });

  it("로컬 토글이 모드·signal보다 우선한다", () => {
    expect(resolveReflectionCollapsed({
      ...base,
      localOverride: false,
      defaultCollapsed: true,
      forceSignal: "collapse",
    })).toBe(false);
  });

  it("부모 expand signal은 lifecycle·모드 기본 접힘을 무시한다", () => {
    expect(resolveReflectionCollapsed({
      ...base,
      forceSignal: "expand",
      lifecycleKey: "PENDING",
      defaultCollapsed: true,
    })).toBe(false);
  });

  it("PENDING/RESOLVED는 모드 OFF에서도 접힌다", () => {
    expect(resolveReflectionCollapsed({ ...base, lifecycleKey: "PENDING" })).toBe(true);
    expect(resolveReflectionCollapsed({ ...base, lifecycleKey: "RESOLVED" })).toBe(true);
  });
});