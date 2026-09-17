// @vitest-environment happy-dom
import { beforeEach, describe, expect, it, vi } from "vitest";
import { computed, ref } from "vue";
import { swalFire } from "@/shared/utils/swal";
import { useAuthStore } from "@/shared/auth/stores/auth";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { useEntryCopy } from "./useEntryCopy";

vi.mock("@/shared/utils/swal", () => ({
  swalFire: vi.fn().mockResolvedValue(undefined),
}));

vi.mock("@/shared/auth/stores/auth", () => ({
  useAuthStore: vi.fn(() => ({ isLocalProfile: false })),
}));

const FIXTURE_ENTRY_CONTENT = "<p>회의 기록</p>";
const FIXTURE_REFLECTION_CONTENT = "<p>후속 검토</p>";

function makeEntry(overrides: Partial<JournalEntryDto> = {}): JournalEntryDto {
  return {
    id: 7,
    stdrdDt: "2026-08-31",
    content: FIXTURE_ENTRY_CONTENT,
    ...overrides,
  } as JournalEntryDto;
}

function makeCopy(overrides: {
  entry?: JournalEntryDto;
  reflections?: JournalEntryDto[];
} = {}) {
  const entry = ref(overrides.entry ?? makeEntry());
  const reflections = ref(overrides.reflections ?? [
    makeEntry({
      id: 8,
      content: FIXTURE_REFLECTION_CONTENT,
      lifecycle: { lifecycleKey: "OPEN" },
    }),
  ]);
  return useEntryCopy({
    entry: computed(() => entry.value),
    reflections: computed(() => reflections.value),
    t: (key) => key === "journal.weekday.mon" ? "월" : key,
  });
}

describe("useEntryCopy :: 엔트리 복사", () => {
  const writeText = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    writeText.mockResolvedValue(undefined);
    Object.defineProperty(navigator, "clipboard", {
      configurable: true,
      value: { writeText },
    });
    vi.mocked(useAuthStore).mockReturnValue({ isLocalProfile: false } as never);
  });

  it("full 모드는 날짜·본문·리플렉션을 공통 CRLF 경계로 복사한다", async () => {
    const copy = makeCopy();

    await copy.copyEntry("full");

    expect(writeText).toHaveBeenCalledWith(
      `2026-08-31 (월)\r\n회의 기록\r\n\r\n후속 검토`,
    );
    expect(swalFire).toHaveBeenCalledWith({
      icon: "success",
      text: "journal.copy.full.success",
    });
  });

  it("body 모드는 리플렉션을 제외하고 범위별 성공 문구를 사용한다", async () => {
    const copy = makeCopy();

    await copy.copyEntry("body");

    expect(writeText).toHaveBeenCalledWith("2026-08-31 (월)\r\n회의 기록");
    expect(swalFire).toHaveBeenCalledWith({
      icon: "success",
      text: "journal.copy.body.success",
    });
  });

  it("리플렉션이 없으면 공용 tooltip·성공 문구를 사용하고 로컬 프로필은 ID를 덧붙인다", async () => {
    vi.mocked(useAuthStore).mockReturnValue({ isLocalProfile: true } as never);
    const copy = makeCopy({ reflections: [] });

    expect(copy.copyIncludeTitle.value).toBe("common.copy (id 7)");
    await copy.copyEntry("full");

    expect(swalFire).toHaveBeenCalledWith({ icon: "success", text: "common.copy.success" });
  });

  it("클립보드 쓰기 실패를 기록하고 실패 문구를 표시한다", async () => {
    const error = new Error("clipboard unavailable");
    const errorSpy = vi.spyOn(console, "error").mockImplementation(() => undefined);
    writeText.mockRejectedValue(error);
    const copy = makeCopy();

    await copy.copyEntry("no-pending");

    expect(errorSpy).toHaveBeenCalledWith(
      "[journal-entry-copy] clipboard copy failed",
      { entryId: 7, mode: "no-pending" },
      error,
    );
    expect(swalFire).toHaveBeenCalledWith({ icon: "error", text: "common.copy.failure" });
    errorSpy.mockRestore();
  });
});

describe("useEntryCopy :: 엔트리 링크 복사", () => {
  const writeText = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    writeText.mockResolvedValue(undefined);
    Object.defineProperty(navigator, "clipboard", {
      configurable: true,
      value: { writeText },
    });
    vi.mocked(useAuthStore).mockReturnValue({ isLocalProfile: false } as never);
  });

  it("앱 base와 일자·엔트리 ID를 포함한 절대 딥링크를 복사한다", async () => {
    const copy = makeCopy();

    await copy.copyEntryLink();

    const copiedUrl = String(writeText.mock.calls[0]?.[0]);
    expect(copiedUrl).toBe(`${window.location.origin}/vue-app/journal/daily?stdrdDt=2026-08-31&entryId=7`);
    expect(swalFire).toHaveBeenCalledWith({ icon: "success", text: "common.copy.success" });
  });

  it("일자 또는 ID가 없으면 원인을 기록하고 클립보드를 호출하지 않는다", async () => {
    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => undefined);
    const copy = makeCopy({ entry: makeEntry({ id: undefined }) });

    await copy.copyEntryLink();

    expect(writeText).not.toHaveBeenCalled();
    expect(warnSpy).toHaveBeenCalledWith(
      "[journal-entry-copy] link copy skipped",
      { entryId: undefined, stdrdDt: "2026-08-31", reason: "missing-entry-axis" },
    );
    warnSpy.mockRestore();
  });
});
