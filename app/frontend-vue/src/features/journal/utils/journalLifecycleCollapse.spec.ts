import { describe, expect, it } from "vitest";
import {
  resolveChapterAggregateLifecycle,
  resolveChapterCollapsed,
  resolveEntryCollapsed,
} from "./journalLifecycleCollapse";

describe("저널 라이프사이클 접힘 계약", () => {
  it("빈 챕터와 혼합 상태 챕터는 집계 라이프사이클을 만들지 않는다", () => {
    expect(resolveChapterAggregateLifecycle([])).toBeNull();
    expect(resolveChapterAggregateLifecycle([
      { lifecycle: { lifecycleKey: "PENDING" } },
      { lifecycle: { lifecycleKey: "RESOLVED" } },
    ])).toBeNull();
    expect(resolveChapterAggregateLifecycle([
      { lifecycle: { lifecycleKey: "PENDING" } },
      {},
    ])).toBeNull();
  });

  it("하위 엔트리가 전부 PENDING이면 챕터를 PENDING으로 집계한다", () => {
    expect(resolveChapterAggregateLifecycle([
      { lifecycle: { lifecycleKey: "PENDING" } },
      { lifecycle: { lifecycleKey: "PENDING" } },
    ])).toBe("PENDING");
  });

  it("하위 엔트리가 전부 RESOLVED이면 챕터를 RESOLVED로 집계한다", () => {
    expect(resolveChapterAggregateLifecycle([
      { lifecycle: { lifecycleKey: "RESOLVED" } },
      { lifecycle: { lifecycleKey: "RESOLVED" } },
    ])).toBe("RESOLVED");
  });

  it("엔트리 자체 토글은 챕터 강제값과 라이프사이클보다 우선한다", () => {
    expect(resolveEntryCollapsed({
      localOverride: false,
      forceCollapsed: true,
      lifecycleKey: "RESOLVED",
      serverCollapsed: true,
    })).toBe(false);
  });

  it("챕터 강제값은 엔트리 라이프사이클보다 우선한다", () => {
    expect(resolveEntryCollapsed({
      localOverride: null,
      forceCollapsed: false,
      lifecycleKey: "PENDING",
      serverCollapsed: true,
    })).toBe(false);
  });

  it("PENDING과 RESOLVED 엔트리는 자동으로 접고 OPEN은 서버 상태를 따른다", () => {
    expect(resolveEntryCollapsed({
      localOverride: null,
      lifecycleKey: "PENDING",
      serverCollapsed: false,
    })).toBe(true);
    expect(resolveEntryCollapsed({
      localOverride: null,
      lifecycleKey: "RESOLVED",
      serverCollapsed: false,
    })).toBe(true);
    expect(resolveEntryCollapsed({
      localOverride: null,
      lifecycleKey: "OPEN",
      serverCollapsed: true,
    })).toBe(true);
  });

  it("자동 접힘 억제는 라이프사이클만 건너뛰고 서버 COLLAPSED는 유지한다", () => {
    expect(resolveEntryCollapsed({
      localOverride: null,
      lifecycleKey: "RESOLVED",
      disableLifecycleCollapse: true,
      serverCollapsed: false,
    })).toBe(false);
    expect(resolveEntryCollapsed({
      localOverride: null,
      lifecycleKey: "PENDING",
      disableLifecycleCollapse: true,
      serverCollapsed: true,
    })).toBe(true);
  });

  it("챕터는 로컬 토글, 전체 라이프사이클 집계, 서버 상태 순서로 접힌다", () => {
    expect(resolveChapterCollapsed({
      localOverride: false,
      aggregateLifecycleKey: "PENDING",
      serverCollapsed: true,
    })).toBe(false);
    expect(resolveChapterCollapsed({
      localOverride: null,
      aggregateLifecycleKey: "RESOLVED",
      serverCollapsed: false,
    })).toBe(true);
    expect(resolveChapterCollapsed({
      localOverride: null,
      aggregateLifecycleKey: null,
      serverCollapsed: true,
    })).toBe(true);
  });
});
